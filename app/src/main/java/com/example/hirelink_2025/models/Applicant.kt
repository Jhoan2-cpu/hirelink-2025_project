package com.example.hirelink_2025.models

data class Applicant(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val profession: String = "",
    val experience: String = "",
    val skills: List<String> = emptyList(),
    val applicationDate: String = "",
    val status: ApplicationStatus = ApplicationStatus.PENDING,
    val profileImage: String? = null,
    val jobId: String = "", // ID del trabajo al que aplicó
    val coverLetter: String? = null
)
