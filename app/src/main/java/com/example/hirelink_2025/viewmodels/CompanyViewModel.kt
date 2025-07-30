package com.example.hirelink_2025.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hirelink_2025.models.Company
import com.example.hirelink_2025.network.Callback
import com.example.hirelink_2025.network.FirestoreService
import com.example.hirelink_2025.network.VoidCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel para manejo de compañías del usuario - Conectado directamente a FirestoreService
 */
class CompanyViewModel : ViewModel() {

    private val firestoreService = FirestoreService()

    // Lista de compañías
    private val _listCompany = MutableLiveData<List<Company>>()
    val listCompany: LiveData<List<Company>> get() = _listCompany

    // Estados de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Errores
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Estado local para compañía seleccionada
    private val _selectedCompany = MutableStateFlow<Company?>(null)
    val selectedCompany: StateFlow<Company?> = _selectedCompany.asStateFlow()

    // Estado para operaciones
    private val _operationInProgress = MutableStateFlow(false)
    val operationInProgress: StateFlow<Boolean> = _operationInProgress.asStateFlow()

    private val _operationResult = MutableStateFlow<String?>(null)
    val operationResult: StateFlow<String?> = _operationResult.asStateFlow()

    init {
        Log.d("CompanyViewModel", "ViewModel initialized")
    }

    /**
     * Cargar compañías del usuario actual
     */
    fun loadUserCompanies(ownerId: String) {
        Log.d("CompanyViewModel", "Loading companies for owner: $ownerId")
        _isLoading.value = true
        _error.value = null
        
        firestoreService.getCompaniesByOwner(ownerId, object: Callback<List<Company>>{
            override fun onSuccess(result: List<Company>) {
                Log.d("CompanyViewModel", "Successfully loaded ${result.size} companies")
                _listCompany.value = result
                _isLoading.value = false
            }

            override fun onError(exception: Exception) {
                Log.e("CompanyViewModel", "Error loading companies", exception)
                _error.value = "Error al cargar las compañías: ${exception.message}"
                _isLoading.value = false
            }
        })
    }

    /**
     * Crear nueva compañía
     */
    fun createCompany(company: Company, callback: (Boolean, String?) -> Unit) {
        Log.d("CompanyViewModel", "Creating company: ${company.name}")
        _operationInProgress.value = true
        
        firestoreService.createCompany(company, object : Callback<String> {
            override fun onSuccess(result: String) {
                Log.d("CompanyViewModel", "Company created with ID: $result")
                _operationResult.value = "Compañía creada exitosamente"
                _operationInProgress.value = false
                callback(true, result)
            }

            override fun onError(exception: Exception) {
                Log.e("CompanyViewModel", "Failed to create company", exception)
                _operationResult.value = "Error al crear la compañía: ${exception.message}"
                _operationInProgress.value = false
                callback(false, null)
            }
        })
    }

    /**
     * Actualizar compañía existente
     */
    fun updateCompany(company: Company, callback: (Boolean) -> Unit) {
        Log.d("CompanyViewModel", "Updating company: ${company.name}")
        _operationInProgress.value = true
        
        firestoreService.updateCompany(company, object : VoidCallback {
            override fun onSuccess() {
                Log.d("CompanyViewModel", "Company updated successfully")
                _operationResult.value = "Compañía actualizada exitosamente"
                _operationInProgress.value = false
                // Recargar lista después de actualizar
                loadUserCompanies(company.ownerId)
                callback(true)
            }

            override fun onError(exception: Exception) {
                Log.e("CompanyViewModel", "Failed to update company", exception)
                _operationResult.value = "Error al actualizar la compañía: ${exception.message}"
                _operationInProgress.value = false
                callback(false)
            }
        })
    }

    /**
     * Eliminar compañía
     */
    fun deleteCompany(companyId: String, ownerId: String, callback: (Boolean) -> Unit) {
        Log.d("CompanyViewModel", "Deleting company: $companyId")
        _operationInProgress.value = true
        
        firestoreService.deleteCompany(companyId, object : VoidCallback {
            override fun onSuccess() {
                Log.d("CompanyViewModel", "Company deleted successfully")
                _operationResult.value = "Compañía eliminada exitosamente"
                _operationInProgress.value = false
                // Limpiar selección si es la compañía eliminada
                if (_selectedCompany.value?.id == companyId) {
                    _selectedCompany.value = null
                }
                // Recargar lista después de eliminar
                loadUserCompanies(ownerId)
                callback(true)
            }

            override fun onError(exception: Exception) {
                Log.e("CompanyViewModel", "Failed to delete company", exception)
                _operationResult.value = "Error al eliminar la compañía: ${exception.message}"
                _operationInProgress.value = false
                callback(false)
            }
        })
    }

