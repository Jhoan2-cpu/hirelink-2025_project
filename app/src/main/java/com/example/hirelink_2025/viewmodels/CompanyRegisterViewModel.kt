package com.example.hirelink_2025.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hirelink_2025.models.Company
import android.net.Uri
import com.example.hirelink_2025.network.AuthCallback
import com.example.hirelink_2025.network.Callback
import com.example.hirelink_2025.network.FirebaseStorageService
import com.example.hirelink_2025.network.FirestoreService
import com.example.hirelink_2025.network.VoidCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel específico para el registro de compañías - Conectado directamente a FirestoreService
 */
class CompanyRegisterViewModel : ViewModel() {

    // Estado del formulario de registro
    private val _uiState = MutableStateFlow(CompanyRegisterUiState())
    val uiState: StateFlow<CompanyRegisterUiState> = _uiState.asStateFlow()

    //Implementando Firebase Services
    val firestoreService = FirestoreService()
    val storageService = FirebaseStorageService()
    var isLoading = MutableLiveData<Boolean>()

    init {
        Log.d("CompanyRegisterViewModel", "ViewModel initialized")
    }

    /**
     * Registrar nueva compañía con logo
     */
    fun registerCompany(
        name: String,
        type: String,
        description: String,
        size: String,
        foundedYear: Int,
        address: String,
        city: String,
        country: String,
        email: String,
        phone: String,
        website: String,
        logoUri: Uri?,
        ownerId: String
    ) {
        Log.d("CompanyRegisterViewModel", "Registering company: $name")
        
        // Validar campos
        val validationResult = validateCompanyData(
            name, type, description, address, city, email, phone, website
        )
        
        if (validationResult !is ValidationResult.Success) {
            _uiState.value = _uiState.value.copy(
                validationErrors = (validationResult as ValidationResult.Error).errors
            )
            return
        }
        
        // Limpiar errores previos
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null,
            validationErrors = emptyList()
        )
        
