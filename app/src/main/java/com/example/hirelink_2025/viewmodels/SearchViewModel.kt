package com.example.hirelink_2025.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hirelink_2025.models.Job
import com.example.hirelink_2025.models.Company
import com.example.hirelink_2025.models.JobStatus
import com.example.hirelink_2025.models.JobFilters
import com.example.hirelink_2025.models.SalaryRange
import com.example.hirelink_2025.network.Callback
import com.example.hirelink_2025.network.FirestoreService
import kotlinx.coroutines.launch

/**
 * ViewModel para búsqueda de empleos siguiendo arquitectura MVVM
 * Maneja la lógica de búsqueda flexible por tipo de empleo y ubicación
 */
class SearchViewModel : ViewModel() {

    private val firestoreService = FirestoreService()

    // Campos de búsqueda
    private val _jobTypeQuery = MutableLiveData<String>()
    val jobTypeQuery: LiveData<String> = _jobTypeQuery

    private val _locationQuery = MutableLiveData<String>()
    val locationQuery: LiveData<String> = _locationQuery

    // Estados de resultados
    private val _searchResults = MutableLiveData<List<Job>>()
    val searchResults: LiveData<List<Job>> = _searchResults

    private val _companiesCache = MutableLiveData<Map<String, Company>>()
    val companiesCache: LiveData<Map<String, Company>> = _companiesCache

    // Estados de UI
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isSearching = MutableLiveData<Boolean>()
    val isSearching: LiveData<Boolean> = _isSearching

    private val _hasSearched = MutableLiveData<Boolean>()
    val hasSearched: LiveData<Boolean> = _hasSearched

    private val _isEmpty = MutableLiveData<Boolean>()
    val isEmpty: LiveData<Boolean> = _isEmpty

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _searchCount = MutableLiveData<Int>()
    val searchCount: LiveData<Int> = _searchCount

    // Filtros de búsqueda
    private val _activeFilters = MutableLiveData<JobFilters>()
    val activeFilters: LiveData<JobFilters> = _activeFilters

    private val _allResults = MutableLiveData<List<Job>>()  // Resultados sin filtrar
    
    init {
        _hasSearched.value = false
        _isEmpty.value = false
        _searchCount.value = 0
        _companiesCache.value = emptyMap()
        _activeFilters.value = JobFilters()
    }

    /**
     * Actualiza el campo de tipo de empleo
     */
    fun updateJobType(jobType: String) {
        _jobTypeQuery.value = jobType
    }

    /**
     * Actualiza el campo de ubicación
     */
    fun updateLocation(location: String) {
        _locationQuery.value = location
    }

    /**
     * Realiza búsqueda de empleos con parámetros flexibles
     */
    fun searchJobs() {
        val jobType = _jobTypeQuery.value?.trim()
        val location = _locationQuery.value?.trim()

        // Marcar que se intentó hacer búsqueda (para que refresh funcione)
        _hasSearched.value = true

        // Validar que al menos un campo tenga contenido
        if (jobType.isNullOrBlank() && location.isNullOrBlank()) {
            _errorMessage.value = "Ingresa al menos un criterio de búsqueda"
            _isLoading.value = false
            _isSearching.value = false
            return
        }

        _isLoading.value = true
        _isSearching.value = true
        _errorMessage.value = ""

        android.util.Log.d("SearchViewModel", "Searching jobs with jobType: '$jobType', location: '$location'")

        searchJobsFlexible(jobType, location)
    }

    /**
     * Búsqueda flexible de empleos por tipo y ubicación
     */
    private fun searchJobsFlexible(jobType: String?, location: String?) {
        // Paso 1: Obtener todas las compañías que coincidan con la ubicación (si se especifica)
        if (!location.isNullOrBlank()) {
            searchByLocationAndJobType(location, jobType)
        } else {
            // Si no hay filtro de ubicación, buscar solo por tipo de empleo
            searchByJobTypeOnly(jobType)
        }
    }

    /**
     * Buscar por ubicación y tipo de empleo
     */
    private fun searchByLocationAndJobType(location: String, jobType: String?) {
        // Primero obtener compañías que coincidan con la ubicación
        firestoreService.getAllCompanies(object : Callback<List<Company>> {
            override fun onSuccess(companies: List<Company>) {
                // Filtrar compañías por ubicación (address, city, country)
                val matchingCompanies = companies.filter { company ->
                    company.address.contains(location, ignoreCase = true) ||
                    company.city.contains(location, ignoreCase = true) ||
                    company.country.contains(location, ignoreCase = true)
                }

                android.util.Log.d("SearchViewModel", "Found ${matchingCompanies.size} companies matching location '$location'")

                if (matchingCompanies.isNotEmpty()) {
                    // Obtener jobs de estas compañías
                    getJobsFromCompanies(matchingCompanies, jobType)
                } else {
                    // No hay compañías en esa ubicación
                    _searchResults.value = emptyList()
                    _searchCount.value = 0
                    _isEmpty.value = true
                    _isLoading.value = false
                    _isSearching.value = false
                }
            }

            override fun onError(exception: Exception) {
                android.util.Log.e("SearchViewModel", "Error getting companies: ${exception.message}")
                _isLoading.value = false
                _isSearching.value = false
                _errorMessage.value = "Error buscando compañías: ${exception.message}"
            }
        })
    }

