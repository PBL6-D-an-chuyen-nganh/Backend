package com.pbl.backend.security;

import com.pbl.backend.model.User;
import com.pbl.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailService implements UserDetailsService {

    @Autowired
    private UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepo.findByEmail(email);
//                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return new CustomUserDetails(user);
    }

    public UserDetails loadUserById(Integer userId) throws UsernameNotFoundException {
        User user = userRepo.findByUserId(userId);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with userId: " + userId);
        }
        return new CustomUserDetails(user);
    }

}