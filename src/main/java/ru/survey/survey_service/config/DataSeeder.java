package ru.survey.survey_service.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.survey.survey_service.entity.Role;
import ru.survey.survey_service.entity.RoleName;
import ru.survey.survey_service.entity.User;
import ru.survey.survey_service.repository.RoleRepository;
import ru.survey.survey_service.repository.UserRepository;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedData(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            // Создаем роли, если их нет
            Role adminRole = ensureRole(roleRepository, RoleName.ADMIN);
            ensureRole(roleRepository, RoleName.CREATOR);

            // Создаем первого админа, если его нет
            String adminEmail = "admin@test.com";
            if (!userRepository.existsByEmail(adminEmail)) {
                User admin = new User();
                admin.setEmail(adminEmail);
                admin.setPasswordHash(passwordEncoder.encode("admin123"));
                admin.setDisplayName("Администратор");
                admin.getRoles().add(adminRole);
                userRepository.save(admin);
                System.out.println("========================================");
                System.out.println("Создан администратор:");
                System.out.println("Email: " + adminEmail);
                System.out.println("Пароль: admin123");
                System.out.println("========================================");
            }
        };
    }

    private Role ensureRole(RoleRepository roleRepository, RoleName name) {
        return roleRepository.findByName(name).orElseGet(() -> {
            var r = new Role();
            r.setName(name);
            return roleRepository.save(r);
        });
    }
}

