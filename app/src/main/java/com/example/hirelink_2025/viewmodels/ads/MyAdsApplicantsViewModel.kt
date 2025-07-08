package com.example.hirelink_2025.viewmodels.ads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hirelink_2025.models.Applicant
import com.example.hirelink_2025.models.ApplicationStatus
import com.example.hirelink_2025.repository.ApplicantsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel que maneja la lógica de aplicantes
 * MVVM: Separa lógica de negocio de la UI
 */
class MyAdsApplicantsViewModel(
    private val repository: ApplicantsRepository
) : ViewModel() {

    // Estado interno privado
    private val _uiState = MutableStateFlow(ApplicantsUiState())
    val uiState: StateFlow<ApplicantsUiState> = _uiState.asStateFlow()

    // ✅ CORREGIDO: Variable principal para todos los aplicantes
    private val _allApplicants = MutableStateFlow<List<Applicant>>(emptyList())
    val allApplicants: StateFlow<List<Applicant>> = _allApplicants.asStateFlow()

    // Estados públicos derivados para los fragments
    val pendingApplicants: StateFlow<List<Applicant>> = _allApplicants
        .map { applicants -> applicants.filter { it.status == ApplicationStatus.PENDING } }
        .stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val acceptedApplicants: StateFlow<List<Applicant>> = _allApplicants
        .map { applicants -> applicants.filter { it.status == ApplicationStatus.ACCEPTED } }
        .stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val rejectedApplicants: StateFlow<List<Applicant>> = _allApplicants
        .map { applicants -> applicants.filter { it.status == ApplicationStatus.REJECTED } }
        .stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private var currentJobId: String? = null

    /**
     * Inicializar con un job específico
     */
    fun initializeWithJob(jobId: String) {
        if (currentJobId != jobId) {
            currentJobId = jobId
            loadApplicants(jobId)
        }
    }

    /**
     * ✅ CORREGIDO: Actualizar estado de aplicante
     */
    fun updateApplicantStatus(applicantId: String, newStatus: ApplicationStatus) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // Actualizar lista local inmediatamente para UI responsiva
                val updatedList = _allApplicants.value.map { applicant ->
                    if (applicant.id == applicantId) {
                        applicant.copy(status = newStatus)
                    } else {
                        applicant
                    }
                }

                _allApplicants.value = updatedList

                // Actualizar en repository/API (si está disponible)
                currentJobId?.let { jobId ->
                    repository.updateApplicantStatus(applicantId, newStatus)
                        .onFailure { error ->
                            // Revertir cambios si falla
                            loadApplicants(jobId)
                            throw error
                        }
                }

                _uiState.value = _uiState.value.copy(isLoading = false)

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al actualizar estado: ${e.message}"
                )
            }
        }
    }

    /**
     * ✅ CORREGIDO: Actualizar lista completa de aplicantes
     */
    fun updateApplicantsList(applicants: List<Applicant>) {
        _allApplicants.value = applicants
    }

    /**
     * Cargar aplicantes del repository
     */
    private fun loadApplicants(jobId: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                repository.getApplicantsByJobId(jobId).collect { applicants ->
                    _allApplicants.value = applicants
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error desconocido"
                )
            }
        }
    }

    /**
     * Aceptar aplicante
     */
    fun acceptApplicant(applicantId: String) {
        updateApplicantStatus(applicantId, ApplicationStatus.ACCEPTED)
    }

    /**
     * Rechazar aplicante
     */
    fun rejectApplicant(applicantId: String) {
        updateApplicantStatus(applicantId, ApplicationStatus.REJECTED)
    }

    /**
     * ✅ CORREGIDO: Obtener aplicante por ID
     */
    fun getApplicantById(applicantId: String): Applicant? {
        return _allApplicants.value.find { it.id == applicantId }
    }

    /**
     * Limpiar errores
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Obtener estadísticas
     */
    fun getApplicantsStats(): ApplicantsStats {
        val all = _allApplicants.value
        return ApplicantsStats(
            total = all.size,
            pending = all.count { it.status == ApplicationStatus.PENDING },
            accepted = all.count { it.status == ApplicationStatus.ACCEPTED },
            rejected = all.count { it.status == ApplicationStatus.REJECTED }
        )
    }
}

/**
 * Estado de la UI
 */
data class ApplicantsUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Estadísticas de aplicantes
 */
data class ApplicantsStats(
    val total: Int,
    val pending: Int,
    val accepted: Int,
    val rejected: Int
)