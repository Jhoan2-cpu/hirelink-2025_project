package com.example.hirelink_2025.network

import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.auth.FirebaseAuth
import com.example.hirelink_2025.models.*
import com.example.hirelink_2025.utils.PasswordUtils

/**
 * Servicio para manejar todas las operaciones con Firestore
 */
class FirestoreService {
    
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    
    init {
        // Configuración para evitar problemas con reCAPTCHA en desarrollo
        try {
            auth.firebaseAuthSettings.setAppVerificationDisabledForTesting(true)
            Log.d("FirestoreService", "App verification disabled for testing")
        } catch (e: Exception) {
            Log.w("FirestoreService", "No se pudo deshabilitar App Verification: ${e.message}")
        }
    }
    
    // Nombres de las colecciones
    companion object {
        const val USERS_COLLECTION = "users"
        const val COMPANIES_COLLECTION = "companies"
        const val JOBS_COLLECTION = "jobs"
        const val APPLICATIONS_COLLECTION = "applications"
        const val CATEGORIES_COLLECTION = "categories"
        const val BOOKMARKS_COLLECTION = "bookmarks"
        const val NOTIFICATIONS_COLLECTION = "notifications"
        const val USER_PROFILES_COLLECTION = "user_profiles"
        const val USER_CREDENTIALS_COLLECTION = "user_credentials" // Solo para desarrollo
    }
    
    /**
     * Método de prueba para verificar conectividad Firebase
     */
    fun testFirebaseConnection(callback: VoidCallback) {
        Log.d("FirestoreService", "Testing Firebase connection...")
        
        // Intentar leer un documento de prueba
        db.collection("test")
            .document("connection")
            .get()
            .addOnSuccessListener { 
                Log.d("FirestoreService", "Firebase connection test successful")
                callback.onSuccess()
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreService", "Firebase connection test failed", exception)
                callback.onError(exception)
            }
    }
    
    /**
     * Método de diagnóstico para verificar estado del usuario
     */
    fun diagnoseUser(email: String, callback: Callback<String>) {
        Log.d("FirestoreService", "Diagnosing user: $email")
        
        var diagnostics = "=== DIAGNÓSTICO DE USUARIO ===\n"
        diagnostics += "Email: $email\n"
        
        // Verificar en colección users
        db.collection(USERS_COLLECTION)
            .whereEqualTo("email", email.trim())
            .get()
            .addOnSuccessListener { userQuery ->
                diagnostics += "Usuarios encontrados: ${userQuery.size()}\n"
                
                if (!userQuery.isEmpty) {
                    val userId = userQuery.documents[0].id
                    diagnostics += "User ID: $userId\n"
                    
                    // Verificar credenciales
                    db.collection(USER_CREDENTIALS_COLLECTION)
                        .document(userId)
                        .get()
                        .addOnSuccessListener { credDoc ->
                            diagnostics += "Credenciales encontradas: ${credDoc.exists()}\n"
                            if (credDoc.exists()) {
                                val cred = credDoc.toObject(UserCredentials::class.java)
                                diagnostics += "Email en credenciales: ${cred?.email}\n"
                            }
                            callback.onSuccess(diagnostics)
                        }
                        .addOnFailureListener { 
                            diagnostics += "Error verificando credenciales: ${it.message}\n"
                            callback.onSuccess(diagnostics)
                        }
                } else {
                    diagnostics += "Usuario no encontrado en Firestore\n"
                    callback.onSuccess(diagnostics)
                }
            }
            .addOnFailureListener { exception ->
                diagnostics += "Error en diagnóstico: ${exception.message}\n"
                callback.onError(Exception(diagnostics))
            }
    }
    
    // ================================
    // OPERACIONES CON USUARIOS
    // ================================
    
    /**
     * Crear un nuevo usuario
     */
    fun createUser(user: User, callback: VoidCallback) {
        Log.d("FirestoreService", "createUser() called with user: $user")
        db.collection(USERS_COLLECTION)
            .document(user.id)
            .set(user)
            .addOnSuccessListener { 
                Log.d("FirestoreService", "User document created successfully in Firestore")
                callback.onSuccess() 
            }
            .addOnFailureListener { 
                Log.e("FirestoreService", "Failed to create user document", it)
                callback.onError(it) 
            }
    }
    
