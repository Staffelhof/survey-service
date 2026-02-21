package ru.survey.survey_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "answer_options", uniqueConstraints = {
        @UniqueConstraint(name = "uk_answer_options_answer_option", columnNames = {"answer_id", "option_id"})
}, indexes = {
        @Index(name = "idx_answer_options_answer_id", columnList = "answer_id"),
        @Index(name = "idx_answer_options_option_id", columnList = "option_id")
})
public class AnswerOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_id", nullable = false, foreignKey = @ForeignKey(name = "fk_answer_options_answer"))
    private Answer answer;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id", nullable = false, foreignKey = @ForeignKey(name = "fk_answer_options_option"))
    private QuestionOption option;
}

