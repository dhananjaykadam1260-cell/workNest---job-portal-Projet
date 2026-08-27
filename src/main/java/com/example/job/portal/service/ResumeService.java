package com.example.job.portal.service;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

import com.example.job.portal.model.Resume;
import com.example.job.portal.model.User;

public interface ResumeService {

    void uploadResume(User user, MultipartFile file) throws IOException;

    Resume getResume(User user);

    

}