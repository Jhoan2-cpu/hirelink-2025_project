package com.example.hirelink_2025.viewmodels.ads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hirelink_2025.models.Application
import com.example.hirelink_2025.models.ApplicationStatus
import com.example.hirelink_2025.models.User
import com.example.hirelink_2025.network.FirestoreService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel adaptado para usar el modelo Application simplificado + User
 * MVVM: Separa lógica de negocio de la UI
 */
class MyAdsApplicantsViewModel(
    private val firestoreService: FirestoreService = FirestoreService()
) : ViewModel() {

    // Estado interno privado
    private val _uiState = MutableStateFlow(ApplicantsUiState())
    val uiState: StateFlow<ApplicantsUiState> = _uiState.asStateFlow()

    // Variable principal para todas las aplicaciones
    private val _allApplications = MutableStateFlow<List<Application>>(emptyList())
    val allApplications: StateFlow<List<Application>> = _allApplications.asStateFlow()

    // Cache de usuarios para acceso rápido
    private val _usersCache = MutableStateFlow<Map<String, User>>(emptyMap())
    private val usersCache: StateFlow<Map<String, User>> = _usersCache.asStateFlow()

    // Estados públicos derivados para los fragments
    val pendingApplications: StateFlow<List<Application>> = _allApplications
        .map { applications -> applications.filter { it.status == ApplicationStatus.PENDING } }
        .stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val acceptedApplications: StateFlow<List<Application>> = _allApplications
        .map { applications -> applications.filter { it.status == ApplicationStatus.ACCEPTED } }
        .stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val rejectedApplications: StateFlow<List<Application>> = _allApplications
        .map { applications -> applications.filter { it.status == ApplicationStatus.REJECTED } }
        .stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private var currentJobId: String? = null

    /**
     * Inicializar con un job específico
     */
    fun initializeWithJob(jobId: String) {
        if (currentJobId != jobId) {
            currentJobId = jobId
            loadApplications(jobId)
        }
    }

    /**
     * Actualizar estado de una aplicación
     */
    fun updateApplicationStatus(applicationId: String, newStatus: ApplicationStatus) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // Actualizar lista local inmediatamente para UI responsiva
                val updatedList = _allApplications.value.map { application ->
                    if (application.applicationId == applicationId) {
                        application.copy(status = newStatus, reviewedAt = System.currentTimeMillis())
                    } else {
                        application
                    }
                }

                _allApplications.value = updatedList

                // Actualizar en Firestore
                firestoreService.updateApplicationStatus(applicationId, newStatus) { success ->
                    if (!success) {
                        // Revertir cambios si falla
                        currentJobId?.let { jobId ->
                            loadApplications(jobId)
                        }
                    }
                }

                _uiState.value = _uiState.value.copy(isLoading = false)

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al actualizar estado: ${e.message}"
                )
            }
        }
    }

    /**
     * Actualizar lista completa de aplicaciones (para datos de prueba)
     */
    fun updateApplicationsList(applications: List<Application>) {
        _allApplications.value = applications
        
        // Cargar usuarios correspondientes
        val userIds = applications.map { it.applicantId }.distinct()
        loadUsers(userIds)
    }

    /**
     * Cargar aplicaciones desde Firestore
     */
    private fun loadApplications(jobId: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                firestoreService.getApplicationsByJobId(jobId, object : com.example.hirelink_2025.network.Callback<List<Application>> {
                    override fun onSuccess(applications: List<Application>) {
                        _allApplications.value = applications
                        
                        // Cargar información de usuarios
                        val userIds = applications.map { it.applicantId }.distinct()
                        loadUsers(userIds)
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = null
                        )
                    }
                    
                    override fun onError(exception: Exception) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = exception.message ?: "Error desconocido"
                        )
                    }
                })
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error desconocido"
                )
            }
        }
    }

    /**
     * Cargar información de usuarios
     */
    private fun loadUsers(userIds: List<String>) {
        viewModelScope.launch {
            val currentCache = _usersCache.value.toMutableMap()
            
            userIds.forEach { userId ->
                if (!currentCache.containsKey(userId)) {
                    firestoreService.getUserById(userId, object : com.example.hirelink_2025.network.Callback<User?> {
                        override fun onSuccess(user: User?) {
                            user?.let {
                                currentCache[userId] = it
                                _usersCache.value = currentCache.toMap()
                            }
                        }
                        
                        override fun onError(exception: Exception) {
                            // Log error but continue loading other users
                            android.util.Log.w("MyAdsApplicantsViewModel", "Failed to load user $userId: ${exception.message}")
                        }
                    })
                }
            }
        }
    }

    /**
     * Obtener usuario por ID desde cache
     */
    fun getUserById(userId: String): User? {
        return _usersCache.value[userId]
    }

    /**
     * Actualizar cache de usuarios (para datos de prueba)
     */
    fun updateUserCache(userId: String, user: User) {
        val currentCache = _usersCache.value.toMutableMap()
        currentCache[userId] = user
        _usersCache.value = currentCache.toMap()
    }

    /**
     * Obtener aplicación por ID
     */
    fun getApplicationById(applicationId: String): Application? {
        return _allApplications.value.find { it.applicationId == applicationId }
    }

    /**
     * Aceptar aplicación
     */
    fun acceptApplication(applicationId: String) {
        updateApplicationStatus(applicationId, ApplicationStatus.ACCEPTED)
    }

    /**
     * Rechazar aplicación
     */
    fun rejectApplication(applicationId: String) {
        updateApplicationStatus(applicationId, ApplicationStatus.REJECTED)
    }

    /**
     * Aceptar aplicante (alias para compatibilidad)
     */
    fun acceptApplicant(applicantId: String) {
        // TODO: Buscar applicationId por applicantId
        // Por ahora, asumir que applicantId es applicationId
        acceptApplication(applicantId)
    }

    /**
     * Rechazar aplicante (alias para compatibilidad)
     */
    fun rejectApplicant(applicantId: String) {
        // TODO: Buscar applicationId por applicantId
        // Por ahora, asumir que applicantId es applicationId
        rejectApplication(applicantId)
    }

    /**
     * Limpiar errores
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Obtener estadísticas
     */
    fun getApplicationsStats(): ApplicantsStats {
        val all = _allApplications.value
        return ApplicantsStats(
            total = all.size,
            pending = all.count { it.status == ApplicationStatus.PENDING },
            accepted = all.count { it.status == ApplicationStatus.ACCEPTED },
            rejected = all.count { it.status == ApplicationStatus.REJECTED }
        )
    }

    /**
     * Crear datos de prueba para testing
     */
    fun createTestApplications(jobId: String): List<Application> {
        return listOf(
            Application(
                applicationId = "app1",
                jobId = jobId,
                applicantId = "user1",
                appliedAt = System.currentTimeMillis() - 86400000, // 1 día atrás
                status = ApplicationStatus.PENDING
            ),
            Application(
                applicationId = "app2",
                jobId = jobId,
                applicantId = "user2",
                appliedAt = System.currentTimeMillis() - 172800000, // 2 días atrás
                status = ApplicationStatus.ACCEPTED,
                reviewedAt = System.currentTimeMillis() - 86400000
            ),
            Application(
                applicationId = "app3",
                jobId = jobId,
                applicantId = "user3",
                appliedAt = System.currentTimeMillis() - 259200000, // 3 días atrás
                status = ApplicationStatus.REJECTED,
                reviewedAt = System.currentTimeMillis() - 172800000
            )
        )
    }
}

/**
 * Estado de la UI
 */
data class ApplicantsUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Estadísticas de aplicantes
 */
data class ApplicantsStats(
    val total: Int,
    val pending: Int,
    val accepted: Int,
    val rejected: Int
)