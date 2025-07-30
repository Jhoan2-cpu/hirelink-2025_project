package com.example.hirelink_2025.network

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.UUID

/**
 * Servicio para manejar operaciones de Firebase Storage
 */
class FirebaseStorageService {
    
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val storageRef: StorageReference = storage.reference
    
    companion object {
        private const val COMPANY_LOGOS_PATH = "company_logos"
        private const val MAX_FILE_SIZE_MB = 5L * 1024 * 1024 // 5MB
    }
    
    /**
     * Subir logo de compañía a Firebase Storage
     */
    fun uploadCompanyLogo(
        imageUri: Uri,
        companyId: String,
        callback: Callback<String>
    ) {
        Log.d("FirebaseStorageService", "Uploading company logo for company: $companyId")
        
        val fileName = "${companyId}_${UUID.randomUUID()}.jpg"
        val logoRef = storageRef.child("$COMPANY_LOGOS_PATH/$fileName")
        
        // Subir archivo
        val uploadTask = logoRef.putFile(imageUri)
        
        uploadTask
            .addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                Log.d("FirebaseStorageService", "Upload progress: $progress%")
            }
            .addOnSuccessListener { taskSnapshot ->
                Log.d("FirebaseStorageService", "Upload successful")
                
                // Obtener URL de descarga
                logoRef.downloadUrl
                    .addOnSuccessListener { downloadUrl ->
                        Log.d("FirebaseStorageService", "Download URL obtained: $downloadUrl")
                        callback.onSuccess(downloadUrl.toString())
                    }
                    .addOnFailureListener { exception ->
                        Log.e("FirebaseStorageService", "Failed to get download URL", exception)
                        handleStorageError(exception, callback)
                    }
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseStorageService", "Upload failed", exception)
                handleStorageError(exception, callback)
            }
    }
    
    /**
     * Subir logo de compañía temporal (antes de tener ID de compañía)
     */
    fun uploadTemporaryCompanyLogo(
        imageUri: Uri,
        callback: Callback<String>
    ) {
        Log.d("FirebaseStorageService", "Uploading temporary company logo")
        
        
        val fileName = "temp_${UUID.randomUUID()}.jpg"
        val logoRef = storageRef.child("$COMPANY_LOGOS_PATH/$fileName")
        
        // Subir archivo
        val uploadTask = logoRef.putFile(imageUri)
        
        uploadTask
            .addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                Log.d("FirebaseStorageService", "Upload progress: $progress%")
            }
            .addOnSuccessListener { taskSnapshot ->
                Log.d("FirebaseStorageService", "Temporary upload successful")
                
                // Obtener URL de descarga
                logoRef.downloadUrl
                    .addOnSuccessListener { downloadUrl ->
                        Log.d("FirebaseStorageService", "Temporary download URL obtained: $downloadUrl")
                        callback.onSuccess(downloadUrl.toString())
                    }
                    .addOnFailureListener { exception ->
                        Log.e("FirebaseStorageService", "Failed to get temporary download URL", exception)
                        handleStorageError(exception, callback)
                    }
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseStorageService", "Temporary upload failed", exception)
                handleStorageError(exception, callback)
            }
    }
    
    /**
     * Eliminar logo de compañía del storage
     */
    fun deleteCompanyLogo(
        logoUrl: String,
        callback: VoidCallback
    ) {
        if (logoUrl.isEmpty()) {
            Log.d("FirebaseStorageService", "No logo URL provided, nothing to delete")
            callback.onSuccess()
            return
        }
        
        Log.d("FirebaseStorageService", "Deleting company logo: $logoUrl")
        
        try {
            val logoRef = storage.getReferenceFromUrl(logoUrl)
            
            logoRef.delete()
                .addOnSuccessListener {
                    Log.d("FirebaseStorageService", "Logo deleted successfully")
                    callback.onSuccess()
                }
                .addOnFailureListener { exception ->
                    Log.e("FirebaseStorageService", "Failed to delete logo", exception)
                    callback.onError(exception)
                }
        } catch (e: Exception) {
            Log.e("FirebaseStorageService", "Invalid logo URL", e)
            callback.onError(e)
        }
    }
    
    /**
     * Validar si el archivo es una imagen válida
     */
    fun isValidImageFile(uri: Uri): Boolean {
        val contentResolver = storage.app.applicationContext.contentResolver
        val mimeType = contentResolver.getType(uri)
        
        return mimeType?.startsWith("image/") == true
    }
    
    /**
     * Obtener el tamaño del archivo
     */
    fun getFileSize(uri: Uri): Long {
        val contentResolver = storage.app.applicationContext.contentResolver
        
        return try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.available().toLong()
            } ?: 0L
        } catch (e: Exception) {
            Log.e("FirebaseStorageService", "Error getting file size", e)
            0L
        }
    }
    
    /**
     * Validar tamaño de archivo
     */
    fun isValidFileSize(uri: Uri): Boolean {
        val fileSize = getFileSize(uri)
        return fileSize <= MAX_FILE_SIZE_MB
    }
    
    
    /**
     * Manejar errores de Storage con fallback a logo vacío
     */
    private fun handleStorageError(exception: Exception, callback: Callback<String>) {
        val errorMessage = exception.message ?: "Unknown error"
        
        when {
            errorMessage.contains("404") || errorMessage.contains("Not Found") -> {
                Log.w("FirebaseStorageService", "Storage bucket not configured, using empty logo")
                callback.onSuccess("")
            }
            errorMessage.contains("Object does not exist") -> {
                Log.w("FirebaseStorageService", "Storage object issue, using empty logo")
                callback.onSuccess("")
            }
            else -> {
                // Para otros errores, devolver error real
                callback.onError(exception)
            }
        }
    }
}