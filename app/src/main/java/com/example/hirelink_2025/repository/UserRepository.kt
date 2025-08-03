package com.example.hirelink_2025.repository

import com.example.hirelink_2025.models.User
import com.example.hirelink_2025.models.UserProfile
import com.example.hirelink_2025.network.FirestoreService
import com.example.hirelink_2025.network.AuthCallback
import com.example.hirelink_2025.network.Callback
import com.example.hirelink_2025.network.VoidCallback
import com.example.hirelink_2025.network.ExistsCallback
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository para gestión de usuarios siguiendo patrón Repository
 * Centraliza todas las operaciones relacionadas con usuarios
 * Optimizado para Firestore NoSQL
 */
class UserRepository private constructor() {
    
    private val firestoreService = FirestoreService()
    
    // Cache local de usuario actual
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: Flow<User?> = _currentUser.asStateFlow()
    
    companion object {
        @Volatile
        private var INSTANCE: UserRepository? = null
        
        fun getInstance(): UserRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserRepository().also { INSTANCE = it }
            }
        }
    }
    
    // MARK: - Authentication Operations
    
    /**
     * Registrar nuevo usuario
     */
    fun registerUser(
        email: String,
        password: String,
        name: String,
        phone: String? = null,
        callback: AuthCallback
    ) {
        val userData = User(
            id = "", // Se asignará por Firebase Auth
            email = email.trim(),
            name = name.trim(),
            phone = phone?.trim(),
            profileImageUrl = null,
            active = true,
            createdAt = System.currentTimeMillis(),
            lastLoginAt = System.currentTimeMillis()
        )
        
        firestoreService.registerUser(email.trim(), password, userData, object : AuthCallback {
            override fun onSuccess(userId: String, isNewUser: Boolean) {
                val userWithId = userData.copy(id = userId)
                _currentUser.value = userWithId
                callback.onSuccess(userId, isNewUser)
            }
            
            override fun onError(exception: Exception) {
                callback.onError(exception)
            }
        })
    }
    
    /**
     * Login de usuario
     */
    fun loginUser(email: String, password: String, callback: Callback<User>) {
        firestoreService.loginUser(email, password, object : AuthCallback {
            override fun onSuccess(userId: String, isNewUser: Boolean) {
                // Obtener datos completos del usuario
                firestoreService.getUserById(userId, object : Callback<User?> {
                    override fun onSuccess(user: User?) {
                        if (user != null) {
                            // Actualizar lastLoginAt
                            val updatedUser = user.copy(lastLoginAt = System.currentTimeMillis())
                            updateUserLastLogin(userId)
                            _currentUser.value = updatedUser
                            callback.onSuccess(updatedUser)
                        } else {
                            callback.onError(Exception("Usuario no encontrado en base de datos"))
                        }
                    }
                    
                    override fun onError(exception: Exception) {
                        callback.onError(exception)
                    }
                })
            }
            
            override fun onError(exception: Exception) {
                callback.onError(exception)
            }
        })
    }
    
    /**
     * Cerrar sesión
     */
    fun signOut() {
        firestoreService.signOut()
        _currentUser.value = null
    }
    
    /**
     * Verificar estado de autenticación
     */
    fun checkAuthState(callback: Callback<User?>) {
        if (firestoreService.isUserAuthenticated()) {
            val userId = firestoreService.getCurrentUserId()
            userId?.let { id ->
                getUserById(id, callback)
            } ?: callback.onSuccess(null)
        } else {
            callback.onSuccess(null)
        }
    }
    
    /**
     * Resetear contraseña
     */
    fun resetPassword(email: String, callback: VoidCallback) {
        firestoreService.resetPassword(email, callback)
    }
    
    // MARK: - User Data Operations
    
    /**
     * Obtener usuario por ID
     */
    fun getUserById(userId: String, callback: Callback<User?>) {
        firestoreService.getUserById(userId, object : Callback<User?> {
            override fun onSuccess(user: User?) {
                if (user != null && userId == firestoreService.getCurrentUserId()) {
                    _currentUser.value = user
                }
                callback.onSuccess(user)
            }
            
            override fun onError(exception: Exception) {
                callback.onError(exception)
            }
        })
    }
    
    /**
     * Actualizar datos del usuario
     */
    fun updateUser(user: User, callback: VoidCallback) {
        firestoreService.updateUser(user, object : VoidCallback {
            override fun onSuccess() {
                // Actualizar cache local si es el usuario actual
                if (user.id == firestoreService.getCurrentUserId()) {
                    _currentUser.value = user
                }
                callback.onSuccess()
            }
            
            override fun onError(exception: Exception) {
                callback.onError(exception)
            }
        })
    }
    
    /**
     * Actualizar última vez que hizo login (interno)
     */
    private fun updateUserLastLogin(userId: String) {
        _currentUser.value?.let { currentUser ->
            val updatedUser = currentUser.copy(lastLoginAt = System.currentTimeMillis())
            firestoreService.updateUser(updatedUser, object : VoidCallback {
                override fun onSuccess() {
                    _currentUser.value = updatedUser
                }
                override fun onError(exception: Exception) {
                    // Error silencioso para esta operación interna
                }
            })
        }
    }
    
    /**
     * Verificar si usuario existe
     */
    fun checkUserExists(email: String, callback: ExistsCallback) {
        firestoreService.checkUserExists(email, callback)
    }
    
    // MARK: - User Profile Operations
    
    /**
     * Obtener perfil de usuario
     */
    fun getUserProfile(userId: String, callback: Callback<UserProfile?>) {
        firestoreService.getUserProfile(userId, callback)
    }
    
    /**
     * Crear o actualizar perfil de usuario
     */
    fun saveUserProfile(userProfile: UserProfile, callback: VoidCallback) {
        val profileWithTimestamp = userProfile.copy(lastUpdated = System.currentTimeMillis())
        firestoreService.saveUserProfile(profileWithTimestamp, callback)
    }
    
    // MARK: - Utility Methods
    
    /**
     * Obtener ID del usuario actual
     */
    fun getCurrentUserId(): String? {
        return firestoreService.getCurrentUserId()
    }
    
    /**
     * Verificar si hay usuario autenticado
     */
    fun isUserAuthenticated(): Boolean {
        return firestoreService.isUserAuthenticated()
    }
    
    /**
     * Testear conexión Firebase
     */
    fun testFirebaseConnection(callback: VoidCallback) {
        firestoreService.testFirebaseConnection(callback)
    }
    
    /**
     * Limpiar cache local
     */
    fun clearCache() {
        _currentUser.value = null
    }
}