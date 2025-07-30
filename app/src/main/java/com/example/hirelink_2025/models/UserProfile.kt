package com.example.hirelink_2025.models

data class UserProfile(
    val userId: String = "",
    val profession: String? = null,
    val bio: String = "",
    val skills: List<String> = emptyList(),
    val experience: List<WorkExperience> = emptyList(),
    val education: List<Education> = emptyList(),
    val languages: List<String> = emptyList(),
    val location: String = "",
    val availability: String = "", // "Inmediato", "2 semanas", etc.
    val salaryExpectation: String = "",
    val linkedinUrl: String = "",
    val portfolioUrl: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
)

data class WorkExperience(
    val id: String = "",
    val company: String = "",
    val position: String = "",
    val description: String = "",
    val startDate: String = "",
    val endDate: String = "", // Vacío si es trabajo actual
    val isCurrent: Boolean = false,
    val skills: List<String> = emptyList()
)

data class Education(
    val id: String = "",
    val institution: String = "",
    val degree: String = "",
    val field: String = "",
    val startYear: String = "",
    val endYear: String = "",
    val description: String = ""
)