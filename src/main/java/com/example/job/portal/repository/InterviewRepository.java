package com.example.job.portal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.job.portal.model.Interview;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {

    Optional<Interview> findByApplicationId(Long applicationId);

    List<Interview> findByApplicationJobRecruiterEmail(
            String recruiterEmail);

    List<Interview> findByApplicationJobRecruiterEmailAndStatus(
            String recruiterEmail,
            String status);

    List<Interview> findByApplicationUserId(Long userId);
}