package com.example.hirelink_2025.view.ui.activities


import android.content.Intent //Usado para cambiar de Activity
import android.os.Bundle //Representa datos pasados entre activities o guardados en estado.
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity //Permite usar componentes modernos (compatibilidad).
import androidx.navigation.NavController //Objeto que controla la navegación entre fragmentos.
import androidx.navigation.fragment.NavHostFragment //Es el contenedor (que muestra el fragmento actual en el gráfico de navegación).
import androidx.navigation.ui.AppBarConfiguration// Define qué fragmentos son principales (top level) para el comportamiento del botón "back" y título.
import androidx.navigation.ui.setupWithNavController//importa extensión para vincular vistas como BottomNavigationView
import com.example.hirelink_2025.R//Referencia a recursos como id, layout, drawable, etc.
import com.example.hirelink_2025.databinding.ActivityMainBinding //Clase ViewBinding del activity_main.xml

// Imports para notificaciones FCM
import com.google.firebase.messaging.FirebaseMessaging
import com.example.hirelink_2025.network.FirestoreService
import com.example.hirelink_2025.network.JobNotificationService

class MainActivity : AppCompatActivity() {// La pantalla principal de la App extienede de AppCompatActivity()
    //Esto permite usar compatibilidad con funciones modernas de UI (Solo aspectos visuales).

