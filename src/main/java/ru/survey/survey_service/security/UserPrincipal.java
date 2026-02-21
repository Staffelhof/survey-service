package ru.survey.survey_service.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.survey.survey_service.entity.Role;
import ru.survey.survey_service.entity.User;

import java.util.Collection;

public class UserPrincipal implements UserDetails {
    private final Long id;
    private final String email;
    private final String passwordHash;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long id, String email, String passwordHash, boolean enabled, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.enabled = enabled;
        this.authorities = authorities;
    }

    public static UserPrincipal from(User user) {
        var auths = user.getRoles().stream()
                .map(Role::getName)
                .map(Enum::name)
                .map(rn -> "ROLE_" + rn)
                .map(SimpleGrantedAuthority::new)
                .toList();
        return new UserPrincipal(user.getId(), user.getEmail(), user.getPasswordHash(), user.isEnabled(), auths);
    }

    public Long getId() {
        return id;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return enabled;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}

