package com.example.hirelink_2025.ui.activities


import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.hirelink_2025.R
import com.example.hirelink_2025.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!isUserLoggedIn()) {
            navigateToAuth()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()
    }

    private fun isUserLoggedIn(): Boolean {
        val sharedPrefs = getSharedPreferences("auth", MODE_PRIVATE)
        return sharedPrefs.getBoolean("is_logged_in", false)
    }

    private fun navigateToAuth() {
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun setupBottomNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.mainNavHostFragment) as NavHostFragment
        navController = navHostFragment.navController

        // CONFIGURAR TOP-LEVEL DESTINATIONS
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.searchFragment,     // Top level
                R.id.profileFragment,    // Top level
                R.id.myApplicationsFragment,
                R.id.myAdsFragment // Top level
                // NO incluir: resultFragment, jobDetailsFragment, etc.
            )
        )

        // Configurar con AppBarConfiguration
        binding.bottomNavigation.setupWithNavController(navController)

        // Listener personalizado para mantener botones activos
        navController.addOnDestinationChangedListener { _, destination, _ ->
            updateBottomNavigationSelection(destination.id)
        }
    }

    private fun updateBottomNavigationSelection(destinationId: Int) {
        val activeItemId = when (destinationId) {
            // Search section (mantener searchFragment activo)
            R.id.searchFragment,
            R.id.searchResultListFragment,
            R.id.jobDescriptionDetailFragment,
            R.id.searchFilterMenuFragment,
            R.id.jobLocationDetailDialog,
            R.id.searchFilterDetailFragment-> R.id.searchFragment

            // Profile section (mantener profileFragment activo)
            R.id.profileFragment,
            R.id.profileEditFragment,
            R.id.profileEditExperienceAddFragment -> R.id.profileFragment

            // myApplicactions section (mantener jobsFragment activo)
            R.id.myApplicationsFragment,
            R.id.applicationDetailFragment -> R.id.myApplicationsFragment

            // myAds section (mantener jobsFragment activo)
            R.id.myAdsFragment,
            R.id.myAdDetailFragment,
            R.id.myAdsApplicantsFragment,
            R.id.myAdsEditFragment,
            R.id.myAdsRegisterFragment,
            R.id.jobLocationDetailDialog,
            R.id.profileViewFragment,
            R.id.applicantProfileFragment,
            R.id.editLocationFragment-> R.id.myAdsFragment

            // Default
            else -> destinationId
        }

        // Actualizar selección en BottomNavigation
        binding.bottomNavigation.menu.findItem(activeItemId)?.isChecked = true
    }
}