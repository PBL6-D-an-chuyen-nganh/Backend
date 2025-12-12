package com.pbl.backend.service;

import com.pbl.backend.dto.request.ChangePasswordRequest;
import com.pbl.backend.dto.request.UserProfileUpdateRequest;
import com.pbl.backend.dto.response.UserDTO;
import com.pbl.backend.model.User;
import com.pbl.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
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

    public Page<UserDTO> getUsersWithRoleUser(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("userId").descending());

        Page<User> userPage = userRepo.findByRole(User.Role.ROLE_USER, pageable);

        return userPage.map(UserDTO::fromEntity);
    }

    public User getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        System.out.println("DEBUG: Đang tìm user với email: [" + email + "]");

        User user = userRepo.findByEmail(email);

        if (user == null) {
            System.out.println("ERROR: Không tìm thấy user trong DB!");
            throw new RuntimeException("User not found with email: " + email);
        }

        System.out.println("DEBUG: Tìm thấy user ID: " + user.getUserId());
        return user;
    }


    public UserDTO getCurrentUserProfile() {
        User user = getCurrentUser();
        return UserDTO.fromEntity(user);
    }

    public UserDTO updateCurrentUserProfile(UserProfileUpdateRequest request) {
        User user = getCurrentUser();

        if (request.getName() != null && !request.getName().isEmpty()) {
            user.setName(request.getName());
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isEmpty()) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        return UserDTO.fromEntity(userRepo.save(user));
    }

    public void changePassword(ChangePasswordRequest request) {
        User user = getCurrentUser();

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu cũ không chính xác");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepo.save(user);
    }

    public void deleteMyAccount() {
        User user = getCurrentUser();
        userRepo.delete(user);
    }
}
