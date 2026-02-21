package ru.survey.survey_service.dto;

import ru.survey.survey_service.entity.QuestionType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ResultDtos {

    public record SurveyResults(
            UUID surveyPublicId,
            String title,
            long submissionsCount,
            List<QuestionResult> questions
    ) {}

    public record QuestionResult(
            Long questionId,
            int position,
            QuestionType type,
            String text,
            List<OptionCount> optionCounts,
            List<String> textAnswers
    ) {}

    public record OptionCount(
            Long optionId,
            int position,
            String text,
            long count
    ) {}

    public record SubmissionDetails(
            UUID submissionPublicId,
            UUID surveyPublicId,
            String surveyTitle,
            String respondentIdentifier,
            Instant submittedAt,
            List<SubmissionAnswer> answers
    ) {}

    public record SubmissionAnswer(
            Long questionId,
            int questionPosition,
            QuestionType questionType,
            String questionText,
            String textValue,
            List<SubmissionOption> selectedOptions
    ) {}

    public record SubmissionOption(
            Long optionId,
            int position,
            String text
    ) {}

    public record SubmissionListItem(
            UUID submissionPublicId,
            String respondentIdentifier,
            Instant submittedAt
    ) {}
}

