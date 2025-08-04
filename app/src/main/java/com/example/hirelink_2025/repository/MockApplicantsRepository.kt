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
    private val fakeApplicants = mutableListOf(Applicant())

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