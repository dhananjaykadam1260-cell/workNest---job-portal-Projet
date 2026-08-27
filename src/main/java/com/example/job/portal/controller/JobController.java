package com.example.job.portal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.job.portal.model.Job;
import com.example.job.portal.model.Recruiter;
import com.example.job.portal.model.User;
import com.example.job.portal.service.JobService;

import com.example.job.portal.service.ApplicationService;

import jakarta.servlet.http.HttpSession;

@Controller
public class JobController {
	
	@Autowired
	private ApplicationService applicationService;

    @Autowired
    private JobService jobService;

    @GetMapping("/post-job")
    public String postJobPage(HttpSession session,
                              org.springframework.ui.Model model) {

        Recruiter recruiter =
                (Recruiter) session.getAttribute("loggedRecruiter");

        if (recruiter == null) {
            return "redirect:/recruiter-login";
        }

        Job job = new Job();
        job.setCompanyName(recruiter.getCompanyName());

        model.addAttribute("job", job);

        return "post-job";
    }

    @PostMapping("/post-job")
    public String saveJob(@ModelAttribute Job job,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {

        Recruiter recruiter =
                (Recruiter) session.getAttribute("loggedRecruiter");

        if (recruiter == null) {
            return "redirect:/recruiter-login";
        }

        job.setRecruiterEmail(recruiter.getEmail());

        jobService.saveJob(job);

        redirectAttributes.addFlashAttribute("success",
                "Job Posted Successfully!");

        return "redirect:/recruiter-dashboard";
    }
    
    @GetMapping("/my-jobs")
    public String myJobs(HttpSession session,
                         Model model){

        Recruiter recruiter =
                (Recruiter) session.getAttribute("loggedRecruiter");

        if(recruiter==null){
            return "redirect:/recruiter-login";
        }

        model.addAttribute("jobs",
                jobService.getJobs(recruiter.getEmail()));

        return "my-jobs";
    }
    
    @GetMapping("/edit-job/{id}")
    public String editJob(
            @PathVariable Long id,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        Recruiter recruiter =
                (Recruiter) session.getAttribute("loggedRecruiter");

        if (recruiter == null) {
            return "redirect:/recruiter-login";
        }

        Job job = jobService.getJob(id);

        if (job == null) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Job not found."
            );
            return "redirect:/my-jobs";
        }

        if (job.getRecruiterEmail() == null
                || !job.getRecruiterEmail()
                        .equalsIgnoreCase(recruiter.getEmail())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "You are not authorized to edit this job."
            );

            return "redirect:/my-jobs";
        }

        model.addAttribute("job", job);

        return "edit-job";
    }
    
    @PostMapping("/update-job")
    public String updateJob(
            @ModelAttribute Job job,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Recruiter recruiter =
                (Recruiter) session.getAttribute("loggedRecruiter");

        if (recruiter == null) {
            return "redirect:/recruiter-login";
        }

        Job existingJob = jobService.getJob(job.getId());

        if (existingJob == null) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Job not found."
            );

            return "redirect:/my-jobs";
        }

        if (existingJob.getRecruiterEmail() == null
                || !existingJob.getRecruiterEmail()
                        .equalsIgnoreCase(recruiter.getEmail())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "You are not authorized to update this job."
            );

            return "redirect:/my-jobs";
        }

        job.setRecruiterEmail(recruiter.getEmail());
        job.setCompanyName(existingJob.getCompanyName());
        job.setPostedDate(existingJob.getPostedDate());

        jobService.updateJob(job);

        redirectAttributes.addFlashAttribute(
                "success",
                "Job updated successfully."
        );

        return "redirect:/my-jobs";
    }
    
    @GetMapping("/delete-job/{id}")
    public String deleteJob(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Recruiter recruiter =
                (Recruiter) session.getAttribute("loggedRecruiter");

        if (recruiter == null) {
            return "redirect:/recruiter-login";
        }

        Job job = jobService.getJob(id);

        if (job == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Job not found.");

            return "redirect:/my-jobs";
        }

        if (job.getRecruiterEmail() == null ||
                !job.getRecruiterEmail()
                        .equalsIgnoreCase(recruiter.getEmail())) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "You are not authorized to delete this job.");

            return "redirect:/my-jobs";
        }

        // Delete applications first
        applicationService.deleteApplicationsByJob(job);

        // Delete job
        jobService.deleteJob(id);

        redirectAttributes.addFlashAttribute(
                "success",
                "Job deleted successfully.");

        return "redirect:/my-jobs";
    }
    
    @GetMapping("/jobs")
    public String browseJobs(HttpSession session, Model model) {

        User loggedUser = (User) session.getAttribute("loggedUser");

        if (loggedUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("loggedUser", loggedUser);
        model.addAttribute("jobs", jobService.getAllJobs());

        return "jobs";
    }
    
    @GetMapping("/job-details/{id}")
    public String jobDetails(@PathVariable Long id,
                             HttpSession session,
                             Model model) {

        com.example.job.portal.model.User loggedUser =
                (com.example.job.portal.model.User) session.getAttribute("loggedUser");

        if (loggedUser == null) {
            return "redirect:/login";
        }

        Job job = jobService.getJob(id);

        if (job == null) {
            return "redirect:/jobs";
        }

        model.addAttribute("loggedUser", loggedUser);
        model.addAttribute("job", job);

        return "job-details";
    }
}