package com.example.hirelink_2025.viewmodels.ads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hirelink_2025.models.Applicant
import com.example.hirelink_2025.models.ApplicationStatus
import com.example.hirelink_2025.repository.ApplicantsRepository
import com.example.hirelink_2025.repository.MockApplicantsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * ViewModel para manejar la lógica de presentación de los aplicantes
 * Sigue el patrón MVVM: no contiene referencias directas a la Vista
 */
class MyAdsApplicantsViewModel(
    private val repository: ApplicantsRepository = MockApplicantsRepository()
) : ViewModel() {

    // Estados privados (solo el ViewModel puede modificarlos)
    private val _uiState = MutableStateFlow(ApplicantsUiState())
    val uiState: StateFlow<ApplicantsUiState> = _uiState.asStateFlow()

    // Estados específicos para diferentes tipos de aplicantes
    private val _pendingApplicants = MutableStateFlow<List<Applicant>>(emptyList())
    val pendingApplicants: StateFlow<List<Applicant>> = _pendingApplicants.asStateFlow()

    private val _acceptedApplicants = MutableStateFlow<List<Applicant>>(emptyList())
    val acceptedApplicants: StateFlow<List<Applicant>> = _acceptedApplicants.asStateFlow()

    private val _rejectedApplicants = MutableStateFlow<List<Applicant>>(emptyList())
    val rejectedApplicants: StateFlow<List<Applicant>> = _rejectedApplicants.asStateFlow()

    // Estado para mensajes de error
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Estado para mensajes de éxito
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    /**
     * Cargar aplicantes por ID de trabajo
     */
    fun loadApplicants(jobId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                repository.getApplicantsByJobId(jobId)
                    .catch { throwable ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = throwable.message ?: "Error desconocido"
                        )
                        _errorMessage.value = "Error al cargar aplicantes: ${throwable.message}"
                    }
                    .collect { applicants ->
                        updateApplicantsLists(applicants)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isEmpty = applicants.isEmpty(),
                            error = null
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error desconocido"
                )
                _errorMessage.value = "Error inesperado: ${e.message}"
            }
        }
    }

    /**
     * Aceptar un aplicante
     */
    fun acceptApplicant(applicantId: String) {
        viewModelScope.launch {
            val result = repository.updateApplicantStatus(applicantId, ApplicationStatus.ACCEPTED)

            result.fold(
                onSuccess = {
                    _successMessage.value = "Aplicante aceptado exitosamente"
                    // Los datos se actualizarán automáticamente a través del Flow del repository
                },
                onFailure = { throwable ->
                    _errorMessage.value = "Error al aceptar aplicante: ${throwable.message}"
                }
            )
        }
    }

    /**
     * Rechazar un aplicante
     */
    fun rejectApplicant(applicantId: String) {
        viewModelScope.launch {
            val result = repository.updateApplicantStatus(applicantId, ApplicationStatus.REJECTED)

            result.fold(
                onSuccess = {
                    _successMessage.value = "Aplicante rechazado"
                    // Los datos se actualizarán automáticamente a través del Flow del repository
                },
                onFailure = { throwable ->
                    _errorMessage.value = "Error al rechazar aplicante: ${throwable.message}"
                }
            )
        }
    }

    /**
     * Actualizar las listas de aplicantes por estado
     */
    private fun updateApplicantsLists(allApplicants: List<Applicant>) {
        _pendingApplicants.value = allApplicants.filter { it.status == ApplicationStatus.PENDING }
        _acceptedApplicants.value = allApplicants.filter { it.status == ApplicationStatus.ACCEPTED }
        _rejectedApplicants.value = allApplicants.filter { it.status == ApplicationStatus.REJECTED }
    }

    /**
     * Obtener aplicante por ID
     */
    fun getApplicantById(applicantId: String, onResult: (Applicant?) -> Unit) {
        viewModelScope.launch {
            val result = repository.getApplicantById(applicantId)
            result.fold(
                onSuccess = { applicant ->
                    onResult(applicant)
                },
                onFailure = { throwable ->
                    _errorMessage.value = "Error al obtener aplicante: ${throwable.message}"
                    onResult(null)
                }
            )
        }
    }

    /**
     * Limpiar mensajes de error
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    /**
     * Limpiar mensajes de éxito
     */
    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    /**
     * Obtener contadores para la UI
     */
    fun getPendingCount(): Int = _pendingApplicants.value.size
    fun getAcceptedCount(): Int = _acceptedApplicants.value.size
    fun getRejectedCount(): Int = _rejectedApplicants.value.size
    fun getTotalCount(): Int = getPendingCount() + getAcceptedCount() + getRejectedCount()
}

/**
 * Data class para representar el estado de la UI
 */
data class ApplicantsUiState(
    val isLoading: Boolean = false,
    val isEmpty: Boolean = false,
    val error: String? = null
)