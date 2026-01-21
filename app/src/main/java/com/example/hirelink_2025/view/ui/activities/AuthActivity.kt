package com.example.hirelink_2025.view.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.hirelink_2025.R
import com.example.hirelink_2025.databinding.ActivityAuthBinding

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("AuthActivity", "onCreate called")

        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)//Muestra la interfaz en pantalla

        setupNavigation()//Prepara la navegación
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager//
            .findFragmentById(R.id.auth_nav_host_fragment) as NavHostFragment //Encontramos el navHostFragment definido en el activity_auth.xml
        navController = navHostFragment.navController
        Log.d("AuthActivity", "Navigation setup complete")
    }

    // Método público para navegar a MainActivity
    fun navigateToMain() {
        Log.d("AuthActivity", "navigateToMain called")

        // Guardar estado de login
        val sharedPrefs = getSharedPreferences("auth", MODE_PRIVATE)
        sharedPrefs.edit().putBoolean("is_logged_in", true).apply()
        Log.d("AuthActivity", "Login state saved")

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
        Log.d("AuthActivity", "Navigating to MainActivity")
    }
}