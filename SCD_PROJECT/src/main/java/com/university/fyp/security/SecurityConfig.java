package com.university.fyp.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/register").permitAll()
                        .requestMatchers("/api/auth/me").permitAll()
                        .requestMatchers("/api/public/**").permitAll()

                        // Student endpoints
                        .requestMatchers("/api/student/**").hasRole("STUDENT")

                        // Supervisor endpoints
                        .requestMatchers("/api/supervisor/**").hasRole("SUPERVISOR")

                        // Committee endpoints
                        .requestMatchers("/api/committee/**").hasRole("COMMITTEE_MEMBER")

                        // FYP Committee endpoints
                        .requestMatchers("/api/fyp-committee/**").hasRole("FYP_COMMITTEE")

                        // Document endpoints (role-based access controlled in service layer)
                        .requestMatchers(HttpMethod.GET, "/api/documents/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/documents/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/documents/**").authenticated()

                        // Group endpoints (role-based access controlled in service layer)
                        .requestMatchers(HttpMethod.GET, "/api/groups/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/groups/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/groups/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/groups/**").authenticated()

                        // All other requests must be authenticated
                        .anyRequest().authenticated())
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
