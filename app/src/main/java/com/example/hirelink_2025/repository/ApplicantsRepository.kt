package com.example.hirelink_2025.repository

import com.example.hirelink_2025.models.Applicant
import com.example.hirelink_2025.models.ApplicationStatus
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz del Repository para aplicantes
 * Abstracción que permite cambiar entre datos falsos y Firebase fácilmente
 */
interface ApplicantsRepository {
    suspend fun getApplicantsByJobId(jobId: String): Flow<List<Applicant>>
    suspend fun getApplicantById(applicantId: String): Result<Applicant?>
    suspend fun updateApplicantStatus(applicantId: String, status: ApplicationStatus): Result<Unit>
}