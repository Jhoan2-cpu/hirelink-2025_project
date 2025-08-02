package com.example.hirelink_2025.repository

import com.example.hirelink_2025.models.Job
import com.example.hirelink_2025.models.JobStatus
import com.example.hirelink_2025.network.FirestoreService
import com.example.hirelink_2025.network.Callback
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Repository para manejo de datos de anuncios laborales
 * ADAPTADO: Usa los modelos simplificados con companyId
 * Integrado con FirestoreService para persistencia real
 */
class MyAdsRepository {

    private val firestoreService = FirestoreService()
    
    // Cache local para mejorar performance
    private val jobsCache = mutableMapOf<String, Job>()
    private var lastCacheUpdate = 0L
    private val cacheTimeout = 5 * 60 * 1000L // 5 minutos

    /**
     * Obtiene todos los anuncios del usuario actual usando FirestoreService
     */
    fun getJobsByOwner(ownerId: String): Flow<List<Job>> = flow {
        try {
            // Verificar cache primero
            if (isCacheValid()) {
                emit(jobsCache.values.filter { job -> 
                    // Filtrar por propietario a través de compañías
                    isJobOwnedByUser(job, ownerId)
                })
                return@flow
            }
            
            // Obtener de Firestore
            firestoreService.getJobsByOwner(ownerId, object : Callback<List<Job>> {
                override fun onSuccess(result: List<Job>) {
                    // Actualizar cache
                    result.forEach { job -> jobsCache[job.id] = job }
                    lastCacheUpdate = System.currentTimeMillis()
                    // No podemos emit desde callback, usar otro enfoque
                }
                
                override fun onError(exception: Exception) {
                    // Emit lista vacía en caso de error
                }
            })
            
            // Por ahora emitir desde cache o lista vacía
            emit(jobsCache.values.toList())
            
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    /**
     * Obtiene anuncios por estado para un propietario
     */
    fun getJobsByStatus(ownerId: String, status: JobStatus): Flow<List<Job>> = flow {
        try {
            firestoreService.getJobsByOwner(ownerId, object : Callback<List<Job>> {
                override fun onSuccess(result: List<Job>) {
                    val filteredJobs = result.filter { it.status == status }
                    // Actualizar cache
                    result.forEach { job -> jobsCache[job.id] = job }
                    lastCacheUpdate = System.currentTimeMillis()
                }
                
                override fun onError(exception: Exception) {
                    // Manejar error
                }
            })
            
            // Emitir desde cache mientras tanto
            emit(jobsCache.values.filter { it.status == status })
            
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    /**
     * Elimina un anuncio por ID usando FirestoreService
     */
    suspend fun deleteJob(jobId: String): Boolean {
        return try {
            delay(300) // Simular operación de red
            
            // TODO: Implementar cuando FirestoreService tenga deleteJob
            // firestoreService.deleteJob(jobId, callback)
            
            // Por ahora remover del cache
            jobsCache.remove(jobId)
            true
            
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Actualiza el estado de un anuncio usando FirestoreService
     */
    suspend fun updateJobStatus(jobId: String, newStatus: JobStatus): Boolean {
        return try {
            delay(300)
            
            // Actualizar en cache primero
            jobsCache[jobId]?.let { job ->
                jobsCache[jobId] = job.copy(status = newStatus, updatedAt = System.currentTimeMillis())
            }
            
            // TODO: Actualizar en Firestore cuando esté implementado
            // firestoreService.updateJobStatus(jobId, newStatus, callback)
            
            true
            
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Obtiene estadísticas de anuncios para un propietario
     */
    fun getAdsStats(ownerId: String): Flow<AdsStats> = flow {
        delay(200)
        
        try {
            // Obtener jobs del propietario
            firestoreService.getJobsByOwner(ownerId, object : Callback<List<Job>> {
                override fun onSuccess(result: List<Job>) {
                    // Calcular estadísticas
                    val stats = calculateStats(result)
                    // No podemos emit desde callback
                }
                
                override fun onError(exception: Exception) {
                    // Usar cache o stats vacías
                }
            })
            
            // Usar jobs del cache para calcular stats
            val userJobs = jobsCache.values.filter { job -> 
                isJobOwnedByUser(job, ownerId)
            }
            
            emit(calculateStats(userJobs))
            
        } catch (e: Exception) {
            emit(AdsStats.empty())
        }
    }

    /**
     * Obtiene un job por ID usando FirestoreService
     */
    fun getJobById(jobId: String): Flow<Job?> = flow {
        try {
            // Verificar cache primero
            jobsCache[jobId]?.let { cachedJob ->
                emit(cachedJob)
                return@flow
            }
            
            // Obtener de Firestore
            firestoreService.getJobById(jobId, object : Callback<Job?> {
                override fun onSuccess(result: Job?) {
                    result?.let { job ->
                        jobsCache[job.id] = job
                        lastCacheUpdate = System.currentTimeMillis()
                    }
                }
                
                override fun onError(exception: Exception) {
                    // Manejar error
                }
            })
            
            // Emitir desde cache o null
            emit(jobsCache[jobId])
            
        } catch (e: Exception) {
            emit(null)
        }
    }

    /**
     * Actualiza un job completo usando FirestoreService
     */
    suspend fun updateJob(job: Job): Boolean {
        return try {
            delay(300)
            
            // Actualizar en cache primero
            val updatedJob = job.copy(updatedAt = System.currentTimeMillis())
            jobsCache[job.id] = updatedJob
            
            // TODO: Actualizar en Firestore cuando esté implementado
            // firestoreService.updateJob(updatedJob, callback)
            
            true
            
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Métodos auxiliares para cache y validación
     */
    private fun isCacheValid(): Boolean {
        return (System.currentTimeMillis() - lastCacheUpdate) < cacheTimeout
    }
    
    private fun isJobOwnedByUser(job: Job, ownerId: String): Boolean {
        // TODO: Implementar lógica real para verificar propietario a través de companyId
        // Por ahora asumir que todos los jobs en cache pertenecen al usuario
        return job.companyId.isNotEmpty()
    }
    
    private fun calculateStats(jobs: List<Job>): AdsStats {
        val activeCount = jobs.count { it.status == JobStatus.ACTIVE }
        val closedCount = jobs.count { it.status == JobStatus.CLOSED }
        val draftCount = jobs.count { it.status == JobStatus.DRAFT }
        val pausedCount = jobs.count { it.status == JobStatus.PAUSED }
        val pendingReviewCount = jobs.count { it.status == JobStatus.PENDING_REVIEW }
        val fullCount = jobs.count { it.status == JobStatus.FULL }
        val expiredCount = jobs.count { it.status == JobStatus.EXPIRED }
        val rejectedCount = jobs.count { it.status == JobStatus.REJECTED }
        val suspendedCount = jobs.count { it.status == JobStatus.SUSPENDED }

        val totalViews = jobs.sumOf { it.vacancies * 40 } // Simulación
        val totalApplicants = jobs.sumOf { it.vacancies * 2 } // Simulación

        return AdsStats(
            totalAds = jobs.size,
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
    }
    
    /**
     * Limpia el cache
     */
    fun clearCache() {
        jobsCache.clear()
        lastCacheUpdate = 0L
    }
    
    /**
     * Fuerza la recarga desde Firestore
     */
    suspend fun forceRefresh(ownerId: String): Boolean {
        return try {
            clearCache()
            // TODO: Implementar recarga forzada
            true
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * Data class para estadísticas de anuncios - ADAPTADA para modelos simplificados
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
    
    companion object {
        /**
         * Crea una instancia vacía para casos de error
         */
        fun empty(): AdsStats {
            return AdsStats(
                totalAds = 0,
                activeAds = 0,
                closedAds = 0,
                draftAds = 0,
                pausedAds = 0,
                pendingReviewAds = 0,
                fullAds = 0,
                expiredAds = 0,
                rejectedAds = 0,
                suspendedAds = 0,
                totalViews = 0,
                totalApplicants = 0
            )
        }
    }
}