package com.example.hirelink_2025.repository

import com.example.hirelink_2025.models.User
import com.example.hirelink_2025.models.LoginRequest
import com.example.hirelink_2025.models.RegisterRequest
import kotlinx.coroutines.delay

class AuthRepository {

    suspend fun login(loginRequest: LoginRequest): User {
        delay(1500)

        if (loginRequest.email == "admin@test.com" && loginRequest.password == "123456") {
            val user = User(
                id = "1",
                email = loginRequest.email,
                name = "Usuario Demo",
                phone = "+51 999 888 777"
            )

            // AGREGAR: Guardar sesión
            saveUserSession(user)

            return user
        } else {
            throw Exception("Credenciales incorrectas")
        }
    }

    suspend fun register(registerRequest: RegisterRequest): User {
        delay(2000)

        if (registerRequest.email == "admin@test.com") {
            throw Exception("El email ya está registrado")
        }

        val user = User(
            id = "2",
            email = registerRequest.email,
            name = registerRequest.name,
            phone = registerRequest.phone
        )

        // AGREGAR: Guardar sesión
        saveUserSession(user)

        return user
    }

    // AGREGAR ESTOS MÉTODOS
    private fun saveUserSession(user: User) {
        // En una app real, aquí guardarías el token JWT
        // Por ahora simulamos con SharedPreferences
    }

    fun isUserLoggedIn(): Boolean {
        // Verificar si hay token válido
        return false // Por defecto
    }

    suspend fun logout() {
        delay(500)
        // Limpiar SharedPreferences o token
    }
}