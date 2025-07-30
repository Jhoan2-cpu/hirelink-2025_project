package com.example.hirelink_2025.view.ui.fragments.profile

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import com.example.hirelink_2025.R
import com.example.hirelink_2025.databinding.FragmentProfileBinding
import com.example.hirelink_2025.view.adapter.ProfilePagerAdapter
import com.google.android.material.tabs.TabLayoutMediator

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var pagerAdapter: ProfilePagerAdapter

    // Variables para los datos del usuario
    private var userName: String = "Nombre del usuario"
    private var userProfession: String = "Ingeniero de Sistemas"
    private var userLocation: String = "Chimbote, Perú"
    private var userPhone: String = "+51 980440594"
    private var userEmail: String = "usuario@gmail.com"
    private var profileImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadUserData()
        setupUserProfile()
        setupViewPager()
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        // Recargar datos cuando regresamos del modo edición
        loadUserData()
        setupUserProfile()
    }

    private fun loadUserData() {
        // Cargar datos desde SharedPreferences (guardados en ProfileEditFragment)
        val sharedPrefs = requireContext().getSharedPreferences("user_profile", Context.MODE_PRIVATE)

        // Cargar datos básicos (si existen, usar los guardados; si no, usar defaults)
        userProfession = sharedPrefs.getString("education", "Ingeniero de Sistemas") ?: "Ingeniero de Sistemas"
        userLocation = sharedPrefs.getString("location", "Chimbote, Perú") ?: "Chimbote, Perú"
        userPhone = sharedPrefs.getString("phone", "+51 980440594") ?: "+51 980440594"
        userEmail = sharedPrefs.getString("email", "usuario@gmail.com") ?: "usuario@gmail.com"

        // Cargar nombre de usuario (puedes agregar esto al sistema de edición si quieres)
        userName = sharedPrefs.getString("user_name", "Nombre del usuario") ?: "Nombre del usuario"

        // Cargar imagen de perfil si existe
        val imageUriString = sharedPrefs.getString("profile_image_uri", null)
        profileImageUri = if (imageUriString != null) Uri.parse(imageUriString) else null
    }

    private fun setupUserProfile() {
        with(binding) {
            // Información básica del usuario
            userName.text = this@ProfileFragment.userName
            userProfession.text = this@ProfileFragment.userProfession
            userLocation.text = this@ProfileFragment.userLocation

            // Información de contacto
            userPhone.text = this@ProfileFragment.userPhone
            userEmail.text = this@ProfileFragment.userEmail

            // Imagen de perfil
            profileImageUri?.let { uri ->
                try {
                    profileImage.setImageURI(uri)
                } catch (e: Exception) {
                    // Si hay error cargando la imagen, usar la por defecto
                    profileImage.setImageResource(R.drawable.profile_random)
                }
            }
        }
    }

    private fun setupViewPager() {
        // Setup ViewPager2 con TabLayout para modo VISTA (solo lectura)
        pagerAdapter = ProfilePagerAdapter(requireActivity())
        binding.profileViewPager.adapter = pagerAdapter

        // Conectar TabLayout con ViewPager2
        TabLayoutMediator(binding.profileTabLayout, binding.profileViewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.about_me)
                1 -> getString(R.string.experience)
                2 -> getString(R.string.skills)
                else -> ""
            }
        }.attach()
    }

    private fun setupClickListeners() {
        // Settings button
        binding.settingsButton.setOnClickListener {
            showSettingsMenu()
        }

        // Edit FAB - NAVEGAR AL MODO EDICIÓN
        binding.editFab.setOnClickListener {
            navigateToEditProfile()
        }

        // Profile image click
        binding.profileImage.setOnClickListener {
            Toast.makeText(requireContext(), "Usa el botón de edición para cambiar la foto", Toast.LENGTH_SHORT).show()
        }

        // Contact info clicks
        binding.userPhone.setOnClickListener {
            dialPhoneNumber(binding.userPhone.text.toString())
        }

        binding.userEmail.setOnClickListener {
            sendEmail(binding.userEmail.text.toString())
        }
    }

    private fun navigateToEditProfile() {
        try {
            // Navegar al ProfileEditFragment
            findNavController().navigate(R.id.action_profileFragment_to_profileEditFragment)
        } catch (e: Exception) {
            // Si no existe la acción de navegación, mostrar mensaje
            Toast.makeText(
                requireContext(),
                "Configura la navegación en nav_graph.xml:\n" +
                        "action_profileFragment_to_profileEditFragment",
                Toast.LENGTH_LONG
            ).show()

            // TODO: Descomentar cuando tengas configurada la navegación
            // findNavController().navigate(R.id.action_profileFragment_to_profileEditFragment)
        }
    }

    private fun showSettingsMenu() {
        // Show settings options
        val options = arrayOf(
            "Configuración de cuenta",
            "Privacidad",
            "Notificaciones",
            "Ayuda",
            "Limpiar datos",
            "Cerrar sesión"
        )

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Configuración")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> Toast.makeText(requireContext(), "Configuración de cuenta", Toast.LENGTH_SHORT).show()
                1 -> Toast.makeText(requireContext(), "Privacidad", Toast.LENGTH_SHORT).show()
                2 -> Toast.makeText(requireContext(), "Notificaciones", Toast.LENGTH_SHORT).show()
                3 -> Toast.makeText(requireContext(), "Ayuda", Toast.LENGTH_SHORT).show()
                4 -> showClearDataConfirmation()
                5 -> showLogoutConfirmation()
            }
        }
        builder.show()
    }

    private fun showClearDataConfirmation() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Limpiar Datos")
        builder.setMessage("¿Estás seguro de que quieres eliminar todos los datos del perfil? Esta acción no se puede deshacer.")

        builder.setPositiveButton("Sí, limpiar") { _, _ ->
            clearAllProfileData()
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun clearAllProfileData() {
        // Limpiar SharedPreferences
        val sharedPrefs = requireContext().getSharedPreferences("user_profile", Context.MODE_PRIVATE)
        sharedPrefs.edit().clear().apply()

        // Recargar datos (volverán a los defaults)
        loadUserData()
        setupUserProfile()

        Toast.makeText(requireContext(), "Datos del perfil eliminados", Toast.LENGTH_SHORT).show()
    }

    private fun showLogoutConfirmation() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Cerrar sesión")
        builder.setMessage("¿Estás seguro de que quieres cerrar sesión?")
        builder.setPositiveButton("Sí") { _, _ ->
            // TODO: Implementar logout logic
            Toast.makeText(requireContext(), "Sesión cerrada", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun dialPhoneNumber(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "No se pudo abrir el marcador", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendEmail(email: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
            putExtra(Intent.EXTRA_SUBJECT, "Contacto desde HireLink")
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "No se pudo abrir el cliente de email", Toast.LENGTH_SHORT).show()
        }
    }

    // Métodos para proporcionar datos a los fragments de las tabs
    fun getCurrentUserData(): UserData {
        return UserData(
            name = userName,
            profession = userProfession,
            location = userLocation,
            phone = userPhone,
            email = userEmail,
            aboutMe = getCurrentAboutMe(),
            experiences = getCurrentExperiences(),
            skills = getCurrentSkills()
        )
    }

    private fun getCurrentAboutMe(): String {
        val sharedPrefs = requireContext().getSharedPreferences("user_profile", Context.MODE_PRIVATE)
        return sharedPrefs.getString("about_me", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.")
            ?: "Lorem ipsum dolor sit amet, consectetur adipiscing elit..."
    }

    private fun getCurrentExperiences(): List<Experience> {
        // TODO: Implementar carga desde SharedPreferences o Room
        // Por ahora retorna datos de ejemplo
        return listOf(
            Experience(
                position = "Analista",
                company = "Universidad Nacional Del Santo",
                description = "Desarrollador técnico en la empresa x trabajando 2 años.",
                years = 2
            )
        )
    }

    private fun getCurrentSkills(): List<String> {
        // TODO: Implementar carga desde SharedPreferences o Room
        // Por ahora retorna datos de ejemplo
        return listOf(
            "Programador en Python",
            "Manejo de Power BI"
        )
    }

    // Data classes para manejar los datos
    data class UserData(
        val name: String,
        val profession: String,
        val location: String,
        val phone: String,
        val email: String,
        val aboutMe: String,
        val experiences: List<Experience>,
        val skills: List<String>
    )

    data class Experience(
        val position: String,
        val company: String,
        val description: String,
        val years: Int
    )
}