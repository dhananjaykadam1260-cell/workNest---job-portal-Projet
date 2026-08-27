package com.example.job.portal.controller;

import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.job.portal.model.Application;
import com.example.job.portal.model.Recruiter;
import com.example.job.portal.model.Resume;
import com.example.job.portal.service.ApplicationService;
import com.example.job.portal.service.EmailService;
import com.example.job.portal.service.JobService;
import com.example.job.portal.service.RecruiterService;
import com.example.job.portal.service.ResumeService;

import jakarta.servlet.http.HttpSession;

@Controller
public class RecruiterController {

    @Autowired
    private ResumeService resumeService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private JobService jobService;

    @Autowired
    private RecruiterService recruiterService;


    @GetMapping("/recruiter-register")
    public String registerPage() {
        return "recruiter-register";
    }

    @PostMapping("/recruiter-register")
    public String register(
            @ModelAttribute Recruiter recruiter,
            @RequestParam String confirmPassword,
            Model model) {

        if (!recruiter.getPassword().equals(confirmPassword)) {
            model.addAttribute(
                    "error",
                    "Passwords do not match."
            );
            return "recruiter-register";
        }

        if (recruiter.getPassword().length() < 6) {
            model.addAttribute(
                    "error",
                    "Password must contain at least 6 characters."
            );
            return "recruiter-register";
        }

        String result = recruiterService.register(recruiter);

        if (result.equals("exists")) {
            model.addAttribute(
                    "error",
                    "Email already registered"
            );
            return "recruiter-register";
        }

        return "redirect:/recruiter-login";
    }


    @GetMapping("/recruiter-login")
    public String loginPage() {
        return "recruiter-login";
    }

    @PostMapping("/recruiter-login")
    public String login(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        Recruiter recruiter =
                recruiterService.login(email, password);

        if (recruiter == null) {
            model.addAttribute(
                    "error",
                    "Invalid Email or Password"
            );
            return "recruiter-login";
        }

        session.setAttribute(
                "loggedRecruiter",
                recruiter
        );

        return "redirect:/recruiter-dashboard";
    }


    @GetMapping("/recruiter-dashboard")
    public String dashboard(
            HttpSession session,
            Model model) {

        Recruiter recruiter =
                (Recruiter) session.getAttribute(
                        "loggedRecruiter"
                );

        if (recruiter == null) {
            return "redirect:/recruiter-login";
        }

        String recruiterEmail =
                recruiter.getEmail();

        List<Application> allApplications =
                applicationService.getRecruiterApplications(
                        recruiterEmail
                );

        List<Application> shortlistedApplications =
                applicationService.getRecruiterApplicationsByStatus(
                        recruiterEmail,
                        "Shortlisted"
                );

        List<Application> interviewApplications =
                applicationService.getRecruiterApplicationsByStatus(
                        recruiterEmail,
                        "Interview"
                );

        model.addAttribute(
                "recruiter",
                recruiter
        );

        model.addAttribute(
                "totalJobs",
                jobService.getTotalJobs(
                        recruiterEmail
                )
        );

        model.addAttribute(
                "applications",
                allApplications.size()
        );

        model.addAttribute(
                "shortlisted",
                shortlistedApplications.size()
        );

        model.addAttribute(
                "interviews",
                interviewApplications.size()
        );

        model.addAttribute(
                "jobs",
                jobService.getJobs(
                        recruiterEmail
                )
        );

        return "recruiter-dashboard";
    }


    @GetMapping("/recruiter-applications")
    public String applications(
            HttpSession session,
            Model model) {

        Recruiter recruiter =
                (Recruiter) session.getAttribute(
                        "loggedRecruiter"
                );

        if (recruiter == null) {
            return "redirect:/recruiter-login";
        }

        List<Application> applications =
                applicationService.getRecruiterApplications(
                        recruiter.getEmail()
                );

        model.addAttribute(
                "recruiter",
                recruiter
        );

        model.addAttribute(
                "applications",
                applications
        );

        return "recruiter-applications";
    }


    @PostMapping("/recruiter/application/{id}/status")
    public String updateApplicationStatus(
            @PathVariable Long id,
            @RequestParam String status,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Recruiter recruiter =
                (Recruiter) session.getAttribute(
                        "loggedRecruiter"
                );

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

        String requestedStatus =
                status == null
                        ? ""
                        : status.trim();

        String previousStatus =
                application.getStatus() == null
                        ? ""
                        : application.getStatus().trim();

        boolean statusChanged =
                !previousStatus.equalsIgnoreCase(
                        requestedStatus
                );

        boolean updated =
                applicationService.updateStatus(
                        id,
                        requestedStatus,
                        recruiter.getEmail()
                );

        if (!updated) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Unable to update application status."
            );

