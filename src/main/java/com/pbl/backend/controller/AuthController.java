package com.pbl.backend.controller;

import com.pbl.backend.dto.request.*;
import com.pbl.backend.dto.response.JwtAuthResponse;
import com.pbl.backend.dto.response.UserDTO;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    @PostMapping("/login")
    public ResponseEntity<?> createToken(@RequestBody JwtAuthRequest request) throws Exception {
        User user = userRepo.findByEmail(request.getEmail());

        if (user == null) {
            return new ResponseEntity<>(
                    "Email is not registered !!",
                    HttpStatus.NOT_FOUND
            );
        }

        if ("INACTIVE".equals(user.getAuthStatus())) {
            return new ResponseEntity<>(
                    "Email is not verified. Please verify your email before logging in.",
                    HttpStatus.FORBIDDEN
            );
        }

        try {
            this.authenticate(request.getEmail(), request.getPassword());
        } catch (RuntimeException e) {
            return new ResponseEntity<>(
                    "Invalid email or password !!",
                    HttpStatus.UNAUTHORIZED
            );
        }

        UserDetails userDetails = this.userDetailsService.loadUserByUsername(request.getEmail());

        String accessToken = jwtTokenHelper.generateAccessToken(userDetails, user.getUserId());
        String refreshToken = jwtTokenHelper.generateRefreshToken(userDetails, user.getUserId());

        JwtAuthResponse response = new JwtAuthResponse();
        response.setToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setUser(UserDTO.fromEntity(user));

        String roleMessage = "";
        if ("ROLE_USER".equals(user.getRole())) {
            roleMessage = "User login successfully";
        } else if ("ROLE_DOCTOR".equals(user.getRole())) {
            roleMessage = "Doctor login successfully";
        } else {
            roleMessage = "Đăng nhập thành công với vai trò " + user.getRole();
        }
        response.setMessage(roleMessage);

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

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = userRepo.findByEmail(request.getEmail());
            if (user != null) {
                if ("INACTIVE".equals(user.getAuthStatus())) {
                    otpService.clearOtp(request.getEmail());
                    String otp = otpService.generateOTP(request.getEmail());
                    mailService.sendOtpEmail(request.getEmail(), otp);
                    return new ResponseEntity<>(
                            "User already exists with INACTIVE status. New OTP sent to email.",
                            HttpStatus.OK
                    );
                }
                return new ResponseEntity<>(
                        "User already exists with ACTIVE status. Please login.",
                        HttpStatus.BAD_REQUEST
                );
            }

            UserDTO userDTO = new UserDTO();
            userDTO.setEmail(request.getEmail());
            userDTO.setName(request.getName());
            userDTO.setPhoneNumber(request.getPhoneNumber());
            userDTO.setRole("ROLE_USER");
            userDTO.setAuthStatus("INACTIVE");

            userService.registerNewUser(userDTO, request.getPassword());
            String otp = otpService.generateOTP(request.getEmail());
            mailService.sendOtpEmail(request.getEmail(), otp);

            return new ResponseEntity<>(
                    "User created with INACTIVE status. OTP sent to email.",
                    HttpStatus.OK
            );

        } catch (DataIntegrityViolationException e) {
            log.warn("Duplicate email during registration: {}", request.getEmail(), e);
            User existingUser = userRepo.findByEmail(request.getEmail());
            if (existingUser != null) {
                if ("INACTIVE".equals(existingUser.getAuthStatus())) {
                    otpService.clearOtp(request.getEmail());
                    String otp = otpService.generateOTP(request.getEmail());
                    mailService.sendOtpEmail(request.getEmail(), otp);
                    return new ResponseEntity<>(
                            "User already exists with INACTIVE status. New OTP sent to email.",
                            HttpStatus.OK
                    );
                }
                return new ResponseEntity<>(
                        "User already exists with ACTIVE status. Please login.",
                        HttpStatus.BAD_REQUEST
                );
            }
            throw e;
        }
    }


    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest request) {
        boolean isValid = otpService.verifyOtp(request.getEmail(), request.getOtp());

        if (!isValid) {
            return new ResponseEntity<>("Invalid or expired OTP", HttpStatus.BAD_REQUEST);
        }

        User user = userRepo.findByEmail(request.getEmail());
        if (user == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        user.setAuthStatus("ACTIVE");
        userRepo.save(user);

        UserDTO updatedUser = UserDTO.fromEntity(user);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @GetMapping("/get-otp")
    public ResponseEntity<String> getOtp(@RequestParam String email) {
        String otp = otpService.getLatestOtp(email);
        if (otp != null) {
            return new ResponseEntity<>(otp, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("No OTP found for the provided email.", HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/current-user")
    public ResponseEntity<?> getUser(Principal principal) {
        if (principal == null) {
            return new ResponseEntity<>("User is not authenticated", HttpStatus.UNAUTHORIZED);
        }
        User user = this.userRepo.findByEmail(principal.getName());
        return new ResponseEntity<>(UserDTO.fromEntity(user), HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            jwtTokenHelper.invalidateToken(token);
        }
        return new ResponseEntity<>("Successfully logged out", HttpStatus.OK);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        User user = userRepo.findByEmail(request.getEmail());
        if (user == null) {
            return new ResponseEntity<>("Email not found", HttpStatus.NOT_FOUND);
        }

        String otp = otpService.generateOTP(request.getEmail());
        mailService.sendOtpEmail(request.getEmail(), otp);

        return new ResponseEntity<>("OTP has been sent to your email for password reset.", HttpStatus.OK);
    }


    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        boolean isValid = otpService.verifyOtp(request.getEmail(), request.getOtp());

        if (!isValid) {
            return new ResponseEntity<>("Invalid or expired OTP", HttpStatus.BAD_REQUEST);
        }

        User user = userRepo.findByEmail(request.getEmail());
        if (user == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        userService.updatePassword(user, request.getNewPassword());

        return new ResponseEntity<>("Password has been successfully reset.", HttpStatus.OK);
    }
    @PostMapping("/resend-otp")
    public ResponseEntity<String> resendOtp(@RequestBody ResendOtpRequest request) {
        User user = userRepo.findByEmail(request.getEmail());
        if (user == null) {
            return new ResponseEntity<>("Email not found", HttpStatus.NOT_FOUND);
        }

        otpService.clearOtp(request.getEmail());
        String newOtp = otpService.generateOTP(request.getEmail());

        mailService.sendOtpEmail(request.getEmail(), newOtp);

        return new ResponseEntity<>("A new OTP has been sent to your email.", HttpStatus.OK);
    }

}