package com.example.hirelink_2025.viewmodels.ads

data class AdDetailUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val adData: AdData? = null
)

data class AdData(
    val titulo: String,
    val descripcion: String,
    val habilidades: String,
    val fecha: String,
    val tipoEmpleo: String,
    val cargo: String,
    val modalidad: String,
    val estado: String,
    val telefono: String,
    val email: String,
    val cantidadVacantes: String
)