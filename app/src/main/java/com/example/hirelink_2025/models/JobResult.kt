package com.example.hirelink_2025.models


// Clase de datos para representar un trabajo
data class JobResult(
    val id: Int,
    val title: String,
    val company: String,
    val location: String,
    val salary: String,
    val type: String,
    val publishedDate: String,
    val description: String,
    var isBookmarked: Boolean = false
)