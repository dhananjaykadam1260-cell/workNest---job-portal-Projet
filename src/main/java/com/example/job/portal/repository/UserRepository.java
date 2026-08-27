package com.example.job.portal.repository;




import org.springframework.data.jpa.repository.JpaRepository;

import com.example.job.portal.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmailAndPassword(String email, String password);
    User findByEmail(String email);
}