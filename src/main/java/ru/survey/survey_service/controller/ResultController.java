package ru.survey.survey_service.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.survey.survey_service.dto.ResultDtos;
import ru.survey.survey_service.service.ResultService;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/results")
public class ResultController {
    private final ResultService resultService;

    public ResultController(ResultService resultService) {
        this.resultService = resultService;
    }

    @GetMapping("/{surveyPublicId}")
    public ResultDtos.SurveyResults getResults(@PathVariable UUID surveyPublicId) {
        return resultService.getResults(surveyPublicId);
    }

    @GetMapping("/{surveyPublicId}/export.csv")
    public ResponseEntity<byte[]> exportCsv(@PathVariable UUID surveyPublicId) {
        String csv = resultService.exportCsv(surveyPublicId);
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"results-" + surveyPublicId + ".csv\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(bytes);
    }

    @GetMapping("/{surveyPublicId}/submissions")
    public List<ResultDtos.SubmissionListItem> listSubmissions(@PathVariable UUID surveyPublicId) {
        return resultService.listSubmissions(surveyPublicId);
    }

    @GetMapping("/{surveyPublicId}/submissions/{submissionPublicId}")
    public ResultDtos.SubmissionDetails getSubmissionDetails(
            @PathVariable UUID surveyPublicId,
            @PathVariable UUID submissionPublicId
    ) {
        return resultService.getSubmissionDetails(surveyPublicId, submissionPublicId);
    }
}

