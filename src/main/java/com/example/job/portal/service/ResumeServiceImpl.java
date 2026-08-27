package com.example.job.portal.service;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.job.portal.model.Resume;
import com.example.job.portal.model.User;
import com.example.job.portal.repository.ResumeRepository;

@Service
public class ResumeServiceImpl implements ResumeService {

    @Autowired
    private ResumeRepository resumeRepository;

    @Override
    public void uploadResume(User user, MultipartFile file) throws IOException {

        Resume resume = resumeRepository.findByUserId(user.getId());

        if (resume == null) {
            resume = new Resume();
            resume.setUser(user);
        }

        resume.setFileName(file.getOriginalFilename());
        resume.setFileType(file.getContentType());
        resume.setData(file.getBytes());
        resume.setUploadDate(LocalDateTime.now());

        resumeRepository.save(resume);
    }

    @Override
    public Resume getResume(User user) {
        return resumeRepository.findByUserId(user.getId());
    }

}