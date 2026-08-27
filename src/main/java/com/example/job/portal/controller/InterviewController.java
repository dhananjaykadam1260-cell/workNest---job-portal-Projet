package com.example.job.portal.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.job.portal.model.Application;
import com.example.job.portal.model.Interview;
import com.example.job.portal.model.Recruiter;
import com.example.job.portal.repository.ApplicationRepository;
import com.example.job.portal.service.ApplicationService;
import com.example.job.portal.service.EmailService;
import com.example.job.portal.service.InterviewService;

import jakarta.servlet.http.HttpSession;

@Controller
public class InterviewController {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private InterviewService interviewService;

    @Autowired
    private EmailService emailService;

    @GetMapping("/recruiter/application/{id}/schedule-interview")
    public String scheduleInterviewPage(
            @PathVariable Long id,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        Recruiter recruiter =
                (Recruiter) session.getAttribute("loggedRecruiter");

        if (recruiter == null) {
            return "redirect:/recruiter-login";
        }

        Application application =
                applicationService.getApplicationById(id);

        if (application == null) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Application not found."
            );

            return "redirect:/recruiter-applications";
        }

        if (application.getJob() == null ||
                application.getJob().getRecruiterEmail() == null ||
                !application.getJob()
                        .getRecruiterEmail()
                        .trim()
                        .equalsIgnoreCase(
                                recruiter.getEmail().trim())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "You cannot schedule an interview for this application."
            );

            return "redirect:/recruiter-applications";
        }

        if (application.getStatus() == null ||
                !application.getStatus()
                        .equalsIgnoreCase("Shortlisted")) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Only shortlisted candidates can be scheduled for an interview."
            );

            return "redirect:/recruiter-shortlisted";
        }

        ApplicationRepository.CandidateSummary candidateSummary =
                applicationService.getCandidateSummary(id);

        model.addAttribute("recruiter", recruiter);
        model.addAttribute("application", application);
        model.addAttribute("applicationId", id);
        model.addAttribute("candidateSummary", candidateSummary);

        return "recruiter-schedule-interview";
    }

    @PostMapping("/recruiter/schedule-interview")
    public String scheduleInterview(

            @RequestParam Long applicationId,

            @RequestParam String interviewDate,
            @RequestParam String interviewTime,
            @RequestParam String interviewMode,

            @RequestParam(required = false) String meetingLink,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String notes,

            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Recruiter recruiter =
                (Recruiter) session.getAttribute("loggedRecruiter");

        if (recruiter == null) {
            return "redirect:/recruiter-login";
        }

        Application application =
                applicationService.getApplicationById(applicationId);

        if (application == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Application not found."
            );

            return "redirect:/recruiter-applications";
        }

        if (application.getJob() == null
                || application.getJob().getRecruiterEmail() == null
                || !application.getJob()
                        .getRecruiterEmail()
                        .trim()
                        .equalsIgnoreCase(
                                recruiter.getEmail().trim())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "You cannot schedule an interview for this application."
            );

            return "redirect:/recruiter-applications";
        }

        if (application.getStatus() == null
                || !application.getStatus()
                        .equalsIgnoreCase("Shortlisted")) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Only shortlisted candidates can be scheduled for an interview."
            );

            return "redirect:/recruiter-shortlisted";
        }

        Optional<Interview> existingInterview =
                interviewService.getInterviewByApplicationId(
                        applicationId
                );

        if (existingInterview.isPresent()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "An interview has already been scheduled for this application."
            );

            return "redirect:/recruiter-interviews";
        }

        try {

            LocalDate date =
                    LocalDate.parse(interviewDate);

            LocalTime time =
                    LocalTime.parse(interviewTime);

            String mode =
                    interviewMode == null
                            ? ""
                            : interviewMode.trim();

            if (!"Online".equalsIgnoreCase(mode)
                    && !"Offline".equalsIgnoreCase(mode)) {

                redirectAttributes.addFlashAttribute(
                        "error",
                        "Please select a valid interview mode."
                );

                return "redirect:/recruiter/application/"
                        + applicationId
                        + "/schedule-interview";
            }

            if ("Online".equalsIgnoreCase(mode)
                    && (meetingLink == null
                    || meetingLink.isBlank())) {

                redirectAttributes.addFlashAttribute(
                        "error",
                        "Meeting link is required for an online interview."
                );

                return "redirect:/recruiter/application/"
                        + applicationId
                        + "/schedule-interview";
            }

            if ("Offline".equalsIgnoreCase(mode)
                    && (location == null
                    || location.isBlank())) {

                redirectAttributes.addFlashAttribute(
                        "error",
                        "Location is required for an offline interview."
                );

                return "redirect:/recruiter/application/"
                        + applicationId
                        + "/schedule-interview";
            }

            if (date.isBefore(LocalDate.now())) {

                redirectAttributes.addFlashAttribute(
                        "error",
                        "Interview date cannot be in the past."
                );

                return "redirect:/recruiter/application/"
                        + applicationId
                        + "/schedule-interview";
            }

            Interview savedInterview =
                    interviewService.scheduleInterview(
                            application,
                            date,
                            time,
                            mode,
                            meetingLink,
                            location,
                            notes
                    );

            boolean statusUpdated =
                    applicationService.updateStatus(
                            applicationId,
                            "Interview",
                            recruiter.getEmail()
                    );

            if (!statusUpdated) {

                if (savedInterview != null
                        && savedInterview.getId() != null) {

                    interviewService.cancelInterview(
                            savedInterview.getId()
                    );
                }

                redirectAttributes.addFlashAttribute(
                        "error",
                        "Interview was saved, but the application status could not be updated."
                );

                return "redirect:/recruiter/application/"
                        + applicationId
                        + "/schedule-interview";
            }

            emailService.sendInterviewScheduledEmail(
                    savedInterview
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Interview scheduled successfully."
            );

            return "redirect:/recruiter-interviews";

        } catch (Exception e) {

            e.printStackTrace();

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Unable to schedule interview: "
                            + e.getMessage()
            );

            return "redirect:/recruiter/application/"
                    + applicationId
                    + "/schedule-interview";
        }
    }
    
    @GetMapping("/recruiter-interviews")
    public String interviews(
            HttpSession session,
            Model model) {

        Recruiter recruiter =
                (Recruiter) session.getAttribute(
                        "loggedRecruiter");

        if (recruiter == null) {
            return "redirect:/recruiter-login";
        }

        List<Application> applications =
                applicationService
                        .getRecruiterApplicationsByStatus(
                                recruiter.getEmail(),
                                "Interview");

        Map<Long, Interview> interviewsByApplicationId =
                new HashMap<>();

        for (Application application : applications) {

            interviewService
                    .getInterviewByApplicationId(
                            application.getId())
                    .ifPresent(interview ->
                            interviewsByApplicationId.put(
                                    application.getId(),
                                    interview));
        }

        model.addAttribute(
                "recruiter",
                recruiter);

        model.addAttribute(
                "applications",
                applications);

        model.addAttribute(
                "interviewsByApplicationId",
                interviewsByApplicationId);

        return "recruiter-interviews";
    }
}