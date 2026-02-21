package ru.survey.survey_service.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.survey.survey_service.dto.PublicSurveyDtos;
import ru.survey.survey_service.entity.*;
import ru.survey.survey_service.repository.*;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class PublicSurveyService {
    private final SurveyRepository surveyRepository;
    private final SubmissionRepository submissionRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final AnswerRepository answerRepository;
    private final AnswerOptionRepository answerOptionRepository;

    public PublicSurveyService(
            SurveyRepository surveyRepository,
            SubmissionRepository submissionRepository,
            QuestionRepository questionRepository,
            QuestionOptionRepository questionOptionRepository,
            AnswerRepository answerRepository,
            AnswerOptionRepository answerOptionRepository
    ) {
        this.surveyRepository = surveyRepository;
        this.submissionRepository = submissionRepository;
        this.questionRepository = questionRepository;
        this.questionOptionRepository = questionOptionRepository;
        this.answerRepository = answerRepository;
        this.answerOptionRepository = answerOptionRepository;
    }

    public PublicSurveyDtos.PublicSurvey getPublicSurvey(UUID surveyPublicId) {
        Survey s = surveyRepository.findByPublicId(surveyPublicId)
                .orElseThrow(() -> new IllegalArgumentException("Опрос не найден"));

        if (!s.isPublished() || !s.isActive()) {
            throw new IllegalArgumentException("Опрос недоступен");
        }

        if (!isInTimeWindow(s.getStartsAt(), s.getEndsAt())) {
            throw new IllegalArgumentException("Опрос закрыт по времени");
        }

        var questions = questionRepository.findAllBySurveyIdOrderByPositionAsc(s.getId()).stream()
                .map(q -> {
                    var opts = questionOptionRepository.findAllByQuestionIdOrderByPositionAsc(q.getId()).stream()
                            .map(o -> new PublicSurveyDtos.PublicOption(o.getId(), o.getPosition(), o.getText()))
                            .toList();
                    return new PublicSurveyDtos.PublicQuestion(q.getId(), q.getPosition(), q.getType(), q.getText(), q.isRequired(), opts);
                })
                .toList();

        return new PublicSurveyDtos.PublicSurvey(
                s.getPublicId(),
                s.getTitle(),
                s.getDescription(),
                s.isAnonymous(),
                s.isSingleSubmission(),
                s.getStartsAt(),
                s.getEndsAt(),
                questions
        );
    }

    @Transactional
    public PublicSurveyDtos.SubmitResponse submit(UUID surveyPublicId, String respondentToken, PublicSurveyDtos.SubmitRequest req) {
        if (respondentToken == null || respondentToken.isBlank()) {
            throw new IllegalArgumentException("Требуется токен респондента");
        }
        if (respondentToken.length() > 64) {
            throw new IllegalArgumentException("Токен респондента слишком длинный");
        }

        Survey s = surveyRepository.findByPublicId(surveyPublicId)
                .orElseThrow(() -> new IllegalArgumentException("Опрос не найден"));
        if (!s.isPublished() || !s.isActive()) {
            throw new IllegalArgumentException("Опрос недоступен");
        }
        if (!isInTimeWindow(s.getStartsAt(), s.getEndsAt())) {
            throw new IllegalArgumentException("Опрос закрыт по времени");
        }

        if (s.isSingleSubmission()) {
            var existing = submissionRepository.findBySurveyIdAndRespondentToken(s.getId(), respondentToken);
            if (existing.isPresent()) {
                throw new IllegalArgumentException("Вы уже проходили этот опрос");
            }
        }

        var questions = questionRepository.findAllBySurveyIdOrderByPositionAsc(s.getId());
        if (questions.isEmpty()) {
            throw new IllegalArgumentException("В опросе нет вопросов");
        }

        Map<Long, Question> questionById = new HashMap<>();
        for (var q : questions) questionById.put(q.getId(), q);

        // option id -> option, plus validation of belongs-to
        Map<Long, QuestionOption> optionById = new HashMap<>();
        for (var q : questions) {
            var opts = questionOptionRepository.findAllByQuestionIdOrderByPositionAsc(q.getId());
            for (var o : opts) optionById.put(o.getId(), o);
        }

        // build answers map from request
        Map<Long, PublicSurveyDtos.AnswerRequest> reqByQ = new HashMap<>();
        for (var ar : req.answers()) {
            if (ar.questionId() == null) continue;
            reqByQ.put(ar.questionId(), ar);
        }

        // validate required questions answered
        for (var q : questions) {
            var ar = reqByQ.get(q.getId());
            if (q.isRequired()) {
                if (ar == null) throw new IllegalArgumentException("Не заполнен обязательный вопрос: " + q.getId());
                if (q.getType() == QuestionType.TEXT) {
                    if (ar.textValue() == null || ar.textValue().isBlank()) {
                        throw new IllegalArgumentException("Требуется текстовый ответ на вопрос: " + q.getId());
                    }
                    if (ar.textValue().length() > 2000) {
                        throw new IllegalArgumentException("Текстовый ответ слишком длинный (максимум 2000 символов): " + q.getId());
                    }
                } else {
                    if (ar.optionIds() == null || ar.optionIds().isEmpty()) {
                        throw new IllegalArgumentException("Требуется выбор варианта ответа на вопрос: " + q.getId());
                    }
                }
            }
        }

        if (!s.isAnonymous()) {
            if (req.respondentIdentifier() == null || req.respondentIdentifier().isBlank()) {
                throw new IllegalArgumentException("Требуется идентификатор респондента для неанонимного опроса");
            }
        }

        var submission = new Submission();
        submission.setSurvey(s);
        submission.setRespondentToken(respondentToken);
        submission.setRespondentIdentifier(s.isAnonymous() ? null : req.respondentIdentifier());
        submission = submissionRepository.save(submission);

        // create answers
        for (var q : questions) {
            var ar = reqByQ.get(q.getId());
            if (ar == null) continue; // optional question can be skipped

            var ans = new Answer();
            ans.setSubmission(submission);
            ans.setQuestion(q);

            if (q.getType() == QuestionType.TEXT) {
                if (ar.textValue() != null) {
                    if (ar.textValue().length() > 2000) throw new IllegalArgumentException("Текстовый ответ слишком длинный (максимум 2000 символов): " + q.getId());
                    ans.setTextValue(ar.textValue());
                }
                ans = answerRepository.save(ans);
                continue;
            }

            var optionIds = ar.optionIds() == null ? List.<Long>of() : ar.optionIds();
            if (q.getType() == QuestionType.SINGLE_CHOICE && optionIds.size() > 1) {
                throw new IllegalArgumentException("Разрешен только один вариант ответа: " + q.getId());
            }

            ans = answerRepository.save(ans);

            for (Long optId : optionIds) {
                var opt = optionById.get(optId);
                if (opt == null) throw new IllegalArgumentException("Вариант ответа не найден: " + optId);
                if (!opt.getQuestion().getId().equals(q.getId())) {
                    throw new IllegalArgumentException("Вариант ответа не принадлежит этому вопросу: " + optId);
                }
                var ao = new AnswerOption();
                ao.setAnswer(ans);
                ao.setOption(opt);
                answerOptionRepository.save(ao);
            }
        }

        return new PublicSurveyDtos.SubmitResponse(submission.getPublicId());
    }

    private boolean isInTimeWindow(LocalDateTime startsAt, LocalDateTime endsAt) {
        LocalDateTime now = LocalDateTime.now();
        if (startsAt != null && now.isBefore(startsAt)) return false;
        if (endsAt != null && now.isAfter(endsAt)) return false;
        return true;
    }
}

