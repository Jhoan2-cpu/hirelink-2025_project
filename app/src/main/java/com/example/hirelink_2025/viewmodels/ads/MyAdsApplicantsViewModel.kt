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

    private val _allApplicants = MutableStateFlow<List<Applicant>>(emptyList())

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
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            repository.updateApplicantStatus(applicantId, ApplicationStatus.ACCEPTED)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    // Los datos se actualizarán automáticamente por el Flow
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Error al aceptar aplicante"
                    )
                }
        }
    }

    /**
     * Rechazar aplicante
     */
    fun rejectApplicant(applicantId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            repository.updateApplicantStatus(applicantId, ApplicationStatus.REJECTED)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Error al rechazar aplicante"
                    )
                }
        }
    }

    /**
     * Obtener aplicante por ID - CORREGIDO
     */
    fun getApplicantById(applicantId: String): Applicant? {
        // Usar _allApplicants en lugar de las propiedades inexistentes
        return _allApplicants.value.find { it.id == applicantId }
    }

    /**
     * Limpiar errores
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * Estado de la UI
 */
data class ApplicantsUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)