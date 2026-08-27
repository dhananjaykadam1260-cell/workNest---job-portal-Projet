package com.example.job.portal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.job.portal.model.Recruiter;

public interface RecruiterRepository extends JpaRepository<Recruiter, Long>{

    Optional<Recruiter> findByEmail(String email);

}