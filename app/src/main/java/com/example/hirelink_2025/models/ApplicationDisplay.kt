package com.example.hirelink_2025.models

data class ApplicationDisplay(
    val jobTitle: String,
    val companyName: String,
    val applicationDate: String,
    val status: String,
    val logoResId: Int // drawable del logo de la empresa
)
