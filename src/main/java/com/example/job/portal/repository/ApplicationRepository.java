package com.example.job.portal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.job.portal.model.Application;
import com.example.job.portal.model.Job;
import com.example.job.portal.model.User;

public interface ApplicationRepository
        extends JpaRepository<Application, Long> {

    boolean existsByUserAndJob(User user, Job job);

    List<Application> findByUser(User user);

    List<Application> findByJob(Job job);

    List<Application> findByJob_RecruiterEmail(
            String recruiterEmail);

    long countByJob_RecruiterEmail(
            String recruiterEmail);

    List<Application> findByJob_RecruiterEmailAndStatusIgnoreCase(
            String recruiterEmail,
            String status);

    long countByJob_RecruiterEmailAndStatusIgnoreCase(
            String recruiterEmail,
            String status);

    @Query("""
        SELECT a
        FROM Application a
        JOIN FETCH a.user
        JOIN FETCH a.job
        WHERE a.id = :id
    """)
    Optional<Application> findApplicationWithUserAndJob(
            @Param("id") Long id);

    interface CandidateSummary {

        String getName();

        String getEmail();

        String getJobTitle();
    }

    @Query(value = """
        SELECT
            u.name AS name,
            u.email AS email,
            j.job_title AS jobTitle
        FROM application a
        JOIN users u ON a.user_id = u.id
        JOIN job j ON a.job_id = j.id
        WHERE a.id = :id
        """, nativeQuery = true)
    Optional<CandidateSummary> findCandidateSummary(
            @Param("id") Long id);
}