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

    // Simulación de datos falsos
    private val fakeJobs = mutableListOf<JobAd>(
        JobAd(
            id = "1",
            titulo = "Desarrollador Android Senior",
            descripcion = "Buscamos desarrollador Android con experiencia en Kotlin y MVVM",
            empresa = "TechCorp SA",
            habilidades = listOf("Kotlin", "Android", "MVVM", "Coroutines"),
            fecha = "2025-01-15",
            tipoEmpleo = "Tiempo Completo",
            cargo = "Senior Developer",
            modalidad = "Remoto",
            estado = "Activo",
            telefono = "+57 300 123 4567",
            email = "rh@techcorp.com",
            cantidadVacantes = "2",
            salario = "$2,500,000 - $3,500,000",
            ubicacion = "Bogotá, Colombia"
        ),
        JobAd(
            id = "2",
            titulo = "Diseñador UX/UI",
            descripcion = "Diseñador creativo para aplicaciones móviles y web",
            empresa = "DesignStudio",
            habilidades = listOf("Figma", "Adobe XD", "Prototyping", "User Research"),
            fecha = "2025-01-14",
            tipoEmpleo = "Medio Tiempo",
            cargo = "UX Designer",
            modalidad = "Híbrido",
            estado = "Activo",
            telefono = "+57 310 987 6543",
            email = "jobs@designstudio.co",
            cantidadVacantes = "1",
            salario = "$1,800,000 - $2,200,000",
            ubicacion = "Medellín, Colombia"
        ),
        JobAd(
            id = "3",
            titulo = "Data Scientist",
            descripcion = "Analista de datos con experiencia en Machine Learning",
            empresa = "DataLab Inc",
            habilidades = listOf("Python", "SQL", "Machine Learning", "TensorFlow"),
            fecha = "2025-01-13",
            tipoEmpleo = "Tiempo Completo",
            cargo = "Data Scientist",
            modalidad = "Presencial",
            estado = "Pausado",
            telefono = "+57 320 456 7890",
            email = "hiring@datalab.com",
            cantidadVacantes = "3",
            salario = "$3,000,000 - $4,000,000",
            ubicacion = "Cali, Colombia"
        )
    )

    private val fakeApplicants = mutableListOf<Applicant>(
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
            jobId = "2",
            name = "Carlos Rodríguez",
            profession = "Diseñador UX",
            experience = "4 años de experiencia",
            skills = listOf("Figma", "Sketch", "User Research", "Prototyping"),
            applicationDate = "2025-01-14",
            status = ApplicationStatus.PENDING,
            email = "carlos.rodriguez@email.com",
            phone = "+57 303 456 7890"
        )
    )

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