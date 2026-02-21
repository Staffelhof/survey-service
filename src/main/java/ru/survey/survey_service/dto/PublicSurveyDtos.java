package ru.survey.survey_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.survey.survey_service.entity.QuestionType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class PublicSurveyDtos {

    public record PublicSurvey(
            UUID publicId,
            String title,
            String description,
            boolean anonymous,
            boolean singleSubmission,
            LocalDateTime startsAt,
            LocalDateTime endsAt,
            List<PublicQuestion> questions
    ) {}

    public record PublicQuestion(
            Long id,
            int position,
            QuestionType type,
            String text,
            boolean required,
            List<PublicOption> options
    ) {}

    public record PublicOption(
            Long id,
            int position,
            String text
    ) {}

    public record SubmitRequest(
            @Size(max = 255) String respondentIdentifier,
            @NotNull @Valid List<AnswerRequest> answers
    ) {}

    public record AnswerRequest(
            @NotNull Long questionId,
            String textValue,
            List<Long> optionIds
    ) {}

    public record SubmitResponse(
            UUID submissionPublicId
    ) {}
}

