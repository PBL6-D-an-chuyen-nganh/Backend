package com.pbl.backend.repository;

import com.pbl.backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUserId(Long userId);
    User findByEmail(String email);
    Page<User> findByRole(User.Role role, Pageable pageable);
    Page<User> findByRoleAndNameContainingIgnoreCase(User.Role role, String name, Pageable pageable);
}
