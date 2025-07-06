package com.example.hirelink_2025.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hirelink_2025.models.RegisterRequest
import com.example.hirelink_2025.models.User
import com.example.hirelink_2025.repository.AuthRepository
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

    private val authRepository = AuthRepository()

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun register(name: String, email: String, password: String, confirmPassword: String, phone: String?) {
        if (!validateInputs(name, email, password, confirmPassword)) {
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            try {
                val registerRequest = RegisterRequest(
                    name = name.trim(),
                    email = email.trim(),
                    password = password,
                    phone = phone?.trim()
                )
                val user = authRepository.register(registerRequest)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    user = user,
                    isRegisterSuccess = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error desconocido"
                )
            }
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
}