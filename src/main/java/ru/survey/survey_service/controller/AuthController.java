package ru.survey.survey_service.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import ru.survey.survey_service.dto.AuthDtos;
import ru.survey.survey_service.entity.User;
import ru.survey.survey_service.repository.UserRepository;
import ru.survey.survey_service.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    public AuthController(UserService userService, UserRepository userRepository, AuthenticationManager authenticationManager, SecurityContextRepository securityContextRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    @PostMapping("/register")
    public AuthDtos.MeResponse register(@Valid @RequestBody AuthDtos.RegisterRequest req) {
        var u = userService.registerCreator(req.email(), req.password(), req.displayName());
        // Перезагружаем пользователя из БД, чтобы убедиться, что все связи загружены
        u = userRepository.findByEmail(req.email().toLowerCase())
                .orElseThrow(() -> new IllegalStateException("Пользователь не найден после регистрации"));
        return toMe(u);
    }

    @PostMapping("/login")
    public AuthDtos.MeResponse login(@Valid @RequestBody AuthDtos.LoginRequest req, HttpServletRequest request, HttpServletResponse response) {
        try {
            var auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.email().toLowerCase(), req.password())
            );
            
            // Создаём SecurityContext и устанавливаем аутентификацию
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(auth);
            SecurityContextHolder.setContext(securityContext);
            
            // Сохраняем SecurityContext в сессии
            securityContextRepository.saveContext(securityContext, request, response);
            
            // Создаём сессию явно
            request.getSession(true);

            User u = userRepository.findByEmail(req.email().toLowerCase())
                    .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
            return toMe(u);
        } catch (org.springframework.security.core.AuthenticationException e) {
            throw new IllegalArgumentException("Неверный email или пароль");
        }
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        new SecurityContextLogoutHandler().logout(request, null, auth);
    }

    @GetMapping("/me")
    public AuthDtos.MeResponse me() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new IllegalStateException("Не авторизован");
        }
        var u = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        return toMe(u);
    }

    private AuthDtos.MeResponse toMe(User u) {
        var roles = u.getRoles().stream().map(r -> r.getName().name()).toArray(String[]::new);
        return new AuthDtos.MeResponse(u.getId(), u.getEmail(), u.getDisplayName(), roles);
    }
}

