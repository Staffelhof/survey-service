package ru.survey.survey_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.survey.survey_service.entity.Role;
import ru.survey.survey_service.entity.RoleName;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}

