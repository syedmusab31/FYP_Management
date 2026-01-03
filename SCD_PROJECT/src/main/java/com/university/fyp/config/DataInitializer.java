package com.university.fyp.config;

import com.university.fyp.entity.Role;
import com.university.fyp.entity.User;
import com.university.fyp.repository.RoleRepository;
import com.university.fyp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            // Initialize Roles
            List<String> roleNames = Arrays.asList("STUDENT", "SUPERVISOR", "COMMITTEE_MEMBER", "FYP_COMMITTEE");

            for (String roleName : roleNames) {
                if (!roleRepository.existsByName(roleName)) {
                    Role role = new Role();
                    role.setName(roleName);
                    role.setDescription("Role for " + roleName.toLowerCase().replace("_", " "));
                    roleRepository.save(role);
                }
            }

            // Initialize Default FYP Committee Admin User
            // Email: admin@university.edu
            // Password: password123
            String adminEmail = "admin@university.edu";
            if (!userRepository.existsByEmail(adminEmail)) {
                Role fypRole = roleRepository.findByName("FYP_COMMITTEE")
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));

                User admin = new User();
                admin.setEmail(adminEmail);
                admin.setPassword(passwordEncoder.encode("password123"));
                admin.setFullName("FYP Committee Admin");
                admin.setRole(fypRole);
                admin.setIsActive(true);
                admin.setCreatedAt(Instant.now());
                admin.setUpdatedAt(Instant.now());

                userRepository.save(admin);
                System.out.println("Default Admin User created: " + adminEmail);
            }
        };
    }
}
