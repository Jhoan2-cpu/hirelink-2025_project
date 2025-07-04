package com.example.hirelink_2025.models

data class JobAd(
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val empresa: String = "",
    val habilidades: List<String> = emptyList(),
    val fecha: String = "",
    val tipoEmpleo: String = "",
    val cargo: String = "",
    val modalidad: String = "",
    val estado: String = "",
    val telefono: String = "",
    val email: String = "",
    val cantidadVacantes: String = "",
    val salario: String = "",
    val ubicacion: String = ""
)