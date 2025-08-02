package com.example.hirelink_2025.models
//1: REGISTRO
//2: DETALLE
data class Company(
    val id: String = "",
    val name: String = "",//1
    val type: String = "", //1 Industry type
    val description: String = "",//1
    val size: CompanySize = CompanySize.SMALL,
    val foundedYear: Int = 0,//1
    val address: String = "",//1
    val city: String = "",//1
    val country: String = "",//1
    val phone: String = "",//1
    val email: String = "",//1
    val website: String = "",//1
    val logoUrl: String = "",//1
    val ownerId: String = "", // User ID who owns this company
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
//val employeeCount: Int = 0,//NO HAY
//val activeJobsCount: Int = 0,//NO HAY


enum class CompanySize {
    STARTUP,     // 1-10 empleados
    SMALL,       // 11-50 empleados
    MEDIUM,      // 51-200 empleados
    LARGE,       // 201-1000 empleados
    ENTERPRISE   // 1000+ empleados
}