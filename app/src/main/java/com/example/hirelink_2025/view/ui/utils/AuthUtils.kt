package com.example.hirelink_2025.view.ui.utils

import android.content.Context
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object AuthUtils {
    
    /**
     * Mostrar diálogo de confirmación para recuperar contraseña
     */
    fun showPasswordResetDialog(
        context: Context,
        email: String,
        onConfirm: () -> Unit
    ) {
        MaterialAlertDialogBuilder(context)
            .setTitle("Recuperar contraseña")
            .setMessage("¿Enviar enlace de recuperación a $email?")
            .setPositiveButton("Enviar") { _, _ -> 
                onConfirm()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    /**
     * Mostrar mensaje de éxito con acción
     */
    fun showSuccessMessage(context: Context, message: String) {
        Toast.makeText(context, "✅ $message", Toast.LENGTH_LONG).show()
    }
    
    /**
     * Mostrar mensaje de error con formato
     */
    fun showErrorMessage(context: Context, message: String) {
        Toast.makeText(context, "❌ $message", Toast.LENGTH_LONG).show()
    }
    
    /**
     * Validar fortaleza de contraseña
     */
    fun getPasswordStrength(password: String): PasswordStrength {
        return when {
            password.length < 6 -> PasswordStrength.WEAK
            password.length < 8 -> PasswordStrength.MEDIUM
            password.matches(Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$")) -> 
                PasswordStrength.STRONG
            else -> PasswordStrength.MEDIUM
        }
    }
    
    enum class PasswordStrength(val message: String, val color: Int) {
        WEAK("Contraseña débil", android.R.color.holo_red_light),
        MEDIUM("Contraseña aceptable", android.R.color.holo_orange_light),
        STRONG("Contraseña segura", android.R.color.holo_green_light)
    }
}