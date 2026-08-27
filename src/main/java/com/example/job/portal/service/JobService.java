package com.example.job.portal.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.job.portal.model.Job;
import com.example.job.portal.repository.JobRepository;

@Service
public class JobService {

    @Autowired
    private JobRepository jobRepository;

    public void saveJob(Job job) {

        job.setPostedDate(LocalDate.now());

        jobRepository.save(job);
    }

    public List<Job> getJobs(String recruiterEmail){

        return jobRepository.findByRecruiterEmail(recruiterEmail);

    }

    public long getTotalJobs(String email){

        return jobRepository.countByRecruiterEmail(email);

    }

    public Job getJob(Long id){

        return jobRepository.findById(id).orElse(null);

    }
    
    public void updateJob(Job job) {
        jobRepository.save(job);
    }

    public void deleteJob(Long id) {
        jobRepository.deleteById(id);
    }
    
    public long getTotalJobs() {
        return jobRepository.count();
    }
    
    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }
    
    public Job getJobById(Long id) {
        return jobRepository.findById(id).orElse(null);
    }
    

}