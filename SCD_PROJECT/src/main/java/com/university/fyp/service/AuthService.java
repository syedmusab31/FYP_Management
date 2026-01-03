package com.university.fyp.service;

import com.university.fyp.dto.JwtResponse;
import com.university.fyp.dto.LoginRequest;
import com.university.fyp.dto.RegisterRequest;
import com.university.fyp.dto.UserDTO;
import com.university.fyp.entity.Group;
import com.university.fyp.entity.Role;
import com.university.fyp.entity.User;
import com.university.fyp.repository.GroupRepository;
import com.university.fyp.repository.RoleRepository;
import com.university.fyp.repository.UserRepository;
import com.university.fyp.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final GroupRepository groupRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public JwtResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new JwtResponse(
                jwt,
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().getName(),
                user.getGroup() != null ? user.getGroup().getId() : null);
    }

    @Transactional
    public void register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Default to Student role (ID 1) if roleId is not provided
        Long roleId = registerRequest.getRoleId() != null ? registerRequest.getRoleId() : 1L;

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFullName(registerRequest.getFullName());
        user.setRole(role);
        user.setIsActive(true);

        userRepository.save(user);
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
    }

    public UserDTO getCurrentUserDTO() {
        User user = getCurrentUser();

        // Trigger lazy loading of relationships to avoid proxy serialization issues
        if (user.getRole() != null) {
            user.getRole().getId();
            user.getRole().getName();
        }
        if (user.getGroup() != null) {
            user.getGroup().getId();
            user.getGroup().getGroupName();
        }

        return UserDTO.fromUser(user);
    }
}
