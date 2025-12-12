package com.pbl.backend.controller;

import com.pbl.backend.dto.response.UserDTO;
import com.pbl.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")

public class UserAdminController {

    @Autowired
    private UserService userService;

    @GetMapping("/role-user")
    public ResponseEntity<Page<UserDTO>> getUsersWithRoleUser(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<UserDTO> userPage = userService.getUsersWithRoleUser(page, size);
        return ResponseEntity.ok(userPage);
    }
}
