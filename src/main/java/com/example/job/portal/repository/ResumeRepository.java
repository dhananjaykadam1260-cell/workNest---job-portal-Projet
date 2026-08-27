package com.example.job.portal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.job.portal.model.Resume;

public interface ResumeRepository extends JpaRepository<Resume, Long> {

    Resume findByUserId(Long userId);

}