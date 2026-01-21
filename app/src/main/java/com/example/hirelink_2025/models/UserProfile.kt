package com.example.hirelink_2025.models

data class UserProfile(
    val userId: String = "",//--
    val profession: String? = null,//1 2
    val bio: String = "",//1 2
    val skills: List<String> = emptyList(),//1 2
    val experience: List<WorkExperience> = emptyList(),//1
    val education: List<Education> = emptyList(),//1
    val languages: List<String> = emptyList(),//1 2
    val location: String = "",//1 2
    val availability: String = "", //1 2"Inmediato", "2 semanas", etc.
    val salaryExpectation: String = "",//1 2
    val socialNetworkUrl: String = "",//1 2
    val portfolioUrl: String = "",//1 2
    val lastUpdated: Long = System.currentTimeMillis()//
)

data class WorkExperience(
    val id: String = "",
    val company: String = "",//11
    val position: String = "",//11
    val description: String = "",//11
    val startDate: String = "",//
    val endDate: String = "", // Vacío si es trabajo actual
    val isCurrent: Boolean = false,
    //val skills: List<String> = emptyList() ---siempre es CERO
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