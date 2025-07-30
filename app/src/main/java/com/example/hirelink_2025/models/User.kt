package com.example.hirelink_2025.models

data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val phone: String? = null,
    val profileImageUrl: String? = null,
    val active: Boolean = true
)

/**
 * Modelo para credenciales de usuario (solo para desarrollo/fallback)
 * IMPORTANTE: En producción, las contraseñas se manejan por Firebase Auth
 */
data class UserCredentials(
    val userId: String = "",
    val email: String = "",
    val passwordHash: String = "", // Hash de la contraseña, nunca texto plano
    val salt: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val phone: String? = null
)