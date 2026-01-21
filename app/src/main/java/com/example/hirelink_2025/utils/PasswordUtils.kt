package com.example.hirelink_2025.utils

import java.security.MessageDigest
import java.security.SecureRandom
import kotlin.random.Random

/**
 * Utilidades para manejo seguro de contraseñas
 * IMPORTANTE: Solo para método fallback de desarrollo
 */
object PasswordUtils {
    
    /**
     * Genera un salt aleatorio
     */
    fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return salt.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Crear hash de contraseña con salt
     */
    fun hashPassword(password: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val saltedPassword = password + salt
        val hashedBytes = md.digest(saltedPassword.toByteArray())
        return hashedBytes.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Verificar contraseña
     */
    fun verifyPassword(password: String, salt: String, storedHash: String): Boolean {
        val computedHash = hashPassword(password, salt)
        return computedHash == storedHash
    }
    
    /**
     * Validar fortaleza de contraseña
     */
    fun validatePasswordStrength(password: String): PasswordValidation {
        return when {
            password.length < 6 -> PasswordValidation.TOO_SHORT
            password.isBlank() -> PasswordValidation.EMPTY
            else -> PasswordValidation.VALID
        }
    }
    
    enum class PasswordValidation(val message: String) {
        VALID("Contraseña válida"),
        TOO_SHORT("La contraseña debe tener al menos 6 caracteres"),
        EMPTY("La contraseña no puede estar vacía")
    }
}