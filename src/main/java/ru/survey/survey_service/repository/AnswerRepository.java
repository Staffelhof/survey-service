package ru.survey.survey_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.survey.survey_service.entity.Answer;

import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
    List<Answer> findAllBySubmissionId(Long submissionId);
    List<Answer> findAllByQuestionId(Long questionId);
}

