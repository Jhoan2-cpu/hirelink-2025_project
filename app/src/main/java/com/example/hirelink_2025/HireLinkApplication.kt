package com.example.hirelink_2025

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HireLinkApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        Log.d("HireLinkApplication", "Initializing Firebase")
        
        // Inicializar Firebase
        FirebaseApp.initializeApp(this)
        
        // Configurar Firebase Auth para desarrollo
        try {
            val auth = FirebaseAuth.getInstance()
            auth.firebaseAuthSettings.setAppVerificationDisabledForTesting(true)
            Log.d("HireLinkApplication", "App verification disabled for testing")
        } catch (e: Exception) {
            Log.e("HireLinkApplication", "Error configuring Firebase Auth", e)
        }
        
        // Configurar Firestore
        try {
            val firestore = FirebaseFirestore.getInstance()
            // Habilitar logging offline para desarrollo
            firestore.enableNetwork()
            Log.d("HireLinkApplication", "Firestore configured successfully")
        } catch (e: Exception) {
            Log.e("HireLinkApplication", "Error configuring Firestore", e)
        }
    }
}