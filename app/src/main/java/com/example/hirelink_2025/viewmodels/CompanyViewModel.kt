package com.example.hirelink_2025.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.hirelink_2025.models.Company
import com.example.hirelink_2025.repository.CompanyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CompanyViewModel(
    private val companyRepository: CompanyRepository
) : ViewModel() {

    private val _companies = MutableStateFlow<List<Company>>(emptyList())
    val companies: StateFlow<List<Company>> = _companies.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _selectedCompany = MutableStateFlow<Company?>(null)
    val selectedCompany: StateFlow<Company?> = _selectedCompany.asStateFlow()

    init {
        loadCompanies()
    }

    fun loadCompanies() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val companiesList = companyRepository.getAllCompanies()
                _companies.value = companiesList
            } catch (e: Exception) {
                _error.value = "Error al cargar las compañías: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadCompaniesByOwner(ownerId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val companiesList = companyRepository.getCompaniesByOwner(ownerId)
                _companies.value = companiesList
            } catch (e: Exception) {
                _error.value = "Error al cargar las compañías: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addCompany(company: Company, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val success = companyRepository.addCompany(company)
                if (success) {
                    loadCompanies() // Refresh the list
                    onSuccess()
                } else {
                    val errorMsg = "Error al registrar la compañía"
                    _error.value = errorMsg
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Error al registrar la compañía: ${e.message}"
                _error.value = errorMsg
                onError(errorMsg)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateCompany(company: Company, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val success = companyRepository.updateCompany(company)
                if (success) {
                    loadCompanies() // Refresh the list
                    onSuccess()
                } else {
                    val errorMsg = "Error al actualizar la compañía"
                    _error.value = errorMsg
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Error al actualizar la compañía: ${e.message}"
                _error.value = errorMsg
                onError(errorMsg)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteCompany(companyId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val success = companyRepository.deleteCompany(companyId)
                if (success) {
                    loadCompanies() // Refresh the list
                    onSuccess()
                } else {
                    val errorMsg = "Error al eliminar la compañía"
                    _error.value = errorMsg
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = "Error al eliminar la compañía: ${e.message}"
                _error.value = errorMsg
                onError(errorMsg)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getCompanyById(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val company = companyRepository.getCompanyById(id)
                _selectedCompany.value = company
            } catch (e: Exception) {
                _error.value = "Error al cargar la compañía: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchCompanies(query: String) {
        if (query.isBlank()) {
            loadCompanies()
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val searchResults = companyRepository.searchCompanies(query)
                _companies.value = searchResults
            } catch (e: Exception) {
                _error.value = "Error en la búsqueda: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun filterCompaniesByType(type: String) {
        if (type.isBlank()) {
            loadCompanies()
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val filteredResults = companyRepository.getCompaniesByType(type)
                _companies.value = filteredResults
            } catch (e: Exception) {
                _error.value = "Error al filtrar por tipo: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun filterCompaniesByCity(city: String) {
        if (city.isBlank()) {
            loadCompanies()
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val filteredResults = companyRepository.getCompaniesByCity(city)
                _companies.value = filteredResults
            } catch (e: Exception) {
                _error.value = "Error al filtrar por ciudad: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearSelectedCompany() {
        _selectedCompany.value = null
    }

    class Factory(
        private val companyRepository: CompanyRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CompanyViewModel::class.java)) {
                return CompanyViewModel(companyRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}