    /**
     * Buscar solo por tipo de empleo
     */
    private fun searchByJobTypeOnly(jobType: String?) {
        if (jobType.isNullOrBlank()) {
            _errorMessage.value = "Ingresa un criterio de búsqueda"
            _isLoading.value = false
            _isSearching.value = false
            return
        }

        // Obtener todos los jobs activos y filtrar por tipo
        firestoreService.getActiveJobs(100, object : Callback<List<Job>> {
            override fun onSuccess(jobs: List<Job>) {
                val filteredJobs = filterJobsByType(jobs, jobType)
                processSearchResults(filteredJobs)
            }

            override fun onError(exception: Exception) {
                android.util.Log.e("SearchViewModel", "Error getting jobs: ${exception.message}")
                _isLoading.value = false
                _isSearching.value = false
                _errorMessage.value = "Error en la búsqueda: ${exception.message}"
            }
        })
    }

    /**
     * Obtener jobs de las compañías especificadas y filtrar por tipo
     */
    private fun getJobsFromCompanies(companies: List<Company>, jobType: String?) {
        val allJobs = mutableListOf<Job>()
        var pendingRequests = companies.size

        if (pendingRequests == 0) {
            processSearchResults(emptyList())
            return
        }

        companies.forEach { company ->
            firestoreService.getJobsByCompany(company.id, object : Callback<List<Job>> {
                override fun onSuccess(jobs: List<Job>) {
                    // Filtrar solo jobs activos
                    val activeJobs = jobs.filter { it.status == JobStatus.ACTIVE }
                    
                    // Filtrar por tipo de empleo si se especifica
                    val filteredJobs = if (!jobType.isNullOrBlank()) {
                        filterJobsByType(activeJobs, jobType)
                    } else {
                        activeJobs
                    }
                    
                    allJobs.addAll(filteredJobs)
                    pendingRequests--

                    if (pendingRequests == 0) {
                        processSearchResults(allJobs)
                    }
                }

                override fun onError(exception: Exception) {
                    android.util.Log.e("SearchViewModel", "Error getting jobs for company ${company.id}: ${exception.message}")
                    pendingRequests--

                    if (pendingRequests == 0) {
                        processSearchResults(allJobs)
                    }
                }
            })
        }
    }

    /**
     * Filtra jobs por tipo de empleo en title, modality o aboutJob
     */
    private fun filterJobsByType(jobs: List<Job>, jobType: String): List<Job> {
        return jobs.filter { job ->
            job.title.contains(jobType, ignoreCase = true) ||
            job.modality.contains(jobType, ignoreCase = true) ||
            job.aboutJob.contains(jobType, ignoreCase = true)
        }
    }

    /**
     * Procesa y muestra los resultados de búsqueda
     */
    private fun processSearchResults(jobs: List<Job>) {
        // Ordenar por fecha de creación (más recientes primero)
        val sortedJobs = jobs.sortedByDescending { it.createdAt }
        
        android.util.Log.d("SearchViewModel", "Search completed: ${sortedJobs.size} jobs found")
        
        // Guardar todos los resultados sin filtrar
        _allResults.value = sortedJobs
        
        // Aplicar filtros a los resultados
        applyFiltersToResults()
        
        _isSearching.value = false

        // Precargar información de compañías para los resultados
        if (sortedJobs.isNotEmpty()) {
            // MANTENER loading=true mientras carga compañías
            loadCompaniesForJobs(sortedJobs)
        } else {
            // Si no hay jobs, terminar loading inmediatamente
            _isLoading.value = false
        }
    }

    /**
     * Precarga información de compañías para mostrar con los resultados
     */
    private fun loadCompaniesForJobs(jobs: List<Job>) {
        val companyIds = jobs.map { it.companyId }.distinct().filter { it.isNotEmpty() }
        
        if (companyIds.isEmpty()) {
            // Si no hay companyIds, terminar loading inmediatamente
            _isLoading.value = false
            android.util.Log.d("SearchViewModel", "No companies to load - Loading finished")
            return
        }

        val companiesMap = mutableMapOf<String, Company>()
        var pendingRequests = companyIds.size

        companyIds.forEach { companyId ->
            firestoreService.getCompanyById(companyId, object : Callback<Company?> {
                override fun onSuccess(company: Company?) {
                    company?.let { companiesMap[it.id] = it }
                    pendingRequests--

                    if (pendingRequests == 0) {
                        _companiesCache.value = companiesMap
                        _isLoading.value = false  // ✅ TERMINAR loading cuando se cargan todas las compañías
                        android.util.Log.d("SearchViewModel", "Loaded ${companiesMap.size} companies for search results - Loading finished")
                    }
                }

                override fun onError(exception: Exception) {
                    android.util.Log.e("SearchViewModel", "Error loading company $companyId: ${exception.message}")
                    pendingRequests--

                    if (pendingRequests == 0) {
                        _companiesCache.value = companiesMap
                        _isLoading.value = false  // ✅ TERMINAR loading también en caso de error
                        android.util.Log.d("SearchViewModel", "Finished loading companies (with errors) - Loading finished")
                    }
                }
            })
        }
    }

