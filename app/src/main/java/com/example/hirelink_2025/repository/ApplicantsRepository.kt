package com.example.hirelink_2025.repository

import com.example.hirelink_2025.models.Applicant
import com.example.hirelink_2025.models.ApplicationStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository para manejar los datos de los aplicantes
 * Implementa el patrón Repository para separar la lógica de acceso a datos
 */
interface ApplicantsRepository {
    suspend fun getApplicantsByJobId(jobId: String): Flow<List<Applicant>>
    suspend fun updateApplicantStatus(applicantId: String, status: ApplicationStatus): Result<Unit>
    suspend fun getApplicantById(applicantId: String): Result<Applicant>
}

/**
 * Implementación del Repository con datos mock
 * En el futuro se reemplazará con FirestoreApplicantsRepository
 */
class MockApplicantsRepository : ApplicantsRepository {

    private val _applicants = MutableStateFlow<List<Applicant>>(emptyList())
    private val applicants: StateFlow<List<Applicant>> = _applicants.asStateFlow()

    override suspend fun getApplicantsByJobId(jobId: String): Flow<List<Applicant>> {
        // Simular carga de datos
        delay(1000)

        // Datos mock
        val mockApplicants = listOf(
            Applicant(
                id = "1",
                name = "María García",
                email = "maria.garcia@email.com",
                phone = "+51 987654321",
                profession = "Desarrolladora Senior",
                experience = "5 años de experiencia",
                skills = listOf("Kotlin", "Java", "Android", "MVVM", "Room"),
                applicationDate = "Hace 2 días",
                status = ApplicationStatus.PENDING,
                jobId = jobId,
                coverLetter = "Estimado equipo de reclutamiento, estoy muy interesada en esta posición..."
            ),
            Applicant(
                id = "2",
                name = "Carlos Rodríguez",
                email = "carlos.rodriguez@email.com",
                phone = "+51 976543210",
                profession = "Desarrollador Full Stack",
                experience = "3 años de experiencia",
                skills = listOf("React", "Node.js", "MongoDB", "JavaScript", "TypeScript"),
                applicationDate = "Hace 1 día",
                status = ApplicationStatus.PENDING,
                jobId = jobId,
                coverLetter = "Me complace postular para esta oportunidad..."
            ),
            Applicant(
                id = "3",
                name = "Ana Martínez",
                email = "ana.martinez@email.com",
                phone = "+51 965432109",
                profession = "Diseñadora UX/UI",
                experience = "4 años de experiencia",
                skills = listOf("Figma", "Adobe XD", "Sketch", "Prototyping", "User Research"),
                applicationDate = "Hace 4 días",
                status = ApplicationStatus.ACCEPTED,
                jobId = jobId,
                coverLetter = "He trabajado en proyectos similares..."
            ),
            Applicant(
                id = "4",
                name = "Pedro Jiménez",
                email = "pedro.jimenez@email.com",
                phone = "+51 954321098",
                profession = "Desarrollador Junior",
                experience = "1 año de experiencia",
                skills = listOf("Java", "Kotlin", "SQLite", "Git", "Android Studio"),
                applicationDate = "Hace 6 días",
                status = ApplicationStatus.REJECTED,
                jobId = jobId,
                coverLetter = "Aunque soy junior, tengo muchas ganas de aprender..."
            ),
            Applicant(
                id = "5",
                name = "Laura Fernández",
                email = "laura.fernandez@email.com",
                phone = "+51 943210987",
                profession = "QA Engineer",
                experience = "2 años de experiencia",
                skills = listOf("Manual Testing", "Selenium", "Cypress", "Test Automation"),
                applicationDate = "Hace 3 días",
                status = ApplicationStatus.ACCEPTED,
                jobId = jobId,
                coverLetter = "Mi experiencia en testing automatizado..."
            )
        )

        _applicants.value = mockApplicants
        return applicants
    }

    override suspend fun updateApplicantStatus(applicantId: String, status: ApplicationStatus): Result<Unit> {
        return try {
            // Simular delay de red
            delay(500)

            val currentApplicants = _applicants.value.toMutableList()
            val index = currentApplicants.indexOfFirst { it.id == applicantId }

            if (index != -1) {
                currentApplicants[index] = currentApplicants[index].copy(status = status)
                _applicants.value = currentApplicants
                Result.success(Unit)
            } else {
                Result.failure(Exception("Aplicante no encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getApplicantById(applicantId: String): Result<Applicant> {
        return try {
            delay(300)
            val applicant = _applicants.value.find { it.id == applicantId }
            if (applicant != null) {
                Result.success(applicant)
            } else {
                Result.failure(Exception("Aplicante no encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}