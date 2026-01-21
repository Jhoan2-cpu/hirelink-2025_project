package com.example.hirelink_2025.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hirelink_2025.models.JobAd // CAMBIAR DE Job A JobAd
import com.example.hirelink_2025.models.JobCategory
import com.example.hirelink_2025.repository.JobRepository // USAR EL REPOSITORY EXISTENTE
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class JobsUiState(
    val isLoading: Boolean = false,
    val jobs: List<JobAd> = emptyList(), // CAMBIAR A JobAd
    val filteredJobs: List<JobAd> = emptyList(), // CAMBIAR A JobAd
    val categories: List<JobCategory> = emptyList(),
    val selectedCategory: JobCategory? = null,
    val searchQuery: String = "",
    val error: String? = null
)

class JobsViewModel : ViewModel() {

    private val jobRepository = JobRepository.getInstance() // USAR SINGLETON EXISTENTE

    private val _uiState = MutableStateFlow(JobsUiState())
    val uiState: StateFlow<JobsUiState> = _uiState.asStateFlow()

    init {
        loadJobs()
        loadCategories()
    }

    private fun loadJobs() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val jobs = jobRepository.getAllJobs() // USAR REPOSITORY REAL

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    jobs = jobs,
                    filteredJobs = jobs
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al cargar trabajos: ${e.message}"
                )
            }
        }
    }

    private fun loadCategories() {
        val categories = listOf(
            JobCategory("all", "Todos"),
            JobCategory("tech", "Tecnología"),
            JobCategory("design", "Diseño"),
            JobCategory("data", "Datos"),
            JobCategory("marketing", "Marketing")
        )
        _uiState.value = _uiState.value.copy(categories = categories)
    }

    fun searchJobs(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        filterJobs()
    }

    fun selectCategory(category: JobCategory?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        filterJobs()
    }

    private fun filterJobs() {
        val currentState = _uiState.value
        val filteredJobs = currentState.jobs.filter { job ->
            val matchesSearch = if (currentState.searchQuery.isBlank()) {
                true
            } else {
                job.titulo.contains(currentState.searchQuery, ignoreCase = true) ||
                        job.empresa.contains(currentState.searchQuery, ignoreCase = true) ||
                        job.descripcion.contains(currentState.searchQuery, ignoreCase = true)
            }

            val matchesCategory = currentState.selectedCategory?.let { category ->
                when (category.id) {
                    "all" -> true
                    "tech" -> job.titulo.contains("Desarrollador", ignoreCase = true) ||
                            job.titulo.contains("Android", ignoreCase = true)
                    "design" -> job.titulo.contains("Diseñador", ignoreCase = true) ||
                            job.titulo.contains("UX", ignoreCase = true)
                    "data" -> job.titulo.contains("Data", ignoreCase = true)
                    else -> true
                }
            } ?: true

            matchesSearch && matchesCategory
        }

        _uiState.value = currentState.copy(filteredJobs = filteredJobs)
    }

    fun refreshJobs() {
        loadJobs()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}