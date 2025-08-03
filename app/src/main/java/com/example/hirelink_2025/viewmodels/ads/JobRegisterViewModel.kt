package com.example.hirelink_2025.viewmodels.ads

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hirelink_2025.models.Company
import com.example.hirelink_2025.models.Job
import com.example.hirelink_2025.models.JobStatus
import com.example.hirelink_2025.network.Callback
import com.example.hirelink_2025.network.FirestoreService
import com.google.firebase.auth.FirebaseAuth

/**
 * ViewModel para el registro de trabajos/anuncios
 * Conectado directamente a FirestoreService
 */
class JobRegisterViewModel : ViewModel() {

    private val firestoreService = FirestoreService()
    private val auth = FirebaseAuth.getInstance()

    // Estado de las compañías del usuario
    private val _userCompanies = MutableLiveData<List<Company>>()
    val userCompanies: LiveData<List<Company>> = _userCompanies

    // Compañía seleccionada
    private val _selectedCompany = MutableLiveData<Company?>()
    val selectedCompany: LiveData<Company?> = _selectedCompany

    // Estados de UI
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _isSuccess = MutableLiveData<Boolean>()
    val isSuccess: LiveData<Boolean> = _isSuccess

    private val _validationErrors = MutableLiveData<List<String>>()
    val validationErrors: LiveData<List<String>> = _validationErrors

    init {
        loadUserCompanies()
    }

    /**
     * Carga las compañías del usuario actual
     */
    private fun loadUserCompanies() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d("JobRegisterViewModel", "Loading companies for user: ${currentUser.uid}")
            
            firestoreService.getCompaniesByOwner(currentUser.uid, object : Callback<List<Company>> {
                override fun onSuccess(result: List<Company>) {
                    Log.d("JobRegisterViewModel", "Successfully loaded ${result.size} companies")
                    _userCompanies.value = result
                }

                override fun onError(exception: Exception) {
                    Log.e("JobRegisterViewModel", "Error loading companies", exception)
                    _errorMessage.value = "Error al cargar compañías: ${exception.message}"
                }
            })
        } else {
            Log.w("JobRegisterViewModel", "No authenticated user found")
            _errorMessage.value = "Usuario no autenticado"
        }
    }

    /**
     * Selecciona una compañía
     */
    fun selectCompany(company: Company?) {
        Log.d("JobRegisterViewModel", "Company selected: ${company?.name}")
        _selectedCompany.value = company
    }

    /**
     * Registra un nuevo trabajo con los campos del modelo simplificado
     */
    fun registerJob(
        title: String,
        aboutCompany: String,
        aboutJob: String,
        skills: String,
        vacancies: Int,
        employmentType: String,
        modality: String,
        deadline: String,
        offerSalary: String,
        phone: String,
        email: String,
        website: String,
        selectedLocation: String = "Lima, Perú"
    ) {
        val currentUser = auth.currentUser
        val company = _selectedCompany.value

        if (currentUser == null) {
            _errorMessage.value = "Usuario no autenticado"
            return
        }

        if (company == null) {
            _errorMessage.value = "Debe seleccionar una compañía"
            return
        }

        // Validar campos requeridos
        val validationResult = validateJobData(
            title, aboutJob, skills, vacancies
        )
        if (validationResult.isNotEmpty()) {
            _validationErrors.value = validationResult
            return
        }

        Log.d("JobRegisterViewModel", "Registering job: $title for company: ${company.name}")
        _isLoading.value = true
        _errorMessage.value = ""
        _validationErrors.value = emptyList()

        // Crear objeto Job con campos del modelo simplificado
        val currentTime = System.currentTimeMillis()
        val job = Job(
            id = "", // Se generará automáticamente
            title = title.trim(),
            modality = modality.trim(),
            salary = offerSalary.trim(),
            requirements = parseRequirements(skills),
            postedDate = formatDate(currentTime),
            vacancies = vacancies,
            employmentType = employmentType.trim(),
            status = JobStatus.ACTIVE,
            companyId = company.id,
            createdAt = currentTime,
            updatedAt = currentTime,
            aboutJob = aboutJob.trim(),
            deadline = deadline.trim(),
            offerSalary = offerSalary.trim()
        )

        // Registrar en Firestore
        firestoreService.createJob(job, object : Callback<String> {
            override fun onSuccess(result: String) {
                Log.d("JobRegisterViewModel", "Job registered successfully with ID: $result")
                _isLoading.value = false
                _isSuccess.value = true
            }

            override fun onError(exception: Exception) {
                Log.e("JobRegisterViewModel", "Error registering job", exception)
                _isLoading.value = false
                _errorMessage.value = "Error al registrar el anuncio: ${exception.message}"
            }
        })
    }

    /**
     * Valida los datos del trabajo
     */
    private fun validateJobData(
        title: String,
        aboutJob: String,
        skills: String,
        vacancies: Int
    ): List<String> {
        val errors = mutableListOf<String>()

        if (title.isBlank()) {
            errors.add("El título del trabajo es requerido")
        } else if (title.length < 3) {
            errors.add("El título debe tener al menos 3 caracteres")
        }

        if (aboutJob.isBlank()) {
            errors.add("La descripción del empleo es requerida")
        } else if (aboutJob.length < 20) {
            errors.add("La descripción debe tener al menos 20 caracteres")
        }

        if (skills.isBlank()) {
            errors.add("Los requisitos son requeridos")
        }

        if (vacancies <= 0) {
            errors.add("El número de vacantes debe ser mayor a 0")
        }

        return errors
    }

    /**
     * Convierte string de habilidades a lista
     */
    private fun parseRequirements(skills: String): List<String> {
        return skills.split(",", "\n", ";")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }

    /**
     * Formatea timestamp a string de fecha
     */
    private fun formatDate(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }

    /**
     * Limpia errores
     */
    fun clearErrors() {
        _errorMessage.value = ""
        _validationErrors.value = emptyList()
    }

    /**
     * Resetea el estado de éxito
     */
    fun resetSuccess() {
        _isSuccess.value = false
    }

    /**
     * Recarga las compañías del usuario
     */
    fun refreshCompanies() {
        loadUserCompanies()
    }

    /**
     * Obtiene las opciones de modalidad
     */
    fun getModalityOptions(): List<String> {
        return listOf(
            "Presencial",
            "Remoto",
            "Híbrido"
        )
    }

    /**
     * Obtiene las opciones de tipo de empleo
     */
    fun getEmploymentTypeOptions(): List<String> {
        return listOf(
            "Tiempo Completo",
            "Medio Tiempo",
            "Por Horas",
            "Freelance",
            "Práctica Profesional"
        )
    }
}