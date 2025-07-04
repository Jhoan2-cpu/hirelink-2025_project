package com.example.hirelink_2025.repository

import com.example.hirelink_2025.models.Applicant
import com.example.hirelink_2025.models.ApplicationStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Implementación falsa del Repository para pruebas
 * FASE 1: Datos en memoria
 */
class MockApplicantsRepository : ApplicantsRepository {

    // Datos falsos simulando base de datos
    private val fakeApplicants = mutableListOf(
        Applicant(
            id = "app1",
            jobId = "1",
            name = "Juan Carlos Pérez",
            profession = "Desarrollador Android",
            experience = "5 años de experiencia",
            skills = listOf("Kotlin", "Java", "Android SDK", "MVVM"),
            applicationDate = "2025-01-16",
            status = ApplicationStatus.PENDING,
            email = "juan.perez@email.com",
            phone = "+57 301 234 5678"
        ),
        Applicant(
            id = "app2",
            jobId = "1",
            name = "María González",
            profession = "Desarrolladora Mobile",
            experience = "3 años de experiencia",
            skills = listOf("Kotlin", "Flutter", "React Native"),
            applicationDate = "2025-01-15",
            status = ApplicationStatus.ACCEPTED,
            email = "maria.gonzalez@email.com",
            phone = "+57 302 345 6789"
        ),
        Applicant(
            id = "app3",
            jobId = "1",
            name = "Carlos Rodríguez",
            profession = "Diseñador UX",
            experience = "4 años de experiencia",
            skills = listOf("Figma", "Sketch", "User Research", "Prototyping"),
            applicationDate = "2025-01-14",
            status = ApplicationStatus.PENDING,
            email = "carlos.rodriguez@email.com",
            phone = "+57 303 456 7890"
        ),
        Applicant(
            id = "app4",
            jobId = "2",
            name = "Ana Martínez",
            profession = "Product Designer",
            experience = "6 años de experiencia",
            skills = listOf("Adobe XD", "Figma", "User Testing", "Wireframing"),
            applicationDate = "2025-01-13",
            status = ApplicationStatus.ACCEPTED,
            email = "ana.martinez@email.com",
            phone = "+57 304 567 8901"
        ),
        Applicant(
            id = "app5",
            jobId = "2",
            name = "Diego López",
            profession = "UX Researcher",
            experience = "2 años de experiencia",
            skills = listOf("User Research", "Analytics", "Prototyping"),
            applicationDate = "2025-01-12",
            status = ApplicationStatus.REJECTED,
            email = "diego.lopez@email.com",
            phone = "+57 305 678 9012"
        )
    )

    override suspend fun getApplicantsByJobId(jobId: String): Flow<List<Applicant>> = flow {
        while (true) {
            delay(500) // Simular latencia de red
            val applicants = fakeApplicants.filter { it.jobId == jobId }
            emit(applicants)
            delay(2000) // Actualizar cada 2 segundos para simular tiempo real
        }
    }

    override suspend fun getApplicantById(applicantId: String): Result<Applicant?> {
        delay(300)
        return try {
            val applicant = fakeApplicants.find { it.id == applicantId }
            Result.success(applicant)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateApplicantStatus(
        applicantId: String,
        status: ApplicationStatus
    ): Result<Unit> {
        delay(500) // Simular operación de red
        return try {
            val applicantIndex = fakeApplicants.indexOfFirst { it.id == applicantId }
            if (applicantIndex != -1) {
                fakeApplicants[applicantIndex] = fakeApplicants[applicantIndex].copy(status = status)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Aplicante no encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}