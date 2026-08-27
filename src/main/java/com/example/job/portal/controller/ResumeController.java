package com.example.job.portal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.job.portal.model.Resume;
import com.example.job.portal.model.User;
import com.example.job.portal.service.ResumeService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ResumeController {

    @Autowired
    private ResumeService resumeService;

    @PostMapping("/resume/upload")
    public String uploadResume(@RequestParam("resume") MultipartFile file,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {

        User loggedUser = (User) session.getAttribute("loggedUser");

        if (loggedUser == null) {
            return "redirect:/login";
        }

        try {
            resumeService.uploadResume(loggedUser, file);
            redirectAttributes.addFlashAttribute("success", "Resume uploaded successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to upload resume.");
        }

        return "redirect:/resume?success=true";
    }

    @GetMapping("/resume/download")
    public ResponseEntity<ByteArrayResource> downloadResume(HttpSession session) {

        User loggedUser = (User) session.getAttribute("loggedUser");

        if (loggedUser == null) {
            return ResponseEntity.notFound().build();
        }

        Resume resume = resumeService.getResume(loggedUser);

        if (resume == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(resume.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resume.getFileName() + "\"")
                .body(new ByteArrayResource(resume.getData()));
    }

}