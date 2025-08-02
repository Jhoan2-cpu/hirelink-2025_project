package com.example.hirelink_2025.repository

import android.util.Log
import com.example.hirelink_2025.models.Company
import com.example.hirelink_2025.network.FirestoreService
import com.example.hirelink_2025.network.Callback
import com.example.hirelink_2025.network.VoidCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository para manejo de compañías usando FirestoreService
 */
class CompanyRepository {
    private val firestoreService = FirestoreService()
    
    private val _companies = MutableStateFlow<List<Company>>(emptyList())
    val companies: StateFlow<List<Company>> = _companies.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        Log.d("CompanyRepository", "Repository initialized with FirestoreService")
    }

    /**
     * Cargar compañías del usuario actual
     */
    fun loadUserCompanies(ownerId: String) {
        Log.d("CompanyRepository", "Loading companies for owner: $ownerId")
        _isLoading.value = true
        _error.value = null
        
        firestoreService.getCompaniesByOwner(ownerId, object : Callback<List<Company>> {
            override fun onSuccess(result: List<Company>) {
                Log.d("CompanyRepository", "Successfully loaded ${result.size} companies")
                _companies.value = result
                _isLoading.value = false
            }
            
            override fun onError(exception: Exception) {
                Log.e("CompanyRepository", "Error loading companies", exception)
                _error.value = exception.message ?: "Error cargando compañías"
                _isLoading.value = false
            }
        })
    }

    /**
     * Obtener compañía por ID
     */
    fun getCompanyById(companyId: String, callback: (Company?) -> Unit) {
        Log.d("CompanyRepository", "Getting company by ID: $companyId")
        
        firestoreService.getCompanyById(companyId, object : Callback<Company?> {
            override fun onSuccess(result: Company?) {
                Log.d("CompanyRepository", "Company found: ${result?.name}")
                callback(result)
            }
            
            override fun onError(exception: Exception) {
                Log.e("CompanyRepository", "Error getting company", exception)
                callback(null)
            }
        })
    }

    /**
     * Crear nueva compañía
     */
    fun createCompany(company: Company, callback: (Boolean, String?) -> Unit) {
        Log.d("CompanyRepository", "Creating company: ${company.name}")
        _isLoading.value = true
        _error.value = null
        
        firestoreService.createCompany(company, object : Callback<String> {
            override fun onSuccess(result: String) {
                Log.d("CompanyRepository", "Company created successfully with ID: $result")
                // Recargar lista después de crear
                loadUserCompanies(company.ownerId)
                callback(true, result)
            }
            
            override fun onError(exception: Exception) {
                Log.e("CompanyRepository", "Error creating company", exception)
                _isLoading.value = false
                _error.value = exception.message ?: "Error creando compañía"
                callback(false, null)
            }
        })
    }

    /**
     * Actualizar compañía existente
     */
    fun updateCompany(company: Company, callback: (Boolean) -> Unit) {
        Log.d("CompanyRepository", "Updating company: ${company.name}")
        _isLoading.value = true
        _error.value = null
        
        firestoreService.updateCompany(company, object : VoidCallback {
            override fun onSuccess() {
                Log.d("CompanyRepository", "Company updated successfully")
                // Recargar lista después de actualizar
                loadUserCompanies(company.ownerId)
                callback(true)
            }
            
            override fun onError(exception: Exception) {
                Log.e("CompanyRepository", "Error updating company", exception)
                _isLoading.value = false
                _error.value = exception.message ?: "Error actualizando compañía"
                callback(false)
            }
        })
    }

    /**
     * Eliminar compañía
     */
    fun deleteCompany(companyId: String, ownerId: String, callback: (Boolean) -> Unit) {
        Log.d("CompanyRepository", "Deleting company: $companyId")
        _isLoading.value = true
        _error.value = null
        
        firestoreService.deleteCompany(companyId, object : VoidCallback {
            override fun onSuccess() {
                Log.d("CompanyRepository", "Company deleted successfully")
                // Recargar lista después de eliminar
                loadUserCompanies(ownerId)
                callback(true)
            }
            
            override fun onError(exception: Exception) {
                Log.e("CompanyRepository", "Error deleting company", exception)
                _isLoading.value = false
                _error.value = exception.message ?: "Error eliminando compañía"
                callback(false)
            }
        })
    }

    /**
     * Buscar compañías por nombre
     */
    fun searchCompanies(query: String, callback: (List<Company>) -> Unit) {
        Log.d("CompanyRepository", "Searching companies with query: $query")
        
        firestoreService.searchCompanies(query, object : Callback<List<Company>> {
            override fun onSuccess(result: List<Company>) {
                Log.d("CompanyRepository", "Search returned ${result.size} companies")
                callback(result)
            }
            
            override fun onError(exception: Exception) {
                Log.e("CompanyRepository", "Error searching companies", exception)
                callback(emptyList())
            }
        })
    }

    /**
     * Filtrar compañías por tipo
     */
    fun getCompaniesByType(type: String, callback: (List<Company>) -> Unit) {
        val currentCompanies = _companies.value
        val filtered = currentCompanies.filter { it.type.equals(type, ignoreCase = true) }
        callback(filtered)
    }

    /**
     * Filtrar compañías por ciudad
     */
    fun getCompaniesByCity(city: String, callback: (List<Company>) -> Unit) {
        val currentCompanies = _companies.value
        val filtered = currentCompanies.filter { it.city.equals(city, ignoreCase = true) }
        callback(filtered)
    }

    /**
     * Filtrar compañías por tamaño usando CompanySize enum
     */
    fun getCompaniesBySize(size: com.example.hirelink_2025.models.CompanySize, callback: (List<Company>) -> Unit) {
        val currentCompanies = _companies.value
        val filtered = currentCompanies.filter { it.size == size }
        callback(filtered)
    }

    /**
     * Limpiar error
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Obtener todas las compañías (sin filtro de usuario)
     */
    fun getAllCompanies(callback: (List<Company>) -> Unit) {
        Log.d("CompanyRepository", "Getting all companies")
        
        firestoreService.getAllCompanies(object : Callback<List<Company>> {
            override fun onSuccess(result: List<Company>) {
                Log.d("CompanyRepository", "Retrieved ${result.size} companies")
                callback(result)
            }
            
            override fun onError(exception: Exception) {
                Log.e("CompanyRepository", "Error getting all companies", exception)
                callback(emptyList())
            }
        })
    }
}