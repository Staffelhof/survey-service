package ru.survey.survey_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

public class UserDtos {

    public record UserListItem(
            Long id,
            String email,
            String displayName,
            boolean enabled,
            Instant createdAt,
            List<String> roles
    ) {}

    public record UpdateUserRolesRequest(
            @NotNull List<@NotBlank String> roles
    ) {}

    public record UpdateUserEnabledRequest(
            boolean enabled
    ) {}

}
