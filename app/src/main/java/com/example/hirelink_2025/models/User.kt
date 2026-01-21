package com.example.hirelink_2025.models

data class User(
    val id: String = "",
    val email: String = "",//1 2
    val name: String = "",// Cambiado de name a fullName
    val phone: String? = null,//1 2
    val profileImageUrl: String? = null,//1
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis()
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