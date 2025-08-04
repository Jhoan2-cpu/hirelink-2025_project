package com.example.hirelink_2025.repository

import com.example.hirelink_2025.models.Applicant
import com.example.hirelink_2025.models.ApplicationStatus
import com.example.hirelink_2025.models.JobAd
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Repository que simula datos de trabajos y aplicantes
 * Fase 1: Datos falsos en memoria
 */
class JobRepository {
    

    // Simulación de datos falsos - ACTUALIZADO con propiedades correctas
    private val fakeJobs = mutableListOf<JobAd>()

    private val fakeApplicants = mutableListOf<Applicant>()

    /**
     * Obtener todos los trabajos
     */
    suspend fun getAllJobs(): List<JobAd> {
        delay(500) // Simular delay de red
        return fakeJobs
    }

    /**
     * Obtener trabajo por ID
     */
    suspend fun getJobById(jobId: String): JobAd? {
        delay(300)
        return fakeJobs.find { it.id == jobId }
    }

    /**
     * Buscar trabajos por término
     */
    suspend fun searchJobs(query: String): List<JobAd> {
        delay(400)
        return if (query.isBlank()) {
            fakeJobs
        } else {
            fakeJobs.filter {
                it.titulo.contains(query, ignoreCase = true) ||
                        it.empresa.contains(query, ignoreCase = true) ||
                        it.ubicacion.contains(query, ignoreCase = true) ||
                        it.habilidades.any { skill -> skill.contains(query, ignoreCase = true) }
            }
        }
    }

    /**
     * Filtrar trabajos por modalidad
     */
    suspend fun getJobsByModality(modalidad: String): List<JobAd> {
        delay(300)
        return fakeJobs.filter { it.modalidad.equals(modalidad, ignoreCase = true) }
    }

    /**
     * Obtener aplicantes de un trabajo
     */
    suspend fun getApplicantsByJobId(jobId: String): List<Applicant> {
        delay(400)
        return fakeApplicants.filter { it.jobId == jobId }
    }

    /**
     * Obtener aplicantes pendientes
     */
    fun getPendingApplicants(jobId: String): Flow<List<Applicant>> = flow {
        while (true) {
            delay(500)
            val pending = fakeApplicants.filter {
                it.jobId == jobId && it.status == ApplicationStatus.PENDING
            }
            emit(pending)
        }
    }

    /**
     * Obtener aplicantes aceptados
     */
    fun getAcceptedApplicants(jobId: String): Flow<List<Applicant>> = flow {
        while (true) {
            delay(500)
            val accepted = fakeApplicants.filter {
                it.jobId == jobId && it.status == ApplicationStatus.ACCEPTED
            }
            emit(accepted)
        }
    }

    /**
     * Aceptar aplicante
     */
    suspend fun acceptApplicant(applicantId: String): Result<Unit> {
        delay(300)
        return try {
            val applicant = fakeApplicants.find { it.id == applicantId }
            applicant?.let {
                val index = fakeApplicants.indexOf(it)
                fakeApplicants[index] = it.copy(status = ApplicationStatus.ACCEPTED)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Rechazar aplicante
     */
    suspend fun rejectApplicant(applicantId: String): Result<Unit> {
        delay(300)
        return try {
            val applicant = fakeApplicants.find { it.id == applicantId }
            applicant?.let {
                val index = fakeApplicants.indexOf(it)
                fakeApplicants[index] = it.copy(status = ApplicationStatus.REJECTED)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: JobRepository? = null

        fun getInstance(): JobRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: JobRepository().also { INSTANCE = it }
            }
        }
    }
}