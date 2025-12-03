package com.pbl.backend.dto.response;

import com.pbl.backend.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

public class UserDTO {
    private Long userId;
    private String name;
    private String email;
    private String phoneNumber;
    private String role;
    private String authStatus;

    public static UserDTO fromEntity(User user) {
        return new UserDTO(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole().name(),
                user.getAuthStatus()
        );
    }

    public User toEntity() {
        User user = new User();
        user.setUserId(this.userId);
        user.setName(this.name);
        user.setEmail(this.email);
        user.setPhoneNumber(this.phoneNumber);
        user.setRole(User.Role.valueOf(this.role));
        user.setAuthStatus(this.authStatus);
        return user;
    }
}
