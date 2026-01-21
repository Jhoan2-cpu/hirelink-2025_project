package com.example.hirelink_2025.models
//1: REGISTRO
//2: DETALLE
data class Company(
    val id: String = "",
    val name: String = "",//1 2
    val type: String = "", //1 2 Industry type
    val description: String = "",//1 2
    val size: CompanySize = CompanySize.SMALL, //1 2
    val foundedYear: Int = 0,//1 2
    val ubication: String = "",
    val address: String = "",//1 2
    val city: String = "",//1 2
    val country: String = "",//1 2
    val phone: String = "",//1 2
    val email: String = "",//1 2
    val website: String = "",//1 2
    val logoUrl: String = "",//1 2
    val ownerId: String = "", // User ID who owns this company
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    // Campos adicionales para compatibilidad con Firestore existente
    val employeeCount: Int = 0, // Ignorado pero evita warnings
    val activeJobsCount: Int = 0 // Ignorado pero evita warnings
) {
    /**
     * Obtiene el tamaño de la compañía de forma legible
     */
    fun getDisplaySize(): String {
        return when (size) {
            CompanySize.STARTUP -> "1-10 empleados"
            CompanySize.SMALL -> "11-50 empleados"
            CompanySize.MEDIUM -> "51-200 empleados"
            CompanySize.LARGE -> "201-1000 empleados"
            CompanySize.ENTERPRISE -> "1000+ empleados"
        }
    }
}
//val employeeCount: Int = 0,//NO HAY
//val activeJobsCount: Int = 0,//NO HAY


enum class CompanySize {
    STARTUP,     // 1-10 empleados
    SMALL,       // 11-50 empleados
    MEDIUM,      // 51-200 empleados
    LARGE,       // 201-1000 empleados
    ENTERPRISE   // 1000+ empleados
}