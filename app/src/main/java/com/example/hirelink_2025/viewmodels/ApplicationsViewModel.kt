package com.example.hirelink_2025.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.hirelink_2025.models.Application
import com.example.hirelink_2025.models.ApplicationStatus
import com.example.hirelink_2025.models.User
import com.example.hirelink_2025.models.UserProfile
import com.example.hirelink_2025.models.Company
import com.example.hirelink_2025.network.Callback
import com.example.hirelink_2025.network.FirestoreService
import com.example.hirelink_2025.network.VoidCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel para manejo de postulaciones/aplicaciones
 */
class ApplicationsViewModel : ViewModel() {

    private val firestoreService = FirestoreService()

    // Lista de postulaciones
    private val _applications = MutableStateFlow<List<Application>>(emptyList())
    val applications: StateFlow<List<Application>> = _applications.asStateFlow()

    // Estados de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Errores
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Cache de información de usuarios y empresas
    private val _usersCache = MutableStateFlow<Map<String, User>>(emptyMap())
    val usersCache: StateFlow<Map<String, User>> = _usersCache.asStateFlow()
    
    private val _userProfilesCache = MutableStateFlow<Map<String, UserProfile>>(emptyMap())
    val userProfilesCache: StateFlow<Map<String, UserProfile>> = _userProfilesCache.asStateFlow()
    
    private val _companiesCache = MutableStateFlow<Map<String, Company>>(emptyMap())
    val companiesCache: StateFlow<Map<String, Company>> = _companiesCache.asStateFlow()
    
    private val _jobsCache = MutableStateFlow<Map<String, com.example.hirelink_2025.models.Job>>(emptyMap())
    val jobsCache: StateFlow<Map<String, com.example.hirelink_2025.models.Job>> = _jobsCache.asStateFlow()

    // Estado para operaciones
    private val _operationResult = MutableStateFlow<String?>(null)
    val operationResult: StateFlow<String?> = _operationResult.asStateFlow()

    init {
        Log.d("ApplicationsViewModel", "ViewModel initialized")
    }

    /**
     * Cargar postulaciones para un trabajo específico
     */
    fun loadApplicationsForJob(jobId: String) {
        Log.d("ApplicationsViewModel", "Loading applications for job: $jobId")
        _isLoading.value = true
        _error.value = null
        
        firestoreService.getApplicationsByJobId(jobId, object : Callback<List<Application>> {
            override fun onSuccess(result: List<Application>) {
                Log.d("ApplicationsViewModel", "Successfully loaded ${result.size} applications")
                _applications.value = result
                _isLoading.value = false
                
                // Cargar información adicional de los usuarios
                loadUsersInfoForApplications(result)
            }

            override fun onError(exception: Exception) {
                Log.e("ApplicationsViewModel", "Error loading applications", exception)
                _error.value = "Error al cargar las postulaciones: ${exception.message}"
                _isLoading.value = false
            }
        })
    }

    /**
     * Cargar información de usuarios para las postulaciones
     */
    private fun loadUsersInfoForApplications(applications: List<Application>) {
        val userIds = applications.map { it.applicantId }.distinct()
        
        userIds.forEach { userId ->
            loadUserInfo(userId)
            loadUserProfile(userId)
        }
    }

    /**
     * Cargar información básica de un usuario
     */
    private fun loadUserInfo(userId: String) {
        firestoreService.getUserById(userId, object : Callback<User?> {
            override fun onSuccess(result: User?) {
                result?.let { user ->
                    val currentCache = _usersCache.value.toMutableMap()
                    currentCache[userId] = user
                    _usersCache.value = currentCache
                }
            }

            override fun onError(exception: Exception) {
                Log.e("ApplicationsViewModel", "Error loading user info: $userId", exception)
            }
        })
    }

    /**
     * Cargar perfil de un usuario
     */
    private fun loadUserProfile(userId: String) {
        firestoreService.getUserProfile(userId, object : Callback<UserProfile?> {
            override fun onSuccess(result: UserProfile?) {
                result?.let { profile ->
                    val currentCache = _userProfilesCache.value.toMutableMap()
                    currentCache[userId] = profile
                    _userProfilesCache.value = currentCache
                }
            }

            override fun onError(exception: Exception) {
                Log.e("ApplicationsViewModel", "Error loading user profile: $userId", exception)
            }
        })
    }

    /**
     * Actualizar estado de una postulación
     */
    fun updateApplicationStatus(applicationId: String, status: ApplicationStatus, callback: (Boolean) -> Unit) {
        Log.d("ApplicationsViewModel", "Updating application $applicationId status to $status")
        
        firestoreService.updateJobApplicationStatus(applicationId, status, object : VoidCallback {
            override fun onSuccess() {
                Log.d("ApplicationsViewModel", "Application status updated successfully")
                _operationResult.value = "Estado actualizado correctamente"
                
                // Actualizar la lista local
                val updatedApplications = _applications.value.map { app ->
                    if (app.applicationId == applicationId) {
                        app.copy(status = status)
                    } else {
                        app
                    }
                }
                _applications.value = updatedApplications
                
                callback(true)
            }

            override fun onError(exception: Exception) {
                Log.e("ApplicationsViewModel", "Error updating application status", exception)
                _operationResult.value = "Error al actualizar el estado: ${exception.message}"
                callback(false)
            }
        })
    }

