package com.example.job.portal.model;

import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String jobTitle;

    private String companyName;

    private String location;

    private String salary;

    private String experience;

    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getJobTitle() {
		return jobTitle;
	}

	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getSalary() {
		return salary;
	}

	public void setSalary(String salary) {
		this.salary = salary;
	}

	public String getExperience() {
		return experience;
	}

	public void setExperience(String experience) {
		this.experience = experience;
	}

	public String getJobType() {
		return jobType;
	}

	public void setJobType(String jobType) {
		this.jobType = jobType;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getSkills() {
		return skills;
	}

	public void setSkills(String skills) {
		this.skills = skills;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getVacancy() {
		return vacancy;
	}

	public void setVacancy(Integer vacancy) {
		this.vacancy = vacancy;
	}

	public LocalDate getLastDate() {
		return lastDate;
	}

	public void setLastDate(LocalDate lastDate) {
		this.lastDate = lastDate;
	}

	public LocalDate getPostedDate() {
		return postedDate;
	}

	public void setPostedDate(LocalDate postedDate) {
		this.postedDate = postedDate;
	}

	public String getRecruiterEmail() {
		return recruiterEmail;
	}

	public void setRecruiterEmail(String recruiterEmail) {
		this.recruiterEmail = recruiterEmail;
	}

	private String jobType;

    private String category;

    private String skills;

    @Column(length = 5000)
    private String description;

    private Integer vacancy;

    private LocalDate lastDate;

    private LocalDate postedDate;

    private String recruiterEmail;

    public Job() {
    }

    // Generate Getters and Setters
}