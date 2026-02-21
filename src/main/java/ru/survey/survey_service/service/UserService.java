package ru.survey.survey_service.service;

import jakarta.persistence.EntityManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.survey.survey_service.entity.Role;
import ru.survey.survey_service.entity.RoleName;
import ru.survey.survey_service.entity.User;
import ru.survey.survey_service.repository.RoleRepository;
import ru.survey.survey_service.repository.UserRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, EntityManager entityManager) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.entityManager = entityManager;
    }

    @Transactional
    public User registerCreator(String email, String rawPassword, String displayName) {
        String normalizedEmail = email.toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email уже зарегистрирован");
        }

        var creatorRole = roleRepository.findByName(RoleName.CREATOR)
                .orElseThrow(() -> new IllegalStateException("Роль CREATOR не найдена"));

        var user = new User();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setDisplayName(displayName);
        user.getRoles().add(creatorRole);
        user = userRepository.save(user);
        // Принудительно сохраняем изменения в БД
        entityManager.flush();
        return user;
    }

    public User getByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
    }

    public User getByIdOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
    }

    public List<User> listAll() {
        return userRepository.findAll();
    }

    @Transactional
    public void setEnabled(Long userId, boolean enabled) {
        var u = getByIdOrThrow(userId);
        u.setEnabled(enabled);
        userRepository.save(u);
    }

    @Transactional
    public void setRoles(Long userId, List<String> roleNames) {
        var u = getByIdOrThrow(userId);
        Set<Role> newRoles = roleNames.stream()
                .map(rn -> {
                    try {
                        RoleName roleName = RoleName.valueOf(rn.toUpperCase());
                        return roleRepository.findByName(roleName)
                                .orElseThrow(() -> new IllegalArgumentException("Роль не найдена: " + rn));
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Некорректное имя роли: " + rn);
                    }
                })
                .collect(Collectors.toSet());
        u.getRoles().clear();
        u.getRoles().addAll(newRoles);
        userRepository.save(u);
    }

    @Transactional
    public void delete(Long userId) {
        userRepository.deleteById(userId);
    }
}

