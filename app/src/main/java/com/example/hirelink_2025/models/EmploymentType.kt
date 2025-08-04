package com.example.hirelink_2025.models

enum class EmploymentType(val displayName: String) {
    FULL_TIME("Tiempo completo"),
    PART_TIME("Tiempo parcial"),
    INTERNSHIP("Práctica"),
    CONTRACT("Contrato"),
    TEMPORARY("Temporal"),
    FREELANCE("Freelance");

    companion object {
        fun fromDisplayName(displayName: String): EmploymentType? {
            return values().find { it.displayName == displayName }
        }

        fun getAllDisplayNames(): List<String> {
            return values().map { it.displayName }
        }
    }
}