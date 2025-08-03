package com.example.hirelink_2025.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hirelink_2025.models.Job
import com.example.hirelink_2025.models.Company
import com.example.hirelink_2025.models.Application
import com.example.hirelink_2025.models.ApplicationStatus
import com.example.hirelink_2025.network.FirestoreService
import com.example.hirelink_2025.network.Callback
import com.example.hirelink_2025.utils.LocationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class JobDetailUiState(
    val isLoading: Boolean = false,
    val job: Job? = null,
    val company: Company? = null,
    val error: String? = null,
    val isApplying: Boolean = false,
    val applicationSuccess: Boolean = false,
    val bookmarked: Boolean = false,
    val canApply: Boolean = true,
    val hasAlreadyApplied: Boolean = false,
    val isOwnerOfCompany: Boolean = false
)

class JobDetailViewModel : ViewModel() {

    private val firestoreService = FirestoreService()

    private val _uiState = MutableStateFlow(JobDetailUiState())
    val uiState: StateFlow<JobDetailUiState> = _uiState.asStateFlow()

    fun loadJobDetail(jobId: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        firestoreService.getJobById(jobId, object : Callback<Job?> {
            override fun onSuccess(job: Job?) {
                if (job != null) {
                    _uiState.value = _uiState.value.copy(job = job)
                    
                    // Cargar datos de la empresa si existe companyId
                    if (job.companyId.isNotEmpty()) {
                        loadCompanyData(job.companyId)
                        // checkApplicationEligibility se llamará DESPUÉS de cargar la empresa
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            company = null
                        )
                        // Si no hay empresa, verificar elegibilidad inmediatamente
                        checkApplicationEligibility(jobId)
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Empleo no encontrado"
                    )
                }
            }

            override fun onError(exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al cargar el empleo: ${exception.message}"
                )
            }
        })
    }



    private fun loadCompanyData(companyId: String) {
        firestoreService.getCompanyById(companyId, object : Callback<Company?> {
            override fun onSuccess(company: Company?) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    company = company
                )
                
                // Verificar elegibilidad DESPUÉS de cargar la empresa
                val jobId = _uiState.value.job?.id
                if (jobId != null) {
                    checkApplicationEligibility(jobId)
                }
            }

            override fun onError(exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    company = null
                )
                
                // Verificar elegibilidad aunque falle la carga de empresa
                val jobId = _uiState.value.job?.id
                if (jobId != null) {
                    checkApplicationEligibility(jobId)
                }
            }
        })
    }

    fun getCompanyLocation(): Pair<Double, Double>? {
        val company = _uiState.value.company
        if (company?.ubication?.isNotEmpty() == true) {
            val locationUtils = LocationUtils()
            return locationUtils.extractCoordinates(company.ubication)
        }
        return null
    }

    private fun checkApplicationEligibility(jobId: String) {
        val currentUserId = firestoreService.getCurrentUserId()
        if (currentUserId == null) {
            _uiState.value = _uiState.value.copy(
                canApply = false,
                error = "Usuario no autenticado"
            )
            return
        }

        val job = _uiState.value.job
        val company = _uiState.value.company

        // Verificar si el usuario es propietario de la empresa
        if (company != null && company.ownerId == currentUserId) {
            _uiState.value = _uiState.value.copy(
                canApply = false,
                isOwnerOfCompany = true
            )
            return
        }

        // Verificar si ya aplicó a este trabajo
        firestoreService.hasUserAppliedToJob(currentUserId, jobId) { hasApplied ->
            _uiState.value = _uiState.value.copy(
                canApply = !hasApplied,
                hasAlreadyApplied = hasApplied
            )
        }
    }

    fun applyToJob() {
        val currentState = _uiState.value
        
        // Validaciones previas
        if (!currentState.canApply) {
            when {
                currentState.isOwnerOfCompany -> {
                    _uiState.value = currentState.copy(
                        error = "No puedes aplicar a empleos de tu propia empresa"
                    )
                }
                currentState.hasAlreadyApplied -> {
                    _uiState.value = currentState.copy(
                        error = "Ya has aplicado a este empleo"
                    )
                }
                else -> {
                    _uiState.value = currentState.copy(
                        error = "No puedes aplicar a este empleo"
                    )
                }
            }
            return
        }

        val currentUserId = firestoreService.getCurrentUserId()
        val jobId = currentState.job?.id

        if (currentUserId == null || jobId == null) {
            _uiState.value = currentState.copy(
                error = "Error: Usuario o empleo no válido"
            )
            return
        }

        _uiState.value = currentState.copy(isApplying = true, error = null)

        // Crear la aplicación
        val application = Application(
            jobId = jobId,
            applicantId = currentUserId,
            appliedAt = System.currentTimeMillis(),
            status = ApplicationStatus.PENDING
        )

        firestoreService.createJobApplication(application, object : Callback<String> {
            override fun onSuccess(applicationId: String) {
                _uiState.value = _uiState.value.copy(
                    isApplying = false,
                    applicationSuccess = true,
                    canApply = false,
                    hasAlreadyApplied = true
                )
            }

            override fun onError(exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isApplying = false,
                    error = "Error al enviar la aplicación: ${exception.message}"
                )
            }
        })
    }

    fun toggleBookmark() {
        _uiState.value = _uiState.value.copy(
            bookmarked = !_uiState.value.bookmarked
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearApplicationSuccess() {
        _uiState.value = _uiState.value.copy(applicationSuccess = false)
    }
}