        // Validar logo si se proporciona
        if (logoUri != null) {
            if (!storageService.isValidImageFile(logoUri)) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "El archivo seleccionado no es una imagen válida"
                )
                return
            }
            
            if (!storageService.isValidFileSize(logoUri)) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "El archivo es demasiado grande. Máximo 5MB permitido."
                )
                return
            }
        }
        
        // Si hay logo, primero subirlo, luego crear la compañía
        if (logoUri != null) {
            Log.d("CompanyRegisterViewModel", "Uploading logo first")
            storageService.uploadTemporaryCompanyLogo(logoUri, object : Callback<String> {
                override fun onSuccess(logoUrl: String) {
                    Log.d("CompanyRegisterViewModel", "Logo uploaded successfully: $logoUrl")
                    createCompanyWithLogo(name, type, description, size, foundedYear, 
                                        address, city, country, email, phone, website, 
                                        logoUrl, ownerId)
                }

                override fun onError(exception: Exception) {
                    Log.e("CompanyRegisterViewModel", "Failed to upload logo", exception)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Error al subir el logo: ${exception.message}"
                    )
                }
            })
        } else {
            // Sin logo, crear compañía directamente
            createCompanyWithLogo(name, type, description, size, foundedYear, 
                                address, city, country, email, phone, website, 
                                "", ownerId)
        }

    }

    /**
     * Crear compañía con logo URL
     */
    private fun createCompanyWithLogo(
        name: String,
        type: String,
        description: String,
        size: String,
        foundedYear: Int,
        address: String,
        city: String,
        country: String,
        email: String,
        phone: String,
        website: String,
        logoUrl: String,
        ownerId: String
    ) {
        Log.d("CompanyRegisterViewModel", "Creating company with logo URL: $logoUrl")
        
        // Crear objeto Company
        val company = Company(
            id = "", // Se generará automáticamente
            name = name.trim(),
            type = type.trim(),
            description = description.trim(),
            size = size,
            foundedYear = foundedYear,
            address = address.trim(),
            city = city.trim(),
            country = country.trim(),
            email = email.trim(),
            phone = phone.trim(),
            website = website.trim(),
            ownerId = ownerId,
            logoUrl = logoUrl,
            employeeCount = parseEmployeeCount(size),
            activeJobsCount = 0,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        // Registrar compañía en Firestore
        firestoreService.createCompany(company, object : Callback<String> {
            override fun onSuccess(result: String) {
                Log.d("CompanyRegisterViewModel", "Company created successfully with ID: $result")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    registeredCompanyId = result,
                    error = null
                )
            }

            override fun onError(exception: Exception) {
                Log.e("CompanyRegisterViewModel", "Failed to create company", exception)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = false,
                    error = "Error al registrar la compañía: ${exception.message}"
                )
            }
        })
    }

    /**
     * Validar datos de la compañía
     */
    private fun validateCompanyData(
        name: String,
        type: String,
        description: String,
        address: String,
        city: String,
        email: String,
        phone: String,
        website: String
    ): ValidationResult {
        val errors = mutableListOf<String>()

        // Validaciones requeridas
        if (name.isBlank()) {
            errors.add("El nombre de la compañía es requerido")
        } else if (name.length < 2) {
            errors.add("El nombre debe tener al menos 2 caracteres")
        }

        if (type.isBlank()) {
            errors.add("El tipo de industria es requerido")
        }

        if (description.isBlank()) {
            errors.add("La descripción es requerida")
        } else if (description.length < 10) {
            errors.add("La descripción debe tener al menos 10 caracteres")
        }

        if (address.isBlank()) {
            errors.add("La dirección es requerida")
        }

        if (city.isBlank()) {
            errors.add("La ciudad es requerida")
        }

        // Validaciones de formato
        if (email.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errors.add("El formato del email es inválido")
        }

        if (phone.isNotBlank() && !isValidPhone(phone)) {
            errors.add("El formato del teléfono es inválido")
        }

        if (website.isNotBlank() && !isValidWebsite(website)) {
            errors.add("El formato del sitio web es inválido")
        }

        return if (errors.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Error(errors)
        }
    }

    /**
     * Validar formato de teléfono (básico)
     */
    private fun isValidPhone(phone: String): Boolean {
        val cleanPhone = phone.replace("\\s".toRegex(), "").replace("-", "")
        return cleanPhone.matches(Regex("^[+]?[0-9]{7,15}$"))
    }

    /**
     * Validar formato de website
     */
    private fun isValidWebsite(website: String): Boolean {
        return android.util.Patterns.WEB_URL.matcher(website).matches() ||
                website.matches(Regex("^[a-zA-Z0-9][a-zA-Z0-9-]{1,61}[a-zA-Z0-9]\\.[a-zA-Z]{2,}$"))
    }

    /**
     * Parsear tamaño de empleados a número
     */
    private fun parseEmployeeCount(size: String): Int {
        return when (size) {
            "1-10" -> 5
            "11-50" -> 30
            "51-100" -> 75
            "101-500" -> 300
            "501-1000" -> 750
            "1000+" -> 1500
            else -> 0
        }
    }

    /**
     * Limpiar errores
     */
    fun clearErrors() {
        _uiState.value = _uiState.value.copy(
            error = null,
            validationErrors = emptyList()
        )
    }

    /**
     * Resetear estado después de éxito
     */
    fun resetState() {
        _uiState.value = CompanyRegisterUiState()
    }

    /**
     * Obtener lista de tipos de industria
     */
    fun getIndustryTypes(): List<String> {
        return listOf(
            "Tecnología",
            "Finanzas",
            "Salud",
            "Educación",
            "Retail",
            "Construcción",
            "Manufactura",
            "Telecomunicaciones",
            "Energía",
            "Turismo",
            "Logística",
            "Consultoría",
            "Medios",
            "Agricultura",
            "Otros"
        )
    }

    /**
     * Obtener lista de tamaños de empresa
     */
    fun getCompanySizes(): List<String> {
        return listOf(
            "1-10",
            "11-50", 
            "51-100",
            "101-500",
            "501-1000",
            "1000+"
        )
    }

    /**
     * Resultado de validación
     */
    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val errors: List<String>) : ValidationResult()
    }
}

/**
 * Estado de la UI para registro de compañía
 */
data class CompanyRegisterUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val validationErrors: List<String> = emptyList(),
    val registeredCompanyId: String? = null
)