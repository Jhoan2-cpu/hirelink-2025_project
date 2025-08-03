package com.example.hirelink_2025.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hirelink_2025.models.Company
import com.example.hirelink_2025.models.CompanyLocation
import com.example.hirelink_2025.models.Job
import com.example.hirelink_2025.models.JobStatus
import com.example.hirelink_2025.network.Callback
import com.example.hirelink_2025.network.FirestoreService
import com.example.hirelink_2025.utils.LocationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class JobMapUiState(
    val isLoading: Boolean = false,
    val companyLocations: List<CompanyLocation> = emptyList(),
    val userLatitude: Double? = null,
    val userLongitude: Double? = null,
    val totalJobs: Int = 0,
    val totalCompanies: Int = 0,
    val nearbyCompanies: Int = 0,
    val error: String? = null
)

class JobMapViewModel : ViewModel() {

    private val firestoreService = FirestoreService()
    private val locationUtils = LocationUtils()

    private val _uiState = MutableStateFlow(JobMapUiState())
    val uiState: StateFlow<JobMapUiState> = _uiState.asStateFlow()

    /**
     * Carga todas las compañías y sus ofertas activas para mostrar en el mapa
     */
    fun loadJobLocations() {
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            firestoreService.getAllCompanies(object : Callback<List<Company>> {
                override fun onSuccess(companies: List<Company>) {
                    android.util.Log.d("JobMapViewModel", "Loaded ${companies.size} companies")
                    loadJobsForCompanies(companies)
                }

                override fun onError(exception: Exception) {
                    android.util.Log.e("JobMapViewModel", "Error loading companies: ${exception.message}")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Error al cargar empresas: ${exception.message}"
                        )
                    }
                }
            })
        }
    }

    /**
     * Carga los trabajos activos para cada empresa
     */
    private fun loadJobsForCompanies(companies: List<Company>) {
        val companyLocations = mutableListOf<CompanyLocation>()
        var pendingRequests = companies.size
        var totalJobsCount = 0

        if (companies.isEmpty()) {
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    companyLocations = emptyList(),
                    totalJobs = 0,
                    totalCompanies = 0,
                    nearbyCompanies = 0
                )
            }
            return
        }

        companies.forEach { company ->
            android.util.Log.d("JobMapViewModel", "Processing company: ${company.name} (ID: ${company.id})")
            
            // Solo procesar empresas que tengan coordenadas válidas
            val coordinates = extractCoordinatesFromCompany(company)
            if (coordinates != null) {
                android.util.Log.d("JobMapViewModel", "Company ${company.name} has valid coordinates, loading jobs...")
                firestoreService.getJobsByCompany(company.id, object : Callback<List<Job>> {
                    override fun onSuccess(jobs: List<Job>) {
                        android.util.Log.d("JobMapViewModel", "Company ${company.name}: Found ${jobs.size} total jobs")
                        val activeJobs = jobs.filter { it.status == JobStatus.ACTIVE }
                        android.util.Log.d("JobMapViewModel", "Company ${company.name}: ${activeJobs.size} active jobs")
                        
                        if (activeJobs.isNotEmpty()) {
                            val userLocation = getCurrentUserLocation()
                            val distance = if (userLocation != null) {
                                locationUtils.calculateDistance(
                                    userLocation.first, userLocation.second,
                                    coordinates.first, coordinates.second
                                )
                            } else {
                                0.0
                            }

                            val companyLocation = CompanyLocation(
                                company = company,
                                latitude = coordinates.first,
                                longitude = coordinates.second,
                                activeJobsCount = activeJobs.size,
                                distance = distance
                            )
                            companyLocations.add(companyLocation)
                            totalJobsCount += activeJobs.size
                        }

                        pendingRequests--
                        if (pendingRequests == 0) {
                            processCompanyLocations(companyLocations, totalJobsCount)
                        }
                    }

                    override fun onError(exception: Exception) {
                        android.util.Log.e("JobMapViewModel", "Error loading jobs for company ${company.id}: ${exception.message}")
                        pendingRequests--
                        if (pendingRequests == 0) {
                            processCompanyLocations(companyLocations, totalJobsCount)
                        }
                    }
                })
            } else {
                android.util.Log.w("JobMapViewModel", "Company ${company.name} has no valid coordinates, skipping...")
                pendingRequests--
                if (pendingRequests == 0) {
                    processCompanyLocations(companyLocations, totalJobsCount)
                }
            }
        }
    }

    /**
     * Procesa las ubicaciones de las empresas y actualiza el UI state
     */
    private fun processCompanyLocations(companyLocations: List<CompanyLocation>, totalJobs: Int) {
        val nearbyCount = companyLocations.count { it.isNearby() }
        
        android.util.Log.d("JobMapViewModel", "Processed ${companyLocations.size} company locations with $totalJobs total jobs")
        
        _uiState.update { 
            it.copy(
                isLoading = false,
                companyLocations = companyLocations.sortedBy { location -> location.distance },
                totalJobs = totalJobs,
                totalCompanies = companyLocations.size,
                nearbyCompanies = nearbyCount,
                error = null
            )
        }
    }

    /**
     * Extrae coordenadas de una empresa
     * Intenta obtener las coordenadas desde diferentes campos del modelo Company
     */
    private fun extractCoordinatesFromCompany(company: Company): Pair<Double, Double>? {
        android.util.Log.d("JobMapViewModel", "Extracting coordinates for company: ${company.name}")
        android.util.Log.d("JobMapViewModel", "Ubication field: '${company.ubication}'")
        android.util.Log.d("JobMapViewModel", "Address field: '${company.address}'")
        android.util.Log.d("JobMapViewModel", "City: '${company.city}', Country: '${company.country}'")
        
        // Primero intentar con ubication (coordenadas exactas)
        if (company.ubication.isNotEmpty()) {
            val coordinates = locationUtils.extractCoordinates(company.ubication)
            if (coordinates != null) {
                android.util.Log.d("JobMapViewModel", "Found exact coordinates: $coordinates")
                return coordinates
            }
        }
        
        // Si no hay coordenadas exactas, usar geocoding desde address/city
        return if (company.city.isNotEmpty() && company.country.isNotEmpty()) {
            val coordinates = locationUtils.getCoordinatesFromAddress(company.address, company.city, company.country)
            android.util.Log.d("JobMapViewModel", "Geocoded coordinates: $coordinates")
            coordinates
        } else {
            android.util.Log.w("JobMapViewModel", "No valid location data for company: ${company.name}")
            null
        }
    }

    /**
     * Actualiza la ubicación del usuario
     */
    fun updateUserLocation(latitude: Double, longitude: Double) {
        _uiState.update { 
            it.copy(
                userLatitude = latitude,
                userLongitude = longitude
            )
        }
        
        // Recalcular distancias si ya tenemos ubicaciones de empresas
        val currentLocations = _uiState.value.companyLocations
        if (currentLocations.isNotEmpty()) {
            val updatedLocations = currentLocations.map { location ->
                val distance = locationUtils.calculateDistance(
                    latitude, longitude,
                    location.latitude, location.longitude
                )
                location.copy(distance = distance)
            }
            
            val nearbyCount = updatedLocations.count { it.isNearby() }
            
            _uiState.update { current ->
                current.copy(
                    companyLocations = updatedLocations.sortedBy { it.distance },
                    nearbyCompanies = nearbyCount
                )
            }
        }
    }

    /**
     * Obtiene la ubicación actual del usuario
     */
    private fun getCurrentUserLocation(): Pair<Double, Double>? {
        val state = _uiState.value
        return if (state.userLatitude != null && state.userLongitude != null) {
            Pair(state.userLatitude, state.userLongitude)
        } else {
            null
        }
    }

    /**
     * Obtiene las ofertas activas de una empresa específica
     */
    fun getActiveJobsForCompany(companyId: String, callback: Callback<List<Job>>) {
        firestoreService.getJobsByCompany(companyId, object : Callback<List<Job>> {
            override fun onSuccess(jobs: List<Job>) {
                val activeJobs = jobs.filter { it.status == JobStatus.ACTIVE }
                callback.onSuccess(activeJobs)
            }

            override fun onError(exception: Exception) {
                callback.onError(exception)
            }
        })
    }

    /**
     * Limpia los errores
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Filtra ubicaciones por distancia
     */
    fun filterByDistance(maxDistance: Double) {
        val currentLocations = _uiState.value.companyLocations
        val filteredLocations = currentLocations.filter { it.distance <= maxDistance }
        
        _uiState.update { 
            it.copy(companyLocations = filteredLocations)
        }
    }

    /**
     * Resetea los filtros
     */
    fun resetFilters() {
        loadJobLocations()
    }
}