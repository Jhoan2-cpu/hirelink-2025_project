package com.example.hirelink_2025.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.hirelink_2025.repository.ApplicantsRepository
import com.example.hirelink_2025.repository.MockApplicantsRepository
import com.example.hirelink_2025.viewmodels.ads.MyAdsApplicantsViewModel

/**
 * Factory para crear ViewModels con dependencias
 * MVVM: Inyección de dependencias manual - Company ViewModels usan FirestoreService directamente
 */
class ViewModelFactory : ViewModelProvider.Factory {

    // Repository instances (solo para ViewModels que aún usan repository)
    private val applicantsRepository: ApplicantsRepository = MockApplicantsRepository()

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MyAdsApplicantsViewModel::class.java) -> {
                MyAdsApplicantsViewModel(applicantsRepository) as T
            }
            modelClass.isAssignableFrom(CompanyViewModel::class.java) -> {
                CompanyViewModel() as T
            }
            modelClass.isAssignableFrom(CompanyRegisterViewModel::class.java) -> {
                CompanyRegisterViewModel() as T
            }
            // Agregar otros ViewModels aquí conforme los creemos
            else -> throw IllegalArgumentException("ViewModel class desconocida: ${modelClass.name}")
        }
    }
}