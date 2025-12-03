package com.pbl.backend.service;

import com.pbl.backend.dto.response.UserDTO;
import com.pbl.backend.model.User;
import com.pbl.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserDTO registerNewUser(UserDTO userDTO, String rawPassword) {
        User user = userDTO.toEntity();

        if (rawPassword != null && !rawPassword.isEmpty()) {
            user.setPassword(passwordEncoder.encode(rawPassword));
        }

        User newUser = userRepo.save(user);
        return UserDTO.fromEntity(newUser);
    }

    public UserDTO updateUser(UserDTO userDTO) {
        User user = userRepo.findById(userDTO.getUserId())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with userId: " + userDTO.getUserId()
                ));

        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setRole(User.Role.valueOf(userDTO.getRole()));
        user.setAuthStatus(userDTO.getAuthStatus());

        User updatedUser = userRepo.save(user);
        return UserDTO.fromEntity(updatedUser);
    }

    public UserDTO getUserById(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with userId: " + userId
                ));
        return UserDTO.fromEntity(user);
    }

    public List<UserDTO> getAllUser() {
        return userRepo.findAll().stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public void deleteUser(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with userId: " + userId
                ));
        userRepo.delete(user);
    }

    public void updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);
    }
}