    /**
     * Obtener usuario por ID
     */
    fun getUserById(userId: String, callback: Callback<User?>) {
        Log.d("FirestoreService", "Getting user by ID: $userId")
        db.collection(USERS_COLLECTION)
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    try {
                        val user = document.toObject(User::class.java)
                        Log.d("FirestoreService", "User found: ${user?.fullName}")
                        callback.onSuccess(user)
                    } catch (e: Exception) {
                        Log.w("FirestoreService", "Failed to deserialize user document $userId: ${e.message}")
                        callback.onSuccess(null)
                    }
                } else {
                    Log.w("FirestoreService", "User document $userId does not exist")
                    callback.onSuccess(null)
                }
            }
            .addOnFailureListener { 
                Log.e("FirestoreService", "Error getting user by ID $userId", it)
                callback.onError(it) 
            }
    }
    
    /**
     * Actualizar usuario
     */
    fun updateUser(user: User, callback: VoidCallback) {
        db.collection(USERS_COLLECTION)
            .document(user.id)
            .set(user)
            .addOnSuccessListener { callback.onSuccess() }
            .addOnFailureListener { callback.onError(it) }
    }
    
    /**
     * Verificar si existe un usuario con el email dado
     */
    fun checkUserExists(email: String, callback: ExistsCallback) {
        db.collection(USERS_COLLECTION)
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                callback.onSuccess(!querySnapshot.isEmpty)
            }
            .addOnFailureListener { callback.onError(it) }
    }
    
    // ================================
    // OPERACIONES CON EMPRESAS
    // ================================
    
    /**
     * Crear una nueva empresa
     */
    fun createCompany(company: Company, callback: Callback<String>) {
        val docRef = db.collection(COMPANIES_COLLECTION).document()
        val companyWithId = company.copy(id = docRef.id)
        
        docRef.set(companyWithId)
            .addOnSuccessListener { callback.onSuccess(docRef.id) }
            .addOnFailureListener { callback.onError(it) }
    }
    
    /**
     * Obtener empresa por ID
     */
    fun getCompanyById(companyId: String, callback: Callback<Company?>) {
        Log.d("FirestoreService", "Looking for company with ID: '$companyId'")
        db.collection(COMPANIES_COLLECTION)
            .document(companyId)
            .get()
            .addOnSuccessListener { document ->
                Log.d("FirestoreService", "Document query result - exists: ${document.exists()}, document ID: '${document.id}'")
                if (document.exists()) {
                    try {
                        val company = document.toObject(Company::class.java)
                        Log.d("FirestoreService", "Company deserialized successfully: ${company?.name ?: "null"}")
                        callback.onSuccess(company)
                    } catch (e: Exception) {
                        Log.w("FirestoreService", "Failed to deserialize company document ${document.id}: ${e.message}")
                        
                        // Intentar recuperar los datos manualmente para evitar el error de deserialización
                        try {
                            val data = document.data
                            if (data != null) {
                                Log.d("FirestoreService", "Attempting manual recovery of company data")
                                val manualCompany = createCompanyFromRawData(document.id, data)
                                callback.onSuccess(manualCompany)
                            } else {
                                callback.onSuccess(null)
                            }
                        } catch (e2: Exception) {
                            Log.e("FirestoreService", "Manual recovery also failed: ${e2.message}")
                            callback.onSuccess(null)
                        }
                    }
                } else {
                    Log.w("FirestoreService", "Document with ID '$companyId' does not exist in Firestore")
                    callback.onSuccess(null)
                }
            }
            .addOnFailureListener { 
                Log.e("FirestoreService", "Error querying company by ID '$companyId': ${it.message}")
                callback.onError(it) 
            }
    }
    
    /**
     * Crear objeto Company desde datos raw de Firestore (para recuperación manual)
     */
    private fun createCompanyFromRawData(documentId: String, data: Map<String, Any>): Company {
        // Función helper para obtener strings safely
        fun getString(key: String): String = data[key]?.toString() ?: ""
        fun getLong(key: String): Long = (data[key] as? Number)?.toLong() ?: 0L
        fun getInt(key: String): Int = (data[key] as? Number)?.toInt() ?: 0
        
        // Manejar el enum CompanySize de forma segura
        val sizeString = getString("size")
        val companySize = when (sizeString) {
            "STARTUP" -> CompanySize.STARTUP
            "SMALL" -> CompanySize.SMALL
            "MEDIUM" -> CompanySize.MEDIUM
            "LARGE" -> CompanySize.LARGE
            "ENTERPRISE" -> CompanySize.ENTERPRISE
            // Casos legacy que pueden estar en la base de datos
            "1-10", "10-100" -> CompanySize.SMALL  // Corregir valores inválidos
            else -> {
                Log.w("FirestoreService", "Unknown company size '$sizeString', defaulting to SMALL")
                CompanySize.SMALL
            }
        }
        
        return Company(
            id = documentId,
            name = getString("name"),
            type = getString("type"),
            description = getString("description"),
            size = companySize,
            foundedYear = getInt("foundedYear"),
            address = getString("address"),
            city = getString("city"),
            country = getString("country"),
            phone = getString("phone"),
            email = getString("email"),
            website = getString("website"),
            logoUrl = getString("logoUrl"),
            ownerId = getString("ownerId"),
            createdAt = getLong("createdAt"),
            updatedAt = getLong("updatedAt")
        )
    }
    
    /**
     * Obtener empresas por usuario propietario
     */
    fun getCompaniesByOwner(ownerId: String, callback: Callback<List<Company>>) {
        Log.d("FirestoreService", "Getting companies for owner: $ownerId")
        db.collection(COMPANIES_COLLECTION)
            .whereEqualTo("ownerId", ownerId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                Log.d("FirestoreService", "Found ${querySnapshot.size()} companies for owner")
                val companies = mutableListOf<Company>()
                for (document in querySnapshot.documents) {
                    try {
                        val company = document.toObject(Company::class.java)
                        company?.let { companies.add(it) }
                    } catch (e: Exception) {
                        Log.w("FirestoreService", "Failed to deserialize company document ${document.id}: ${e.message}")
                        // Skip this document and continue with others
                    }
                }
                callback.onSuccess(companies)
            }
            .addOnFailureListener { 
                Log.e("FirestoreService", "Error getting companies by owner", it)
                callback.onError(it) 
            }
    }

    /**
     * Obtener todas las compañías (sin filtro de usuario)
     */
    fun getAllCompanies(callback: Callback<List<Company>>) {
        Log.d("FirestoreService", "Getting all companies")
        db.collection(COMPANIES_COLLECTION)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                Log.d("FirestoreService", "Found ${querySnapshot.size()} total companies")
                val companies = mutableListOf<Company>()
                for (document in querySnapshot.documents) {
                    try {
                        val company = document.toObject(Company::class.java)
                        company?.let { companies.add(it) }
                    } catch (e: Exception) {
                        Log.w("FirestoreService", "Failed to deserialize company document ${document.id}: ${e.message}")
                        // Skip this document and continue with others
                    }
                }
                callback.onSuccess(companies)
            }
            .addOnFailureListener { 
                Log.e("FirestoreService", "Error getting all companies", it)
                callback.onError(it) 
            }
    }
    
    /**
     * Actualizar empresa
     */
    fun updateCompany(company: Company, callback: VoidCallback) {
        db.collection(COMPANIES_COLLECTION)
            .document(company.id)
            .set(company)
            .addOnSuccessListener { callback.onSuccess() }
            .addOnFailureListener { callback.onError(it) }
    }
    
    /**
     * Eliminar empresa
     */
    fun deleteCompany(companyId: String, callback: VoidCallback) {
        db.collection(COMPANIES_COLLECTION)
            .document(companyId)
            .delete()
            .addOnSuccessListener { callback.onSuccess() }
            .addOnFailureListener { callback.onError(it) }
    }
    
    /**
     * Buscar empresas por nombre
     */
    fun searchCompanies(query: String, callback: Callback<List<Company>>) {
        db.collection(COMPANIES_COLLECTION)
            .whereGreaterThanOrEqualTo("name", query)
            .whereLessThanOrEqualTo("name", query + '\uf8ff')
            .limit(20)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val companies = mutableListOf<Company>()
                for (document in querySnapshot.documents) {
                    try {
                        val company = document.toObject(Company::class.java)
                        company?.let { companies.add(it) }
                    } catch (e: Exception) {
                        Log.w("FirestoreService", "Failed to deserialize company document ${document.id}: ${e.message}")
                        // Skip this document and continue with others
                    }
                }
                callback.onSuccess(companies)
            }
            .addOnFailureListener { callback.onError(it) }
    }
    
    /**
     * Limpiar URLs de placeholder problemáticas de compañías existentes
     */
    fun cleanPlaceholderUrls(ownerId: String, callback: VoidCallback) {
        Log.d("FirestoreService", "Cleaning placeholder URLs for owner: $ownerId")
        
        db.collection(COMPANIES_COLLECTION)
            .whereEqualTo("ownerId", ownerId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val batch = db.batch()
                var updatesCount = 0
                
                for (document in querySnapshot.documents) {
                    try {
                        val company = document.toObject(Company::class.java)
                        if (company?.logoUrl?.contains("via.placeholder.com") == true) {
                            Log.d("FirestoreService", "Cleaning placeholder URL for company: ${company.name}")
                            batch.update(document.reference, "logoUrl", "")
                            updatesCount++
                        }
                    } catch (e: Exception) {
                        Log.w("FirestoreService", "Failed to deserialize company document ${document.id}: ${e.message}")
                        // Skip this document and continue with others
                        continue
                    }
                }
                
                if (updatesCount > 0) {
                    batch.commit()
                        .addOnSuccessListener {
                            Log.d("FirestoreService", "Successfully cleaned $updatesCount placeholder URLs")
                            callback.onSuccess()
                        }
                        .addOnFailureListener { exception ->
                            Log.e("FirestoreService", "Error cleaning placeholder URLs", exception)
                            callback.onError(exception)
                        }
                } else {
                    Log.d("FirestoreService", "No placeholder URLs to clean")
                    callback.onSuccess()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreService", "Error getting companies for cleanup", exception)
                callback.onError(exception)
            }
    }
    
    // ================================
    // OPERACIONES CON TRABAJOS
    // ================================
    
    /**
     * Crear un nuevo trabajo
     */
    fun createJob(job: Job, callback: Callback<String>) {
        val docRef = db.collection(JOBS_COLLECTION).document()
        val jobWithId = job.copy(id = docRef.id)
        
        docRef.set(jobWithId)
            .addOnSuccessListener { callback.onSuccess(docRef.id) }
            .addOnFailureListener { callback.onError(it) }
    }
    
    /**
     * Obtener trabajo por ID
     */
    fun getJobById(jobId: String, callback: Callback<Job?>) {
        db.collection(JOBS_COLLECTION)
            .document(jobId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    try {
                        val job = document.toObject(Job::class.java)
                        callback.onSuccess(job)
                    } catch (e: Exception) {
                        Log.w("FirestoreService", "Failed to deserialize job document ${document.id}: ${e.message}")
                        callback.onSuccess(null)
                    }
                } else {
                    callback.onSuccess(null)
                }
            }
            .addOnFailureListener { callback.onError(it) }
    }
    
    /**
     * Obtener trabajos activos con paginación (sin requerir índice compuesto)
     */
    fun getActiveJobs(limit: Int = 20, callback: Callback<List<Job>>) {
        db.collection(JOBS_COLLECTION)
            .whereEqualTo("status", JobStatus.ACTIVE.name)
            .limit(limit.toLong())
            .get()
            .addOnSuccessListener { querySnapshot ->
                val jobs = querySnapshot.toObjects(Job::class.java)
                    .sortedByDescending { it.createdAt ?: 0L } // Ordenar en el cliente
                callback.onSuccess(jobs)
            }
            .addOnFailureListener { callback.onError(it) }
    }
    
    /**
     * Buscar trabajos por filtros
     */
    fun searchJobs(
        title: String? = null,
        location: String? = null,
        modality: String? = null,
        category: String? = null,
        limit: Int = 20,
        callback: Callback<List<Job>>
    ) {
        var query: Query = db.collection(JOBS_COLLECTION)
            .whereEqualTo("status", JobStatus.ACTIVE.name)
        
        // Aplicar filtros si están presentes
        title?.let {
            query = query.whereGreaterThanOrEqualTo("title", it)
                .whereLessThanOrEqualTo("title", it + '\uf8ff')
        }
        
        location?.let {
            query = query.whereEqualTo("location", it)
        }
        
        modality?.let {
            query = query.whereEqualTo("modality", it)
        }
        
        query.limit(limit.toLong())
            .get()
            .addOnSuccessListener { querySnapshot ->
                val jobs = querySnapshot.toObjects(Job::class.java)
                    .sortedByDescending { it.createdAt ?: 0L } // Ordenar en el cliente
                callback.onSuccess(jobs)
            }
            .addOnFailureListener { callback.onError(it) }
    }
    
    /**
     * Obtener trabajos por empresa (usando companyId)
     */
    fun getJobsByCompany(companyId: String, callback: Callback<List<Job>>) {
        db.collection(JOBS_COLLECTION)
            .whereEqualTo("companyId", companyId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val jobs = querySnapshot.toObjects(Job::class.java)
                    .sortedByDescending { it.createdAt ?: 0L } // Ordenar en el cliente
                callback.onSuccess(jobs)
            }
            .addOnFailureListener { callback.onError(it) }
    }
    
    /**
     * Actualizar trabajo
     */
    fun updateJob(job: Job, callback: VoidCallback) {
        db.collection(JOBS_COLLECTION)
            .document(job.id)
            .set(job)
            .addOnSuccessListener { callback.onSuccess() }
            .addOnFailureListener { callback.onError(it) }
    }
    
    /**
     * Eliminar trabajo
     */
    fun deleteJob(jobId: String, callback: VoidCallback) {
        db.collection(JOBS_COLLECTION)
            .document(jobId)
            .delete()
            .addOnSuccessListener { callback.onSuccess() }
            .addOnFailureListener { callback.onError(it) }
    }
    
    /**
     * Cambiar estado de trabajo
     */
    fun updateJobStatus(jobId: String, status: JobStatus, callback: VoidCallback) {
        db.collection(JOBS_COLLECTION)
            .document(jobId)
            .update(mapOf(
                "status" to status.name,
                "updatedAt" to System.currentTimeMillis()
            ))
            .addOnSuccessListener { callback.onSuccess() }
            .addOnFailureListener { callback.onError(it) }
    }
    
    /**
     * Obtener trabajos por propietario (usando companyId del propietario)
     */
    fun getJobsByOwner(ownerId: String, callback: Callback<List<Job>>) {
        Log.d("FirestoreService", "Getting jobs for owner: $ownerId")
        
        // Primero obtenemos las compañías del propietario
        getCompaniesByOwner(ownerId, object : Callback<List<Company>> {
            override fun onSuccess(companies: List<Company>) {
                if (companies.isEmpty()) {
                    Log.d("FirestoreService", "No companies found for owner, returning empty jobs list")
                    callback.onSuccess(emptyList())
                    return
                }
                
                // Obtener trabajos para todas las compañías del propietario
                val allJobs = mutableListOf<Job>()
                var pendingRequests = companies.size
                
                companies.forEach { company ->
                    db.collection(JOBS_COLLECTION)
                        .whereEqualTo("companyId", company.id)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            val jobs = querySnapshot.toObjects(Job::class.java)
                            allJobs.addAll(jobs)
                            pendingRequests--
                            
                            if (pendingRequests == 0) {
                                // Ordenar todos los trabajos por fecha de creación
                                val sortedJobs = allJobs.sortedByDescending { it.createdAt }
                                Log.d("FirestoreService", "Found ${sortedJobs.size} total jobs for owner")
                                callback.onSuccess(sortedJobs)
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e("FirestoreService", "Error getting jobs for company ${company.id}", exception)
                            pendingRequests--
                            
                            if (pendingRequests == 0) {
                                val sortedJobs = allJobs.sortedByDescending { it.createdAt }
                                callback.onSuccess(sortedJobs)
                            }
                        }
                }
            }
            
            override fun onError(exception: Exception) {
                Log.e("FirestoreService", "Error getting companies for owner", exception)
                callback.onError(exception)
            }
        })
    }
    
    /**
     * Obtener trabajos por propietario y estado (usando companyId del propietario)
     */
    fun getJobsByOwnerAndStatus(ownerId: String, status: JobStatus, callback: Callback<List<Job>>) {
        Log.d("FirestoreService", "Getting jobs for owner: $ownerId with status: $status")
        
        // Primero obtenemos las compañías del propietario
        getCompaniesByOwner(ownerId, object : Callback<List<Company>> {
            override fun onSuccess(companies: List<Company>) {
                if (companies.isEmpty()) {
                    Log.d("FirestoreService", "No companies found for owner, returning empty jobs list")
                    callback.onSuccess(emptyList())
                    return
                }
                
                // Obtener trabajos para todas las compañías del propietario con el estado específico
                val allJobs = mutableListOf<Job>()
                var pendingRequests = companies.size
                
                companies.forEach { company ->
                    db.collection(JOBS_COLLECTION)
                        .whereEqualTo("companyId", company.id)
                        .whereEqualTo("status", status)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            val jobs = querySnapshot.toObjects(Job::class.java)
                            allJobs.addAll(jobs)
                            pendingRequests--
                            
                            if (pendingRequests == 0) {
                                // Ordenar todos los trabajos por fecha de creación
                                val sortedJobs = allJobs.sortedByDescending { it.createdAt }
                                Log.d("FirestoreService", "Found ${sortedJobs.size} jobs for owner with status $status")
                                callback.onSuccess(sortedJobs)
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e("FirestoreService", "Error getting jobs for company ${company.id} with status", exception)
                            pendingRequests--
                            
                            if (pendingRequests == 0) {
                                val sortedJobs = allJobs.sortedByDescending { it.createdAt }
                                callback.onSuccess(sortedJobs)
                            }
                        }
                }
            }
            
            override fun onError(exception: Exception) {
                Log.e("FirestoreService", "Error getting companies for owner", exception)
                callback.onError(exception)
            }
        })
    }
    
    /**
     * Eliminar trabajo por ID y propietario (seguridad usando companyId)
     */
    fun deleteJobByOwner(jobId: String, ownerId: String, callback: VoidCallback) {
        Log.d("FirestoreService", "Deleting job: $jobId for owner: $ownerId")
        
        db.collection(JOBS_COLLECTION)
            .document(jobId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    try {
                        val job = document.toObject(Job::class.java)
                        if (job?.companyId?.isNotEmpty() == true) {
                            // Verificar que la compañía pertenece al propietario
                            getCompanyById(job.companyId, object : Callback<Company?> {
                                override fun onSuccess(company: Company?) {
                                    if (company?.ownerId == ownerId) {
                                        // Eliminar el documento
                                        document.reference.delete()
                                            .addOnSuccessListener {
                                                Log.d("FirestoreService", "Job deleted successfully")
                                                callback.onSuccess()
                                            }
                                            .addOnFailureListener { exception ->
                                                Log.e("FirestoreService", "Error deleting job", exception)
                                                callback.onError(exception)
                                            }
                                    } else {
                                        Log.w("FirestoreService", "Job owner mismatch")
                                        callback.onError(Exception("No tienes permisos para eliminar este anuncio"))
                                    }
                                }
                                
                                override fun onError(exception: Exception) {
                                    Log.e("FirestoreService", "Error verifying company ownership", exception)
                                    callback.onError(Exception("Error verificando permisos"))
                                }
                            })
                        } else {
                            Log.w("FirestoreService", "Job has no companyId")
                            callback.onError(Exception("Anuncio inválido"))
                        }
                    } catch (e: Exception) {
                        Log.w("FirestoreService", "Failed to deserialize job document ${document.id}: ${e.message}")
                        callback.onError(Exception("Error al procesar el anuncio"))
                    }
                } else {
                    Log.w("FirestoreService", "Job not found")
                    callback.onError(Exception("Anuncio no encontrado"))
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreService", "Error getting job for deletion", exception)
                callback.onError(exception)
            }
    }
    
    // ================================
    // OPERACIONES CON APLICACIONES (Application model)
    // ================================
    
    /**
     * Crear nueva postulación usando modelo Application
     */
    fun createJobApplication(application: Application, callback: Callback<String>) {
        val docRef = db.collection(APPLICATIONS_COLLECTION).document()
        val applicationWithId = application.copy(applicationId = docRef.id)
        
        docRef.set(applicationWithId)
            .addOnSuccessListener { callback.onSuccess(docRef.id) }
            .addOnFailureListener { callback.onError(it) }
    }
    
    /**
     * Obtener postulaciones por trabajo (para ver postulantes)
     */
    fun getApplicationsByJobId(jobId: String, callback: Callback<List<Application>>) {
        Log.d("FirestoreService", "Getting applications for job: $jobId")
        db.collection(APPLICATIONS_COLLECTION)
            .whereEqualTo("jobId", jobId)
            .orderBy("appliedAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val applications = mutableListOf<Application>()
                for (document in querySnapshot.documents) {
                    try {
                        val application = document.toObject(Application::class.java)
                        application?.let { applications.add(it) }
                    } catch (e: Exception) {
                        Log.w("FirestoreService", "Failed to deserialize application document ${document.id}: ${e.message}")
                    }
                }
                Log.d("FirestoreService", "Found ${applications.size} applications for job")
                callback.onSuccess(applications)
            }
            .addOnFailureListener { 
                Log.e("FirestoreService", "Error getting applications for job", it)
                callback.onError(it) 
            }
    }
    
    /**
     * Obtener postulaciones por usuario (para ver mis postulaciones)
     */
    fun getApplicationsByUserId(userId: String, callback: Callback<List<Application>>) {
        Log.d("FirestoreService", "Getting applications for user: $userId")
        db.collection(APPLICATIONS_COLLECTION)
            .whereEqualTo("applicantId", userId)
            .orderBy("appliedAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val applications = mutableListOf<Application>()
                for (document in querySnapshot.documents) {
                    try {
                        val application = document.toObject(Application::class.java)
                        application?.let { applications.add(it) }
                    } catch (e: Exception) {
                        Log.w("FirestoreService", "Failed to deserialize application document ${document.id}: ${e.message}")
                    }
                }
                Log.d("FirestoreService", "Found ${applications.size} applications for user")
                callback.onSuccess(applications)
            }
            .addOnFailureListener { 
                Log.e("FirestoreService", "Error getting applications for user", it)
                callback.onError(it) 
            }
    }
    
    /**
     * Actualizar estado de postulación
     */
    fun updateJobApplicationStatus(applicationId: String, status: ApplicationStatus, callback: VoidCallback) {
        Log.d("FirestoreService", "Updating application $applicationId status to $status")
        db.collection(APPLICATIONS_COLLECTION)
            .document(applicationId)
            .update("status", status)
            .addOnSuccessListener { 
                Log.d("FirestoreService", "Application status updated successfully")
                callback.onSuccess() 
            }
            .addOnFailureListener { 
                Log.e("FirestoreService", "Error updating application status", it)
                callback.onError(it) 
            }
    }
    
    /**
     * Contar postulaciones por trabajo
     */
    fun countApplicationsByJobId(jobId: String, callback: (Int) -> Unit) {
        db.collection(APPLICATIONS_COLLECTION)
            .whereEqualTo("jobId", jobId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                callback(querySnapshot.size())
            }
            .addOnFailureListener { 
                Log.e("FirestoreService", "Error counting applications", it)
                callback(0) 
            }
    }
    
    /**
     * Verificar si un usuario ya postuló a un trabajo
     */
    fun hasUserAppliedToJob(userId: String, jobId: String, callback: (Boolean) -> Unit) {
        db.collection(APPLICATIONS_COLLECTION)
            .whereEqualTo("applicantId", userId)
            .whereEqualTo("jobId", jobId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                callback(!querySnapshot.isEmpty)
            }
            .addOnFailureListener { 
                Log.e("FirestoreService", "Error checking if user applied", it)
                callback(false) 
            }
    }
    
    // ================================
    // OPERACIONES CON CATEGORÍAS
    // ================================
    
    /**
     * Obtener todas las categorías
     */
    fun getAllCategories(callback: Callback<List<JobCategory>>) {
        db.collection(CATEGORIES_COLLECTION)
            .orderBy("name")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val categories = querySnapshot.toObjects(JobCategory::class.java)
                callback.onSuccess(categories)
            }
            .addOnFailureListener { callback.onError(it) }
    }
    
    /**
     * Crear nueva categoría
     */
    fun createCategory(category: JobCategory, callback: VoidCallback) {
        db.collection(CATEGORIES_COLLECTION)
            .document(category.id)
            .set(category)
            .addOnSuccessListener { callback.onSuccess() }
            .addOnFailureListener { callback.onError(it) }
    }
    
    // ================================
    // OPERACIONES CON BOOKMARKS/FAVORITOS
    // ================================
    
    /**
     * Agregar trabajo a favoritos
     */
    fun addBookmark(userId: String, jobId: String, callback: VoidCallback) {
        val bookmarkData = mapOf(
            "userId" to userId,
            "jobId" to jobId,
            "createdAt" to System.currentTimeMillis()
        )
        
        db.collection(BOOKMARKS_COLLECTION)
            .document("${userId}_${jobId}")
            .set(bookmarkData)
            .addOnSuccessListener { callback.onSuccess() }
            .addOnFailureListener { callback.onError(it) }
    }
    
    /**
     * Remover trabajo de favoritos
     */
    fun removeBookmark(userId: String, jobId: String, callback: VoidCallback) {
        db.collection(BOOKMARKS_COLLECTION)
            .document("${userId}_${jobId}")
            .delete()
            .addOnSuccessListener { callback.onSuccess() }
            .addOnFailureListener { callback.onError(it) }
    }
    
    /**
     * Verificar si un trabajo está en favoritos del usuario
     */
    fun isJobBookmarked(userId: String, jobId: String, callback: ExistsCallback) {
        db.collection(BOOKMARKS_COLLECTION)
            .document("${userId}_${jobId}")
            .get()
            .addOnSuccessListener { document ->
                callback.onSuccess(document.exists())
            }
            .addOnFailureListener { callback.onError(it) }
    }
    
    /**
     * Obtener trabajos favoritos del usuario
     */
    fun getUserBookmarks(userId: String, callback: Callback<List<String>>) {
        db.collection(BOOKMARKS_COLLECTION)
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val jobIds = querySnapshot.documents.mapNotNull { it.getString("jobId") }
                callback.onSuccess(jobIds)
            }
            .addOnFailureListener { callback.onError(it) }
    }
    
    /**
     * Obtener trabajos favoritos completos del usuario
     */
    fun getUserBookmarkedJobs(userId: String, callback: Callback<List<Job>>) {
        getUserBookmarks(userId, object : Callback<List<String>> {
            override fun onSuccess(result: List<String>) {
                if (result.isEmpty()) {
                    callback.onSuccess(emptyList())
                    return
                }
                
                val jobs = mutableListOf<Job>()
                var pendingRequests = result.size
                
                result.forEach { jobId ->
                    getJobById(jobId, object : Callback<Job?> {
                        override fun onSuccess(job: Job?) {
                            job?.let { jobs.add(it) }
                            pendingRequests--
                            if (pendingRequests == 0) {
                                callback.onSuccess(jobs.sortedByDescending { it.postedDate })
                            }
                        }
                        
                        override fun onError(exception: Exception) {
                            pendingRequests--
                            if (pendingRequests == 0) {
                                callback.onSuccess(jobs.sortedByDescending { it.postedDate })
                            }
                        }
                    })
                }
            }
            
            override fun onError(exception: Exception) {
                callback.onError(exception)
            }
        })
    }
    
    
    // ================================
    // OPERACIONES DE ESTADÍSTICAS
    // ================================
    
    /**
     * Obtener estadísticas generales
     */
    fun getGeneralStats(callback: Callback<Map<String, Int>>) {
        val stats = mutableMapOf<String, Int>()
        var pendingRequests = 4
        
        // Contar usuarios
        db.collection(USERS_COLLECTION).get()
            .addOnSuccessListener { querySnapshot ->
                stats["users"] = querySnapshot.size()
                pendingRequests--
                if (pendingRequests == 0) callback.onSuccess(stats)
            }
            .addOnFailureListener { callback.onError(it) }
        
        // Contar empresas
        db.collection(COMPANIES_COLLECTION).get()
            .addOnSuccessListener { querySnapshot ->
                stats["companies"] = querySnapshot.size()
                pendingRequests--
                if (pendingRequests == 0) callback.onSuccess(stats)
            }
            .addOnFailureListener { callback.onError(it) }
        
        // Contar trabajos activos
        db.collection(JOBS_COLLECTION)
            .whereEqualTo("status", JobStatus.ACTIVE.name).get()
            .addOnSuccessListener { querySnapshot ->
                stats["activeJobs"] = querySnapshot.size()
                pendingRequests--
                if (pendingRequests == 0) callback.onSuccess(stats)
            }
            .addOnFailureListener { callback.onError(it) }
        
        // Contar aplicaciones
        db.collection(APPLICATIONS_COLLECTION).get()
            .addOnSuccessListener { querySnapshot ->
                stats["applications"] = querySnapshot.size()
                pendingRequests--
                if (pendingRequests == 0) callback.onSuccess(stats)
            }
            .addOnFailureListener { callback.onError(it) }
    }
    
    // ================================
    // AUTENTICACIÓN
    // ================================
    
    /**
     * Iniciar sesión con email y contraseña
     */
    fun loginUser(email: String, password: String, callback: AuthCallback) {
        Log.d("FirestoreService", "loginUser() called with email: $email")
        
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                Log.d("FirestoreService", "Firebase Auth login successful")
                val userId = authResult.user?.uid ?: ""
                callback.onSuccess(userId, false)
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreService", "Firebase Auth login failed", exception)
                
                // Si es error de configuración o usuario no encontrado, usar método alternativo
                if (exception.message?.contains("CONFIGURATION_NOT_FOUND") == true ||
                    exception.message?.contains("user-not-found") == true ||
                    exception.message?.contains("wrong-password") == true) {
                    Log.w("FirestoreService", "Using fallback login method due to: ${exception.message}")
                    loginUserFallback(email, password, callback)
                } else {
                    callback.onError(exception)
                }
            }
    }
    
    /**
     * Método alternativo de login (solo para desarrollo)
     * Busca usuario por email y valida credenciales
     */
    private fun loginUserFallback(email: String, password: String, callback: AuthCallback) {
        Log.d("FirestoreService", "Using fallback login method")
        
        // Buscar credenciales por email
        db.collection(USER_CREDENTIALS_COLLECTION)
            .whereEqualTo("email", email.trim())
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]
                    val credentials = document.toObject(UserCredentials::class.java)
                    
                    if (credentials != null) {
                        // Validar contraseña
                        val isValidPassword = PasswordUtils.verifyPassword(
                            password, 
                            credentials.salt, 
                            credentials.passwordHash
                        )
                        
                        if (isValidPassword) {
                            Log.d("FirestoreService", "Fallback login successful for user: ${credentials.userId}")
                            callback.onSuccess(credentials.userId, false)
                        } else {
                            Log.w("FirestoreService", "Invalid password in fallback login")
                            callback.onError(Exception("Credenciales inválidas"))
                        }
                    } else {
                        Log.e("FirestoreService", "Failed to parse credentials")
                        callback.onError(Exception("Error procesando credenciales"))
                    }
                } else {
                    Log.w("FirestoreService", "User credentials not found in fallback login")
                    callback.onError(Exception("Usuario no encontrado"))
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreService", "Fallback login failed", exception)
                callback.onError(Exception("Error en login alternativo: ${exception.message}"))
            }
    }
    
    /**
     * Registrar nuevo usuario con email y contraseña
     */
    fun registerUser(email: String, password: String, userData: User, callback: AuthCallback) {
        Log.d("FirestoreService", "registerUser() called with email: $email, userData: $userData")
        
        // Primero intentar el método normal
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                Log.d("FirestoreService", "Firebase Auth user created successfully")
                val userId = authResult.user?.uid ?: ""
                val userWithId = userData.copy(id = userId)
                
                // También crear credenciales para compatibilidad con fallback
                val salt = PasswordUtils.generateSalt()
                val passwordHash = PasswordUtils.hashPassword(password, salt)
                val credentials = UserCredentials(
                    userId = userId,
                    email = email.trim(),
                    passwordHash = passwordHash,
                    salt = salt
                )
                
                Log.d("FirestoreService", "Creating Firestore document and credentials for user: $userId")
                
                var userCreated = false
                var credentialsCreated = false
                var hasError = false
                
                // Crear documento del usuario
                createUser(userWithId, object : VoidCallback {
                    override fun onSuccess() {
                        Log.d("FirestoreService", "User document created successfully")
                        userCreated = true
                        if (credentialsCreated && !hasError) {
                            callback.onSuccess(userId, true)
                        }
                    }
                    
                    override fun onError(exception: Exception) {
                        Log.e("FirestoreService", "Failed to create user document", exception)
                        hasError = true
                        authResult.user?.delete()
                        callback.onError(exception)
                    }
                })
                
                // Crear credenciales para compatibilidad fallback
                db.collection(USER_CREDENTIALS_COLLECTION)
                    .document(userId)
                    .set(credentials)
                    .addOnSuccessListener {
                        Log.d("FirestoreService", "User credentials created successfully")
                        credentialsCreated = true
                        if (userCreated && !hasError) {
                            callback.onSuccess(userId, true)
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("FirestoreService", "Failed to create user credentials", exception)
                        hasError = true
                        authResult.user?.delete()
                        callback.onError(Exception("Error creando credenciales: ${exception.message}"))
                    }
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreService", "Firebase Auth registration failed", exception)
                
                // Si es error de configuración, usar método alternativo
                if (exception.message?.contains("CONFIGURATION_NOT_FOUND") == true) {
                    Log.w("FirestoreService", "Using fallback registration method")
                    registerUserFallback(email, password, userData, callback)
                } else {
                    callback.onError(exception)
                }
            }
    }
    
    /**
     * Método alternativo de registro (solo para desarrollo)
     * Crea usuario directamente en Firestore con ID generado
     */
    private fun registerUserFallback(email: String, password: String, userData: User, callback: AuthCallback) {
        Log.d("FirestoreService", "Using fallback registration method")
        
        // Generar ID único para el usuario
        val userId = db.collection(USERS_COLLECTION).document().id
        val userWithId = userData.copy(id = userId)
        
        // Crear credenciales seguras
        val salt = PasswordUtils.generateSalt()
        val passwordHash = PasswordUtils.hashPassword(password, salt)
        val credentials = UserCredentials(
            userId = userId,
            email = email.trim(),
            passwordHash = passwordHash,
            salt = salt
        )
        
        Log.d("FirestoreService", "Creating user with fallback method, ID: $userId")
        
        // Crear usuario y credenciales en paralelo
        var userCreated = false
        var credentialsCreated = false
        var hasError = false
        
        // Crear documento del usuario
        createUser(userWithId, object : VoidCallback {
            override fun onSuccess() {
                Log.d("FirestoreService", "User document created successfully")
                userCreated = true
                if (credentialsCreated && !hasError) {
                    callback.onSuccess(userId, true)
                }
            }
            
            override fun onError(exception: Exception) {
                Log.e("FirestoreService", "Failed to create user document", exception)
                hasError = true
                callback.onError(Exception("Error creando usuario: ${exception.message}"))
            }
        })
        
        // Crear credenciales del usuario
        db.collection(USER_CREDENTIALS_COLLECTION)
            .document(userId)
            .set(credentials)
            .addOnSuccessListener {
                Log.d("FirestoreService", "User credentials created successfully")
                credentialsCreated = true
                if (userCreated && !hasError) {
                    callback.onSuccess(userId, true)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreService", "Failed to create user credentials", exception)
                hasError = true
                callback.onError(Exception("Error creando credenciales: ${exception.message}"))
            }
    }
    
    /**
     * Enviar email de recuperación de contraseña
     */
    fun resetPassword(email: String, callback: VoidCallback) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener { callback.onSuccess() }
            .addOnFailureListener { callback.onError(it) }
    }
    
    /**
     * Cerrar sesión
     */
    fun signOut() {
        auth.signOut()
    }
    
    /**
     * Obtener usuario actualmente autenticado
     */
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
    
    /**
     * Verificar si hay usuario autenticado
     */
    fun isUserAuthenticated(): Boolean {
        return auth.currentUser != null
    }
    
    // ================================
    // PERFIL EXTENDIDO
    // ================================
    
    /**
     * Subir imagen de perfil
     */
    fun uploadProfileImage(userId: String, imageUri: Uri, callback: Callback<String>) {
        val storageRef = storage.reference.child("profile_images/$userId.jpg")
        
        storageRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                storageRef.downloadUrl
                    .addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        
                        // Actualizar URL en el documento del usuario
                        db.collection(USERS_COLLECTION)
                            .document(userId)
                            .update("profileImageUrl", imageUrl)
                            .addOnSuccessListener { callback.onSuccess(imageUrl) }
                            .addOnFailureListener { callback.onError(it) }
                    }
                    .addOnFailureListener { callback.onError(it) }
            }
            .addOnFailureListener { callback.onError(it) }
    }
    
    /**
     * Obtener perfil completo del usuario con experiencia y habilidades
     */
    fun getUserProfile(userId: String, callback: Callback<UserProfile?>) {
        db.collection(USER_PROFILES_COLLECTION)
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val profile = document.toObject(UserProfile::class.java)
                    callback.onSuccess(profile)
                } else {
                    callback.onSuccess(null)
                }
            }
            .addOnFailureListener { callback.onError(it) }
    }
    
    /**
     * Crear o actualizar perfil completo del usuario
     */
    fun saveUserProfile(profile: UserProfile, callback: VoidCallback) {
        db.collection(USER_PROFILES_COLLECTION)
            .document(profile.userId)
            .set(profile)
            .addOnSuccessListener { callback.onSuccess() }
            .addOnFailureListener { callback.onError(it) }
    }
    
    // ================================
    // NOTIFICACIONES
    // ================================
    
    /**
     * Crear nueva notificación
     */
    fun createNotification(notification: Notification, callback: Callback<String>) {
        val docRef = db.collection(NOTIFICATIONS_COLLECTION).document()
        val notificationWithId = notification.copy(id = docRef.id)
        
        docRef.set(notificationWithId)
            .addOnSuccessListener { callback.onSuccess(docRef.id) }
            .addOnFailureListener { callback.onError(it) }
    }
    
    /**
     * Obtener notificaciones del usuario
     */
    fun getUserNotifications(userId: String, callback: Callback<List<Notification>>) {
        db.collection(NOTIFICATIONS_COLLECTION)
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val notifications = querySnapshot.toObjects(Notification::class.java)
                callback.onSuccess(notifications)
            }
            .addOnFailureListener { callback.onError(it) }
    }
    
    /**
     * Marcar notificación como leída
     */
    fun markNotificationAsRead(notificationId: String, callback: VoidCallback) {
        db.collection(NOTIFICATIONS_COLLECTION)
            .document(notificationId)
            .update("read", true)
            .addOnSuccessListener { callback.onSuccess() }
            .addOnFailureListener { callback.onError(it) }
    }
    
    /**
     * Marcar todas las notificaciones del usuario como leídas
     */
    fun markAllNotificationsAsRead(userId: String, callback: VoidCallback) {
        db.collection(NOTIFICATIONS_COLLECTION)
            .whereEqualTo("userId", userId)
            .whereEqualTo("read", false)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val batch = db.batch()
                
                querySnapshot.documents.forEach { document ->
                    batch.update(document.reference, "read", true)
                }
                
                batch.commit()
                    .addOnSuccessListener { callback.onSuccess() }
                    .addOnFailureListener { callback.onError(it) }
            }
            .addOnFailureListener { callback.onError(it) }
    }
    
    /**
     * Contar notificaciones no leídas
     */
    fun getUnreadNotificationsCount(userId: String, callback: CountCallback) {
        db.collection(NOTIFICATIONS_COLLECTION)
            .whereEqualTo("userId", userId)
            .whereEqualTo("read", false)
            .get()
            .addOnSuccessListener { querySnapshot ->
                callback.onSuccess(querySnapshot.size())
            }
            .addOnFailureListener { callback.onError(it) }
    }
    
    // ================================
    // BÚSQUEDA AVANZADA
    // ================================
    
    /**
     * Búsqueda avanzada de trabajos con múltiples filtros
     */
    fun searchJobsAdvanced(
        title: String? = null,
        location: String? = null,
        modality: WorkType? = null,
        category: String? = null,
        employmentType: String? = null,
        salaryMin: Int? = null,
        experienceLevel: String? = null,
        companyName: String? = null,
        postedSince: Long? = null, // timestamp
        limit: Int = 20,
        callback: Callback<List<Job>>
    ) {
        var query: Query = db.collection(JOBS_COLLECTION)
            .whereEqualTo("status", JobStatus.ACTIVE.name)
        
        // Aplicar filtros
        title?.let {
            query = query.whereGreaterThanOrEqualTo("title", it)
                .whereLessThanOrEqualTo("title", it + '\uf8ff')
        }
        
        location?.let {
            query = query.whereEqualTo("location", it)
        }
        
        modality?.let {
            query = query.whereEqualTo("modality", it.name)
        }
        
        category?.let {
            query = query.whereEqualTo("category", it)
        }
        
        employmentType?.let {
            query = query.whereEqualTo("employmentType", it)
        }
        
        // Nota: companyName no está disponible directamente en el modelo Job simplificado
        // Para buscar por compañía se debe usar getJobsByCompany(companyId)
        
        postedSince?.let {
            query = query.whereGreaterThanOrEqualTo("postedDate", it)
        }
        
        query.limit(limit.toLong())
            .get()
            .addOnSuccessListener { querySnapshot ->
                var jobs = querySnapshot.toObjects(Job::class.java)
                
                // Filtros adicionales que no se pueden hacer directamente en Firestore
                salaryMin?.let { minSalary ->
                    jobs = jobs.filter { job ->
                        job.salary.replace(Regex("[^0-9]"), "").toIntOrNull()?.let { salary ->
                            salary >= minSalary
                        } ?: false
                    }
                }
                
                experienceLevel?.let { level ->
                    jobs = jobs.filter { job ->
                        job.aboutJob.contains(level, ignoreCase = true) ||
                        job.requirements.any { it.contains(level, ignoreCase = true) }
                    }
                }
                
                // Ordenar en el cliente
                callback.onSuccess(jobs.sortedByDescending { it.createdAt ?: 0L })
            }
            .addOnFailureListener { callback.onError(it) }
    }
    
    /**
     * Obtener trabajos recomendados para un usuario basado en su perfil
     */
    fun getRecommendedJobs(userId: String, limit: Int = 10, callback: Callback<List<Job>>) {
        // Primero obtener el perfil del usuario
        getUserProfile(userId, object : Callback<UserProfile?> {
            override fun onSuccess(profile: UserProfile?) {
                if (profile == null) {
                    // Si no hay perfil, devolver trabajos activos recientes
                    getActiveJobs(limit, callback)
                    return
                }
                
                // Buscar trabajos que coincidan con las habilidades del usuario
                var query: Query = db.collection(JOBS_COLLECTION)
                    .whereEqualTo("status", JobStatus.ACTIVE.name)
                
                // Si el usuario tiene una profesión preferida
                profile.profession?.let { profession ->
                    query = query.whereGreaterThanOrEqualTo("title", profession)
                        .whereLessThanOrEqualTo("title", profession + '\uf8ff')
                }
                
                query.limit(limit.toLong())
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        var jobs = querySnapshot.toObjects(Job::class.java)
                        
                        // Filtrar por habilidades del usuario
                        if (profile.skills.isNotEmpty()) {
                            jobs = jobs.filter { job ->
                                job.requirements.any { requirement ->
                                    profile.skills.any { skill ->
                                        requirement.contains(skill, ignoreCase = true)
                                    }
                                }
                            }
                        }
                        
                        // Ordenar por relevancia (trabajos con más coincidencias de habilidades primero)
                        jobs = jobs.sortedByDescending { job ->
                            job.requirements.count { requirement ->
                                profile.skills.any { skill ->
                                    requirement.contains(skill, ignoreCase = true)
                                }
                            }
                        }
                        
                        // Ordenar por relevancia y fecha
                        jobs = jobs.sortedWith(compareByDescending<Job> { job ->
                            job.requirements.count { requirement ->
                                profile.skills.any { skill ->
                                    requirement.contains(skill, ignoreCase = true)
                                }
                            }
                        }.thenByDescending { it.createdAt ?: 0L })
                        
                        callback.onSuccess(jobs.take(limit))
                    }
                    .addOnFailureListener { callback.onError(it) }
            }
            
            override fun onError(exception: Exception) {
                // Si falla obtener el perfil, devolver trabajos activos
                getActiveJobs(limit, callback)
            }
        })
    }
    
    /**
     * Obtener trabajos similares basado en un trabajo específico
     */
    fun getSimilarJobs(jobId: String, limit: Int = 5, callback: Callback<List<Job>>) {
        getJobById(jobId, object : Callback<Job?> {
            override fun onSuccess(job: Job?) {
                if (job == null) {
                    callback.onSuccess(emptyList())
                    return
                }
                
                // Buscar trabajos similares por compañía
                db.collection(JOBS_COLLECTION)
                    .whereEqualTo("status", JobStatus.ACTIVE.name)
                    .whereEqualTo("companyId", job.companyId)
                    .whereNotEqualTo("id", jobId) // Excluir el trabajo actual
                    .limit(limit.toLong())
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        val similarJobs = querySnapshot.toObjects(Job::class.java)
                            .sortedByDescending { it.createdAt }
                        callback.onSuccess(similarJobs)
                    }
                    .addOnFailureListener { callback.onError(it) }
            }
            
            override fun onError(exception: Exception) {
                callback.onError(exception)
            }
        })
    }
    
    // ================================
    // ANÁLISIS Y REPORTES
    // ================================
    
    /**
     * Obtener estadísticas avanzadas de la plataforma
     */
    fun getAdvancedStats(callback: Callback<AdvancedStats>) {
        val stats = AdvancedStats()
        var pendingRequests = 6
        
        // Usuarios activos (último mes)
        val lastMonth = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L)
        db.collection(USERS_COLLECTION)
            .whereGreaterThan("lastLogin", lastMonth)
            .get()
            .addOnSuccessListener { querySnapshot ->
                stats.activeUsers = querySnapshot.size()
                pendingRequests--
                if (pendingRequests == 0) callback.onSuccess(stats)
            }
            .addOnFailureListener { callback.onError(it) }
        
        // Aplicaciones del último mes
        db.collection(APPLICATIONS_COLLECTION)
            .whereGreaterThan("applicationDate", lastMonth)
            .get()
            .addOnSuccessListener { querySnapshot ->
                stats.recentApplications = querySnapshot.size()
                pendingRequests--
                if (pendingRequests == 0) callback.onSuccess(stats)
            }
            .addOnFailureListener { callback.onError(it) }
        
        // Trabajos por modalidad
        db.collection(JOBS_COLLECTION)
            .whereEqualTo("status", JobStatus.ACTIVE.name)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val jobs = querySnapshot.toObjects(Job::class.java)
                stats.jobsByModality = jobs.groupBy { it.modality }
                    .mapValues { it.value.size }
                pendingRequests--
                if (pendingRequests == 0) callback.onSuccess(stats)
            }
            .addOnFailureListener { callback.onError(it) }
        
        // Top empresas con más trabajos (usando companyId)
        db.collection(JOBS_COLLECTION)
            .whereEqualTo("status", JobStatus.ACTIVE.name)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val jobs = querySnapshot.toObjects(Job::class.java)
                val companyJobCounts = mutableMapOf<String, Int>()
                
                // Contar trabajos por companyId
                jobs.groupBy { it.companyId }
                    .forEach { (companyId, jobsList) ->
                        if (companyId.isNotEmpty()) {
                            companyJobCounts[companyId] = jobsList.size
                        }
                    }
                
                // Convertir IDs de compañía a nombres (esto sería ideal hacerlo de forma asíncrona)
                // Por ahora dejamos los IDs
                stats.topCompanies = companyJobCounts.toList()
                    .sortedByDescending { it.second }
                    .take(10)
                    .toMap()
                    
                pendingRequests--
                if (pendingRequests == 0) callback.onSuccess(stats)
            }
            .addOnFailureListener { callback.onError(it) }
        
        // Promedio de aplicaciones por trabajo
        db.collection(APPLICATIONS_COLLECTION)
            .get()
            .addOnSuccessListener { applicationsSnapshot ->
                db.collection(JOBS_COLLECTION)
                    .whereEqualTo("status", JobStatus.ACTIVE.name)
                    .get()
                    .addOnSuccessListener { jobsSnapshot ->
                        if (jobsSnapshot.size() > 0) {
                            stats.avgApplicationsPerJob = applicationsSnapshot.size().toDouble() / jobsSnapshot.size()
                        }
                        pendingRequests--
                        if (pendingRequests == 0) callback.onSuccess(stats)
                    }
                    .addOnFailureListener { callback.onError(it) }
            }
            .addOnFailureListener { callback.onError(it) }
        
        // Tasa de éxito de aplicaciones
        db.collection(APPLICATIONS_COLLECTION)
            .whereEqualTo("status", ApplicationStatus.ACCEPTED.name)
            .get()
            .addOnSuccessListener { acceptedSnapshot ->
                db.collection(APPLICATIONS_COLLECTION)
                    .get()
                    .addOnSuccessListener { totalSnapshot ->
                        if (totalSnapshot.size() > 0) {
                            stats.applicationSuccessRate = (acceptedSnapshot.size().toDouble() / totalSnapshot.size()) * 100
                        }
                        pendingRequests--
                        if (pendingRequests == 0) callback.onSuccess(stats)
                    }
                    .addOnFailureListener { callback.onError(it) }
            }
            .addOnFailureListener { callback.onError(it) }
    }
    
    // ================================
    // MÉTODOS HELPER PARA MODELOS ACTUALIZADOS
    // ================================
    
    /**
     * Obtener información completa de trabajo con datos de la compañía
     */
    fun getJobWithCompanyInfo(jobId: String, callback: Callback<Pair<Job?, Company?>>) {
        getJobById(jobId, object : Callback<Job?> {
            override fun onSuccess(job: Job?) {
                if (job != null && job.companyId.isNotEmpty()) {
                    getCompanyById(job.companyId, object : Callback<Company?> {
                        override fun onSuccess(company: Company?) {
                            callback.onSuccess(Pair(job, company))
                        }
                        
                        override fun onError(exception: Exception) {
                            // Devolver job sin compañía en caso de error
                            callback.onSuccess(Pair(job, null))
                        }
                    })
                } else {
                    callback.onSuccess(Pair(job, null))
                }
            }
            
            override fun onError(exception: Exception) {
                callback.onError(exception)
            }
        })
    }
    
    /**
     * Obtener usuario y perfil juntos
     */
    fun getUserWithProfile(userId: String, callback: Callback<Pair<User?, UserProfile?>>) {
        var user: User? = null
        var profile: UserProfile? = null
        var requestsCompleted = 0
        
        getUserById(userId, object : Callback<User?> {
            override fun onSuccess(result: User?) {
                user = result
                requestsCompleted++
                if (requestsCompleted == 2) {
                    callback.onSuccess(Pair(user, profile))
                }
            }
            
            override fun onError(exception: Exception) {
                requestsCompleted++
                if (requestsCompleted == 2) {
                    callback.onSuccess(Pair(user, profile))
                }
            }
        })
        
        getUserProfile(userId, object : Callback<UserProfile?> {
            override fun onSuccess(result: UserProfile?) {
                profile = result
                requestsCompleted++
                if (requestsCompleted == 2) {
                    callback.onSuccess(Pair(user, profile))
                }
            }
            
            override fun onError(exception: Exception) {
                requestsCompleted++
                if (requestsCompleted == 2) {
                    callback.onSuccess(Pair(user, profile))
                }
            }
        })
    }
    
    /**
     * Buscar trabajos por múltiples compañías
     */
    fun getJobsByCompanies(companyIds: List<String>, callback: Callback<List<Job>>) {
        if (companyIds.isEmpty()) {
            callback.onSuccess(emptyList())
            return
        }
        
        // Firestore limita las consultas "in" a 10 elementos
        val chunks = companyIds.chunked(10)
        val allJobs = mutableListOf<Job>()
        var pendingRequests = chunks.size
        
        chunks.forEach { chunk ->
            db.collection(JOBS_COLLECTION)
                .whereIn("companyId", chunk)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val jobs = querySnapshot.toObjects(Job::class.java)
                    allJobs.addAll(jobs)
                    pendingRequests--
                    
                    if (pendingRequests == 0) {
                        val sortedJobs = allJobs.sortedByDescending { it.createdAt }
                        callback.onSuccess(sortedJobs)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("FirestoreService", "Error getting jobs for company chunk", exception)
                    pendingRequests--
                    
                    if (pendingRequests == 0) {
                        val sortedJobs = allJobs.sortedByDescending { it.createdAt }
                        callback.onSuccess(sortedJobs)
                    }
                }
        }
    }
    
    /**
     * Verificar si un usuario es propietario de una compañía
     */
    fun isUserCompanyOwner(userId: String, companyId: String, callback: (Boolean) -> Unit) {
        getCompanyById(companyId, object : Callback<Company?> {
            override fun onSuccess(company: Company?) {
                callback(company?.ownerId == userId)
            }
            
            override fun onError(exception: Exception) {
                Log.e("FirestoreService", "Error checking company ownership", exception)
                callback(false)
            }
        })
    }
    
    /**
     * Crear trabajo con validación de propietario de compañía
     */
    fun createJobWithValidation(job: Job, ownerId: String, callback: Callback<String>) {
        if (job.companyId.isEmpty()) {
            callback.onError(Exception("CompanyId es requerido"))
            return
        }
        
        isUserCompanyOwner(ownerId, job.companyId) { isOwner ->
            if (isOwner) {
                createJob(job, callback)
            } else {
                callback.onError(Exception("No tienes permisos para crear trabajos para esta compañía"))
            }
        }
    }
}