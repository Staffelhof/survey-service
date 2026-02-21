package ru.survey.survey_service.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.survey.survey_service.security.UserPrincipal;

@Service
public class CurrentUserService {
    public Long requireUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal p)) {
            throw new IllegalStateException("Не авторизован");
        }
        return p.getId();
    }
}

