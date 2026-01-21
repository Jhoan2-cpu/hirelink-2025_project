package com.example.hirelink_2025.models


//MODIFICADO:

data class Application(
    val applicationId: String = "",
    val jobId: String = "",       // Referencia al TRABAJO (Job)
    val applicantId: String = "", // Referencia al usuario (User)
    val appliedAt: Long = System.currentTimeMillis(),
    val reviewedAt: Long? = null,//opcional
    val status: ApplicationStatus = ApplicationStatus.PENDING
)
enum class ApplicationStatus {
    PENDING,    // Pendiente
    ACCEPTED,   // Aceptado
    REJECTED    // Rechazado
}

/*
data class Application(
    val jobTitle: String,
    val companyName: String,
    val applicationDate: String,
    val status: String,
    val logoResId: Int
)
*/