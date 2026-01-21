package com.example.hirelink_2025.network

/**
 * Interfaz genérica para manejar callbacks de operaciones asíncronas con Firestore
 */
interface Callback<T> {
    /**
     * Se ejecuta cuando la operación es exitosa
     * @param result Los datos obtenidos de la operación
     */
    fun onSuccess(result: T)
    
    /**
     * Se ejecuta cuando ocurre un error durante la operación
     * @param exception La excepción que causó el error
     */
    fun onError(exception: Exception)
}

/**
 * Callback específico para operaciones que no retornan datos (void)
 */
interface VoidCallback {
    /**
     * Se ejecuta cuando la operación es exitosa
     */
    fun onSuccess()
    
    /**
     * Se ejecuta cuando ocurre un error durante la operación
     * @param exception La excepción que causó el error
     */
    fun onError(exception: Exception)
}

/**
 * Callback para operaciones de autenticación
 */
interface AuthCallback {
    /**
     * Se ejecuta cuando la autenticación es exitosa
     * @param userId ID del usuario autenticado
     * @param isNewUser Indica si es un usuario nuevo (registro) o existente (login)
     */
    fun onSuccess(userId: String, isNewUser: Boolean = false)
    
    /**
     * Se ejecuta cuando la autenticación falla
     * @param exception La excepción que causó el error
     */
    fun onError(exception: Exception)
}

/**
 * Callback para operaciones de conteo
 */
interface CountCallback {
    /**
     * Se ejecuta cuando la operación de conteo es exitosa
     * @param count El número de elementos contados
     */
    fun onSuccess(count: Int)
    
    /**
     * Se ejecuta cuando ocurre un error durante el conteo
     * @param exception La excepción que causó el error
     */
    fun onError(exception: Exception)
}

/**
 * Callback para verificaciones booleanas
 */
interface ExistsCallback {
    /**
     * Se ejecuta cuando la verificación es exitosa
     * @param exists True si el elemento existe, false en caso contrario
     */
    fun onSuccess(exists: Boolean)
    
    /**
     * Se ejecuta cuando ocurre un error durante la verificación
     * @param exception La excepción que causó el error
     */
    fun onError(exception: Exception)
}