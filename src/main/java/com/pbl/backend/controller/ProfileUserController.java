package com.pbl.backend.controller;

import com.pbl.backend.dto.request.ChangePasswordRequest;
import com.pbl.backend.dto.request.UserProfileUpdateRequest;
import com.pbl.backend.dto.response.UserDTO;
import com.pbl.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/profile")
@RequiredArgsConstructor
public class ProfileUserController {

    private final UserService userService;

    @GetMapping()
    public ResponseEntity<UserDTO> getMyProfile() {
        return ResponseEntity.ok(userService.getCurrentUserProfile());
    }

    @PutMapping("/update")
    public ResponseEntity<UserDTO> update(@RequestBody UserProfileUpdateRequest request) {
        return ResponseEntity.ok(userService.updateCurrentUserProfile(request));
    }

    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ResponseEntity.ok("Đổi mật khẩu thành công.");
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteMyAccount() {
        userService.deleteMyAccount();
        return ResponseEntity.ok("Tài khoản đã được xóa vĩnh viễn.");
    }
}