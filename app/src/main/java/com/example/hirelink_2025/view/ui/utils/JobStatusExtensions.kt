package com.example.hirelink_2025.view.ui.utils

import android.R
import android.content.Context
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.hirelink_2025.models.JobStatus

/**
 * Extensiones MÍNIMAS para JobStatus - Solo para corregir el error
 * Versión simplificada que funciona con el JobStatus actual
 */

/**
 * Aplica el estilo de estado a un TextView (versión básica)
 */
fun TextView.applyJobStatus(status: JobStatus, context: Context) {
    // Corregido: Usa when en lugar de displayName
    text = when (status) {
        JobStatus.ACTIVE -> "Activo"
        JobStatus.CLOSED -> "Cerrado"
        JobStatus.DRAFT -> "Borrador"
        JobStatus.PENDING_REVIEW -> "Pendiente"
        JobStatus.PAUSED -> "Pausado"
        JobStatus.FULL -> "Lleno"
        JobStatus.EXPIRED -> "Expirado"
        JobStatus.REJECTED -> "Rechazado"
        JobStatus.SUSPENDED -> "Suspendido"
    }

    // Corregido: Usa when en lugar de statusColor
    setTextColor(when (status) {
        JobStatus.ACTIVE -> ContextCompat.getColor(context, R.color.holo_green_dark)
        JobStatus.CLOSED -> ContextCompat.getColor(context, R.color.darker_gray)
        JobStatus.DRAFT -> ContextCompat.getColor(context, R.color.holo_orange_dark)
        JobStatus.PENDING_REVIEW -> ContextCompat.getColor(context, R.color.holo_blue_dark)
        JobStatus.PAUSED -> ContextCompat.getColor(context, R.color.holo_purple)
        JobStatus.FULL -> ContextCompat.getColor(context, R.color.holo_red_dark)
        JobStatus.EXPIRED -> ContextCompat.getColor(context, R.color.holo_red_dark)
        JobStatus.REJECTED -> ContextCompat.getColor(context, R.color.holo_red_dark)
        JobStatus.SUSPENDED -> ContextCompat.getColor(context, R.color.black)
    })

    // Fondo básico
    setBackgroundColor(when (status) {
        JobStatus.ACTIVE -> ContextCompat.getColor(context, R.color.holo_green_light)
        JobStatus.CLOSED -> ContextCompat.getColor(context, R.color.darker_gray)
        JobStatus.DRAFT -> ContextCompat.getColor(context, R.color.holo_orange_light)
        else -> ContextCompat.getColor(context, R.color.transparent)
    })

    setPadding(16, 8, 16, 8)
}

/**
 * Obtiene el texto de estado con información adicional
 */
fun JobStatus.getDetailedStatusText(context: Context): String {
    return when (this) {
        JobStatus.ACTIVE -> "Activo • Recibiendo postulaciones"
        JobStatus.CLOSED -> "Cerrado • Proceso terminado"
        JobStatus.DRAFT -> "Borrador • Pendiente de publicación"
        JobStatus.PENDING_REVIEW -> "Pendiente • En revisión"
        JobStatus.PAUSED -> "Pausado • Temporalmente inactivo"
        JobStatus.FULL -> "Lleno • Límite alcanzado"
        JobStatus.EXPIRED -> "Expirado • Tiempo vencido"
        JobStatus.REJECTED -> "Rechazado • Requiere corrección"
        JobStatus.SUSPENDED -> "Suspendido • Contactar soporte"
    }
}

/**
 * Obtiene el emoji del estado
 */
fun JobStatus.getStatusEmoji(): String {
    return when (this) {
        JobStatus.ACTIVE -> "✅"
        JobStatus.CLOSED -> "🔒"
        JobStatus.DRAFT -> "📝"
        JobStatus.PENDING_REVIEW -> "⏳"
        JobStatus.PAUSED -> "⏸️"
        JobStatus.FULL -> "📊"
        JobStatus.EXPIRED -> "⏰"
        JobStatus.REJECTED -> "❌"
        JobStatus.SUSPENDED -> "🚫"
    }
}

/**
 * Obtiene el mensaje de acción recomendada
 */
fun JobStatus.getRecommendedAction(context: Context): String {
    return when (this) {
        JobStatus.DRAFT -> "Completa la información y publica tu anuncio"
        JobStatus.PENDING_REVIEW -> "Espera la aprobación de nuestro equipo"
        JobStatus.ACTIVE -> "Revisa las nuevas postulaciones"
        JobStatus.PAUSED -> "Reactiva tu anuncio para recibir postulaciones"
        JobStatus.FULL -> "Revisa los candidatos y cierra el proceso"
        JobStatus.CLOSED -> "Puedes reactivar o crear un nuevo anuncio"
        JobStatus.EXPIRED -> "Renueva tu anuncio para volverlo a publicar"
        JobStatus.REJECTED -> "Revisa los comentarios y corrige tu anuncio"
        JobStatus.SUSPENDED -> "Contacta con soporte para resolver el problema"
    }
}

/**
 * Verifica si el estado permite mostrar estadísticas
 */
fun JobStatus.canShowStatistics(): Boolean {
    return this in listOf(JobStatus.ACTIVE, JobStatus.PAUSED, JobStatus.FULL, JobStatus.CLOSED)
}

/**
 * Verifica si el estado requiere atención del usuario
 */
fun JobStatus.requiresAttention(): Boolean {
    return this in listOf(JobStatus.DRAFT, JobStatus.REJECTED, JobStatus.EXPIRED, JobStatus.SUSPENDED)
}