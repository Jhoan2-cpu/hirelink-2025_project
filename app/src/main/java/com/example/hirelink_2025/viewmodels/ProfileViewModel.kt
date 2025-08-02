package com.example.hirelink_2025.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hirelink_2025.models.User
import com.example.hirelink_2025.models.UserProfile
import com.example.hirelink_2025.repository.UserRepository
import com.example.hirelink_2025.network.Callback
import com.example.hirelink_2025.network.VoidCallback
import com.example.hirelink_2025.network.FirebaseStorageService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val userProfile: UserProfile? = null,
    val error: String? = null,
    val isProfileUpdated: Boolean = false,
    val isInitialLoad: Boolean = true
)

class ProfileViewModel(
    private val userRepository: UserRepository = UserRepository.getInstance(),
    private val storageService: FirebaseStorageService = FirebaseStorageService()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserData()
    }

    /**
     * Carga los datos del usuario y su perfil
     */
    fun loadUserData(forceRefresh: Boolean = false) {
        if (!forceRefresh && !_uiState.value.isInitialLoad) return

        _uiState.update { it.copy(isLoading = true, error = null) }

        userRepository.getCurrentUserId()?.let { userId ->
            // Carga en paralelo usuario y perfil
            loadUserAndProfile(userId)
        } ?: run {
            _uiState.update { it.copy(
                isLoading = false,
                error = "Usuario no autenticado",
                isInitialLoad = false
            ) }
        }
    }

    private fun loadUserAndProfile(userId: String) {
        viewModelScope.launch {
            // Carga datos básicos del usuario
            userRepository.getUserById(userId, object : Callback<User?> {
                override fun onSuccess(user: User?) {
                    user?.let {
                        // Carga perfil del usuario
                        userRepository.getUserProfile(userId, object : Callback<UserProfile?> {
                            override fun onSuccess(profile: UserProfile?) {
                                _uiState.update { current ->
                                    current.copy(
                                        isLoading = false,
                                        user = user,
                                        userProfile = profile ?: createDefaultProfile(userId),
                                        isInitialLoad = false,
                                        error = null
                                    )
                                }
                            }

                            override fun onError(exception: Exception) {
                                _uiState.update { current ->
                                    current.copy(
                                        isLoading = false,
                                        user = user,
                                        userProfile = createDefaultProfile(userId),
                                        isInitialLoad = false,
                                        error = "Perfil no encontrado, creado uno nuevo"
                                    )
                                }
                            }
                        })
                    } ?: run {
                        _uiState.update { it.copy(
                            isLoading = false,
                            error = "Usuario no encontrado",
                            isInitialLoad = false
                        ) }
                    }
                }

                override fun onError(exception: Exception) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "Error al cargar usuario: ${exception.message}",
                        isInitialLoad = false
                    ) }
                }
            })
        }
    }

    /**
     * Actualiza tanto el usuario como su perfil
     */
    fun updateProfile(user: User, profile: UserProfile) {
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            userRepository.updateUser(user, object : VoidCallback {
                override fun onSuccess() {
                    userRepository.saveUserProfile(profile, object : VoidCallback {
                        override fun onSuccess() {
                            _uiState.update { current ->
                                current.copy(
                                    isLoading = false,
                                    user = user,
                                    userProfile = profile,
                                    isProfileUpdated = true,
                                    error = null
                                )
                            }
                        }

                        override fun onError(exception: Exception) {
                            _uiState.update { current ->
                                current.copy(
                                    isLoading = false,
                                    error = "Error al guardar perfil: ${exception.message}"
                                )
                            }
                        }
                    })
                }

                override fun onError(exception: Exception) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "Error al actualizar usuario: ${exception.message}"
                    ) }
                }
            })
        }
    }

    /**
     * Actualiza solo el perfil del usuario
     */
    fun updateUserProfile(profile: UserProfile) {
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            userRepository.saveUserProfile(profile, object : VoidCallback {
                override fun onSuccess() {
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            userProfile = profile,
                            isProfileUpdated = true,
                            error = null
                        )
                    }
                }

                override fun onError(exception: Exception) {
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            error = "Error al actualizar perfil: ${exception.message}"
                        )
                    }
                }
            })
        }
    }

    /**
     * Crea un perfil por defecto
     */
    private fun createDefaultProfile(userId: String): UserProfile {
        return UserProfile(
            userId = userId,
            profession = null,
            bio = "",
            skills = emptyList(),
            experience = emptyList(),
            education = emptyList(),
            languages = emptyList(),
            location = "",
            availability = "",
            salaryExpectation = "",
            linkedinUrl = "",
            portfolioUrl = "",
            lastUpdated = System.currentTimeMillis()
        )
    }

    /**
     * Actualiza la imagen de perfil del usuario
     */
    fun updateProfileImage(imageUri: Uri) {
        val currentUser = getCurrentUser() ?: return
        
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            // Primero eliminar la imagen anterior si existe
            val currentImageUrl = currentUser.profileImageUrl
            if (!currentImageUrl.isNullOrEmpty()) {
                storageService.deleteProfileImage(currentImageUrl, object : VoidCallback {
                    override fun onSuccess() {
                        // Continue with upload
                    }
                    
                    override fun onError(exception: Exception) {
                        // Log error but continue with upload
                        android.util.Log.w("ProfileViewModel", "Failed to delete old profile image: ${exception.message}")
                    }
                })
            }
            
            // Subir nueva imagen
            storageService.uploadProfileImage(imageUri, currentUser.id, object : Callback<String> {
                override fun onSuccess(result: String) {
                    // Actualizar usuario con nueva URL de imagen
                    val updatedUser = currentUser.copy(profileImageUrl = result)
                    
                    userRepository.updateUser(updatedUser, object : VoidCallback {
                        override fun onSuccess() {
                            _uiState.update { current ->
                                current.copy(
                                    isLoading = false,
                                    user = updatedUser,
                                    isProfileUpdated = true,
                                    error = null
                                )
                            }
                        }
                        
                        override fun onError(exception: Exception) {
                            _uiState.update { current ->
                                current.copy(
                                    isLoading = false,
                                    error = "Error al actualizar perfil: ${exception.message}"
                                )
                            }
                        }
                    })
                }
                
                override fun onError(exception: Exception) {
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            error = "Error al subir imagen: ${exception.message}"
                        )
                    }
                }
            })
        }
    }

    /**
     * Manejo de sesión
     */
    fun signOut() {
        userRepository.signOut()
        _uiState.value = ProfileUiState(isInitialLoad = true)
    }

    /**
     * Manejo de errores
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun resetUpdateFlag() {
        _uiState.update { it.copy(isProfileUpdated = false) }
    }

    fun resetProfileUpdated() {
        _uiState.update { it.copy(isProfileUpdated = false) }
    }

    /**
     * Getter methods para acceso directo
     */
    fun getCurrentUser(): User? {
        return _uiState.value.user
    }

    fun getCurrentUserProfile(): UserProfile? {
        return _uiState.value.userProfile
    }
}