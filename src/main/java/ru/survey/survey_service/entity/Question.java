package ru.survey.survey_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "questions", indexes = {
        @Index(name = "idx_questions_survey_id", columnList = "survey_id")
})
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false, foreignKey = @ForeignKey(name = "fk_questions_survey"))
    private Survey survey;

    @Column(name = "position", nullable = false)
    private int position;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 32)
    private QuestionType type;

    @Column(name = "text", nullable = false, length = 500)
    private String text;

    @Column(name = "required", nullable = false)
    private boolean required = true;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<QuestionOption> options = new ArrayList<>();
}

