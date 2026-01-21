package com.example.hirelink_2025.models

/**
 * Modelo para representar una ubicación de empresa en el mapa
 */
data class CompanyLocation(
    val company: Company,
    val latitude: Double,
    val longitude: Double,
    val activeJobsCount: Int,
    val distance: Double = 0.0 // Distancia desde la ubicación del usuario en km
) {
    /**
     * Verifica si esta ubicación está cerca del usuario (dentro de 10km)
     */
    fun isNearby(): Boolean = distance <= 10.0
    
    /**
     * Obtiene el título para mostrar en el marcador
     */
    fun getMarkerTitle(): String = company.name
    
    /**
     * Obtiene la descripción para mostrar en el marcador
     */
    fun getMarkerSnippet(): String = "$activeJobsCount ofertas activas"
}