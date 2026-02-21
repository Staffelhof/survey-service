package ru.survey.survey_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "question_options", indexes = {
        @Index(name = "idx_question_options_question_id", columnList = "question_id")
})
public class QuestionOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false, foreignKey = @ForeignKey(name = "fk_question_options_question"))
    private Question question;

    @Column(name = "position", nullable = false)
    private int position;

    @Column(name = "text", nullable = false, length = 200)
    private String text;
}

