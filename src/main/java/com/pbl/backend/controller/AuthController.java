package com.pbl.backend.controller;

import com.pbl.backend.dto.JwtAuthRequest;
import com.pbl.backend.dto.JwtAuthResponse;
import com.pbl.backend.dto.UserDTO;
import com.pbl.backend.model.User;
import com.pbl.backend.repository.UserRepository;
import com.pbl.backend.security.JwtTokenHelper;
import com.pbl.backend.service.EmailService;
import com.pbl.backend.service.OTPService;
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

    @Autowired
    private OTPService otpService;

    @Autowired
    private EmailService mailService;

    // Đăng nhập -> tạo JWT token
    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> createToken(@RequestBody JwtAuthRequest request) throws Exception {
        this.authenticate(request.getEmail(), request.getPassword());

        UserDetails userDetails = this.userDetailsService.loadUserByUsername(request.getEmail());
        User user = this.userRepo.findByEmail(request.getEmail());

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

    // Đăng ký user mới -> tạo user INACTIVE + gửi OTP
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody JwtAuthRequest request) {
        // tạo user với trạng thái INACTIVE
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail(request.getEmail());
        userDTO.setRole("ROLE_USER");
        userDTO.setAuthStatus("INACTIVE");

        // lưu user với password, trạng thái ban đầu là INACTIVE
        userService.registerNewUser(userDTO, request.getPassword());

        // sinh OTP & gửi email
        String otp = otpService.generateOtp(request.getEmail());
        mailService.sendOtpEmail(request.getEmail(), otp);

        return new ResponseEntity<>("User created with INACTIVE status. OTP sent to email.", HttpStatus.OK);
    }

    // Verify OTP -> nếu đúng thì update user thành ACTIVE
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestParam String email,
                                       @RequestParam String otp) {
        boolean isValid = otpService.verifyOtp(email, otp);

        if (!isValid) {
            return new ResponseEntity<>("Invalid or expired OTP", HttpStatus.BAD_REQUEST);
        }

        // OTP hợp lệ -> update trạng thái ACTIVE
        User user = userRepo.findByEmail(email);
        if (user == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        user.setAuthStatus("ACTIVE");
        userRepo.save(user); // lưu lại thay đổi

        UserDTO updatedUser = UserDTO.fromEntity(user);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }



    // Lấy user hiện tại từ token
    @GetMapping("/current-user")
    public ResponseEntity<?> getUser(Principal principal) {
        if (principal == null) {
            return new ResponseEntity<>("User is not authenticated", HttpStatus.UNAUTHORIZED);
        }
        User user = this.userRepo.findByEmail(principal.getName());
        return new ResponseEntity<>(UserDTO.fromEntity(user), HttpStatus.OK);
    }

    // Logout (invalidate token)
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            jwtTokenHelper.invalidateToken(token);
        }
        return new ResponseEntity<>("Successfully logged out", HttpStatus.OK);
    }
}
