package ru.survey.survey_service.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.survey.survey_service.dto.SurveyDtos;
import ru.survey.survey_service.service.SurveyService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/surveys")
public class SurveyController {
    private final SurveyService surveyService;

    public SurveyController(SurveyService surveyService) {
        this.surveyService = surveyService;
    }

    @GetMapping
    public List<SurveyDtos.SurveyListItem> listMine() {
        return surveyService.listMine();
    }

    @PostMapping
    public SurveyDtos.SurveyDetails create(@Valid @RequestBody SurveyDtos.CreateSurveyRequest req) {
        return surveyService.create(req);
    }

    @GetMapping("/{publicId}")
    public SurveyDtos.SurveyDetails get(@PathVariable UUID publicId) {
        return surveyService.getMine(publicId);
    }

    @PutMapping("/{publicId}")
    public SurveyDtos.SurveyDetails update(@PathVariable UUID publicId, @Valid @RequestBody SurveyDtos.UpdateSurveyRequest req) {
        return surveyService.update(publicId, req);
    }

    @PostMapping("/{publicId}/publish")
    public SurveyDtos.SurveyDetails publish(@PathVariable UUID publicId) {
        return surveyService.publish(publicId);
    }

    @PostMapping("/{publicId}/active")
    public SurveyDtos.SurveyDetails setActive(@PathVariable UUID publicId, @RequestParam boolean value) {
        return surveyService.setActive(publicId, value);
    }

    @DeleteMapping("/{publicId}")
    public void delete(@PathVariable UUID publicId) {
        surveyService.delete(publicId);
    }
}

