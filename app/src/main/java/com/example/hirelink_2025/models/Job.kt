package com.example.hirelink_2025.models


data class Job(
    val id: String = "",
    val title: String = "",
    val companyName: String = "",
    val companyLogo: String? = null,
    val location: String = "",
    val modality: String = "",
    val salary: String = "",
    val description: String = "",
    val requirements: List<String> = emptyList(),
    val postedDate: String = "",
    val vacancies: Int = 0,
    val employmentType: String = "",
    val bookmarked: Boolean = false,
    val applied: Boolean = false,
    val status: JobStatus = JobStatus.ACTIVE
)
