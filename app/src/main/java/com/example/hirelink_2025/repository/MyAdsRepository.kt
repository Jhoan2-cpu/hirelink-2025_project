package com.example.hirelink_2025.repository

import com.example.hirelink_2025.models.Job
import com.example.hirelink_2025.models.JobStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Repository para manejo de datos de anuncios laborales
 * Implementa mock data por ahora, luego se conectará con Firestore
 * ACTUALIZADO: Maneja todos los estados de JobStatus
 */
class MyAdsRepository {

    // Simulación de base de datos en memoria
    private val mockAds = mutableListOf<Job>()

    init {
        // Inicializar con datos mock
        initializeMockData()
    }

    /**
     * Obtiene todos los anuncios del usuario actual
     */
    fun getMyAds(): Flow<List<Job>> = flow {
        // Simular delay de red
        delay(1000)
        emit(mockAds.toList())
    }

    /**
     * Obtiene anuncios por estado
     */
    fun getAdsByStatus(status: JobStatus): Flow<List<Job>> = flow {
        delay(500)
        emit(mockAds.filter { it.status == status })
    }

    /**
     * Elimina un anuncio por ID
     */
    suspend fun deleteAd(adId: String): Boolean {
        delay(300)
        return try {
            mockAds.removeIf { it.id == adId }
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Actualiza el estado de un anuncio
     */
    suspend fun updateAdStatus(adId: String, newStatus: JobStatus): Boolean {
        delay(300)
        return try {
            val index = mockAds.indexOfFirst { it.id == adId }
            if (index != -1) {
                mockAds[index] = mockAds[index].copy(status = newStatus)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Obtiene estadísticas de anuncios - ACTUALIZADO
     */
    fun getAdsStats(): Flow<AdsStats> = flow {
        delay(200)
        val activeCount = mockAds.count { it.status == JobStatus.ACTIVE }
        val closedCount = mockAds.count { it.status == JobStatus.CLOSED }
        val draftCount = mockAds.count { it.status == JobStatus.DRAFT }
        val pausedCount = mockAds.count { it.status == JobStatus.PAUSED }
        val pendingReviewCount = mockAds.count { it.status == JobStatus.PENDING_REVIEW }
        val fullCount = mockAds.count { it.status == JobStatus.FULL }
        val expiredCount = mockAds.count { it.status == JobStatus.EXPIRED }
        val rejectedCount = mockAds.count { it.status == JobStatus.REJECTED }
        val suspendedCount = mockAds.count { it.status == JobStatus.SUSPENDED }

        val totalViews = mockAds.sumOf { it.vacancies * 40 } // Simulación
        val totalApplicants = mockAds.sumOf { it.vacancies * 2 } // Simulación

        emit(
            AdsStats(
                totalAds = mockAds.size,
                activeAds = activeCount,
                closedAds = closedCount,
                draftAds = draftCount,
                pausedAds = pausedCount,
                pendingReviewAds = pendingReviewCount,
                fullAds = fullCount,
                expiredAds = expiredCount,
                rejectedAds = rejectedCount,
                suspendedAds = suspendedCount,
                totalViews = totalViews,
                totalApplicants = totalApplicants
            )
        )
    }

    /**
     * Obtiene un job por ID
     */
    fun getJobById(jobId: String): Flow<Job?> = flow {
        delay(500)
        emit(mockAds.find { it.id == jobId })
    }

    /**
     * Actualiza un job completo
     */
    suspend fun updateJob(job: Job): Boolean {
        delay(300)
        return try {
            val index = mockAds.indexOfFirst { it.id == job.id }
            if (index != -1) {
                mockAds[index] = job
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Inicializa los datos mock - ACTUALIZADO con todos los estados
     */
    private fun initializeMockData() {
        mockAds.clear()
        mockAds.addAll(
            listOf(
                Job(
                    id = "1",
                    title = "Desarrollador Android",
                    companyName = "InnovaTech",
                    companyLogo = null,
                    location = "Lima, Perú",
                    modality = "Remoto",
                    salary = "S/ 5000 - S/ 6000",
                    description = "Desarrolla apps modernas con Kotlin y Jetpack Compose.",
                    requirements = listOf("Kotlin", "Android Studio", "MVVM", "Git"),
                    postedDate = "Hace 3 días",
                    vacancies = 2,
                    employmentType = "Tiempo completo",
                    status = JobStatus.ACTIVE
                ),
                Job(
                    id = "2",
                    title = "Desarrollador iOS",
                    companyName = "TechSolutions",
                    companyLogo = null,
                    location = "Arequipa, Perú",
                    modality = "Híbrido",
                    salary = "S/ 4500 - S/ 5500",
                    description = "Únete a nuestro equipo para desarrollar apps iOS innovadoras.",
                    requirements = listOf("Swift", "Xcode", "UIKit", "Core Data"),
                    postedDate = "Hace 1 semana",
                    vacancies = 1,
                    employmentType = "Tiempo completo",
                    status = JobStatus.PAUSED
                ),
                Job(
                    id = "3",
                    title = "Desarrollador Flutter",
                    companyName = "StartupPro",
                    companyLogo = null,
                    location = "Cusco, Perú",
                    modality = "Presencial",
                    salary = "S/ 3500 - S/ 4500",
                    description = "Desarrolla aplicaciones multiplataforma con Flutter.",
                    requirements = listOf("Flutter", "Dart", "Firebase", "REST APIs"),
                    postedDate = "Hace 2 días",
                    vacancies = 3,
                    employmentType = "Tiempo completo",
                    status = JobStatus.CLOSED
                ),
                Job(
                    id = "4",
                    title = "Desarrollador Full Stack",
                    companyName = "WebCorp",
                    companyLogo = null,
                    location = "Lima, Perú",
                    modality = "Remoto",
                    salary = "S/ 6000 - S/ 7000",
                    description = "Busca un desarrollador con experiencia en frontend y backend.",
                    requirements = listOf("React", "Node.js", "MongoDB", "Docker"),
                    postedDate = "Hace 5 días",
                    vacancies = 1,
                    employmentType = "Tiempo completo",
                    status = JobStatus.DRAFT
                ),
                Job(
                    id = "5",
                    title = "Diseñador UX/UI",
                    companyName = "DesignStudio",
                    companyLogo = null,
                    location = "Trujillo, Perú",
                    modality = "Híbrido",
                    salary = "S/ 3000 - S/ 4000",
                    description = "Crea experiencias de usuario excepcionales.",
                    requirements = listOf("Figma", "Adobe XD", "Sketch", "Prototyping"),
                    postedDate = "Hace 4 días",
                    vacancies = 2,
                    employmentType = "Tiempo completo",
                    status = JobStatus.PENDING_REVIEW
                ),
                Job(
                    id = "6",
                    title = "Desarrollador Backend Java",
                    companyName = "Enterprise Solutions",
                    companyLogo = null,
                    location = "Lima, Perú",
                    modality = "Presencial",
                    salary = "S/ 5500 - S/ 7000",
                    description = "Desarrolla microservicios robustos con Spring Boot.",
                    requirements = listOf("Java", "Spring Boot", "Microservices", "Docker"),
                    postedDate = "Hace 1 día",
                    vacancies = 1,
                    employmentType = "Tiempo completo",
                    status = JobStatus.FULL
                ),
                Job(
                    id = "7",
                    title = "Analista de Datos",
                    companyName = "DataCorp",
                    companyLogo = null,
                    location = "Lima, Perú",
                    modality = "Remoto",
                    salary = "S/ 4000 - S/ 5000",
                    description = "Analiza grandes volúmenes de datos para obtener insights.",
                    requirements = listOf("Python", "SQL", "Tableau", "Statistics"),
                    postedDate = "Hace 2 semanas",
                    vacancies = 2,
                    employmentType = "Tiempo completo",
                    status = JobStatus.EXPIRED
                ),
                Job(
                    id = "8",
                    title = "Desarrollador React Native",
                    companyName = "MobileFirst",
                    companyLogo = null,
                    location = "Arequipa, Perú",
                    modality = "Remoto",
                    salary = "S/ 4500 - S/ 5500",
                    description = "Desarrolla aplicaciones móviles multiplataforma.",
                    requirements = listOf("React Native", "JavaScript", "Redux", "Firebase"),
                    postedDate = "Hace 3 días",
                    vacancies = 1,
                    employmentType = "Tiempo completo",
                    status = JobStatus.REJECTED
                ),
                Job(
                    id = "9",
                    title = "Ingeniero DevOps",
                    companyName = "CloudTech",
                    companyLogo = null,
                    location = "Lima, Perú",
                    modality = "Híbrido",
                    salary = "S/ 6000 - S/ 8000",
                    description = "Automatiza procesos de despliegue y mantiene infraestructura.",
                    requirements = listOf("AWS", "Docker", "Kubernetes", "CI/CD"),
                    postedDate = "Hace 6 días",
                    vacancies = 1,
                    employmentType = "Tiempo completo",
                    status = JobStatus.SUSPENDED
                )
            )
        )
    }
}

/**
 * Data class para estadísticas de anuncios - ACTUALIZADA
 */
data class AdsStats(
    val totalAds: Int,
    val activeAds: Int,
    val closedAds: Int,
    val draftAds: Int,
    val pausedAds: Int,
    val pendingReviewAds: Int,
    val fullAds: Int,
    val expiredAds: Int,
    val rejectedAds: Int,
    val suspendedAds: Int,
    val totalViews: Int,
    val totalApplicants: Int
) {
    // Propiedades calculadas
    val publishedAds: Int = activeAds + pausedAds + fullAds + closedAds + expiredAds
    val pendingAds: Int = draftAds + pendingReviewAds + rejectedAds
    val problemAds: Int = rejectedAds + suspendedAds + expiredAds
    val successRate: Float = if (publishedAds > 0) activeAds.toFloat() / publishedAds else 0f
    val completionRate: Float = if (totalAds > 0) closedAds.toFloat() / totalAds else 0f

    /**
     * Obtiene el conteo por estado
     */
    fun getCountByStatus(status: JobStatus): Int {
        return when (status) {
            JobStatus.ACTIVE -> activeAds
            JobStatus.CLOSED -> closedAds
            JobStatus.DRAFT -> draftAds
            JobStatus.PAUSED -> pausedAds
            JobStatus.PENDING_REVIEW -> pendingReviewAds
            JobStatus.FULL -> fullAds
            JobStatus.EXPIRED -> expiredAds
            JobStatus.REJECTED -> rejectedAds
            JobStatus.SUSPENDED -> suspendedAds
            else -> 0
        }
    }

    /**
     * Obtiene el porcentaje por estado
     */
    fun getPercentageByStatus(status: JobStatus): Float {
        return if (totalAds > 0) {
            (getCountByStatus(status).toFloat() / totalAds) * 100
        } else {
            0f
        }
    }
}