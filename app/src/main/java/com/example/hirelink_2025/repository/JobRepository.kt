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
    private val fakeJobs = mutableListOf<JobAd>(
        JobAd(
            id = "1",
            titulo = "Desarrollador Android Senior",
            empresa = "TechCorp SA",
            ubicacion = "Bogotá, Colombia",
            salario = "$2,500,000 - $3,500,000",
            descripcion = "Buscamos desarrollador Android con experiencia en Kotlin y MVVM. Trabajo en equipo dinámico con oportunidades de crecimiento.",
            habilidades = listOf("Kotlin", "Android", "MVVM", "Coroutines", "Room", "Retrofit"),
            modalidad = "Remoto",
            fechaPublicacion = "2025-01-15"
        ),
        JobAd(
            id = "2",
            titulo = "Diseñador UX/UI",
            empresa = "DesignStudio",
            ubicacion = "Medellín, Colombia",
            salario = "$1,800,000 - $2,200,000",
            descripcion = "Diseñador creativo para aplicaciones móviles y web. Experiencia en investigación de usuarios y prototipado.",
            habilidades = listOf("Figma", "Adobe XD", "Prototyping", "User Research", "Design Systems"),
            modalidad = "Híbrido",
            fechaPublicacion = "2025-01-14"
        ),
        JobAd(
            id = "3",
            titulo = "Data Scientist",
            empresa = "DataLab Inc",
            ubicacion = "Cali, Colombia",
            salario = "$3,000,000 - $4,000,000",
            descripcion = "Analista de datos con experiencia en Machine Learning. Desarrollo de modelos predictivos y análisis avanzado.",
            habilidades = listOf("Python", "SQL", "Machine Learning", "TensorFlow", "Pandas", "Jupyter"),
            modalidad = "Presencial",
            fechaPublicacion = "2025-01-13"
        ),
        JobAd(
            id = "4",
            titulo = "Desarrollador Frontend React",
            empresa = "WebSolutions",
            ubicacion = "Bogotá, Colombia",
            salario = "$2,200,000 - $2,800,000",
            descripcion = "Desarrollador frontend especializado en React y TypeScript. Experiencia en desarrollo de SPAs modernas.",
            habilidades = listOf("React", "TypeScript", "JavaScript", "CSS3", "HTML5", "Redux"),
            modalidad = "Remoto",
            fechaPublicacion = "2025-01-12"
        ),
        JobAd(
            id = "5",
            titulo = "DevOps Engineer",
            empresa = "CloudTech",
            ubicacion = "Medellín, Colombia",
            salario = "$3,200,000 - $4,200,000",
            descripcion = "Ingeniero DevOps para automatización de infraestructura y despliegues. Experiencia con AWS y Docker.",
            habilidades = listOf("AWS", "Docker", "Kubernetes", "Jenkins", "Terraform", "Linux"),
            modalidad = "Híbrido",
            fechaPublicacion = "2025-01-11"
        ),
        JobAd(
            id = "6",
            titulo = "Product Manager",
            empresa = "StartupTech",
            ubicacion = "Cali, Colombia",
            salario = "$2,800,000 - $3,800,000",
            descripcion = "Product Manager para liderar desarrollo de productos digitales. Experiencia en metodologías ágiles.",
            habilidades = listOf("Scrum", "Product Strategy", "Analytics", "Roadmapping", "User Stories"),
            modalidad = "Presencial",
            fechaPublicacion = "2025-01-10"
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
        ),
        Applicant(
            id = "app4",
            jobId = "3",
            name = "Ana Martínez",
            profession = "Data Scientist",
            experience = "4 años de experiencia",
            skills = listOf("Python", "Machine Learning", "SQL", "TensorFlow"),
            applicationDate = "2025-01-13",
            status = ApplicationStatus.PENDING,
            email = "ana.martinez@email.com",
            phone = "+57 304 567 8901"
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