package com.example.hirelink_2025.viewmodels

import androidx.lifecycle.ViewModel
import com.example.hirelink_2025.models.User
import com.example.hirelink_2025.network.FirestoreService
import com.example.hirelink_2025.network.AuthCallback
import com.example.hirelink_2025.network.Callback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LoginUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null,
    val isLoginSuccess: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val isResetPasswordSent: Boolean = false,
    val resetPasswordMessage: String? = null
)

class LoginViewModel : ViewModel() {

    private val firestoreService = FirestoreService()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        if (!validateInputs(email, password)) {
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        firestoreService.loginUser(email, password, object : AuthCallback {
            override fun onSuccess(userId: String, isNewUser: Boolean) {
                // Obtener datos completos del usuario
                firestoreService.getUserById(userId, object : Callback<User?> {
                    override fun onSuccess(user: User?) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            user = user,
                            isLoginSuccess = true,
                            error = null
                        )
                    }

                    override fun onError(exception: Exception) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Error al obtener datos del usuario: ${exception.message}"
                        )
                    }
                })
            }

            override fun onError(exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = getAuthErrorMessage(exception)
                )
            }
        })
    }

    /**
     * Recuperar contraseña
     */
    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _uiState.value = _uiState.value.copy(
                emailError = "El email es requerido para recuperar la contraseña"
            )
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = _uiState.value.copy(
                emailError = "Email inválido"
            )
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        firestoreService.resetPassword(email, object : com.example.hirelink_2025.network.VoidCallback {
            override fun onSuccess() {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = null,
                    isResetPasswordSent = true,
                    resetPasswordMessage = "Se ha enviado un enlace de recuperación a tu email"
                )
            }

            override fun onError(exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al enviar email de recuperación: ${exception.message}"
                )
            }
        })
    }

    /**
     * Verificar si hay usuario autenticado
     */
    fun checkAuthState() {
        if (firestoreService.isUserAuthenticated()) {
            val userId = firestoreService.getCurrentUserId()
            userId?.let { id ->
                firestoreService.getUserById(id, object : Callback<User?> {
                    override fun onSuccess(user: User?) {
                        if (user != null) {
                            _uiState.value = _uiState.value.copy(
                                user = user,
                                isLoginSuccess = true
                            )
                        }
                    }

                    override fun onError(exception: Exception) {
                        // Usuario autenticado pero sin datos en Firestore
                        // Podrías manejar este caso según sea necesario
                    }
                })
            }
        }
    }

    private fun getAuthErrorMessage(exception: Exception): String {
        return when {
            exception.message?.contains("user-not-found") == true -> 
                "Usuario no encontrado"
            exception.message?.contains("wrong-password") == true -> 
                "Contraseña incorrecta"
            exception.message?.contains("invalid-email") == true -> 
                "Email inválido"
            exception.message?.contains("user-disabled") == true -> 
                "Usuario deshabilitado"
            exception.message?.contains("too-many-requests") == true -> 
                "Demasiados intentos. Intenta más tarde"
            exception.message?.contains("network-request-failed") == true -> 
                "Error de conexión. Verifica tu internet"
            else -> exception.message ?: "Error de autenticación"
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        var isValid = true
        var emailError: String? = null
        var passwordError: String? = null

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

        _uiState.value = _uiState.value.copy(
            emailError = emailError,
            passwordError = passwordError
        )

        return isValid
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearFieldErrors() {
        _uiState.value = _uiState.value.copy(
            emailError = null,
            passwordError = null
        )
    }

    fun resetLoginSuccess() {
        _uiState.value = _uiState.value.copy(isLoginSuccess = false)
    }

    fun clearResetPasswordState() {
        _uiState.value = _uiState.value.copy(
            isResetPasswordSent = false,
            resetPasswordMessage = null
        )
    }
    
    /**
     * Cerrar sesión
     */
    fun signOut() {
        firestoreService.signOut()
        _uiState.value = LoginUiState() // Reset a estado inicial
    }
}