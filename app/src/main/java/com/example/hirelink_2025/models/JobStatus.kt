package com.example.hirelink_2025.models

/**
 * Enum para representar los diferentes estados de una oferta laboral
 * Compatible con versiones básicas y avanzadas
 */
enum class JobStatus {
    // Estados básicos (siempre presentes)
    ACTIVE,
    CLOSED,
    DRAFT,

    // Estados avanzados (opcionales)
    PENDING_REVIEW,
    PAUSED,
    FULL,
    EXPIRED,
    REJECTED,
    SUSPENDED;

    companion object {
        /**
         * Obtiene todos los estados activos (que pueden recibir postulaciones)
         */
        fun getActiveStatuses(): List<JobStatus> {
            return listOf(ACTIVE, FULL)
        }

        /**
         * Obtiene todos los estados editables
         */
        fun getEditableStatuses(): List<JobStatus> {
            return listOf(DRAFT, ACTIVE, PAUSED, REJECTED)
        }

        /**
         * Obtiene todos los estados eliminables
         */
        fun getDeletableStatuses(): List<JobStatus> {
            return listOf(DRAFT, CLOSED, EXPIRED, REJECTED)
        }

        /**
         * Obtiene estado por nombre (útil para parsear desde API)
         */
        fun fromString(value: String): JobStatus? {
            return values().find { it.name.equals(value, ignoreCase = true) }
        }
    }

    /**
     * Verifica si es posible transicionar a otro estado
     */
    fun canTransitionTo(newStatus: JobStatus): Boolean {
        return when (this) {
            DRAFT -> newStatus in listOf(PENDING_REVIEW, ACTIVE, PAUSED)
            PENDING_REVIEW -> newStatus in listOf(ACTIVE, REJECTED, DRAFT)
            ACTIVE -> newStatus in listOf(PAUSED, FULL, CLOSED, EXPIRED, SUSPENDED)
            PAUSED -> newStatus in listOf(ACTIVE, CLOSED, EXPIRED, DRAFT)
            FULL -> newStatus in listOf(CLOSED, ACTIVE)
            CLOSED -> newStatus in listOf(ACTIVE, DRAFT) // Reactivar
            EXPIRED -> newStatus in listOf(ACTIVE, DRAFT) // Renovar
            REJECTED -> newStatus in listOf(DRAFT)
            SUSPENDED -> false // Solo admin puede cambiar
        }
    }

    /**
     * Obtiene las transiciones válidas desde este estado
     */
    fun getValidTransitions(): List<JobStatus> {
        return values().filter { canTransitionTo(it) }
    }

    /**
     * Verifica si el estado es "final" (no puede cambiar)
     */
    fun isFinalStatus(): Boolean {
        return this in listOf(SUSPENDED, CLOSED, EXPIRED, REJECTED)
    }

    /**
     * Verifica si el estado es "público" (visible para usuarios)
     */
    fun isPublicStatus(): Boolean {
        return this in listOf(ACTIVE, FULL, CLOSED, EXPIRED)
    }

    /**
     * Verifica si el estado puede recibir postulaciones
     */
    fun canReceiveApplications(): Boolean {
        return this in listOf(ACTIVE, FULL)
    }

    /**
     * Verifica si el estado puede editarse
     */
    fun canEdit(): Boolean {
        return this in listOf(DRAFT, ACTIVE, PAUSED, REJECTED)
    }

    /**
     * Verifica si el estado puede eliminarse
     */
    fun canDelete(): Boolean {
        return this in listOf(DRAFT, CLOSED, EXPIRED, REJECTED)
    }

    /**
     * Verifica si el estado requiere atención del usuario
     */
    fun requiresAttention(): Boolean {
        return this in listOf(DRAFT, REJECTED, EXPIRED, SUSPENDED)
    }

    /**
     * Obtiene el nivel de urgencia (0-3)
     */
    fun getUrgencyLevel(): Int {
        return when (this) {
            SUSPENDED -> 3 // Crítico
            REJECTED -> 2 // Alto
            EXPIRED -> 2 // Alto
            DRAFT -> 1 // Medio
            PENDING_REVIEW -> 1 // Medio
            FULL -> 1 // Medio
            else -> 0 // Bajo
        }
    }

    /**
     * Obtiene el texto de estado en español
     */
    fun getDisplayText(): String {
        return when (this) {
            ACTIVE -> "Activo"
            CLOSED -> "Cerrado"
            DRAFT -> "Borrador"
            PENDING_REVIEW -> "Pendiente de Revisión"
            PAUSED -> "Pausado"
            FULL -> "Lleno"
            EXPIRED -> "Expirado"
            REJECTED -> "Rechazado"
            SUSPENDED -> "Suspendido"
        }
    }

    /**
     * Obtiene el texto de estado detallado
     */
    fun getDetailedText(): String {
        return when (this) {
            ACTIVE -> "Activo • Recibiendo postulaciones"
            CLOSED -> "Cerrado • Proceso terminado"
            DRAFT -> "Borrador • Pendiente de publicación"
            PENDING_REVIEW -> "Pendiente • En revisión"
            PAUSED -> "Pausado • Temporalmente inactivo"
            FULL -> "Lleno • Límite alcanzado"
            EXPIRED -> "Expirado • Tiempo vencido"
            REJECTED -> "Rechazado • Requiere corrección"
            SUSPENDED -> "Suspendido • Contactar soporte"
        }
    }

    /**
     * Obtiene el emoji asociado al estado
     */
    fun getEmoji(): String {
        return when (this) {
            ACTIVE -> "✅"
            CLOSED -> "🔒"
            DRAFT -> "📝"
            PENDING_REVIEW -> "⏳"
            PAUSED -> "⏸️"
            FULL -> "📊"
            EXPIRED -> "⏰"
            REJECTED -> "❌"
            SUSPENDED -> "🚫"
        }
    }

    /**
     * Obtiene el progreso como porcentaje
     */
    fun getProgressPercentage(): Int {
        return when (this) {
            DRAFT -> 20
            PENDING_REVIEW -> 40
            ACTIVE -> 80
            PAUSED -> 60
            FULL -> 90
            CLOSED -> 100
            EXPIRED -> 100
            REJECTED -> 10
            SUSPENDED -> 0
        }
    }

    /**
     * Obtiene el color asociado al estado (nombre de color)
     */
    fun getColorName(): String {
        return when (this) {
            ACTIVE -> "success"
            CLOSED -> "secondary"
            DRAFT -> "warning"
            PENDING_REVIEW -> "info"
            PAUSED -> "purple"
            FULL -> "orange"
            EXPIRED -> "error"
            REJECTED -> "error"
            SUSPENDED -> "dark_red"
        }
    }
}