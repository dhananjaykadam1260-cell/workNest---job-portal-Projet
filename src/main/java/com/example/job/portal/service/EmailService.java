package com.example.job.portal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.job.portal.model.Application;
import com.example.job.portal.model.Interview;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;


    // =========================================================
    // WELCOME EMAIL
    // =========================================================

    public void sendWelcomeMail(
            String toEmail,
            String name) {

        try {

            SimpleMailMessage message =
                    new SimpleMailMessage();

            message.setTo(toEmail);

            message.setSubject(
                    "Welcome to WorkNest!"
            );

            message.setText(
                    "Dear " + name + ",\n\n"
                    + "Welcome to WorkNest!\n\n"
                    + "Your account has been successfully created.\n\n"
                    + "Regards,\n"
                    + "WorkNest Team"
            );

            mailSender.send(message);

            System.out.println(
                    "Welcome email sent successfully."
            );

        } catch (MailException e) {

            System.out.println(
                    "Failed to send welcome email."
            );

            e.printStackTrace();
        }
    }


    // =========================================================
    // CANDIDATE OTP EMAIL
    // =========================================================

    public void sendOtpMail(
            String toEmail,
            String otp) {

        try {

            SimpleMailMessage message =
                    new SimpleMailMessage();

            message.setTo(toEmail);

            message.setSubject(
                    "WorkNest Password Reset OTP"
            );

            message.setText(
                    "Dear User,\n\n"
                    + "Your OTP for password reset is:\n\n"
                    + otp
                    + "\n\nThis OTP is valid for 5 minutes."
                    + "\n\nIf you did not request a password reset, "
                    + "please ignore this email."
                    + "\n\nRegards,\n"
                    + "WorkNest Team"
            );

            mailSender.send(message);

            System.out.println(
                    "OTP email sent successfully."
            );

        } catch (MailException e) {

            System.out.println(
                    "Failed to send OTP email."
            );

            e.printStackTrace();
        }
    }


    // =========================================================
    // RECRUITER REGISTRATION EMAIL
    // =========================================================

    public void sendRecruiterRegistrationMail(
            String email,
            String recruiterName,
            String companyName) {

        try {

            SimpleMailMessage message =
                    new SimpleMailMessage();

            message.setTo(email);

            message.setSubject(
                    "WorkNest - Recruiter Account Created Successfully"
            );

            message.setText(
                    "Hello " + recruiterName + ",\n\n"
                    + "Your recruiter account has been created successfully "
                    + "on WorkNest.\n\n"
                    + "Company: " + companyName + "\n"
                    + "Email: " + email + "\n\n"
                    + "You can now log in to your recruiter account "
                    + "and start posting jobs.\n\n"
                    + "Thank you for joining WorkNest.\n\n"
                    + "Regards,\n"
                    + "WorkNest Team"
            );

            mailSender.send(message);

            System.out.println(
                    "Recruiter registration email sent successfully."
            );

        } catch (MailException e) {

            System.out.println(
                    "Failed to send recruiter registration email."
            );

            e.printStackTrace();
        }
    }


    // =========================================================
    // RECRUITER OTP EMAIL
    // =========================================================

    public void sendRecruiterOtpMail(
            String toEmail,
            String recruiterName,
            String otp) {

        try {

            SimpleMailMessage message =
                    new SimpleMailMessage();

            message.setTo(toEmail);

            message.setSubject(
                    "WorkNest - Recruiter Password Reset OTP"
            );

            message.setText(
                    "Dear " + recruiterName + ",\n\n"
                    + "We received a request to reset your recruiter "
                    + "account password.\n\n"
                    + "Your OTP is:\n\n"
                    + otp
                    + "\n\n"
                    + "This OTP is valid for 5 minutes.\n\n"
                    + "If you did not request this password reset, "
                    + "please ignore this email.\n\n"
                    + "Regards,\n"
                    + "WorkNest Team"
            );

            mailSender.send(message);

            System.out.println(
                    "Recruiter OTP email sent successfully."
            );

        } catch (MailException e) {

            System.out.println(
                    "Failed to send recruiter OTP email."
            );

            e.printStackTrace();
        }
    }


    // =========================================================
    // APPLICATION STATUS EMAIL
    // =========================================================

    public void sendApplicationStatusEmail(
            Application application,
            String status) {

        try {

            if (application == null
                    || application.getUser() == null
                    || application.getUser().getEmail() == null
                    || application.getUser().getEmail().isBlank()) {

                return;
            }

            String normalizedStatus =
                    status == null
                            ? ""
                            : status.trim();

            String candidateName =
                    application.getUser().getName();

            String jobTitle =
                    application.getJob() != null
                            ? application.getJob().getJobTitle()
                            : "the applied position";

            String subject;
            String body;


            // -------------------------------------------------
            // SHORTLISTED
            // -------------------------------------------------

            if ("Shortlisted".equalsIgnoreCase(
                    normalizedStatus)) {

                subject =
                        "WorkNest - Your Application Has Been Shortlisted";

                body =
                        "Dear " + candidateName + ",\n\n"
                        + "We are pleased to inform you that your "
                        + "application for the position of "
                        + jobTitle
                        + " has been shortlisted.\n\n"
                        + "The recruiter will contact you regarding "
                        + "the next stage of the hiring process.\n\n"
                        + "Regards,\n"
                        + "WorkNest Team";
            }


            // -------------------------------------------------
            // SELECTED
            // -------------------------------------------------

            else if ("Selected".equalsIgnoreCase(
                    normalizedStatus)) {

                subject =
                        "WorkNest - Congratulations! You Have Been Selected";

                body =
                        "Dear " + candidateName + ",\n\n"
                        + "Congratulations!\n\n"
                        + "We are pleased to inform you that you have "
                        + "been selected for the position of "
                        + jobTitle
                        + ".\n\n"
                        + "The recruiter may contact you with the "
                        + "next steps.\n\n"
                        + "Regards,\n"
                        + "WorkNest Team";
            }


            // -------------------------------------------------
            // REJECTED
            // -------------------------------------------------

            else if ("Rejected".equalsIgnoreCase(
                    normalizedStatus)) {

                subject =
                        "WorkNest - Application Update";

                body =
                        "Dear " + candidateName + ",\n\n"
                        + "Thank you for your interest in the position "
                        + "of "
                        + jobTitle
                        + " and for taking the time to apply.\n\n"
                        + "After careful consideration, the recruiter "
                        + "has decided not to move forward with your "
                        + "application at this time.\n\n"
                        + "We appreciate your interest in WorkNest "
                        + "and wish you the very best in your job search.\n\n"
                        + "Regards,\n"
                        + "WorkNest Team";
            }


            // -------------------------------------------------
            // OTHER STATUS
            // -------------------------------------------------

            else {

                return;
            }


            SimpleMailMessage message =
                    new SimpleMailMessage();

            message.setTo(
                    application.getUser().getEmail()
            );

            message.setSubject(subject);

            message.setText(body);

            mailSender.send(message);

            System.out.println(
                    "Application status email sent successfully."
            );

        } catch (Exception e) {

            /*
             * IMPORTANT:
             * Email failure must never break the application
             * status update.
             */

            System.out.println(
                    "Failed to send application status email."
            );

            e.printStackTrace();
        }
    }


    // =========================================================
    // INTERVIEW SCHEDULED EMAIL
    // =========================================================

    public void sendInterviewScheduledEmail(Interview interview) {

        try {

            if (interview == null
                    || interview.getApplication() == null
                    || interview.getApplication().getUser() == null) {

                System.out.println(
                        "Unable to send interview email: invalid interview data.");

                return;
            }

            Application application =
                    interview.getApplication();

            String candidateEmail =
                    application.getUser().getEmail();

            String candidateName =
                    application.getUser().getName();

            if (candidateEmail == null
                    || candidateEmail.isBlank()) {

                System.out.println(
                        "Candidate email is empty.");

                return;
            }

            if (candidateName == null
                    || candidateName.isBlank()) {

                candidateName = "Candidate";
            }

            String jobTitle = "Applied Position";

            String companyName = "WorkNest";

            if (application.getJob() != null) {

                if (application.getJob().getJobTitle() != null) {
                    jobTitle =
                            application.getJob().getJobTitle();
                }

                if (application.getJob().getCompanyName() != null
                        && !application.getJob()
                                .getCompanyName()
                                .isBlank()) {

                    companyName =
                            application.getJob().getCompanyName();
                }
            }

            StringBuilder body =
                    new StringBuilder();

            body.append("Dear ")
                    .append(candidateName)
                    .append(",\n\n");

            body.append(
                    "Your interview has been scheduled successfully.\n\n");

            body.append("Job Title: ")
                    .append(jobTitle)
                    .append("\n");

            body.append("Company: ")
                    .append(companyName)
                    .append("\n");

            body.append("Interview Date: ")
                    .append(interview.getInterviewDate())
                    .append("\n");

            body.append("Interview Time: ")
                    .append(interview.getInterviewTime())
                    .append("\n");

            body.append("Interview Mode: ")
                    .append(interview.getInterviewMode())
                    .append("\n");

            if ("Online".equalsIgnoreCase(
                    interview.getInterviewMode())) {

                if (interview.getMeetingLink() != null
                        && !interview.getMeetingLink().isBlank()) {

                    body.append("Meeting Link: ")
                            .append(interview.getMeetingLink())
                            .append("\n");
                }
            }

            if ("Offline".equalsIgnoreCase(
                    interview.getInterviewMode())) {

                if (interview.getLocation() != null
                        && !interview.getLocation().isBlank()) {

                    body.append("Location: ")
                            .append(interview.getLocation())
                            .append("\n");
                }
            }

            if (interview.getNotes() != null
                    && !interview.getNotes().isBlank()) {

                body.append("Notes: ")
                        .append(interview.getNotes())
                        .append("\n");
            }

            body.append(
                    "\nPlease be available at the scheduled date and time.");

            body.append(
                    "\n\nRegards,\nWorkNest Team");

            SimpleMailMessage message =
                    new SimpleMailMessage();

            message.setTo(candidateEmail);

            message.setSubject(
                    "WorkNest - Interview Scheduled - "
                            + jobTitle);

            message.setText(
                    body.toString());

            mailSender.send(message);

            System.out.println(
                    "Interview email sent successfully to "
                            + candidateEmail);

        } catch (Exception e) {

            System.err.println(
                    "Failed to send interview email.");

            e.printStackTrace();
        }
    }
}