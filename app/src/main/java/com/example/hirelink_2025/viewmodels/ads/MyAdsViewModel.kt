package com.example.hirelink_2025.viewmodels.ads

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hirelink_2025.models.Job
import com.example.hirelink_2025.models.JobStatus
import com.example.hirelink_2025.repository.AdsStats
import com.example.hirelink_2025.repository.MyAdsRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel para MyAdsFragment siguiendo arquitectura MVVM
 * Maneja el estado y lógica de negocio para los anuncios laborales
 * CORREGIDO: Maneja todos los estados de JobStatus
 */
class MyAdsViewModel : ViewModel() {

    private val repository = MyAdsRepository()

    // Estado de los anuncios
    private val _myAds = MutableLiveData<List<Job>>()
    val myAds: LiveData<List<Job>> = _myAds

    // Estado de las estadísticas
    private val _adsStats = MutableLiveData<AdsStats>()
    val adsStats: LiveData<AdsStats> = _adsStats

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

    init {
        loadMyAds()
        loadAdsStats()
    }

    /**
     * Carga todos los anuncios del usuario
     */
    fun loadMyAds() {
        _isLoading.value = true
        _errorMessage.value = ""

        viewModelScope.launch {
            repository.getMyAds()
                .catch { exception ->
                    _isLoading.value = false
                    _errorMessage.value = "Error al cargar anuncios: ${exception.message}"
                }
                .collect { ads ->
                    _myAds.value = ads
                    _isEmpty.value = ads.isEmpty()
                    _isLoading.value = false
                }
        }
    }

    /**
     * Carga las estadísticas de anuncios
     */
    fun loadAdsStats() {
        viewModelScope.launch {
            repository.getAdsStats()
                .catch { exception ->
                    _errorMessage.value = "Error al cargar estadísticas: ${exception.message}"
                }
                .collect { stats ->
                    _adsStats.value = stats
                }
        }
    }

    /**
     * Filtra anuncios por estado
     */
    fun filterAdsByStatus(status: JobStatus) {
        _isLoading.value = true

        viewModelScope.launch {
            repository.getAdsByStatus(status)
                .catch { exception ->
                    _isLoading.value = false
                    _errorMessage.value = "Error al filtrar anuncios: ${exception.message}"
                }
                .collect { ads ->
                    _myAds.value = ads
                    _isEmpty.value = ads.isEmpty()
                    _isLoading.value = false
                }
        }
    }

    /**
     * Elimina un anuncio
     */
    fun deleteAd(job: Job) {
        viewModelScope.launch {
            try {
                val success = repository.deleteAd(job.id)
                if (success) {
                    _deleteSuccess.value = "Anuncio '${job.title}' eliminado correctamente"
                    // Recargar la lista
                    loadMyAds()
                    loadAdsStats()
                } else {
                    _errorMessage.value = "Error al eliminar el anuncio"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al eliminar: ${e.message}"
            }
        }
    }

    /**
     * Actualiza el estado de un anuncio - CORREGIDO
     */
    fun updateAdStatus(job: Job, newStatus: JobStatus) {
        viewModelScope.launch {
            try {
                val success = repository.updateAdStatus(job.id, newStatus)
                if (success) {
                    val statusText = getStatusDisplayText(newStatus)
                    _updateSuccess.value = "Anuncio '${job.title}' $statusText"
                    // Recargar la lista
                    loadMyAds()
                    loadAdsStats()
                } else {
                    _errorMessage.value = "Error al actualizar el estado"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al actualizar: ${e.message}"
            }
        }
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
        loadMyAds()
        loadAdsStats()
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
}