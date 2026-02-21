package ru.survey.survey_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "answers", indexes = {
        @Index(name = "idx_answers_submission_id", columnList = "submission_id"),
        @Index(name = "idx_answers_question_id", columnList = "question_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_answers_submission_question", columnNames = {"submission_id", "question_id"})
})
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false, foreignKey = @ForeignKey(name = "fk_answers_submission"))
    private Submission submission;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false, foreignKey = @ForeignKey(name = "fk_answers_question"))
    private Question question;

    /**
     * Для TEXT-вопроса: текст ответа.
     * Для SINGLE/MULTIPLE: можно оставить null и хранить варианты в answer_options.
     */
    @Column(name = "text_value", length = 2000)
    private String textValue;
}

