package ru.survey.survey_service.service;

import org.springframework.stereotype.Service;
import ru.survey.survey_service.dto.ResultDtos;
import ru.survey.survey_service.entity.QuestionType;
import ru.survey.survey_service.entity.Survey;
import ru.survey.survey_service.repository.*;

import java.util.*;

@Service
public class ResultService {
    private final SurveyRepository surveyRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final SubmissionRepository submissionRepository;
    private final AnswerRepository answerRepository;
    private final AnswerOptionRepository answerOptionRepository;
    private final CurrentUserService currentUserService;

    public ResultService(
            SurveyRepository surveyRepository,
            QuestionRepository questionRepository,
            QuestionOptionRepository questionOptionRepository,
            SubmissionRepository submissionRepository,
            AnswerRepository answerRepository,
            AnswerOptionRepository answerOptionRepository,
            CurrentUserService currentUserService
    ) {
        this.surveyRepository = surveyRepository;
        this.questionRepository = questionRepository;
        this.questionOptionRepository = questionOptionRepository;
        this.submissionRepository = submissionRepository;
        this.answerRepository = answerRepository;
        this.answerOptionRepository = answerOptionRepository;
        this.currentUserService = currentUserService;
    }

    public ResultDtos.SurveyResults getResults(UUID surveyPublicId) {
        Survey s = surveyRepository.findByPublicId(surveyPublicId)
                .orElseThrow(() -> new IllegalArgumentException("Опрос не найден"));
        Long userId = currentUserService.requireUserId();
        if (!s.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("Доступ запрещен");
        }

        var questions = questionRepository.findAllBySurveyIdOrderByPositionAsc(s.getId());
        var submissions = submissionRepository.findAllBySurveyIdOrderBySubmittedAtDesc(s.getId());

        // option counts: optionId -> count
        Map<Long, Long> optionCount = new HashMap<>();
        // text answers: questionId -> list
        Map<Long, List<String>> textAnswers = new HashMap<>();

        for (var sub : submissions) {
            var answers = answerRepository.findAllBySubmissionId(sub.getId());
            for (var a : answers) {
                var q = a.getQuestion();
                if (q.getType() == QuestionType.TEXT) {
                    if (a.getTextValue() != null && !a.getTextValue().isBlank()) {
                        textAnswers.computeIfAbsent(q.getId(), k -> new ArrayList<>()).add(a.getTextValue());
                    }
                } else {
                    var aopts = answerOptionRepository.findAllByAnswerId(a.getId());
                    for (var ao : aopts) {
                        Long optId = ao.getOption().getId();
                        optionCount.put(optId, optionCount.getOrDefault(optId, 0L) + 1);
                    }
                }
            }
        }

        var qResults = questions.stream().map(q -> {
            var optionCounts = List.<ResultDtos.OptionCount>of();
            if (q.getType() != QuestionType.TEXT) {
                var opts = questionOptionRepository.findAllByQuestionIdOrderByPositionAsc(q.getId());
                optionCounts = opts.stream()
                        .map(o -> new ResultDtos.OptionCount(
                                o.getId(),
                                o.getPosition(),
                                o.getText(),
                                optionCount.getOrDefault(o.getId(), 0L)
                        ))
                        .toList();
            }
            var texts = q.getType() == QuestionType.TEXT ? textAnswers.getOrDefault(q.getId(), List.of()) : List.<String>of();

            return new ResultDtos.QuestionResult(
                    q.getId(),
                    q.getPosition(),
                    q.getType(),
                    q.getText(),
                    optionCounts,
                    texts
            );
        }).toList();

        return new ResultDtos.SurveyResults(
                s.getPublicId(),
                s.getTitle(),
                submissions.size(),
                qResults
        );
    }

