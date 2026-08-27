package com.example.job.portal.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.job.portal.model.Interview;
import com.example.job.portal.model.Job;
import com.example.job.portal.model.Resume;
import com.example.job.portal.model.User;
import com.example.job.portal.service.ApplicationService;
import com.example.job.portal.service.InterviewService;
import com.example.job.portal.service.JobService;
import com.example.job.portal.service.ResumeService;
import com.example.job.portal.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class homecontroller {
	
	@Autowired
	private InterviewService interviewService;
	
	@Autowired
	private ResumeService resumeService;

	@Autowired
	private UserService userService;

    @Autowired
    private JobService jobService;
    
    @Autowired
    private ApplicationService applicationService;


    @GetMapping("/")
    public String home(Model model) {

        List<Job> jobs = jobService.getAllJobs();

        // Latest 6 jobs
        List<Job> featuredJobs = jobs.stream()
                .sorted((job1, job2) ->
                        job2.getId().compareTo(job1.getId()))
                .limit(6)
                .toList();

        // Category wise job count
        Map<String, Long> categoryCounts = jobs.stream()
                .filter(job -> job.getCategory() != null)
                .filter(job -> !job.getCategory().isBlank())
                .collect(Collectors.groupingBy(
                        Job::getCategory,
                        Collectors.counting()
                ));

        model.addAttribute("featuredJobs", featuredJobs);

        model.addAttribute(
                "totalJobs",
                jobService.getTotalJobs()
        );

        model.addAttribute(
                "categoryCounts",
                categoryCounts
        );

        // Temporary values until services expose global counts
        model.addAttribute("totalCompanies", 0);
        model.addAttribute("totalCandidates", 0);
        model.addAttribute("totalApplications", 0);
        model.addAttribute("successRate", 0);

        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }
    

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {

        User loggedUser = (User) session.getAttribute("loggedUser");

        if (loggedUser == null) {
            return "redirect:/login";
        }

        Resume resume = resumeService.getResume(loggedUser);

        model.addAttribute("resume", resume);
        model.addAttribute("loggedUser", loggedUser);
        model.addAttribute("availableJobs", jobService.getAllJobs().size());
        model.addAttribute("appliedJobs", 0);
        model.addAttribute("profileCompletion",
                userService.calculateProfileCompletion(loggedUser.getId()));

        return "dashboard";
    }
    
    @GetMapping("/reset-password")
    public String resetPasswordPage() {
        return "reset-password";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }
    
    @GetMapping("/verify-otp")
    public String verifyOtpPage() {
        return "verify-otp";
    }
    
    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        User user = userService.getUserByEmail(email);

        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Email not registered.");
            return "redirect:/forgot-password";
        }

        String otp = String.valueOf((int)(Math.random() * 900000) + 100000);

        session.setAttribute("otp", otp);
        session.setAttribute("email", email);

        userService.sendOtp(email, otp);

        redirectAttributes.addFlashAttribute("success", "OTP sent successfully.");
        return "redirect:/verify-otp";
    }
    
    
    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String otp,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {

        String sessionOtp = (String) session.getAttribute("otp");

        if (sessionOtp == null) {
            redirectAttributes.addFlashAttribute("error", "OTP expired.");
            return "redirect:/forgot-password";
        }

        if (!sessionOtp.equals(otp)) {
            redirectAttributes.addFlashAttribute("error", "Invalid OTP.");
            return "redirect:/verify-otp";
        }

        return "redirect:/reset-password";
    }
    
    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String password,
                                @RequestParam String confirmPassword,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {

        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Passwords do not match.");
            return "redirect:/reset-password";
        }

        String email = (String) session.getAttribute("email");

        userService.updatePassword(email, password);

        session.removeAttribute("otp");
        session.removeAttribute("email");

        redirectAttributes.addFlashAttribute("success", "Password changed successfully.");
        return "redirect:/login";
    }
    
    @GetMapping("/job/{id}")
    public String viewJob(@PathVariable Long id,
                          HttpSession session,
                          Model model,
                          RedirectAttributes redirectAttributes) {

        User loggedUser = (User) session.getAttribute("loggedUser");

        if (loggedUser == null) {
            return "redirect:/login";
        }

        Job job = jobService.getJobById(id);

        if (job == null) {
            redirectAttributes.addFlashAttribute("error", "Job not found.");
            return "redirect:/applied-jobs";
        }

        model.addAttribute("job", job);
        model.addAttribute("loggedUser", loggedUser);

        return "job-details";
    }
    
    @GetMapping("/applied-jobs")
    public String appliedJobs(HttpSession session, Model model) {

        User loggedUser = (User) session.getAttribute("loggedUser");

        if (loggedUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("loggedUser", loggedUser);
        model.addAttribute("applications",
                applicationService.getAppliedJobs(loggedUser));

        return "applied-jobs";
    }
    
    
    @GetMapping("/apply/{id}")
    public String applyPage(@PathVariable Long id,
                            HttpSession session,
                            Model model,
                            RedirectAttributes redirectAttributes) {

        User user = (User) session.getAttribute("loggedUser");

        if (user == null) {
            return "redirect:/login";
        }

        Job job = jobService.getJobById(id);

        if (job == null) {
            redirectAttributes.addFlashAttribute("error", "Job not found.");
            return "redirect:/jobs";
        }

        Resume resume = resumeService.getResume(user);

        model.addAttribute("job", job);
        model.addAttribute("user", user);
        model.addAttribute("resume", resume);

        return "apply-job";
    }
    
    @PostMapping("/apply")
    public String submitApplication(@RequestParam Long jobId,
                                    @RequestParam(required = false) String coverLetter,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {

        User user = (User) session.getAttribute("loggedUser");

        if (user == null) {
            return "redirect:/login";
        }

        Job job = jobService.getJobById(jobId);

        if (job == null) {
            redirectAttributes.addFlashAttribute("error", "Job not found.");
            return "redirect:/jobs";
        }

        Resume resume = resumeService.getResume(user);

        if (resume == null) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Please upload your resume before applying."
            );

            return "redirect:/apply/" + jobId;
        }

        String result = applicationService.apply(user, job);

        if ("already".equals(result)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "You have already applied for this job."
            );

        } else {

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Application submitted successfully."
            );
        }

        return "redirect:/applied-jobs";
    }
    
    @GetMapping("/resume")
    public String resumePage(HttpSession session, Model model) {

        User loggedUser = (User) session.getAttribute("loggedUser");

        if (loggedUser == null) {
            return "redirect:/login";
        }

        Resume resume = resumeService.getResume(loggedUser);

        model.addAttribute("loggedUser", loggedUser);
        model.addAttribute("resume", resume);

        return "resume";
    }
    
    
    @GetMapping("/scheduled-interviews")
    public String scheduledInterviews(
            HttpSession session,
            Model model) {

        User loggedUser =
                (User) session.getAttribute("loggedUser");

        if (loggedUser == null) {
            return "redirect:/login";
        }

        List<Interview> interviews =
                interviewService.getCandidateInterviews(
                        loggedUser.getId());

        model.addAttribute(
                "loggedUser",
                loggedUser);

        model.addAttribute(
                "interviews",
                interviews);

        return "scheduled-interview";
    }
    
    
    
    
}