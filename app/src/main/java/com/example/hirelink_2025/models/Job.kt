package com.example.hirelink_2025.models


data class Job(
    val id: String,
    val title: String,
    val companyName: String,
    val companyLogo: String? = null,
    val location: String,
    val modality: String,
    val salary: String,
    val description: String,
    val requirements: List<String>,
    val postedDate: String,
    val vacancies: Int,
    val employmentType: String,
    val isBookmarked: Boolean = false,
    val hasApplied: Boolean = false,
    val status: JobStatus = JobStatus.ACTIVE
)
