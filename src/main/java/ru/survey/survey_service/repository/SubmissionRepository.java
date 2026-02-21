package ru.survey.survey_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.survey.survey_service.entity.Submission;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    Optional<Submission> findByPublicId(UUID publicId);
    Optional<Submission> findBySurveyIdAndRespondentToken(Long surveyId, String respondentToken);
    List<Submission> findAllBySurveyIdOrderBySubmittedAtDesc(Long surveyId);
}