    /**
     * Obtener información de usuario desde cache
     */
    fun getUserInfo(userId: String): User? {
        return _usersCache.value[userId]
    }

    /**
     * Obtener perfil de usuario desde cache
     */
    fun getUserProfile(userId: String): UserProfile? {
        return _userProfilesCache.value[userId]
    }

    /**
     * Obtener información de empresa desde cache
     */
    fun getCompanyInfo(companyId: String): Company? {
        return _companiesCache.value[companyId]
    }

    /**
     * Cargar información de una empresa y almacenarla en cache
     */
    fun loadCompanyInfo(companyId: String) {
        if (_companiesCache.value.containsKey(companyId)) return // Ya está en cache
        
        firestoreService.getCompanyById(companyId, object : Callback<Company?> {
            override fun onSuccess(result: Company?) {
                result?.let { company ->
                    val currentCache = _companiesCache.value.toMutableMap()
                    currentCache[companyId] = company
                    _companiesCache.value = currentCache
                }
            }

            override fun onError(exception: Exception) {
                Log.e("ApplicationsViewModel", "Error loading company info: $companyId", exception)
            }
        })
    }

    /**
     * Contar postulaciones para un trabajo
     */
    fun getApplicationsCount(jobId: String, callback: (Int) -> Unit) {
        firestoreService.countApplicationsByJobId(jobId, callback)
    }

    /**
     * Limpiar mensaje de resultado de operación
     */
    fun clearOperationResult() {
        _operationResult.value = null
    }

    /**
     * Limpiar error
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Refrescar postulaciones para un trabajo específico
     */
    fun refreshApplications(jobId: String) {
        loadApplicationsForJob(jobId)
    }
    
    /**
     * Refrescar postulaciones del usuario
     */
    fun refreshUserApplications() {
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid
        
        if (userId != null) {
            Log.d("ApplicationsViewModel", "Refreshing user applications - clearing all caches")
            
            // Limpiar todos los caches para forzar una recarga completa
            _applications.value = emptyList()
            _jobsCache.value = emptyMap()
            _companiesCache.value = emptyMap()
            
            // Recargar datos
            loadApplicationsForUser(userId)
        } else {
            Log.w("ApplicationsViewModel", "Cannot refresh: no authenticated user")
            _error.value = "Error: Usuario no autenticado"
            _isLoading.value = false
        }
    }
    /**
     * Cargar todas las postulaciones de un usuario específico
     */
    fun loadApplicationsForUser(userId: String) {
        Log.d("ApplicationsViewModel", "Loading applications for user: $userId")
        _isLoading.value = true
        _error.value = null
        
        // Limpiar caches previos para datos frescos
        _jobsCache.value = emptyMap()
        _companiesCache.value = emptyMap()
        
        firestoreService.getApplicationsByUserId(userId, object : Callback<List<Application>> {
            override fun onSuccess(result: List<Application>) {
                Log.d("ApplicationsViewModel", "Successfully loaded ${result.size} user applications")
                
                if (result.isNotEmpty()) {
                    // Cargar información de trabajos y compañías ANTES de establecer las aplicaciones
                    loadCompleteDataForApplications(result)
                } else {
                    // Si no hay aplicaciones, establecer lista vacía inmediatamente
                    _applications.value = result
                    _isLoading.value = false
                }
            }

            override fun onError(exception: Exception) {
                Log.e("ApplicationsViewModel", "Error loading user applications", exception)
                _error.value = "Error al cargar mis postulaciones: ${exception.message}"
                _isLoading.value = false
            }
        })
    }
    
