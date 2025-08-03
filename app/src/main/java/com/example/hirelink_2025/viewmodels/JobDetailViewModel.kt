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
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            company = null
                        )
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
            }

            override fun onError(exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    company = null
                )
            }
        })
    }

    fun getCompanyLocation(): Pair<Double, Double>? {
        val company = _uiState.value.company
        return company?.ubication?.let { ubication ->
            LocationUtils.extractCoordinates(ubication)
        }
    }

    fun applyToJob() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isApplying = true)

            try {
                kotlinx.coroutines.delay(1500)
                _uiState.value = _uiState.value.copy(
                    isApplying = false,
                    applicationSuccess = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isApplying = false,
                    error = "Error al aplicar: ${e.message}"
                )
            }
        }
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