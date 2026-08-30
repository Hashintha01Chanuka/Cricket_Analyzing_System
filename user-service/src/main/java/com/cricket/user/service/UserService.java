package com.cricket.user.service;

import com.cricket.user.dto.RoleUpdateRequest;
import com.cricket.user.dto.UserResponse;
import com.cricket.user.entity.User;
import com.cricket.user.exception.UserNotFoundException;
import com.cricket.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserResponse getUser(UUID userId) {
        return UserResponse.from(findUserOrThrow(userId));
    }

    @Transactional(readOnly = true)
    public UserResponse getByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(UserResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("No such user: " + username));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream().map(UserResponse::from).toList();
    }

    @Transactional
    public UserResponse updateRole(UUID userId, RoleUpdateRequest request) {
        User user = findUserOrThrow(userId);
        user.setRole(request.role());
        return UserResponse.from(userRepository.save(user));
    }

    private User findUserOrThrow(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }
}