package ru.survey.survey_service.controller;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.survey.survey_service.dto.PublicSurveyDtos;
import ru.survey.survey_service.service.PublicSurveyService;

import java.util.UUID;

@RestController
@RequestMapping("/api/public")
public class PublicSurveyController {
    private final PublicSurveyService publicSurveyService;

    public PublicSurveyController(PublicSurveyService publicSurveyService) {
        this.publicSurveyService = publicSurveyService;
    }

    @GetMapping(value = "/surveys/{publicId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public PublicSurveyDtos.PublicSurvey getSurvey(@PathVariable UUID publicId) {
        return publicSurveyService.getPublicSurvey(publicId);
    }

    /**
     * respondentToken передаём заголовком X-Respondent-Token (храним на клиенте в localStorage).
     */
    @PostMapping(value = "/surveys/{publicId}/submit", produces = MediaType.APPLICATION_JSON_VALUE)
    public PublicSurveyDtos.SubmitResponse submit(
            @PathVariable UUID publicId,
            @RequestHeader(name = "X-Respondent-Token", required = false) String respondentToken,
            @Valid @RequestBody PublicSurveyDtos.SubmitRequest req
    ) {
        return publicSurveyService.submit(publicId, respondentToken, req);
    }
}

