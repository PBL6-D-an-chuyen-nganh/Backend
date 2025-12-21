package com.pbl.backend.controller;

import com.pbl.backend.dto.response.PagedResponse;
import com.pbl.backend.dto.response.UserDTO;
import com.pbl.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @DeleteMapping()
    public ResponseEntity<String> deleteUserById(@RequestParam Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("Đã xoá thành công người dùng có ID: " + id);
    }

    @GetMapping("/search")
    public ResponseEntity<PagedResponse<UserDTO>> searchUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "userId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,

            @RequestParam(required = false) String name
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<UserDTO> doctors = userService.searchUsers(name, pageable);

        PagedResponse<UserDTO> response = new PagedResponse<>(
                doctors.getContent(),
                doctors.getNumber(),
                doctors.getSize(),
                doctors.getTotalElements(),
                doctors.getTotalPages(),
                doctors.isLast()
        );

        return ResponseEntity.ok(response);
    }
}
