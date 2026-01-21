package com.example.hirelink_2025.viewmodels.ads

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdDetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AdDetailUiState())
    val uiState: StateFlow<AdDetailUiState> = _uiState.asStateFlow()

    fun loadAdData(arguments: Bundle?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                arguments?.let { args ->
                    val adData = AdData(
                        titulo = args.getString("titulo", "Sin título"),
                        descripcion = args.getString("descripcion", "Sin descripción"),
                        habilidades = args.getString("habilidades", "No especificadas"),
                        fecha = args.getString("fecha", "Sin fecha"),
                        tipoEmpleo = args.getString("tipo_empleo", "No especificado"),
                        cargo = args.getString("cargo", "No especificado"),
                        modalidad = args.getString("modalidad", "No especificada"),
                        estado = args.getString("estado", "Sin estado"),
                        telefono = args.getString("telefono", "+51 999 999 999"),
                        email = args.getString("email", "contacto@empresa.com"),
                        cantidadVacantes = args.getString("cantidad_vacantes", "1")
                    )

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        adData = adData
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al cargar los datos del anuncio"
                )
            }
        }
    }

    fun formatHabilidades(habilidades: String): String {
        return habilidades.split(",").joinToString("\n") { "• ${it.trim()}" }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}