package com.university.fyp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class FypManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(FypManagementApplication.class, args);
    }
}
