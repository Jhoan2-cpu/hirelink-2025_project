package com.example.hirelink_2025.viewmodels.ads

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hirelink_2025.models.Job
import com.example.hirelink_2025.models.JobStatus
import com.example.hirelink_2025.network.Callback
import com.example.hirelink_2025.network.FirestoreService
import com.example.hirelink_2025.network.VoidCallback
import com.google.firebase.auth.FirebaseAuth

/**
 * ViewModel para MyAdsFragment siguiendo arquitectura MVVM
 * Maneja el estado y lógica de negocio para los anuncios laborales
 * Conectado directamente a FirestoreService
 */
class MyAdsViewModel : ViewModel() {

    private val firestoreService = FirestoreService()
    private val auth = FirebaseAuth.getInstance()

    // Estado de los anuncios
    private val _myAds = MutableLiveData<List<Job>>()
    val myAds: LiveData<List<Job>> = _myAds

    // Estado de las estadísticas (versión simplificada)
    private val _adsStats = MutableLiveData<Map<String, Int>>()
    val adsStats: LiveData<Map<String, Int>> = _adsStats

    // Estados de UI
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isEmpty = MutableLiveData<Boolean>()
    val isEmpty: LiveData<Boolean> = _isEmpty

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    // Estados para acciones
    private val _deleteSuccess = MutableLiveData<String>()
    val deleteSuccess: LiveData<String> = _deleteSuccess

    private val _updateSuccess = MutableLiveData<String>()
    val updateSuccess: LiveData<String> = _updateSuccess

    // Cache para conteo de postulaciones
    private val _applicationsCount = MutableLiveData<Map<String, Int>>()
    val applicationsCount: LiveData<Map<String, Int>> = _applicationsCount

    init {
        getCurrentUserAndLoadAds()
    }