    /**
     * Obtener compañía por ID
     */
    fun getCompanyById(companyId: String, callback: (Company?) -> Unit) {
        Log.d("CompanyViewModel", "Getting company by ID: $companyId")
        _isLoading.value = true
        
        firestoreService.getCompanyById(companyId, object : Callback<Company?> {
            override fun onSuccess(result: Company?) {
                if (result != null) {
                    Log.d("CompanyViewModel", "Company found: ${result.name}")
                } else {
                    Log.w("CompanyViewModel", "Company not found: $companyId")
                }
                _isLoading.value = false
                callback(result)
            }

            override fun onError(exception: Exception) {
                Log.e("CompanyViewModel", "Error getting company by ID", exception)
                _isLoading.value = false
                callback(null)
            }
        })
    }

    /**
     * Seleccionar compañía para edición/detalle
     */
    fun selectCompany(company: Company?) {
        Log.d("CompanyViewModel", "Company selected: ${company?.name}")
        _selectedCompany.value = company
    }

    /**
     * Limpiar URLs de placeholder problemáticas
     */
    fun cleanPlaceholderUrls(ownerId: String, callback: (Boolean) -> Unit) {
        Log.d("CompanyViewModel", "Cleaning placeholder URLs for owner: $ownerId")
        
        firestoreService.cleanPlaceholderUrls(ownerId, object : VoidCallback {
            override fun onSuccess() {
                Log.d("CompanyViewModel", "Placeholder URLs cleaned successfully")
                callback(true)
            }

            override fun onError(exception: Exception) {
                Log.e("CompanyViewModel", "Failed to clean placeholder URLs", exception)
                callback(false)
            }
        })
    }

    /**
     * Buscar compañías por nombre - usando FirestoreService directamente
     */
    fun searchCompanies(query: String, callback: (List<Company>) -> Unit) {
        Log.d("CompanyViewModel", "Searching companies with query: $query")
        _isLoading.value = true
        
        firestoreService.getAllCompanies(object : Callback<List<Company>> {
            override fun onSuccess(result: List<Company>) {
                // Filtrar por nombre localmente
                val filteredCompanies = result.filter { 
                    it.name.contains(query, ignoreCase = true) 
                }
                _isLoading.value = false
                callback(filteredCompanies)
            }

            override fun onError(exception: Exception) {
                Log.e("CompanyViewModel", "Error searching companies", exception)
                _isLoading.value = false
                callback(emptyList())
            }
        })
    }

    /**
     * Limpiar mensaje de resultado de operación
     */
    fun clearOperationResult() {
        _operationResult.value = null
    }

    /**
     * Limpiar error
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Recargar compañías
     */
    fun refreshCompanies(ownerId: String) {
        Log.d("CompanyViewModel", "Refreshing companies for owner: $ownerId")
        loadUserCompanies(ownerId)
    }

    /**
     * Validar datos de compañía antes de crear/actualizar
     */
    fun validateCompany(company: Company): ValidationResult {
        val errors = mutableListOf<String>()

        if (company.name.isBlank()) {
            errors.add("El nombre de la compañía es requerido")
        }

        if (company.type.isBlank()) {
            errors.add("El tipo de industria es requerido")
        }

        if (company.description.isBlank()) {
            errors.add("La descripción es requerida")
        }

        if (company.email.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(company.email).matches()) {
            errors.add("El formato del email es inválido")
        }

        if (company.website.isNotBlank() && !android.util.Patterns.WEB_URL.matcher(company.website).matches()) {
            errors.add("El formato del sitio web es inválido")
        }

        if (company.address.isBlank()) {
            errors.add("La dirección es requerida")
        }

        if (company.city.isBlank()) {
            errors.add("La ciudad es requerida")
        }

        return if (errors.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Error(errors)
        }
    }

    /**
     * Resultado de validación
     */
    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val errors: List<String>) : ValidationResult()
    }
}