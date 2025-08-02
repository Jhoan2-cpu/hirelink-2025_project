package com.example.hirelink_2025.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hirelink_2025.models.User
import com.example.hirelink_2025.repository.UserRepository
import com.example.hirelink_2025.network.AuthCallback
import com.example.hirelink_2025.network.Callback
import com.example.hirelink_2025.network.ExistsCallback
import com.example.hirelink_2025.network.VoidCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RegisterUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null,
    val isRegisterSuccess: Boolean = false,
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null
)

class RegisterViewModel : ViewModel() {

    private val userRepository = UserRepository.getInstance()

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun register(name: String, email: String, password: String, confirmPassword: String, phone: String?) {
        Log.d("RegisterViewModel", "register() called with: name=$name, email=$email, phone=$phone")
        
        if (!validateInputs(name, email, password, confirmPassword)) {
            Log.e("RegisterViewModel", "Validation failed")
            return
        }

        Log.d("RegisterViewModel", "Validation passed, testing Firebase connection first")
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        // Primero probar la conectividad Firebase
        userRepository.testFirebaseConnection(object : VoidCallback {
            override fun onSuccess() {
                Log.d("RegisterViewModel", "Firebase connection OK, proceeding with registration")
                proceedWithRegistration(email, password, name, phone)
            }

            override fun onError(exception: Exception) {
                Log.e("RegisterViewModel", "Firebase connection failed", exception)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error de conectividad Firebase: ${exception.message}"
                )
            }
        })
    }
    
    private fun proceedWithRegistration(email: String, password: String, name: String, phone: String?) {
        Log.d("RegisterViewModel", "Proceeding with registration using UserRepository")

        userRepository.registerUser(email, password, name, phone, object : AuthCallback {
            override fun onSuccess(userId: String, isNewUser: Boolean) {
                Log.d("RegisterViewModel", "Registration successful: userId=$userId, isNewUser=$isNewUser")
                
                // Obtener datos completos del usuario registrado
                userRepository.getUserById(userId, object : Callback<User?> {
                    override fun onSuccess(user: User?) {
                        if (user != null) {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                user = user,
                                isRegisterSuccess = true,
                                error = null
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = "Error al obtener datos del usuario registrado"
                            )
                        }
                    }
                    
                    override fun onError(exception: Exception) {
                        Log.e("RegisterViewModel", "Error getting user data after registration", exception)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Error al obtener datos del usuario: ${exception.message}"
                        )
                    }
                })
            }

            override fun onError(exception: Exception) {
                Log.e("RegisterViewModel", "Registration failed", exception)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = getAuthErrorMessage(exception)
                )
            }
        })
    }

    /**
     * Verificar si un email está disponible
     */
    fun checkEmailAvailability(email: String) {
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return
        }

        userRepository.checkUserExists(email.trim(), object : ExistsCallback {
            override fun onSuccess(exists: Boolean) {
                _uiState.value = _uiState.value.copy(
                    emailError = if (exists) "El email ya está registrado" else null
                )
            }

            override fun onError(exception: Exception) {
                // No mostrar error al usuario para esta verificación silenciosa
            }
        })
    }

    private fun getAuthErrorMessage(exception: Throwable): String {
        return when {
            exception.message?.contains("email-already-in-use") == true -> 
                "El email ya está en uso"
            exception.message?.contains("invalid-email") == true -> 
                "Email inválido"
            exception.message?.contains("weak-password") == true -> 
                "La contraseña es muy débil"
            exception.message?.contains("operation-not-allowed") == true -> 
                "Registro no permitido"
            exception.message?.contains("network-request-failed") == true -> 
                "Error de conexión. Verifica tu internet"
            else -> exception.message ?: "Error de registro"
        }
    }

    private fun validateInputs(name: String, email: String, password: String, confirmPassword: String): Boolean {
        var isValid = true
        var nameError: String? = null
        var emailError: String? = null
        var passwordError: String? = null
        var confirmPasswordError: String? = null

        if (name.isBlank()) {
            nameError = "El nombre es requerido"
            isValid = false
        }

        if (email.isBlank()) {
            emailError = "El email es requerido"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = "Email inválido"
            isValid = false
        }

        if (password.isBlank()) {
            passwordError = "La contraseña es requerida"
            isValid = false
        } else if (password.length < 6) {
            passwordError = "La contraseña debe tener al menos 6 caracteres"
            isValid = false
        }

        if (confirmPassword.isBlank()) {
            confirmPasswordError = "Confirma tu contraseña"
            isValid = false
        } else if (password != confirmPassword) {
            confirmPasswordError = "Las contraseñas no coinciden"
            isValid = false
        }

        _uiState.value = _uiState.value.copy(
            nameError = nameError,
            emailError = emailError,
            passwordError = passwordError,
            confirmPasswordError = confirmPasswordError
        )

        return isValid
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearFieldErrors() {
        _uiState.value = _uiState.value.copy(
            nameError = null,
            emailError = null,
            passwordError = null,
            confirmPasswordError = null
        )
    }
    
    fun resetRegisterSuccess() {
        _uiState.value = _uiState.value.copy(isRegisterSuccess = false)
    }
}