    public String exportCsv(UUID surveyPublicId) {
        var results = getResults(surveyPublicId);
        var sb = new StringBuilder();
        sb.append("surveyPublicId;title;submissionsCount\n");
        sb.append(results.surveyPublicId()).append(';')
                .append(escape(results.title())).append(';')
                .append(results.submissionsCount()).append('\n');
        sb.append("\nquestionPosition;questionId;questionType;questionText;optionPosition;optionId;optionText;count;textAnswer\n");
        for (var q : results.questions()) {
            if (q.type() == QuestionType.TEXT) {
                if (q.textAnswers() == null || q.textAnswers().isEmpty()) {
                    sb.append(q.position()).append(';').append(q.questionId()).append(';').append(q.type()).append(';')
                            .append(escape(q.text())).append(";;;;;").append('\n');
                } else {
                    for (var ta : q.textAnswers()) {
                        sb.append(q.position()).append(';').append(q.questionId()).append(';').append(q.type()).append(';')
                                .append(escape(q.text())).append(";;;;;")
                                .append(escape(ta)).append('\n');
                    }
                }
            } else {
                for (var oc : q.optionCounts()) {
                    sb.append(q.position()).append(';').append(q.questionId()).append(';').append(q.type()).append(';')
                            .append(escape(q.text())).append(';')
                            .append(oc.position()).append(';')
                            .append(oc.optionId()).append(';')
                            .append(escape(oc.text())).append(';')
                            .append(oc.count()).append(';')
                            .append('\n');
                }
            }
        }
        return sb.toString();
    }

    public List<ResultDtos.SubmissionListItem> listSubmissions(UUID surveyPublicId) {
        Survey s = surveyRepository.findByPublicId(surveyPublicId)
                .orElseThrow(() -> new IllegalArgumentException("Опрос не найден"));
        Long userId = currentUserService.requireUserId();
        if (!s.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("Доступ запрещен");
        }

        var submissions = submissionRepository.findAllBySurveyIdOrderBySubmittedAtDesc(s.getId());
        return submissions.stream()
                .map(sub -> new ResultDtos.SubmissionListItem(
                        sub.getPublicId(),
                        sub.getRespondentIdentifier(),
                        sub.getSubmittedAt()
                ))
                .toList();
    }

    public ResultDtos.SubmissionDetails getSubmissionDetails(UUID surveyPublicId, UUID submissionPublicId) {
        Survey s = surveyRepository.findByPublicId(surveyPublicId)
                .orElseThrow(() -> new IllegalArgumentException("Опрос не найден"));
        Long userId = currentUserService.requireUserId();
        if (!s.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("Доступ запрещен");
        }

        var submission = submissionRepository.findByPublicId(submissionPublicId)
                .orElseThrow(() -> new IllegalArgumentException("Прохождение опроса не найдено"));
        if (!submission.getSurvey().getId().equals(s.getId())) {
            throw new IllegalArgumentException("Прохождение не принадлежит этому опросу");
        }

        var questions = questionRepository.findAllBySurveyIdOrderByPositionAsc(s.getId());
        var answers = answerRepository.findAllBySubmissionId(submission.getId());
        var answerByQuestionId = answers.stream()
                .collect(java.util.stream.Collectors.toMap(a -> a.getQuestion().getId(), a -> a));

        var submissionAnswers = questions.stream()
                .map(q -> {
                    var answer = answerByQuestionId.get(q.getId());
                    String textValue = null;
                    List<ResultDtos.SubmissionOption> selectedOptions = List.of();

                    if (answer != null) {
                        if (q.getType() == QuestionType.TEXT) {
                            textValue = answer.getTextValue();
                        } else {
                            var answerOptions = answerOptionRepository.findAllByAnswerId(answer.getId());
                            selectedOptions = answerOptions.stream()
                                    .map(ao -> {
                                        var opt = ao.getOption();
                                        return new ResultDtos.SubmissionOption(
                                                opt.getId(),
                                                opt.getPosition(),
                                                opt.getText()
                                        );
                                    })
                                    .sorted(java.util.Comparator.comparingInt(ResultDtos.SubmissionOption::position))
                                    .toList();
                        }
                    }

                    return new ResultDtos.SubmissionAnswer(
                            q.getId(),
                            q.getPosition(),
                            q.getType(),
                            q.getText(),
                            textValue,
                            selectedOptions
                    );
                })
                .toList();

        return new ResultDtos.SubmissionDetails(
                submission.getPublicId(),
                s.getPublicId(),
                s.getTitle(),
                submission.getRespondentIdentifier(),
                submission.getSubmittedAt(),
                submissionAnswers
        );
    }

    private String escape(String s) {
        if (s == null) return "";
        // простой CSV с ';' — уберём переводы строк
        return s.replace("\r", " ").replace("\n", " ").replace(";", ",");
    }
}

