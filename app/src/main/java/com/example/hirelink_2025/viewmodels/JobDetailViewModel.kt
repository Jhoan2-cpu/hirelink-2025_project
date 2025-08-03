package com.example.hirelink_2025.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hirelink_2025.models.Job
import com.example.hirelink_2025.models.Company
import com.example.hirelink_2025.network.FirestoreService
import com.example.hirelink_2025.network.Callback
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
    val bookmarked: Boolean = false
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
        return if (company != null && company.city.isNotEmpty()) {
            // Por ahora usamos coordenadas de ejemplo basadas en la ciudad
            // En un proyecto real esto vendría de la base de datos o de un servicio de geocodificación
            when (company.city.lowercase()) {
                "bogotá", "bogota" -> Pair(4.7110, -74.0721)
                "medellín", "medellin" -> Pair(6.2442, -75.5812)
                "cali" -> Pair(3.4516, -76.5320)
                "barranquilla" -> Pair(10.9639, -74.7964)
                "cartagena" -> Pair(10.3910, -75.4794)
                "bucaramanga" -> Pair(7.1193, -73.1227)
                "pereira" -> Pair(4.8133, -75.6961)
                "santa marta" -> Pair(11.2408, -74.2099)
                "ibagué", "ibague" -> Pair(4.4389, -75.2322)
                "pasto" -> Pair(1.2136, -77.2811)
                else -> Pair(4.7110, -74.0721) // Default a Bogotá
            }
        } else {
            null
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