    private lateinit var binding: ActivityMainBinding //Vincula el layout activity_main.xml con código Kotlin a través del ViewBinding.
    private lateinit var navController: NavController //Controla la navegación entre fragmentos definidos en el nav graph.
    private lateinit var jobNotificationService: JobNotificationService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!isUserLoggedIn()) {//si el usuario no ha iniciado sesión...
            navigateToAuth() //-> lo mandamos a la pantalla de login
            return//Y Terminamos aquí mismo, evitando que los demás códigos se ejecuten.
        }

        binding = ActivityMainBinding.inflate(layoutInflater) //inicializamos el binding inflando el layout activity_main.xml
        //NOTA: Para usar el binding, primero debemos inflarlo, para eso usamos el método .inflate() y le pasamos el inflador, layoutInflater.
        setContentView(binding.root)//Establecemos el layout inflado como la vista principal
        //...Muestra la interfaz en pantalla

        setupBottomNavigation()
        
        // Configurar notificaciones FCM
        setupFirebaseMessaging()
        
        // Inicializar servicio de notificaciones de empleos
        setupJobNotificationService()
        
        // Manejar navegación desde notificaciones
        handleNotificationNavigation(intent)
    }

    private fun isUserLoggedIn(): Boolean {
        //getSharedPreferences: Método que obtiene (o crea si no existe) un archivo de preferencias con nombre "auth".
        val sharedPrefs = getSharedPreferences("auth", MODE_PRIVATE) //El mode private hace que la app sea el único con acceso a este archivo.
        return sharedPrefs.getBoolean("is_logged_in", false)//Aquí busca un valor booleano asociado a la clave is_logged_in, si no existe -> devuelve false (por defecto).
    }

    private fun navigateToAuth() {
        //Primero cambiamos de activity:
        val intent = Intent(this, AuthActivity::class.java) //almacenamos el intent hacia el AuthActivity (De esta actividad a AuthActivity)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK //Definimos el flags
        //NOTA: FLAG_ACTIVITY_NEW_TASK inicializa una nueva tarea en el stack de actividades.
        //FLAG_ACTIVITY_CLEAR_TASK elimina todas las actividades existentes en la tarea actual.
        //... BORRA CUALQUIER OTRA PANTALLA QUE SE HAYA ABIERTO ANTES.
        //EN resumen: Intent.flags = intent..... garantiza que AuthActivity se convierta en la única pantalla activa, y que el usuario no pueda volver a MainActivity tocando el botón atrás.

        startActivity(intent) //Lanza el nuevo intent
        finish() //Finaliza el main activity actual.. libera memoria
    }

    //Esta función se encarga de conectar la barra de navegación inferior(BottomNavigationView) con el sistema de navegación por fragmentos (Navigation Component), usando NavController
    //Además define los top leve destination, estas no muestran el botón "back" en l AppBar, porque son la raíz del flujo de navegación.
    private fun setupBottomNavigation() {//Configura la barra de navegación inferior.
        val navHostFragment = supportFragmentManager//Permite acceder a los fragmentos alojados en esta actividad (MainActivity).
            .findFragmentById(R.id.mainNavHostFragment) as NavHostFragment //el findFragmentById() busca un fragmento en nuestro layout por su ID
                                                                                //... En este caso estamos buscando al contenedor de fragmentos (el NavHostFragment)
        navController = navHostFragment.navController//inicializamos navController con el controlador de navegación del NavHostFragment.

        // CONFIGURAR TOP-LEVEL DESTINATIONS
        val appBarConfiguration = AppBarConfiguration(//Configuramos el AppBar en función del destino actual. Fragmentos top level:
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

        // Listener personalizado para mantener botones activos.
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

    // ========================================
    // MÉTODOS PARA NOTIFICACIONES FCM
    // ========================================

    /**
     * Configura Firebase Cloud Messaging
     */
    private fun setupFirebaseMessaging() {
        Log.d("MainActivity", "Setting up Firebase Cloud Messaging")
        
        // Obtener token FCM
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("MainActivity", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            Log.d("MainActivity", "FCM Token: $token")
            
            // Guardar token en Firestore
            val firestoreService = FirestoreService()
            val currentUserId = firestoreService.getCurrentUserId()
            
            if (currentUserId != null) {
                firestoreService.saveFCMToken(currentUserId, token)
                Log.d("MainActivity", "FCM token saved for user: $currentUserId")
            } else {
                Log.w("MainActivity", "No authenticated user, cannot save FCM token")
            }
        }
    }

    /**
     * Maneja la navegación cuando la app se abre desde una notificación
     */
    private fun handleNotificationNavigation(intent: Intent?) {
        if (intent == null) return
        
        val navigationDestination = intent.getStringExtra("navigation_destination")
        val jobId = intent.getStringExtra("job_id")
        
        Log.d("MainActivity", "Handling notification navigation - Destination: $navigationDestination, JobId: $jobId")
        
        if (navigationDestination == "job_detail" && !jobId.isNullOrEmpty()) {
            // Navegar al detalle de la oferta laboral
            navigateToJobDetail(jobId)
        }
    }

    /**
     * Navega al detalle de una oferta laboral específica
     */
    private fun navigateToJobDetail(jobId: String) {
        try {
            Log.d("MainActivity", "Navigating to job detail: $jobId")
            
            // Crear bundle con el jobId
            val bundle = Bundle().apply {
                putString("jobId", jobId)
            }
            
            // Navegar al fragment de detalle de trabajo
            navController.navigate(R.id.jobDetailFragment, bundle)
            
            Log.d("MainActivity", "Navigation to job detail successful")
            
        } catch (e: Exception) {
            Log.e("MainActivity", "Error navigating to job detail", e)
            Toast.makeText(this, "Error al abrir la oferta laboral", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Configura el servicio de notificaciones de empleos
     */
    private fun setupJobNotificationService() {
        Log.d("MainActivity", "Setting up job notification service")
        
        jobNotificationService = JobNotificationService(this)
        jobNotificationService.startMonitoring()
        
        Log.d("MainActivity", "Job notification service started")
    }

    /**
     * Maneja nuevos intents cuando la app ya está abierta
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("MainActivity", "onNewIntent called")
        setIntent(intent)
        handleNotificationNavigation(intent)
    }

    /**
     * Detiene el servicio de notificaciones cuando la actividad se destruye
     */
    override fun onDestroy() {
        super.onDestroy()
        if (::jobNotificationService.isInitialized) {
            jobNotificationService.stopMonitoring()
            Log.d("MainActivity", "Job notification service stopped")
        }
    }
}