package com.university.fyp.service;

import com.university.fyp.dto.UserDTO;
import com.university.fyp.entity.User;
import com.university.fyp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<UserDTO> getSupervisors() {
        return userRepository.findByRoleName("SUPERVISOR").stream()
                .map(UserDTO::fromUser)
                .collect(java.util.stream.Collectors.toList());
    }

    public List<UserDTO> getAvailableStudents() {
        return userRepository.findByRoleNameAndGroupIsNull("STUDENT").stream()
                .map(UserDTO::fromUser)
                .collect(java.util.stream.Collectors.toList());
    }
}
