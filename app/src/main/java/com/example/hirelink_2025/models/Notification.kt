package com.example.hirelink_2025.models

data class Notification(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: NotificationType = NotificationType.INFO,
    val read: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val actionUrl: String = "", // URL o acción a realizar cuando se toca la notificación
    val relatedId: String = "", // ID del trabajo, aplicación, etc. relacionado
    val imageUrl: String = "" // URL de imagen opcional para la notificación
)

enum class NotificationType {
    INFO,           // Información general
    APPLICATION,    // Nueva aplicación recibida
    JOB_UPDATE,     // Actualización de trabajo
    MESSAGE,        // Mensaje de empresa
    REMINDER,       // Recordatorio
    SUCCESS,        // Éxito (aplicación aceptada)
    WARNING,        // Advertencia
    ERROR          // Error o problema
}