    /**
     * Cargar información completa (jobs + compañías) para las postulaciones del usuario
     * Solo establece las aplicaciones cuando TODOS los datos están cargados
     */
    private fun loadCompleteDataForApplications(applications: List<Application>) {
        val jobIds = applications.map { it.jobId }.distinct()
        val jobsMap = mutableMapOf<String, com.example.hirelink_2025.models.Job>()
        val companiesMap = mutableMapOf<String, com.example.hirelink_2025.models.Company>()
        
        var jobsLoaded = 0
        val totalJobs = jobIds.size
        
        Log.d("ApplicationsViewModel", "Loading complete data for ${applications.size} applications (${totalJobs} unique jobs)")
        
        if (totalJobs == 0) {
            _applications.value = applications
            _isLoading.value = false
            return
        }
        
        jobIds.forEach { jobId ->
            firestoreService.getJobById(jobId, object : Callback<com.example.hirelink_2025.models.Job?> {
                override fun onSuccess(job: com.example.hirelink_2025.models.Job?) {
                    job?.let { 
                        jobsMap[jobId] = it
                        
                        // Cargar información de la compañía para este job
                        if (it.companyId.isNotEmpty()) {
                            firestoreService.getCompanyById(it.companyId, object : Callback<com.example.hirelink_2025.models.Company?> {
                                override fun onSuccess(company: com.example.hirelink_2025.models.Company?) {
                                    company?.let { companiesMap[it.id] = it }
                                    
                                    jobsLoaded++
                                    checkAndFinishLoading(applications, jobsLoaded, totalJobs, jobsMap, companiesMap)
                                }
                                
                                override fun onError(exception: Exception) {
                                    Log.e("ApplicationsViewModel", "Error loading company for job $jobId", exception)
                                    jobsLoaded++
                                    checkAndFinishLoading(applications, jobsLoaded, totalJobs, jobsMap, companiesMap)
                                }
                            })
                        } else {
                            jobsLoaded++
                            checkAndFinishLoading(applications, jobsLoaded, totalJobs, jobsMap, companiesMap)
                        }
                    } ?: run {
                        jobsLoaded++
                        checkAndFinishLoading(applications, jobsLoaded, totalJobs, jobsMap, companiesMap)
                    }
                }
                
                override fun onError(exception: Exception) {
                    Log.e("ApplicationsViewModel", "Error loading job $jobId", exception)
                    jobsLoaded++
                    checkAndFinishLoading(applications, jobsLoaded, totalJobs, jobsMap, companiesMap)
                }
            })
        }
    }
    
    /**
     * Verificar si se completó la carga y finalizar
     */
    private fun checkAndFinishLoading(
        applications: List<Application>,
        jobsLoaded: Int,
        totalJobs: Int,
        jobsMap: Map<String, com.example.hirelink_2025.models.Job>,
        companiesMap: Map<String, com.example.hirelink_2025.models.Company>
    ) {
        if (jobsLoaded >= totalJobs) {
            Log.d("ApplicationsViewModel", "Finished loading: ${jobsMap.size} jobs, ${companiesMap.size} companies")
            
            // Actualizar caches
            _jobsCache.value = jobsMap
            _companiesCache.value = companiesMap
            
            // Finalmente establecer las aplicaciones (esto trigger los observers)
            _applications.value = applications
            _isLoading.value = false
        }
    }
    
    /**
     * Cargar información de un trabajo
     */
    private fun loadJobInfo(jobId: String) {
        firestoreService.getJobById(jobId, object : Callback<com.example.hirelink_2025.models.Job?> {
            override fun onSuccess(result: com.example.hirelink_2025.models.Job?) {
                result?.let { job ->
                    val currentCache = _jobsCache.value.toMutableMap()
                    currentCache[jobId] = job
                    _jobsCache.value = currentCache
                    
                    // También cargar información de la empresa
                    if (job.companyId.isNotEmpty()) {
                        loadCompanyInfo(job.companyId)
                    }
                }
            }

            override fun onError(exception: Exception) {
                Log.e("ApplicationsViewModel", "Error loading job info: $jobId", exception)
            }
        })
    }
    
    /**
     * Obtener información de un trabajo desde el cache
     */
    fun getJobInfo(jobId: String): com.example.hirelink_2025.models.Job? {
        return _jobsCache.value[jobId]
    }
    
    /**
     * Filtrar postulaciones activas: ELIMINADO - Ahora todas van a revisión
     * Esta función se mantiene para compatibilidad pero devuelve lista vacía
     */
    @Deprecated("Las postulaciones activas se eliminaron, usar getReviewApplications()")
    fun getActiveApplications(): List<Application> {
        return emptyList()
    }
    
    /**
     * Filtrar postulaciones en revisión: TODAS las postulaciones pendientes van aquí
     * (Anteriormente incluía solo las que habían pasado la fecha límite)
     */
    fun getReviewApplications(): List<Application> {
        return _applications.value.filter { application ->
            application.status == ApplicationStatus.PENDING
        }
    }
    
    /**
     * Filtrar postulaciones finalizadas: que han sido aceptadas o rechazadas
     */
    fun getFinishedApplications(): List<Application> {
        return _applications.value.filter { application ->
            application.status == ApplicationStatus.ACCEPTED || application.status == ApplicationStatus.REJECTED
        }
    }
    
    /**
     * Cancelar/eliminar una postulación
     */
    fun cancelApplication(applicationId: String, callback: (Boolean) -> Unit) {
        Log.d("ApplicationsViewModel", "Canceling application: $applicationId")
        
        firestoreService.deleteApplication(applicationId, object : VoidCallback {
            override fun onSuccess() {
                Log.d("ApplicationsViewModel", "Application canceled successfully")
                _operationResult.value = "Postulación cancelada exitosamente"
                
                // Remover la aplicación de la lista local
                val updatedApplications = _applications.value.filter { it.applicationId != applicationId }
                _applications.value = updatedApplications
                
                callback(true)
            }

            override fun onError(exception: Exception) {
                Log.e("ApplicationsViewModel", "Error canceling application", exception)
                _operationResult.value = "Error al cancelar la postulación: ${exception.message}"
                callback(false)
            }
        })
    }
}