    /**
     * Obtiene la compañía para un job específico
     */
    fun getCompanyForJob(job: Job): Company? {
        return _companiesCache.value?.get(job.companyId)
    }

    /**
     * Limpia los resultados de búsqueda
     */
    fun clearSearch() {
        _searchResults.value = emptyList()
        _searchCount.value = 0
        _isEmpty.value = false
        _hasSearched.value = false
        _errorMessage.value = ""
        _companiesCache.value = emptyMap()
    }

    /**
     * Limpia solo los mensajes de error
     */
    fun clearErrorMessage() {
        _errorMessage.value = ""
    }

    /**
     * Verifica si se puede realizar búsqueda
     */
    fun canSearch(): Boolean {
        val jobType = _jobTypeQuery.value?.trim()
        val location = _locationQuery.value?.trim()
        return !jobType.isNullOrBlank() || !location.isNullOrBlank()
    }

    /**
     * Obtiene el resumen de la búsqueda actual
     */
    fun getSearchSummary(): String {
        val jobType = _jobTypeQuery.value?.trim()
        val location = _locationQuery.value?.trim()
        
        return when {
            !jobType.isNullOrBlank() && !location.isNullOrBlank() -> 
                "Búsqueda: '$jobType' en '$location'"
            !jobType.isNullOrBlank() -> 
                "Búsqueda: '$jobType'"
            !location.isNullOrBlank() -> 
                "Búsqueda en: '$location'"
            else -> 
                "Búsqueda vacía"
        }
    }

    /**
     * Recargar resultados (para refresh)
     */
    fun refreshResults() {
        android.util.Log.d("SearchViewModel", "refreshResults() called")
        android.util.Log.d("SearchViewModel", "hasSearched: ${_hasSearched.value}, canSearch: ${canSearch()}")
        android.util.Log.d("SearchViewModel", "jobType: '${_jobTypeQuery.value}', location: '${_locationQuery.value}'")
        
        if (canSearch()) {
            android.util.Log.d("SearchViewModel", "Executing refresh search")
            // Limpiar errores previos antes de refrescar
            _errorMessage.value = ""
            searchJobs()
        } else {
            android.util.Log.w("SearchViewModel", "Cannot refresh: no search criteria available")
            _errorMessage.value = "No hay criterios de búsqueda para refrescar"
        }
    }

    // ===== MÉTODOS PARA FILTROS =====

    /**
     * Actualizar filtros y aplicarlos a los resultados
     */
    fun updateFilters(filters: JobFilters) {
        android.util.Log.d("SearchViewModel", "Updating filters: modality=${filters.modality}, salaryRange=${filters.salaryRange}")
        _activeFilters.value = filters
        applyFiltersToResults()
    }

    /**
     * Limpiar todos los filtros
     */
    fun clearFilters() {
        android.util.Log.d("SearchViewModel", "Clearing all filters")
        _activeFilters.value = JobFilters()
        applyFiltersToResults()
    }

    /**
     * Aplicar filtros activos a los resultados de búsqueda
     */
    private fun applyFiltersToResults() {
        val allJobs = _allResults.value ?: return
        val filters = _activeFilters.value ?: JobFilters()

        android.util.Log.d("SearchViewModel", "Applying filters to ${allJobs.size} jobs")

        val filteredJobs = if (!filters.hasActiveFilters()) {
            // Sin filtros, mostrar todos los resultados
            allJobs
        } else {
            allJobs.filter { job ->
                applyModalityFilter(job, filters.modality) &&
                applySalaryRangeFilter(job, filters.salaryRange)
            }
        }

        android.util.Log.d("SearchViewModel", "Filtered results: ${filteredJobs.size} jobs")

        _searchResults.value = filteredJobs
        _searchCount.value = filteredJobs.size
        _isEmpty.value = filteredJobs.isEmpty()
    }

    /**
     * Aplicar filtro de modalidad
     */
    private fun applyModalityFilter(job: Job, modalityFilter: String): Boolean {
        if (modalityFilter.isEmpty()) return true
        return job.modality.contains(modalityFilter, ignoreCase = true)
    }

    /**
     * Aplicar filtro de rango salarial
     */
    private fun applySalaryRangeFilter(job: Job, salaryRange: SalaryRange): Boolean {
        if (salaryRange == SalaryRange.ALL) return true

        // Extraer valor del salario del job
        val jobSalary = SalaryRange.extractSalaryValue(job.salary)
        if (jobSalary == 0) {
            // Si no tiene salario especificado, no excluir el job
            return true
        }

        return jobSalary >= salaryRange.minSalary && jobSalary <= salaryRange.maxSalary
    }

    /**
     * Obtener filtros activos
     */
    fun getCurrentFilters(): JobFilters {
        return _activeFilters.value ?: JobFilters()
    }
}