package com.pbl.backend.repository;

import com.pbl.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
    User findByUserId(Integer userId);
    User findByEmail(String email);
}
