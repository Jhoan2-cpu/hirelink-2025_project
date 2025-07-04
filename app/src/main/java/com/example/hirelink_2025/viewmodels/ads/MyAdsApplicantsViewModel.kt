package com.example.hirelink_2025.viewmodels.ads

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hirelink_2025.models.Applicant
import com.example.hirelink_2025.models.ApplicationStatus

class MyAdsApplicantsViewModel : ViewModel() {

    private val _applicants = MutableLiveData<List<Applicant>>()
    val applicants: LiveData<List<Applicant>> = _applicants

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isEmpty = MutableLiveData<Boolean>()
    val isEmpty: LiveData<Boolean> = _isEmpty

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun loadApplicants(jobId: String) {
        _isLoading.value = true

        // Simulando carga de datos (reemplazar con llamada real a API/BD)
        // En una implementación real, aquí harías la llamada a tu repository
        loadFakeApplicants(jobId)
    }

    private fun loadFakeApplicants(jobId: String) {
        // Simulando delay de red
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            val fakeApplicants = listOf(
                Applicant(
                    id = "1",
                    name = "María González",
                    email = "maria.gonzalez@email.com",
                    phone = "+51 987654321",
                    profession = "Desarrolladora Android",
                    experience = "3 años de experiencia",
                    skills = listOf("Kotlin", "Java", "Android Studio", "Git"),
                    applicationDate = "Hace 2 días",
                    status = ApplicationStatus.PENDING,
                    jobId = jobId,
                    coverLetter = "Estoy muy interesada en esta posición..."
                ),
                Applicant(
                    id = "2",
                    name = "Carlos Ruiz",
                    email = "carlos.ruiz@email.com",
                    phone = "+51 976543210",
                    profession = "Ingeniero de Software",
                    experience = "5 años de experiencia",
                    skills = listOf("Kotlin", "React Native", "Flutter", "Firebase"),
                    applicationDate = "Hace 1 día",
                    status = ApplicationStatus.PENDING,
                    jobId = jobId,
                    coverLetter = "Mi experiencia en desarrollo móvil..."
                ),
                Applicant(
                    id = "3",
                    name = "Ana Morales",
                    email = "ana.morales@email.com",
                    phone = "+51 965432109",
                    profession = "Desarrolladora Full Stack",
                    experience = "2 años de experiencia",
                    skills = listOf("Android", "iOS", "React", "Node.js"),
                    applicationDate = "Hace 4 días",
                    status = ApplicationStatus.ACCEPTED,
                    jobId = jobId,
                    coverLetter = "He trabajado en proyectos similares..."
                ),
                Applicant(
                    id = "4",
                    name = "Pedro Jiménez",
                    email = "pedro.jimenez@email.com",
                    phone = "+51 954321098",
                    profession = "Desarrollador Junior",
                    experience = "1 año de experiencia",
                    skills = listOf("Java", "Kotlin", "SQLite"),
                    applicationDate = "Hace 6 días",
                    status = ApplicationStatus.REJECTED,
                    jobId = jobId,
                    coverLetter = "Aunque soy junior, tengo muchas ganas..."
                )
            )

            _applicants.value = fakeApplicants
            _isEmpty.value = fakeApplicants.isEmpty()
            _isLoading.value = false
        }, 1000)
    }

    fun acceptApplicant(applicantId: String) {
        updateApplicantStatus(applicantId, ApplicationStatus.ACCEPTED)
    }

    fun rejectApplicant(applicantId: String) {
        updateApplicantStatus(applicantId, ApplicationStatus.REJECTED)
    }

    private fun updateApplicantStatus(applicantId: String, newStatus: ApplicationStatus) {
        val currentApplicants = _applicants.value?.toMutableList() ?: return
        val index = currentApplicants.indexOfFirst { it.id == applicantId }

        if (index != -1) {
            currentApplicants[index] = currentApplicants[index].copy(status = newStatus)
            _applicants.value = currentApplicants
        }
    }

    fun getPendingApplicants(): List<Applicant> {
        return _applicants.value?.filter { it.status == ApplicationStatus.PENDING } ?: emptyList()
    }

    fun getAcceptedApplicants(): List<Applicant> {
        return _applicants.value?.filter { it.status == ApplicationStatus.ACCEPTED } ?: emptyList()
    }

    fun getRejectedApplicants(): List<Applicant> {
        return _applicants.value?.filter { it.status == ApplicationStatus.REJECTED } ?: emptyList()
    }
}