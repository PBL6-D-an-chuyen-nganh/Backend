package com.pbl.backend.security;

import com.pbl.backend.model.User;
import com.pbl.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailService implements UserDetailsService {

    @Autowired
    private UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByEmail(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + username);
        }

        List<GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority(user.getRole().toString()));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                authorities
        );
    }

    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        User user = userRepo.findByUserId(userId);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with userId: " + userId);
        }
        return new CustomUserDetails(user);
    }
}