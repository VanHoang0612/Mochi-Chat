package com.mochi.backend.config;

import com.mochi.backend.enums.RoleType;
import com.mochi.backend.model.Role;
import com.mochi.backend.model.User;
import com.mochi.backend.repository.RoleRepository;
import com.mochi.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class InitDataConfig {
    @Bean
    CommandLineRunner initData(UserRepository userRepository,
                               RoleRepository roleRepository,
                               PasswordEncoder passwordEncoder) {
        return args -> {
            Role adminRole = roleRepository.findByName(RoleType.ROLE_ADMIN.name())
                    .orElseGet(() -> roleRepository.save(Role.builder()
                            .name(RoleType.ROLE_ADMIN.name())
                            .build()));
            Role userRole = roleRepository.findByName(RoleType.ROLE_USER.name())
                    .orElseGet(() -> roleRepository.save(Role.builder()
                            .name(RoleType.ROLE_USER.name())
                            .build()));
            if (userRepository.findByUsername("admin01")
                    .isEmpty()) {
                User admin = new User();
                admin.setUsername("admin01");
                admin.setEmail("admin01@example.com");
                admin.setPassword(passwordEncoder.encode("Admin.123"));
                admin.setEnabled(true);
                admin.getRoles()
                        .add(adminRole);

                userRepository.save(admin);
                System.out.println("Admin account created: admin01/Admin.123");
            }
            if (userRepository.findByUsername("hoang01")
                    .isEmpty()) {
                User user = new User();
                user.setUsername("hoang01");
                user.setEmail("hoang01@gmail.com");
                user.setPassword(passwordEncoder.encode("Hoang@01"));
                user.setEnabled(true);
                user.getRoles()
                        .add(userRole);

                userRepository.save(user);
                System.out.println("user account created: hoang01/Hoang@01");
            }

        };
    }
}
