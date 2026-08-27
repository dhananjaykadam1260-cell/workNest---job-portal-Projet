package com.example.job.portal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.job.portal.model.User;
import com.example.job.portal.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public String register(User user) {

        if (userRepository.findByEmail(user.getEmail()) != null) {
            return "exists";
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepository.save(user);

        emailService.sendWelcomeMail(
                user.getEmail(),
                user.getName()
        );

        return "success";
    }

    public User login(String email, String password) {

        User user = userRepository.findByEmail(email);

        if (user == null || user.getPassword() == null) {
            return null;
        }

        // Login with BCrypt password
        if (passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }

        // Convert old plain-text password to BCrypt
        if (password.equals(user.getPassword())) {

            user.setPassword(
                    passwordEncoder.encode(password)
            );

            return userRepository.save(user);
        }

        return null;
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User updateProfile(User user) {
        return userRepository.save(user);
    }

    public int calculateProfileCompletion(Long id) {

        User user = getUserById(id);

        if (user == null) {
            return 0;
        }

        int total = 6;
        int completed = 0;

        if (user.getName() != null && !user.getName().isBlank()) {
            completed++;
        }

        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            completed++;
        }

        if (user.getMobile() != null && !user.getMobile().isBlank()) {
            completed++;
        }

        if (user.getCity() != null && !user.getCity().isBlank()) {
            completed++;
        }

        if (user.getQualification() != null && !user.getQualification().isBlank()) {
            completed++;
        }

        if (user.getExperience() != null && !user.getExperience().isBlank()) {
            completed++;
        }

        return (completed * 100) / total;
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void sendOtp(String email, String otp) {
        emailService.sendOtpMail(email, otp);
    }

    public void updatePassword(String email, String password) {

        User user = userRepository.findByEmail(email);

        if (user != null) {

            user.setPassword(
                    passwordEncoder.encode(password)
            );

            userRepository.save(user);
        }
    }
}