            return "redirect:/recruiter-applications";
        }

        if (statusChanged
                && ("Shortlisted".equalsIgnoreCase(requestedStatus)
                || "Selected".equalsIgnoreCase(requestedStatus)
                || "Rejected".equalsIgnoreCase(requestedStatus))) {

            Application updatedApplication =
                    applicationService.getApplicationById(id);

            if (updatedApplication != null) {

                emailService.sendApplicationStatusEmail(
                        updatedApplication,
                        requestedStatus
                );
            }
        }

        redirectAttributes.addFlashAttribute(
                "success",
                "Application status updated successfully."
        );

        if ("Shortlisted".equalsIgnoreCase(
                requestedStatus)) {

            return "redirect:/recruiter-shortlisted";
        }

        if ("Interview".equalsIgnoreCase(
                requestedStatus)) {

            return "redirect:/recruiter-interviews";
        }

        if ("Selected".equalsIgnoreCase(
                requestedStatus)) {

            return "redirect:/recruiter-selected";
        }

        if ("Rejected".equalsIgnoreCase(
                requestedStatus)) {

            return "redirect:/recruiter-rejected";
        }

        return "redirect:/recruiter-applications";
    }


    @GetMapping("/recruiter-shortlisted")
    public String shortlisted(
            HttpSession session,
            Model model) {

        Recruiter recruiter =
                (Recruiter) session.getAttribute(
                        "loggedRecruiter"
                );

        if (recruiter == null) {
            return "redirect:/recruiter-login";
        }

        List<Application> allApplications =
                applicationService.getRecruiterApplications(
                        recruiter.getEmail()
                );

        List<Application> shortlistedApplications =
                allApplications.stream()
                        .filter(application ->
                                application.getStatus() != null
                                && application.getStatus()
                                        .equalsIgnoreCase(
                                                "Shortlisted"
                                        ))
                        .toList();

        model.addAttribute(
                "recruiter",
                recruiter
        );

        model.addAttribute(
                "applications",
                shortlistedApplications
        );

        return "recruiter-shortlisted";
    }


    @GetMapping("/recruiter-selected")
    public String selected(
            HttpSession session,
            Model model) {

        Recruiter recruiter =
                (Recruiter) session.getAttribute(
                        "loggedRecruiter"
                );

        if (recruiter == null) {
            return "redirect:/recruiter-login";
        }

        List<Application> applications =
                applicationService
                        .getRecruiterApplicationsByStatus(
                                recruiter.getEmail(),
                                "Selected"
                        );

        model.addAttribute(
                "recruiter",
                recruiter
        );

        model.addAttribute(
                "applications",
                applications
        );

        return "recruiter-selected";
    }


    @GetMapping("/recruiter-rejected")
    public String rejected(
            HttpSession session,
            Model model) {

        Recruiter recruiter =
                (Recruiter) session.getAttribute(
                        "loggedRecruiter"
                );

        if (recruiter == null) {
            return "redirect:/recruiter-login";
        }

        List<Application> applications =
                applicationService
                        .getRecruiterApplicationsByStatus(
                                recruiter.getEmail(),
                                "Rejected"
                        );

        model.addAttribute(
                "recruiter",
                recruiter
        );

        model.addAttribute(
                "applications",
                applications
        );

        return "recruiter-rejected";
    }


    @GetMapping("/recruiter/logout")
    public String logout(
            HttpSession session) {

        session.invalidate();

        return "redirect:/recruiter-login";
    }


    @GetMapping("/edit-company")
    public String editCompany(
            HttpSession session,
            Model model) {

        Recruiter recruiter =
                (Recruiter) session.getAttribute(
                        "loggedRecruiter"
                );

        if (recruiter == null) {
            return "redirect:/recruiter-login";
        }

        model.addAttribute(
                "recruiter",
                recruiter
        );

        return "edit-company";
    }


    @PostMapping("/update-company")
    public String updateCompany(
            @ModelAttribute Recruiter recruiter,
            HttpSession session) {

        Recruiter loggedRecruiter =
                (Recruiter) session.getAttribute(
                        "loggedRecruiter"
                );

        if (loggedRecruiter == null) {
            return "redirect:/recruiter-login";
        }

        recruiter.setId(
                loggedRecruiter.getId()
        );

        recruiter.setPassword(
                loggedRecruiter.getPassword()
        );

        recruiterService.updateRecruiter(
                recruiter
        );

        session.setAttribute(
                "loggedRecruiter",
                recruiter
        );

        return "redirect:/recruiter-dashboard";
    }


    @GetMapping("/recruiter/forgot-password")
    public String forgotPasswordPage() {
        return "recruiter-forgot-password";
    }


    @PostMapping("/recruiter/forgot-password")
    public String sendForgotPasswordOtp(
            @RequestParam String email,
            HttpSession session,
            Model model) {

        Recruiter recruiter =
                recruiterService
                        .findByEmail(email)
                        .orElse(null);

        if (recruiter == null) {

            model.addAttribute(
                    "error",
                    "No recruiter account found with this email address."
            );

            return "recruiter-forgot-password";
        }

        String otp =
                String.valueOf(
                        100000 + new Random().nextInt(900000)
                );

        session.setAttribute(
                "recruiterResetEmail",
                email
        );

        session.setAttribute(
                "recruiterResetOtp",
                otp
        );

        session.setAttribute(
                "recruiterOtpTime",
                System.currentTimeMillis()
        );

        emailService.sendRecruiterOtpMail(
                email,
                recruiter.getRecruiterName(),
                otp
        );

        return "redirect:/recruiter/verify-otp";
    }


    @GetMapping("/recruiter/verify-otp")
    public String verifyOtpPage(
            HttpSession session,
            Model model) {

        String email =
                (String) session.getAttribute(
                        "recruiterResetEmail"
                );

        if (email == null) {
            return "redirect:/recruiter/forgot-password";
        }

        model.addAttribute(
                "email",
                email
        );

        return "recruiter-verify-otp";
    }


    @PostMapping("/recruiter/verify-otp")
    public String verifyOtp(
            @RequestParam String otp,
            HttpSession session,
            Model model) {

        String sessionOtp =
                (String) session.getAttribute(
                        "recruiterResetOtp"
                );

        String email =
                (String) session.getAttribute(
                        "recruiterResetEmail"
                );

        Long otpTime =
                (Long) session.getAttribute(
                        "recruiterOtpTime"
                );

        if (sessionOtp == null
                || email == null
                || otpTime == null) {

            model.addAttribute(
                    "error",
                    "OTP session expired. Please request a new OTP."
            );

            return "recruiter-verify-otp";
        }

        long currentTime =
                System.currentTimeMillis();

        long difference =
                currentTime - otpTime;

        if (difference > 5 * 60 * 1000) {

            session.removeAttribute(
                    "recruiterResetOtp"
            );

            session.removeAttribute(
                    "recruiterOtpTime"
            );

            model.addAttribute(
                    "error",
                    "OTP has expired. Please request a new OTP."
            );

            return "recruiter-verify-otp";
        }

        if (!sessionOtp.equals(otp)) {

            model.addAttribute(
                    "error",
                    "Invalid OTP. Please try again."
            );

            model.addAttribute(
                    "email",
                    email
            );

            return "recruiter-verify-otp";
        }

        session.setAttribute(
                "recruiterOtpVerified",
                true
        );

        return "redirect:/recruiter/reset-password";
    }


    @GetMapping("/recruiter/reset-password")
    public String resetPasswordPage(
            HttpSession session) {

        Boolean verified =
                (Boolean) session.getAttribute(
                        "recruiterOtpVerified"
                );

        if (verified == null || !verified) {
            return "redirect:/recruiter/forgot-password";
        }

        return "recruiter-reset-password";
    }


    @PostMapping("/recruiter/reset-password")
    public String resetPassword(
            @RequestParam String password,
            @RequestParam String confirmPassword,
            HttpSession session,
            Model model) {

        Boolean verified =
                (Boolean) session.getAttribute(
                        "recruiterOtpVerified"
                );

        String email =
                (String) session.getAttribute(
                        "recruiterResetEmail"
                );

        if (verified == null
                || !verified
                || email == null) {

            return "redirect:/recruiter/forgot-password";
        }

        if (!password.equals(confirmPassword)) {

            model.addAttribute(
                    "error",
                    "Passwords do not match."
            );

            return "recruiter-reset-password";
        }

        if (password.length() < 6) {

            model.addAttribute(
                    "error",
                    "Password must contain at least 6 characters."
            );

            return "recruiter-reset-password";
        }

        boolean updated =
                recruiterService.resetPassword(
                        email,
                        password
                );

        if (!updated) {

            model.addAttribute(
                    "error",
                    "Unable to reset password. Please try again."
            );

            return "recruiter-reset-password";
        }

        session.removeAttribute(
                "recruiterResetEmail"
        );

        session.removeAttribute(
                "recruiterResetOtp"
        );

        session.removeAttribute(
                "recruiterOtpTime"
        );

        session.removeAttribute(
                "recruiterOtpVerified"
        );

        return "redirect:/recruiter-login?resetSuccess=true";
    }


    @GetMapping("/recruiter/application/{id}/resume")
    public ResponseEntity<?> downloadCandidateResume(
            @PathVariable Long id,
            HttpSession session) {

        Recruiter recruiter =
                (Recruiter) session.getAttribute(
                        "loggedRecruiter"
                );

        if (recruiter == null) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Please login as recruiter.");
        }

        Application application =
                applicationService.getApplicationById(id);

        if (application == null) {
            return ResponseEntity.notFound().build();
        }

        if (application.getJob() == null
                || application.getJob().getRecruiterEmail() == null
                || !application.getJob()
                        .getRecruiterEmail()
                        .equalsIgnoreCase(
                                recruiter.getEmail()
                        )) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            "You are not authorized to access this resume."
                    );
        }

        if (application.getUser() == null) {
            return ResponseEntity.notFound().build();
        }

        Resume resume =
                resumeService.getResume(
                        application.getUser()
                );

        if (resume == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\""
                                + resume.getFileName()
                                + "\""
                )
                .contentType(
                        MediaType.parseMediaType(
                                resume.getFileType()
                        )
                )
                .body(resume.getData());
    }
}