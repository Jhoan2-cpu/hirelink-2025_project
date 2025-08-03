package com.example.hirelink_2025.network

import android.content.Context
import android.util.Log
import com.example.hirelink_2025.models.Company
import com.example.hirelink_2025.models.Job
import com.example.hirelink_2025.models.JobStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

/**
 * Servicio para monitorear nuevas ofertas laborales y mostrar notificaciones locales
 */
class JobNotificationService(private val context: Context) {
    
    companion object {
        private const val TAG = "JobNotificationService"
        private const val JOBS_COLLECTION = "jobs"
        private const val COMPANIES_COLLECTION = "companies"
    }
    
    private val db = FirebaseFirestore.getInstance()
    private val firestoreService = FirestoreService()
    private val notificationManager = LocalNotificationManager(context)
    private var jobListener: ListenerRegistration? = null
    private val processedJobIds = mutableSetOf<String>()
    
    /**
     * Inicia el monitoreo de nuevas ofertas laborales
     */
    fun startMonitoring() {
        Log.d(TAG, "Starting job notification monitoring")
        
        // Obtener timestamp actual para solo notificar ofertas nuevas
        val startTime = System.currentTimeMillis()
        
        // Escuchar cambios en la colección de trabajos
        jobListener = db.collection(JOBS_COLLECTION)
            .whereEqualTo("status", JobStatus.ACTIVE.name)
            .whereGreaterThan("createdAt", startTime - 60000) // Últimos 1 minuto para evitar spam
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to job changes", error)
                    return@addSnapshotListener
                }
                
                snapshots?.documentChanges?.forEach { change ->
                    when (change.type) {
                        com.google.firebase.firestore.DocumentChange.Type.ADDED -> {
                            try {
                                val job = change.document.toObject(Job::class.java)
                                handleNewJob(job)
                            } catch (e: Exception) {
                                Log.e(TAG, "Error processing new job document", e)
                            }
                        }
                        else -> {
                            // Solo nos interesan los trabajos nuevos
                        }
                    }
                }
            }
        
        Log.d(TAG, "Job monitoring started successfully")
    }
    
    /**
     * Maneja la detección de una nueva oferta laboral
     */
    private fun handleNewJob(job: Job) {
        Log.d(TAG, "New job detected: ${job.title} (ID: ${job.id})")
        
        // Evitar procesar el mismo trabajo múltiples veces
        if (processedJobIds.contains(job.id)) {
            Log.d(TAG, "Job ${job.id} already processed, skipping")
            return
        }
        
        processedJobIds.add(job.id)
        
        // Verificar que no sea muy antiguo (más de 5 minutos)
        val jobAge = System.currentTimeMillis() - (job.createdAt ?: 0)
        if (jobAge > 300000) { // 5 minutos
            Log.d(TAG, "Job ${job.id} is too old (${jobAge}ms), skipping notification")
            return
        }
        
        // Obtener información de la empresa
        if (job.companyId.isNotEmpty()) {
            firestoreService.getCompanyById(job.companyId, object : Callback<Company?> {
                override fun onSuccess(company: Company?) {
                    if (company != null) {
                        Log.d(TAG, "Company found for job notification: ${company.name}")
                        
                        // Verificar que no sea el mismo usuario creando el trabajo
                        val currentUserId = firestoreService.getCurrentUserId()
                        if (currentUserId != null && currentUserId == company.ownerId) {
                            Log.d(TAG, "Skipping notification for job created by current user")
                            return
                        }
                        
                        // Mostrar notificación local
                        notificationManager.showNewJobNotification(job, company)
                        
                        // Crear notificación en Firestore para historial
                        createUserNotificationRecord(job, company)
                        
                    } else {
                        Log.w(TAG, "Company not found for job notification")
                    }
                }
                
                override fun onError(exception: Exception) {
                    Log.e(TAG, "Error getting company for job notification", exception)
                }
            })
        }
    }
    
    /**
     * Crea un registro de notificación en Firestore para el usuario actual
     */
    private fun createUserNotificationRecord(job: Job, company: Company) {
        val currentUserId = firestoreService.getCurrentUserId()
        if (currentUserId == null) {
            Log.w(TAG, "No authenticated user, cannot create notification record")
            return
        }
        
        val notification = com.example.hirelink_2025.models.Notification(
            id = "", // Se generará automáticamente
            userId = currentUserId,
            title = "Nueva oferta laboral",
            message = "Se publicó un anuncio laboral de ${company.name} - ${job.title}",
            type = com.example.hirelink_2025.models.NotificationType.JOB_UPDATE,
            relatedId = job.id,
            actionUrl = "hirelink://job_detail/${job.id}",
            createdAt = System.currentTimeMillis(),
            read = false
        )
        
        val notificationRef = db.collection("user_notifications").document()
        notificationRef.set(notification.copy(id = notificationRef.id))
            .addOnSuccessListener {
                Log.d(TAG, "User notification record created successfully")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error creating user notification record", e)
            }
    }
    
    /**
     * Detiene el monitoreo de ofertas laborales
     */
    fun stopMonitoring() {
        Log.d(TAG, "Stopping job notification monitoring")
        
        jobListener?.remove()
        jobListener = null
        processedJobIds.clear()
        
        Log.d(TAG, "Job monitoring stopped")
    }
    
    /**
     * Muestra una notificación de prueba
     */
    fun showTestNotification() {
        Log.d(TAG, "Showing test notification")
        notificationManager.showTestNotification()
    }
    
    /**
     * Verifica si el monitoreo está activo
     */
    fun isMonitoring(): Boolean {
        return jobListener != null
    }
}