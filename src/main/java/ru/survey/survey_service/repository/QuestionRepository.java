package ru.survey.survey_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.survey.survey_service.entity.Question;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findAllBySurveyIdOrderByPositionAsc(Long surveyId);
}

