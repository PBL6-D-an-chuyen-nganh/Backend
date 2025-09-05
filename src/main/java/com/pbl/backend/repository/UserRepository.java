package com.pbl.backend.repository;

import com.pbl.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;


public interface UserRepository extends JpaRepository<User, Integer> {
    User findByUserId(Integer userId);
    User findByEmail(String email);
}
