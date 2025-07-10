package com.example.hirelink_2025.models

data class Company(
    val id: String = "",
    val name: String = "",
    val type: String = "", // Industry type
    val description: String = "",
    val size: String = "", // Company size (e.g., "1-10", "50-100", "500+")
    val foundedYear: Int = 0,
    val address: String = "",
    val city: String = "",
    val country: String = "",
    val phone: String = "",
    val email: String = "",
    val website: String = "",
    val logoUrl: String = "",
    val employeeCount: Int = 0,
    val activeJobsCount: Int = 0,
    val ownerId: String = "", // User ID who owns this company
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)