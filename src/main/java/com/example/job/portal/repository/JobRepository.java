package com.example.job.portal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.job.portal.model.Job;

public interface JobRepository extends JpaRepository<Job, Long>{

    List<Job> findByRecruiterEmail(String recruiterEmail);

    long countByRecruiterEmail(String recruiterEmail);

}