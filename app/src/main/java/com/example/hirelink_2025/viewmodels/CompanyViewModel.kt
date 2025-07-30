package com.example.hirelink_2025.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hirelink_2025.models.Company
import com.example.hirelink_2025.network.Callback
import com.example.hirelink_2025.network.FirestoreService
import com.example.hirelink_2025.repository.CompanyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.collections.List

/**
 * ViewModel para manejo de compañías del usuario
 */
class CompanyViewModel(
    private val companyRepository: CompanyRepository
) : ViewModel() {

    // StateFlow del repository para la lista de compañías
    val companies: StateFlow<List<Company>> = companyRepository.companies//NO USAR REPOSITORY

    private val _listCompany = MutableLiveData<List<Company>>()
    val listCompany: LiveData<List<Company>> get() = _listCompany


    val isLoading: StateFlow<Boolean> = companyRepository.isLoading
    val error: StateFlow<String?> = companyRepository.error
    private val firestoreService = FirestoreService()
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
       // companyRepository.loadUserCompanies(ownerId)
//        listCompany.value ="f"
        firestoreService.getCompaniesByOwner(ownerId, object: Callback<List<Company>>{
            override fun onSuccess(result: List<Company>) {
                    _listCompany.value = result
            }

            override fun onError(exception: Exception) {
                Log.e("CompanyViewModel", "Error loading companies: ${exception}")
            }

        })

    }

    /**
     * Crear nueva compañía
     */
    fun createCompany(company: Company, callback: (Boolean, String?) -> Unit) {
        Log.d("CompanyViewModel", "Creating company: ${company.name}")
        _operationInProgress.value = true
        
        companyRepository.createCompany(company) { success, companyId ->
            _operationInProgress.value = false
            if (success) {
                _operationResult.value = "Compañía creada exitosamente"
                Log.d("CompanyViewModel", "Company created with ID: $companyId")
            } else {
                _operationResult.value = "Error al crear la compañía"
                Log.e("CompanyViewModel", "Failed to create company")
            }
            callback(success, companyId)
        }
    }

    /**
     * Actualizar compañía existente
     */
    fun updateCompany(company: Company, callback: (Boolean) -> Unit) {
        Log.d("CompanyViewModel", "Updating company: ${company.name}")
        _operationInProgress.value = true
        
        companyRepository.updateCompany(company) { success ->
            _operationInProgress.value = false
            if (success) {
                _operationResult.value = "Compañía actualizada exitosamente"
                Log.d("CompanyViewModel", "Company updated successfully")
            } else {
                _operationResult.value = "Error al actualizar la compañía"
                Log.e("CompanyViewModel", "Failed to update company")
            }
            callback(success)
        }
    }

    /**
     * Eliminar compañía
     */
    fun deleteCompany(companyId: String, ownerId: String, callback: (Boolean) -> Unit) {
        Log.d("CompanyViewModel", "Deleting company: $companyId")
        _operationInProgress.value = true
        
        companyRepository.deleteCompany(companyId, ownerId) { success ->
            _operationInProgress.value = false
            if (success) {
                _operationResult.value = "Compañía eliminada exitosamente"
                Log.d("CompanyViewModel", "Company deleted successfully")
                // Limpiar selección si es la compañía eliminada
                if (_selectedCompany.value?.id == companyId) {
                    _selectedCompany.value = null
                }
            } else {
                _operationResult.value = "Error al eliminar la compañía"
                Log.e("CompanyViewModel", "Failed to delete company")
            }
            callback(success)
        }
    }

    /**
     * Obtener compañía por ID
     */
    fun getCompanyById(companyId: String, callback: (Company?) -> Unit) {
        Log.d("CompanyViewModel", "Getting company by ID: $companyId")
        companyRepository.getCompanyById(companyId) { company ->
            if (company != null) {
                Log.d("CompanyViewModel", "Company found: ${company.name}")
            } else {
                Log.w("CompanyViewModel", "Company not found")
            }
            callback(company)
        }
    }

    /**
     * Seleccionar compañía para edición/detalle
     */
    fun selectCompany(company: Company?) {
        Log.d("CompanyViewModel", "Company selected: ${company?.name}")
        _selectedCompany.value = company
    }

    /**
     * Buscar compañías por nombre
     */
    fun searchCompanies(query: String, callback: (List<Company>) -> Unit) {
        Log.d("CompanyViewModel", "Searching companies with query: $query")
        companyRepository.searchCompanies(query, callback)
    }

    /**
     * Filtrar compañías por tipo
     */
    fun filterByType(type: String, callback: (List<Company>) -> Unit) {
        Log.d("CompanyViewModel", "Filtering companies by type: $type")
        companyRepository.getCompaniesByType(type, callback)
    }

    /**
     * Filtrar compañías por ciudad
     */
    fun filterByCity(city: String, callback: (List<Company>) -> Unit) {
        Log.d("CompanyViewModel", "Filtering companies by city: $city")
        companyRepository.getCompaniesByCity(city, callback)
    }

    /**
     * Filtrar compañías por tamaño
     */
    fun filterBySize(size: String, callback: (List<Company>) -> Unit) {
        Log.d("CompanyViewModel", "Filtering companies by size: $size")
        companyRepository.getCompaniesBySize(size, callback)
    }

    /**
     * Limpiar mensaje de resultado de operación
     */
    fun clearOperationResult() {
        _operationResult.value = null
    }

    /**
     * Limpiar error del repository
     */
    fun clearError() {
        companyRepository.clearError()
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