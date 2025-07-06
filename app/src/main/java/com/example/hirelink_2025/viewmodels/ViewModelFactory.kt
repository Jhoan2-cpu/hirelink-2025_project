package com.example.hirelink_2025.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.hirelink_2025.repository.ApplicantsRepository
import com.example.hirelink_2025.repository.MockApplicantsRepository
import com.example.hirelink_2025.viewmodels.ads.MyAdsApplicantsViewModel

/**
 * Factory para crear ViewModels con dependencias
 * MVVM: Inyección de dependencias manual
 */
class ViewModelFactory : ViewModelProvider.Factory {

    // Repository instances - aquí cambiaremos a Firebase más adelante
    private val applicantsRepository: ApplicantsRepository = MockApplicantsRepository()

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MyAdsApplicantsViewModel::class.java) -> {
                MyAdsApplicantsViewModel(applicantsRepository) as T
            }
            // Agregar otros ViewModels aquí conforme los creemos
            else -> throw IllegalArgumentException("ViewModel class desconocida: ${modelClass.name}")
        }
    }
}