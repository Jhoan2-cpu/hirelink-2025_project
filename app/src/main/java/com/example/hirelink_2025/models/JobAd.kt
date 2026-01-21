package com.example.hirelink_2025.models

data class JobAd(
    val id: String,
    val titulo: String,
    val empresa: String,
    val ubicacion: String,
    val salario: String,
    val descripcion: String,
    val habilidades: List<String>,
    val modalidad: String,
    val fechaPublicacion: String, // Asegúrate de que esta propiedad existe
    val categoria: String = "",
    val experienciaRequerida: String = "",
    val tipoContrato: String = ""
)