package com.pbl.backend.controller;

import com.pbl.backend.dto.JwtAuthRequest;
import com.pbl.backend.dto.JwtAuthResponse;
import com.pbl.backend.dto.UserDTO;
import com.pbl.backend.model.User;
import com.pbl.backend.repository.UserRepository;
import com.pbl.backend.security.JwtTokenHelper;
import com.pbl.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/auth/")
public class AuthController {

    @Autowired
    private JwtTokenHelper jwtTokenHelper;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepo;

    // Đăng nhập -> tạo JWT token
    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> createToken(@RequestBody JwtAuthRequest request) throws Exception {
        this.authenticate(request.getEmail(), request.getPassword());

        UserDetails userDetails = this.userDetailsService.loadUserByUsername(request.getEmail());
        User user = this.userRepo.findByEmail(request.getEmail());
//                .orElseThrow(() -> new RuntimeException("User not found with email: " + request.getEmail()));

        String token = this.jwtTokenHelper.generateToken(userDetails, user.getUserId());

        JwtAuthResponse response = new JwtAuthResponse();
        response.setToken(token);
        response.setUser(UserDTO.fromEntity(user));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private void authenticate(String email, String password) throws Exception {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(email, password);
        try {
            this.authenticationManager.authenticate(authenticationToken);
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Invalid email or password !!");
        }
    }

    // Đăng ký user mới
    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@Valid @RequestBody JwtAuthRequest request) {
        System.out.println("=== REGISTER ENDPOINT CALLED ===");
        System.out.println("Email: " + request.getEmail());

        try {
            UserDTO userDTO = new UserDTO();
            userDTO.setEmail(request.getEmail());
            userDTO.setRole("ROLE_USER");
            userDTO.setAuthStatus("ACTIVE");

            UserDTO registeredUser = this.userService.registerNewUser(userDTO, request.getPassword());
            return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
        } catch (Exception e) {
            System.out.println("Error in register: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // Lấy user hiện tại từ token
    @GetMapping("/current-user")
    public ResponseEntity<?> getUser(Principal principal) {
        if (principal == null) {
            return new ResponseEntity<>("User is not authenticated", HttpStatus.UNAUTHORIZED);
        }
        User user = this.userRepo.findByEmail(principal.getName());
//                .orElseThrow(() -> new RuntimeException("User not found with email: " + principal.getName()));
        return new ResponseEntity<>(UserDTO.fromEntity(user), HttpStatus.OK);
    }

    // Logout (invalidate token)
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            jwtTokenHelper.invalidateToken(token); // Cần implement trong JwtTokenHelper
        }
        return new ResponseEntity<>("Successfully logged out", HttpStatus.OK);
    }
}