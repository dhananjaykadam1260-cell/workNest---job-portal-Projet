package com.example.job.portal.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.job.portal.model.Application;
import com.example.job.portal.model.Interview;
import com.example.job.portal.repository.InterviewRepository;

@Service
public class InterviewService {

    @Autowired
    private InterviewRepository interviewRepository;

    @Transactional
    public Interview scheduleInterview(
            Application application,
            LocalDate interviewDate,
            LocalTime interviewTime,
            String interviewMode,
            String meetingLink,
            String location,
            String notes) {

        if (application == null) {
            throw new IllegalArgumentException(
                    "Application is required.");
        }

        Optional<Interview> existing =
                interviewRepository.findByApplicationId(
                        application.getId());

        if (existing.isPresent()) {
            throw new IllegalStateException(
                    "Interview already scheduled for this application.");
        }

        Interview interview = new Interview();

        interview.setApplication(application);
        interview.setInterviewDate(interviewDate);
        interview.setInterviewTime(interviewTime);
        interview.setInterviewMode(interviewMode);
        interview.setMeetingLink(meetingLink);
        interview.setLocation(location);
        interview.setNotes(notes);
        interview.setStatus("Scheduled");

        return interviewRepository.save(interview);
    }

    public Optional<Interview> getInterviewById(Long id) {
        return interviewRepository.findById(id);
    }

    public Optional<Interview> getInterviewByApplicationId(
            Long applicationId) {

        return interviewRepository.findByApplicationId(
                applicationId);
    }

    public List<Interview> getRecruiterInterviews(
            String recruiterEmail) {

        return interviewRepository
                .findByApplicationJobRecruiterEmail(
                        recruiterEmail);
    }

    public List<Interview> getScheduledInterviews(
            String recruiterEmail) {

        return interviewRepository
                .findByApplicationJobRecruiterEmailAndStatus(
                        recruiterEmail,
                        "Scheduled");
    }

    public List<Interview> getCandidateInterviews(
            Long userId) {

        return interviewRepository
                .findByApplicationUserId(userId);
    }

    @Transactional
    public void cancelInterview(Long interviewId) {

        Interview interview =
                interviewRepository.findById(interviewId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Interview not found."));

        interview.setStatus("Cancelled");

        interviewRepository.save(interview);
    }

    @Transactional
    public void deleteInterview(Long interviewId) {

        if (interviewRepository.existsById(interviewId)) {
            interviewRepository.deleteById(interviewId);
        }
    }
}