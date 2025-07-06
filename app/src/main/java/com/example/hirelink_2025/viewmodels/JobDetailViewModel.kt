package com.example.hirelink_2025.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hirelink_2025.models.JobAd
import com.example.hirelink_2025.repository.JobRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class JobDetailUiState(
    val isLoading: Boolean = false,
    val job: JobAd? = null,
    val error: String? = null,
    val isApplying: Boolean = false,
    val applicationSuccess: Boolean = false,
    val isBookmarked: Boolean = false
)

class JobDetailViewModel : ViewModel() {

    private val jobRepository = JobRepository.getInstance()

    private val _uiState = MutableStateFlow(JobDetailUiState())
    val uiState: StateFlow<JobDetailUiState> = _uiState.asStateFlow()

    fun loadJobDetail(jobId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val job = jobRepository.getJobById(jobId)
                if (job != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        job = job,
                        isBookmarked = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Empleo no encontrado"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al cargar el empleo: ${e.message}"
                )
            }
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
            isBookmarked = !_uiState.value.isBookmarked
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearApplicationSuccess() {
        _uiState.value = _uiState.value.copy(applicationSuccess = false)
    }
}