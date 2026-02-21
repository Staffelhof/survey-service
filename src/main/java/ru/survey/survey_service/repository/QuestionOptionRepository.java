package ru.survey.survey_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.survey.survey_service.entity.QuestionOption;

import java.util.List;

public interface QuestionOptionRepository extends JpaRepository<QuestionOption, Long> {
    List<QuestionOption> findAllByQuestionIdOrderByPositionAsc(Long questionId);
}

