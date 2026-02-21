package ru.survey.survey_service.controller;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.survey.survey_service.dto.UserDtos;
import ru.survey.survey_service.entity.User;
import ru.survey.survey_service.service.CurrentUserService;
import ru.survey.survey_service.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {
    private final UserService userService;
    private final CurrentUserService currentUserService;

    public UserController(UserService userService, CurrentUserService currentUserService) {
        this.userService = userService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<UserDtos.UserListItem> listUsers() {
        return userService.listAll().stream()
                .map(this::toListItem)
                .toList();
    }

    @PutMapping("/{userId}/roles")
    public UserDtos.UserListItem updateRoles(
            @PathVariable Long userId,
            @Valid @RequestBody UserDtos.UpdateUserRolesRequest req
    ) {
        userService.setRoles(userId, req.roles());
        User u = userService.getByIdOrThrow(userId);
        return toListItem(u);
    }

    @PutMapping("/{userId}/enabled")
    public UserDtos.UserListItem updateEnabled(
            @PathVariable Long userId,
            @Valid @RequestBody UserDtos.UpdateUserEnabledRequest req
    ) {
        userService.setEnabled(userId, req.enabled());
        User u = userService.getByIdOrThrow(userId);
        return toListItem(u);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable Long userId) {
        Long currentUserId = currentUserService.requireUserId();
        if (userId.equals(currentUserId)) {
            throw new IllegalArgumentException("Нельзя удалить самого себя");
        }
        userService.delete(userId);
    }

    private UserDtos.UserListItem toListItem(User u) {
        var roles = u.getRoles().stream()
                .map(r -> r.getName().name())
                .toList();
        return new UserDtos.UserListItem(
                u.getId(),
                u.getEmail(),
                u.getDisplayName(),
                u.isEnabled(),
                u.getCreatedAt(),
                roles
        );
    }
}
