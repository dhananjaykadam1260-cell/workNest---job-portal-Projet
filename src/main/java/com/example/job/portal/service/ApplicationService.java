package com.example.job.portal.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.job.portal.model.Application;
import com.example.job.portal.model.Job;
import com.example.job.portal.model.User;
import com.example.job.portal.repository.ApplicationRepository;

@Service
public class ApplicationService {

    @Autowired
    private ApplicationRepository applicationRepository;

    public String apply(User user, Job job) {

        if (applicationRepository.existsByUserAndJob(user, job)) {
            return "already";
        }

        Application application = new Application();

        application.setUser(user);
        application.setJob(job);
        application.setAppliedDate(LocalDate.now());
        application.setStatus("Applied");

        applicationRepository.save(application);

        return "success";
    }

    public List<Application> getAppliedJobs(User user) {
        return applicationRepository.findByUser(user);
    }

    public List<Application> getRecruiterApplications(
            String recruiterEmail) {

        return applicationRepository
                .findByJob_RecruiterEmail(recruiterEmail);
    }

    public List<Application> getRecruiterApplicationsByStatus(
            String recruiterEmail,
            String status) {

        if (status == null) {
            return List.of();
        }

        return applicationRepository
                .findByJob_RecruiterEmailAndStatusIgnoreCase(
                        recruiterEmail,
                        status.trim());
    }

    public long getApplicationCount(
            String recruiterEmail) {

        return applicationRepository
                .countByJob_RecruiterEmail(recruiterEmail);
    }

    public long getApplicationCountByStatus(
            String recruiterEmail,
            String status) {

        if (status == null) {
            return 0;
        }

        return applicationRepository
                .countByJob_RecruiterEmailAndStatusIgnoreCase(
                        recruiterEmail,
                        status.trim());
    }

    public Application getApplicationById(Long id) {

        if (id == null) {
            return null;
        }

        return applicationRepository
                .findApplicationWithUserAndJob(id)
                .orElse(null);
    }

    public ApplicationRepository.CandidateSummary
    getCandidateSummary(Long id) {

        if (id == null) {
            return null;
        }

        return applicationRepository
                .findCandidateSummary(id)
                .orElse(null);
    }

    public boolean updateStatus(
            Long applicationId,
            String status,
            String recruiterEmail) {

        if (applicationId == null
                || status == null
                || recruiterEmail == null) {
            return false;
        }

        status = status.trim();

        if (status.equalsIgnoreCase("Applied")) {
            status = "Applied";
        } else if (status.equalsIgnoreCase("Shortlisted")) {
            status = "Shortlisted";
        } else if (status.equalsIgnoreCase("Interview")) {
            status = "Interview";
        } else if (status.equalsIgnoreCase("Selected")) {
            status = "Selected";
        } else if (status.equalsIgnoreCase("Rejected")) {
            status = "Rejected";
        } else {
            return false;
        }

        Application application =
                applicationRepository
                        .findById(applicationId)
                        .orElse(null);

        if (application == null) {
            return false;
        }

        if (application.getJob() == null) {
            return false;
        }

        if (application.getJob().getRecruiterEmail() == null) {
            return false;
        }

        if (!application.getJob()
                .getRecruiterEmail()
                .equalsIgnoreCase(recruiterEmail.trim())) {
            return false;
        }

        application.setStatus(status);

        applicationRepository.save(application);

        return true;
    }

    public void deleteApplicationsByJob(Job job) {

        if (job == null) {
            return;
        }

        List<Application> applications =
                applicationRepository.findByJob(job);

        for (Application application : applications) {
            applicationRepository.delete(application);
        }
    }
}