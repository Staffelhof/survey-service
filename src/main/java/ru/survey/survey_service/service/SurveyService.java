package ru.survey.survey_service.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.survey.survey_service.dto.SurveyDtos;
import ru.survey.survey_service.entity.Question;
import ru.survey.survey_service.entity.QuestionOption;
import ru.survey.survey_service.entity.QuestionType;
import ru.survey.survey_service.entity.Survey;
import ru.survey.survey_service.repository.SurveyRepository;
import ru.survey.survey_service.repository.UserRepository;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class SurveyService {
    private final SurveyRepository surveyRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public SurveyService(SurveyRepository surveyRepository, UserRepository userRepository, CurrentUserService currentUserService) {
        this.surveyRepository = surveyRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }

    public List<SurveyDtos.SurveyListItem> listMine() {
        Long userId = currentUserService.requireUserId();
        return surveyRepository.findAllByOwnerIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toListItem)
                .toList();
    }

    @Transactional
    public SurveyDtos.SurveyDetails create(SurveyDtos.CreateSurveyRequest req) {
        Long userId = currentUserService.requireUserId();
        var owner = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        var s = new Survey();
        s.setOwner(owner);
        applyEditableFields(s, req.title(), req.description(), req.anonymous(), req.singleSubmission(), req.startsAt(), req.endsAt());
        s.getQuestions().clear();
        s.getQuestions().addAll(buildQuestions(s, req.questions()));

        s = surveyRepository.save(s);
        return toDetails(s);
    }

    @Transactional
    public SurveyDtos.SurveyDetails update(UUID surveyPublicId, SurveyDtos.UpdateSurveyRequest req) {
        Survey s = getMineByPublicIdOrThrow(surveyPublicId);
        if (s.isPublished()) {
            throw new IllegalArgumentException("Опубликованный опрос нельзя редактировать");
        }
        applyEditableFields(s, req.title(), req.description(), req.anonymous(), req.singleSubmission(), req.startsAt(), req.endsAt());

        s.getQuestions().clear();
        s.getQuestions().addAll(buildQuestions(s, req.questions()));

        s = surveyRepository.save(s);
        return toDetails(s);
    }

    @Transactional
    public SurveyDtos.SurveyDetails publish(UUID surveyPublicId) {
        Survey s = getMineByPublicIdOrThrow(surveyPublicId);
        if (s.getQuestions().isEmpty()) {
            throw new IllegalArgumentException("Опрос должен содержать хотя бы один вопрос");
        }
        s.setPublished(true);
        s.setActive(true);
        return toDetails(surveyRepository.save(s));
    }

    @Transactional
    public SurveyDtos.SurveyDetails setActive(UUID surveyPublicId, boolean active) {
        Survey s = getMineByPublicIdOrThrow(surveyPublicId);
        if (!s.isPublished()) {
            throw new IllegalArgumentException("Опрос не опубликован");
        }
        s.setActive(active);
        return toDetails(surveyRepository.save(s));
    }

    @Transactional
    public void delete(UUID surveyPublicId) {
        Survey s = getMineByPublicIdOrThrow(surveyPublicId);
        surveyRepository.delete(s);
    }

    public SurveyDtos.SurveyDetails getMine(UUID surveyPublicId) {
        Survey s = getMineByPublicIdOrThrow(surveyPublicId);
        return toDetails(s);
    }

    private Survey getMineByPublicIdOrThrow(UUID publicId) {
        Long userId = currentUserService.requireUserId();
        Survey s = surveyRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IllegalArgumentException("Опрос не найден"));
        if (!s.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("Доступ запрещен");
        }
        return s;
    }

    private void applyEditableFields(
            Survey s,
            String title,
            String description,
            boolean anonymous,
            boolean singleSubmission,
            java.time.LocalDateTime startsAt,
            java.time.LocalDateTime endsAt
    ) {
        s.setTitle(title);
        s.setDescription(description == null ? "" : description);
        s.setAnonymous(anonymous);
        s.setSingleSubmission(singleSubmission);
        s.setStartsAt(startsAt);
        s.setEndsAt(endsAt);
    }

    private List<Question> buildQuestions(Survey survey, List<SurveyDtos.CreateQuestionRequest> qReqs) {
        if (qReqs == null || qReqs.isEmpty()) {
            throw new IllegalArgumentException("Требуется хотя бы один вопрос");
        }

        return qReqs.stream()
                .sorted(Comparator.comparingInt(SurveyDtos.CreateQuestionRequest::position))
                .map(qr -> {
                    var q = new Question();
                    q.setSurvey(survey);
                    q.setPosition(qr.position());
                    q.setType(qr.type());
                    q.setText(qr.text());
                    q.setRequired(qr.required());

                    q.getOptions().clear();
                    if (qr.type() == QuestionType.SINGLE_CHOICE || qr.type() == QuestionType.MULTIPLE_CHOICE) {
                        if (qr.options() == null || qr.options().isEmpty()) {
                            throw new IllegalArgumentException("Для вопросов с выбором требуются варианты ответов");
                        }
                        var opts = qr.options().stream()
                                .sorted(Comparator.comparingInt(SurveyDtos.CreateOptionRequest::position))
                                .map(or -> {
                                    var o = new QuestionOption();
                                    o.setQuestion(q);
                                    o.setPosition(or.position());
                                    o.setText(or.text());
                                    return o;
                                })
                                .toList();
                        q.getOptions().addAll(opts);
                    }
                    return q;
                })
                .toList();
    }

    private SurveyDtos.SurveyListItem toListItem(Survey s) {
        return new SurveyDtos.SurveyListItem(
                s.getId(),
                s.getPublicId(),
                s.getTitle(),
                s.isPublished(),
                s.isActive(),
                s.isAnonymous(),
                s.isSingleSubmission(),
                s.getStartsAt(),
                s.getEndsAt()
        );
    }

    private SurveyDtos.SurveyDetails toDetails(Survey s) {
        var questions = s.getQuestions().stream()
                .sorted(Comparator.comparingInt(Question::getPosition))
                .map(q -> new SurveyDtos.QuestionDetails(
                        q.getId(),
                        q.getPosition(),
                        q.getType(),
                        q.getText(),
                        q.isRequired(),
                        q.getOptions().stream()
                                .sorted(Comparator.comparingInt(QuestionOption::getPosition))
                                .map(o -> new SurveyDtos.OptionDetails(o.getId(), o.getPosition(), o.getText()))
                                .toList()
                ))
                .toList();

        return new SurveyDtos.SurveyDetails(
                s.getId(),
                s.getPublicId(),
                s.getTitle(),
                s.getDescription(),
                s.isPublished(),
                s.isActive(),
                s.isAnonymous(),
                s.isSingleSubmission(),
                s.getStartsAt(),
                s.getEndsAt(),
                questions
        );
    }
}

