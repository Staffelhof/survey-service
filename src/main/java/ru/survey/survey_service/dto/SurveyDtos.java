package ru.survey.survey_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.survey.survey_service.entity.QuestionType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class SurveyDtos {

    public record SurveyListItem(
            Long id,
            UUID publicId,
            String title,
            boolean published,
            boolean active,
            boolean anonymous,
            boolean singleSubmission,
            LocalDateTime startsAt,
            LocalDateTime endsAt
    ) {}

    public record CreateSurveyRequest(
            @NotBlank @Size(max = 200) String title,
            @Size(max = 1000) String description,
            boolean anonymous,
            boolean singleSubmission,
            LocalDateTime startsAt,
            LocalDateTime endsAt,
            @NotNull @Valid List<CreateQuestionRequest> questions
    ) {}

    public record UpdateSurveyRequest(
            @NotBlank @Size(max = 200) String title,
            @Size(max = 1000) String description,
            boolean anonymous,
            boolean singleSubmission,
            LocalDateTime startsAt,
            LocalDateTime endsAt,
            @NotNull @Valid List<CreateQuestionRequest> questions
    ) {}

    public record CreateQuestionRequest(
            int position,
            @NotNull QuestionType type,
            @NotBlank @Size(max = 500) String text,
            boolean required,
            @Valid List<CreateOptionRequest> options
    ) {}

    public record CreateOptionRequest(
            int position,
            @NotBlank @Size(max = 200) String text
    ) {}

    public record SurveyDetails(
            Long id,
            UUID publicId,
            String title,
            String description,
            boolean published,
            boolean active,
            boolean anonymous,
            boolean singleSubmission,
            LocalDateTime startsAt,
            LocalDateTime endsAt,
            List<QuestionDetails> questions
    ) {}

    public record QuestionDetails(
            Long id,
            int position,
            QuestionType type,
            String text,
            boolean required,
            List<OptionDetails> options
    ) {}

    public record OptionDetails(
            Long id,
            int position,
            String text
    ) {}
}

