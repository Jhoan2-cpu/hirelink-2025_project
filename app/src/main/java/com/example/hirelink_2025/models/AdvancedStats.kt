package com.example.hirelink_2025.models

data class AdvancedStats(
    var activeUsers: Int = 0,
    var recentApplications: Int = 0,
    var jobsByModality: Map<String, Int> = emptyMap(),
    var topCompanies: Map<String, Int> = emptyMap(),
    var avgApplicationsPerJob: Double = 0.0,
    var applicationSuccessRate: Double = 0.0, // Porcentaje
    var generatedAt: Long = System.currentTimeMillis()
)