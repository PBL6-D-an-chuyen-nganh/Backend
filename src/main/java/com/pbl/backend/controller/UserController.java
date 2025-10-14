package com.pbl.backend.controller;

import com.pbl.backend.dto.ApiResponse;
import com.pbl.backend.dto.UserDTO;
import com.pbl.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public ResponseEntity<List<UserDTO>> getAllUser(){
        return ResponseEntity.ok(this.userService.getAllUser());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long userId){
        return ResponseEntity.ok(this.userService.getUserById(userId));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable("userId") Long id,
            @Valid @RequestBody UserDTO userDTO) {

        userDTO.setUserId(id);
        UserDTO updated = this.userService.updateUser(userDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId)
    {
        this.userService.deleteUser(userId);
        return new ResponseEntity<ApiResponse>(new ApiResponse("User deleted Successfully", true), HttpStatus.OK);

    }

}