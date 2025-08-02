package com.example.hirelink_2025.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hirelink_2025.models.Job
import com.example.hirelink_2025.models.JobStatus
import com.example.hirelink_2025.network.Callback
import com.example.hirelink_2025.network.FirestoreService
import com.google.firebase.firestore.FirebaseFirestore

class SearchViewModel : ViewModel() {
    
    private val firestoreService = FirestoreService()
    
    private val _jobs = MutableLiveData<List<Job>>()
    val jobs: LiveData<List<Job>> = _jobs
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    init {
        loadAllJobs()
    }
    
    fun loadAllJobs() {
        _isLoading.value = true
        _error.value = null
        
        // Usar FirebaseFirestore directamente para evitar problemas de índice
        val db = FirebaseFirestore.getInstance()
        db.collection(FirestoreService.JOBS_COLLECTION)
            .whereEqualTo("status", JobStatus.ACTIVE.name)
            .limit(50)
            .get()
            .addOnSuccessListener { querySnapshot ->
                _isLoading.value = false
                val jobs = querySnapshot.toObjects(Job::class.java)
                    .sortedByDescending { it.createdAt ?: 0L } // Ordenar por createdAt en el cliente
                _jobs.value = jobs
            }
            .addOnFailureListener { exception ->
                _isLoading.value = false
                _error.value = "Error al cargar ofertas: ${exception.message}"
            }
    }
    
    /**
     * Search jobs by title. Location parameter is kept for compatibility but not used
     * since location is now stored in Company.city and would require additional queries.
     */
    fun searchJobs(title: String?, location: String? = null) {
        _isLoading.value = true
        _error.value = null
        
        val db = FirebaseFirestore.getInstance()
        var query = db.collection(FirestoreService.JOBS_COLLECTION)
            .whereEqualTo("status", JobStatus.ACTIVE.name)
        
        // Aplicar filtros básicos sin índice compuesto
        title?.takeIf { it.isNotBlank() }?.let {
            query = query.whereGreaterThanOrEqualTo("title", it)
                .whereLessThanOrEqualTo("title", it + '\uf8ff')
        }
        
        query.limit(50)
            .get()
            .addOnSuccessListener { querySnapshot ->
                _isLoading.value = false
                var jobs = querySnapshot.toObjects(Job::class.java)
                
                // Note: Location filtering removed because Job model no longer has location field
                // Location information is now stored in Company.city and would require additional query
                
                // Ordenar por fecha de creación en el cliente
                _jobs.value = jobs.sortedByDescending { it.createdAt ?: 0L }
            }
            .addOnFailureListener { exception ->
                _isLoading.value = false
                _error.value = "Error en búsqueda: ${exception.message}"
            }
    }
    
    fun clearError() {
        _error.value = null
    }
}