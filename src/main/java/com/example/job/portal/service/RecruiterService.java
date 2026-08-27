package com.example.job.portal.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.job.portal.model.Recruiter;
import com.example.job.portal.repository.RecruiterRepository;

@Service
public class RecruiterService {

    @Autowired
    private RecruiterRepository recruiterRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    public String register(Recruiter recruiter) {

        if (recruiterRepository.findByEmail(recruiter.getEmail()).isPresent()) {
            return "exists";
        }

        recruiter.setPassword(
                passwordEncoder.encode(recruiter.getPassword())
        );

        recruiterRepository.save(recruiter);

        emailService.sendRecruiterRegistrationMail(
                recruiter.getEmail(),
                recruiter.getRecruiterName(),
                recruiter.getCompanyName()
        );

        return "success";
    }

    public Recruiter login(String email, String password) {

        Optional<Recruiter> recruiterOpt =
                recruiterRepository.findByEmail(email);

        if (recruiterOpt.isEmpty()) {
            return null;
        }

        Recruiter recruiter = recruiterOpt.get();

        if (recruiter.getPassword() == null) {
            return null;
        }

        // Check BCrypt password
        if (passwordEncoder.matches(
                password,
                recruiter.getPassword())) {

            return recruiter;
        }

        // Convert old plain-text password to BCrypt
        if (password.equals(recruiter.getPassword())) {

            recruiter.setPassword(
                    passwordEncoder.encode(password)
            );

            return recruiterRepository.save(recruiter);
        }

        return null;
    }

    public Optional<Recruiter> findByEmail(String email) {
        return recruiterRepository.findByEmail(email);
    }

    public Optional<Recruiter> findById(Long id) {
        return recruiterRepository.findById(id);
    }

    public void updateRecruiter(Recruiter recruiter) {
        recruiterRepository.save(recruiter);
    }
    
    public boolean resetPassword(String email, String newPassword) {

        Optional<Recruiter> recruiterOpt =
                recruiterRepository.findByEmail(email);

        if (recruiterOpt.isEmpty()) {
            return false;
        }

        Recruiter recruiter = recruiterOpt.get();

        recruiter.setPassword(
                passwordEncoder.encode(newPassword)
        );

        recruiterRepository.save(recruiter);

        return true;
    }
}