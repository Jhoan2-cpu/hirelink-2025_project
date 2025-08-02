package com.example.hirelink_2025.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.hirelink_2025.repository.ApplicantsRepository
import com.example.hirelink_2025.repository.MockApplicantsRepository
import com.example.hirelink_2025.repository.UserRepository
import com.example.hirelink_2025.viewmodels.ads.MyAdsApplicantsViewModel
import com.example.hirelink_2025.viewmodels.ads.MyAdsViewModel
import com.example.hirelink_2025.viewmodels.ads.JobRegisterViewModel

/**
 * Factory para crear ViewModels con dependencias
 * MVVM: Inyección de dependencias manual - Company ViewModels usan FirestoreService directamente
 */
class ViewModelFactory : ViewModelProvider.Factory {

    // Repositorios
    // Usamos el UserRepository existente
    private val userRepository: UserRepository = UserRepository.getInstance()
    private val applicantsRepository: ApplicantsRepository = MockApplicantsRepository()

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MyAdsApplicantsViewModel::class.java) -> {
                MyAdsApplicantsViewModel(applicantsRepository) as T
            }
            modelClass.isAssignableFrom(MyAdsViewModel::class.java) -> {
                MyAdsViewModel() as T
            }
            modelClass.isAssignableFrom(CompanyViewModel::class.java) -> {
                CompanyViewModel() as T
            }
            modelClass.isAssignableFrom(CompanyRegisterViewModel::class.java) -> {
                CompanyRegisterViewModel() as T
            }
            modelClass.isAssignableFrom(JobRegisterViewModel::class.java) -> {
                JobRegisterViewModel() as T
            }
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(userRepository) as T  // Pasamos UserRepository
            }
            modelClass.isAssignableFrom(ApplicationsViewModel::class.java) -> {
                ApplicationsViewModel() as T
            }
            // Agregar otros ViewModels aquí conforme los creemos
            else -> throw IllegalArgumentException("ViewModel class desconocida: ${modelClass.name}")
        }
    }
}