package ru.survey.survey_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.survey.survey_service.entity.Survey;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SurveyRepository extends JpaRepository<Survey, Long> {
    List<Survey> findAllByOwnerIdOrderByCreatedAtDesc(Long ownerId);
    Optional<Survey> findByPublicId(UUID publicId);
}