    /**
     * Obtiene el usuario actual y carga sus anuncios
     */
    private fun getCurrentUserAndLoadAds() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d("MyAdsViewModel", "Loading ads for user: ${currentUser.uid}")
            loadMyAds(currentUser.uid)
        } else {
            Log.w("MyAdsViewModel", "No authenticated user found")
            _errorMessage.value = "Usuario no autenticado"
        }
    }

    /**
     * Carga todos los anuncios del usuario
     */
    fun loadMyAds(ownerId: String? = null) {
        val userId = ownerId ?: auth.currentUser?.uid
        if (userId == null) {
            _errorMessage.value = "Usuario no autenticado"
            return
        }

        Log.d("MyAdsViewModel", "Loading ads for owner: $userId")
        _isLoading.value = true
        _errorMessage.value = ""

        firestoreService.getJobsByOwner(userId, object : Callback<List<Job>> {
            override fun onSuccess(result: List<Job>) {
                Log.d("MyAdsViewModel", "Successfully loaded ${result.size} ads")
                _myAds.value = result
                _isEmpty.value = result.isEmpty()
                _isLoading.value = false
                calculateStats(result)
                
                // Cargar conteo de postulaciones para cada job
                loadApplicationsCounts(result)
            }

            override fun onError(exception: Exception) {
                Log.e("MyAdsViewModel", "Error loading ads", exception)
                _isLoading.value = false
                _errorMessage.value = "Error al cargar anuncios: ${exception.message}"
            }
        })
    }

    /**
     * Calcula las estadísticas de anuncios
     */
    private fun calculateStats(ads: List<Job>) {
        val stats = mutableMapOf<String, Int>()
        stats["total"] = ads.size
        stats["active"] = ads.count { it.status == JobStatus.ACTIVE }
        stats["closed"] = ads.count { it.status == JobStatus.CLOSED }
        stats["draft"] = ads.count { it.status == JobStatus.DRAFT }
        stats["paused"] = ads.count { it.status == JobStatus.PAUSED }
        // Using placeholders for views and applications since they're not in the simplified Job model
        // These could be replaced with actual data from a separate analytics service if needed
        stats["totalViews"] = 0 // Placeholder: views data not available in current Job model
        // Obtener el conteo total de aplicaciones sumando todos los valores del mapa
        val totalApplications = _applicationsCount.value?.values?.sum() ?: 0
        stats["totalApplications"] = totalApplications
        // Alternative: Use vacancies as a rough indicator of job activity
        stats["totalVacancies"] = ads.sumOf { it.vacancies }
        
        _adsStats.value = stats
    }

    /**
     * Filtra anuncios por estado
     */
    fun filterAdsByStatus(status: JobStatus) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _errorMessage.value = "Usuario no autenticado"
            return
        }

        Log.d("MyAdsViewModel", "Filtering ads by status: $status for owner: $userId")
        _isLoading.value = true

        firestoreService.getJobsByOwnerAndStatus(userId, status, object : Callback<List<Job>> {
            override fun onSuccess(result: List<Job>) {
                Log.d("MyAdsViewModel", "Successfully loaded ${result.size} ads with status $status")
                _myAds.value = result
                _isEmpty.value = result.isEmpty()
                _isLoading.value = false
            }

            override fun onError(exception: Exception) {
                Log.e("MyAdsViewModel", "Error filtering ads by status", exception)
                _isLoading.value = false
                _errorMessage.value = "Error al filtrar anuncios: ${exception.message}"
            }
        })
    }

    /**
     * Elimina un anuncio
     */
    fun deleteAd(job: Job) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _errorMessage.value = "Usuario no autenticado"
            return
        }

        Log.d("MyAdsViewModel", "Deleting ad: ${job.title}")

        firestoreService.deleteJobByOwner(job.id, userId, object : VoidCallback {
            override fun onSuccess() {
                Log.d("MyAdsViewModel", "Ad deleted successfully")
                _deleteSuccess.value = "Anuncio '${job.title}' eliminado correctamente"
                // Recargar la lista
                loadMyAds(userId)
            }

            override fun onError(exception: Exception) {
                Log.e("MyAdsViewModel", "Error deleting ad", exception)
                _errorMessage.value = "Error al eliminar: ${exception.message}"
            }
        })
    }

    /**
     * Actualiza el estado de un anuncio
     */
    fun updateAdStatus(job: Job, newStatus: JobStatus) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _errorMessage.value = "Usuario no autenticado"
            return
        }

        Log.d("MyAdsViewModel", "Updating ad status: ${job.title} to $newStatus")

        firestoreService.updateJobStatus(job.id, newStatus, object : VoidCallback {
            override fun onSuccess() {
                val statusText = getStatusDisplayText(newStatus)
                Log.d("MyAdsViewModel", "Ad status updated successfully")
                _updateSuccess.value = "Anuncio '${job.title}' $statusText"
                // Recargar la lista
                loadMyAds(userId)
            }

            override fun onError(exception: Exception) {
                Log.e("MyAdsViewModel", "Error updating ad status", exception)
                _errorMessage.value = "Error al actualizar: ${exception.message}"
            }
        })
    }

    /**
     * Obtiene el texto descriptivo del estado - CORREGIDO
     */
    private fun getStatusDisplayText(status: JobStatus): String {
        return when (status) {
            JobStatus.ACTIVE -> "activado"
            JobStatus.CLOSED -> "cerrado"
            JobStatus.DRAFT -> "guardado como borrador"
            JobStatus.PAUSED -> "pausado"
            JobStatus.PENDING_REVIEW -> "enviado para revisión"
            JobStatus.FULL -> "marcado como lleno"
            JobStatus.EXPIRED -> "expirado"
            JobStatus.REJECTED -> "rechazado"
            JobStatus.SUSPENDED -> "suspendido"
            // Si hay estados adicionales no contemplados, agregar aquí
            else -> "actualizado"
        }
    }

    /**
     * Obtiene anuncios activos
     */
    fun loadActiveAds() {
        filterAdsByStatus(JobStatus.ACTIVE)
    }

    /**
     * Obtiene anuncios cerrados
     */
    fun loadClosedAds() {
        filterAdsByStatus(JobStatus.CLOSED)
    }

    /**
     * Obtiene borradores
     */
    fun loadDraftAds() {
        filterAdsByStatus(JobStatus.DRAFT)
    }

    /**
     * Obtiene anuncios pausados
     */
    fun loadPausedAds() {
        filterAdsByStatus(JobStatus.PAUSED)
    }

    /**
     * Obtiene anuncios pendientes de revisión
     */
    fun loadPendingReviewAds() {
        filterAdsByStatus(JobStatus.PENDING_REVIEW)
    }

    /**
     * Obtiene anuncios llenos
     */
    fun loadFullAds() {
        filterAdsByStatus(JobStatus.FULL)
    }

    /**
     * Obtiene anuncios expirados
     */
    fun loadExpiredAds() {
        filterAdsByStatus(JobStatus.EXPIRED)
    }

    /**
     * Obtiene anuncios rechazados
     */
    fun loadRejectedAds() {
        filterAdsByStatus(JobStatus.REJECTED)
    }

    /**
     * Obtiene anuncios suspendidos
     */
    fun loadSuspendedAds() {
        filterAdsByStatus(JobStatus.SUSPENDED)
    }

    /**
     * Limpia mensajes de error
     */
    fun clearErrorMessage() {
        _errorMessage.value = ""
    }

    /**
     * Limpia mensajes de éxito
     */
    fun clearSuccessMessages() {
        _deleteSuccess.value = ""
        _updateSuccess.value = ""
    }

    /**
     * Recarga todos los datos
     */
    fun refreshData() {
        getCurrentUserAndLoadAds()
    }

    /**
     * Obtiene el número total de anuncios
     */
    fun getTotalAdsCount(): Int {
        return _myAds.value?.size ?: 0
    }

    /**
     * Obtiene el número de anuncios activos
     */
    fun getActiveAdsCount(): Int {
        return _myAds.value?.count { it.status == JobStatus.ACTIVE } ?: 0
    }

    /**
     * Obtiene el número de anuncios cerrados
     */
    fun getClosedAdsCount(): Int {
        return _myAds.value?.count { it.status == JobStatus.CLOSED } ?: 0
    }

    /**
     * Obtiene el número de borradores
     */
    fun getDraftAdsCount(): Int {
        return _myAds.value?.count { it.status == JobStatus.DRAFT } ?: 0
    }

    /**
     * Obtiene el número de anuncios pausados
     */
    fun getPausedAdsCount(): Int {
        return _myAds.value?.count { it.status == JobStatus.PAUSED } ?: 0
    }

    /**
     * Obtiene el número de anuncios pendientes de revisión
     */
    fun getPendingReviewAdsCount(): Int {
        return _myAds.value?.count { it.status == JobStatus.PENDING_REVIEW } ?: 0
    }

    /**
     * Obtiene el número de anuncios llenos
     */
    fun getFullAdsCount(): Int {
        return _myAds.value?.count { it.status == JobStatus.FULL } ?: 0
    }

    /**
     * Obtiene el número de anuncios expirados
     */
    fun getExpiredAdsCount(): Int {
        return _myAds.value?.count { it.status == JobStatus.EXPIRED } ?: 0
    }

    /**
     * Obtiene el número de anuncios rechazados
     */
    fun getRejectedAdsCount(): Int {
        return _myAds.value?.count { it.status == JobStatus.REJECTED } ?: 0
    }

    /**
     * Obtiene el número de anuncios suspendidos
     */
    fun getSuspendedAdsCount(): Int {
        return _myAds.value?.count { it.status == JobStatus.SUSPENDED } ?: 0
    }

    /**
     * Obtiene estadísticas agrupadas por estado
     */
    fun getStatusStatistics(): Map<JobStatus, Int> {
        val ads = _myAds.value ?: emptyList()
        return ads.groupingBy { it.status }.eachCount()
    }

    /**
     * Verifica si se pueden publicar anuncios
     */
    fun canPublishAds(): Boolean {
        return getTotalAdsCount() < 10 // Límite ejemplo
    }

    /**
     * Obtiene anuncios que requieren atención
     */
    fun getAdsRequiringAttention(): List<Job> {
        return _myAds.value?.filter { job ->
            job.status in listOf(
                JobStatus.DRAFT,
                JobStatus.REJECTED,
                JobStatus.EXPIRED,
                JobStatus.SUSPENDED
            )
        } ?: emptyList()
    }

    /**
     * Publicar un anuncio (cambiar de DRAFT a ACTIVE)
     */
    fun publishAd(job: Job) {
        if (job.status == JobStatus.DRAFT) {
            updateAdStatus(job, JobStatus.ACTIVE)
        } else {
            _errorMessage.value = "Solo se pueden publicar anuncios en estado de borrador"
        }
    }

    /**
     * Pausar un anuncio
     */
    fun pauseAd(job: Job) {
        if (job.status == JobStatus.ACTIVE) {
            updateAdStatus(job, JobStatus.PAUSED)
        } else {
            _errorMessage.value = "Solo se pueden pausar anuncios activos"
        }
    }

    /**
     * Reanudar un anuncio pausado
     */
    fun resumeAd(job: Job) {
        if (job.status == JobStatus.PAUSED) {
            updateAdStatus(job, JobStatus.ACTIVE)
        } else {
            _errorMessage.value = "Solo se pueden reanudar anuncios pausados"
        }
    }

    /**
     * Cerrar un anuncio
     */
    fun closeAd(job: Job) {
        if (job.status in listOf(JobStatus.ACTIVE, JobStatus.PAUSED, JobStatus.FULL)) {
            updateAdStatus(job, JobStatus.CLOSED)
        } else {
            _errorMessage.value = "No se puede cerrar este anuncio en su estado actual"
        }
    }

    /**
     * Reactivar un anuncio cerrado
     */
    fun reactivateAd(job: Job) {
        if (job.status == JobStatus.CLOSED) {
            updateAdStatus(job, JobStatus.ACTIVE)
        } else {
            _errorMessage.value = "Solo se pueden reactivar anuncios cerrados"
        }
    }

    /**
     * Renovar un anuncio expirado
     */
    fun renewAd(job: Job) {
        if (job.status == JobStatus.EXPIRED) {
            updateAdStatus(job, JobStatus.ACTIVE)
        } else {
            _errorMessage.value = "Solo se pueden renovar anuncios expirados"
        }
    }

    /**
     * Carga el conteo de postulaciones para todos los jobs
     */
    private fun loadApplicationsCounts(jobs: List<Job>) {
        if (jobs.isEmpty()) {
            _applicationsCount.value = emptyMap()
            return
        }

        val counts = mutableMapOf<String, Int>()
        var pendingRequests = jobs.size

        jobs.forEach { job ->
            firestoreService.countApplicationsByJobId(job.id) { count ->
                synchronized(counts) {
                    counts[job.id] = count
                    pendingRequests--
                    
                    // Cuando todas las respuestas estén listas, actualizar LiveData
                    if (pendingRequests == 0) {
                        _applicationsCount.value = counts.toMap()
                        // Recalcular estadísticas con los nuevos conteos
                        _myAds.value?.let { calculateStats(it) }
                        Log.d("MyAdsViewModel", "Loaded applications counts: $counts")
                    }
                }
            }
        }
    }

    /**
     * Obtiene el conteo de postulaciones para un job específico
     */
    fun getApplicationsCount(jobId: String): Int {
        return _applicationsCount.value?.get(jobId) ?: 0
    }

    /**
     * Refresca el conteo de postulaciones para un job específico
     */
    fun refreshApplicationsCount(jobId: String) {
        firestoreService.countApplicationsByJobId(jobId) { count ->
            val currentCounts = _applicationsCount.value?.toMutableMap() ?: mutableMapOf()
            currentCounts[jobId] = count
            _applicationsCount.value = currentCounts
        }
    }
}