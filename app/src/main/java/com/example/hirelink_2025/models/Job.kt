package com.example.hirelink_2025.models

//: -- indica que son innecesarios.
data class Job(
    val id: String = "",
    val title: String = "",
    val companyName: String = "",//--
    val companyLogo: String? = null,//--
    val location: String = "",//--
    val modality: String = "",
    val salary: String = "",
    val description: String = "",
    val requirements: List<String> = emptyList(),
    val postedDate: String = "",
    val vacancies: Int = 0,
    val employmentType: String = "",
    val bookmarked: Boolean = false,
    val applied: Boolean = false,
    val status: JobStatus = JobStatus.ACTIVE,
    val ownerId: String = "",//<-- id del propietario del anuncio, así extraerá los datos del anuncio
    val companyId: String = "",//-- INNECESARIO PORQUE SE OBTIENE A TRAVÉS DEL PROPIETARIO
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val applicationsCount: Int = 0,
    val viewsCount: Int = 0,
    // Campos adicionales del formulario
    val aboutCompany: String = "",//--
    val aboutJob: String = "",
    val skills: String = "",
    val deadline: String = "",
    val offerSalary: String = "",
    val companyPhone: String = "",
    val companyEmail: String = "",//--
    val companyWebsite: String = "",//--
    val imageUrl: String? = null,//--
    val selectedLocation: String = ""//--
)
