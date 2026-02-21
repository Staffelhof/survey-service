package ru.survey.survey_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "submissions", indexes = {
        @Index(name = "idx_submissions_survey_id", columnList = "survey_id"),
        @Index(name = "idx_submissions_public_id", columnList = "public_id", unique = true)
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_submissions_survey_token", columnNames = {"survey_id", "respondent_token"})
})
public class Submission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, updatable = false)
    private UUID publicId = UUID.randomUUID();

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false, foreignKey = @ForeignKey(name = "fk_submissions_survey"))
    private Survey survey;

    /**
     * Токен устройства/браузера (для ограничения "одно прохождение").
     * Даже для анонимного опроса этот токен НЕ является персональными данными.
     */
    @Column(name = "respondent_token", nullable = false, length = 64)
    private String respondentToken;

    /**
     * Идентификатор респондента (если опрос не анонимный).
     * Можно хранить имя/почту — на фронте дадим простое поле.
     */
    @Column(name = "respondent_identifier", length = 255)
    private String respondentIdentifier;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt = Instant.now();
}

