package com.example.hirelink_2025.models
data class JobResult(
    val id: Int,
    val title: String,
    val company: String,
    val location: String,
    val salary: String,
    val type: String,
    val publishedDate: String,
    val description: String,
    var bookmarked: Boolean = false,

    // Campos adicionales para filtros
    val status: String = "Activo",
    val experienceLevel: String = "Intermedio",
    val employmentType: String = "Tiempo completo",
    val modality: String = "Presencial"
)