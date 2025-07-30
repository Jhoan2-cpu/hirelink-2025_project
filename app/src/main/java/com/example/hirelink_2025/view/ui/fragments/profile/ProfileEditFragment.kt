package com.example.hirelink_2025.view.ui.fragments.profile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import com.example.hirelink_2025.databinding.FragmentProfileEditBinding
import com.example.hirelink_2025.view.adapter.ProfileEditPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator

class ProfileEditFragment : Fragment() {

    private lateinit var binding: FragmentProfileEditBinding
    private lateinit var pagerAdapter: ProfileEditPagerAdapter

    // Variables para los datos editables
    private var currentEducation: String = ""
    private var currentLocation: String = ""
    private var currentPhone: String = ""
    private var currentEmail: String = ""
    private var profileImageUri: Uri? = null

    // Launcher para seleccionar imagen
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                profileImageUri = uri
                binding.profileImage.setImageURI(uri)
                Toast.makeText(requireContext(), "Imagen seleccionada", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadCurrentData()
        setupUI()
        setupViewPager()
        setupClickListeners()
    }

    private fun loadCurrentData() {
        // Cargar datos actuales del usuario (SharedPreferences, Room, etc.)
        // Por ahora usamos datos de ejemplo
        currentEducation = "Ingeniero de Sistemas"
        currentLocation = "Chimbote, Perú"
        currentPhone = "+51 980440594"
        currentEmail = "usuario@gmail.com"

        // Llenar los inputs con datos actuales
        with(binding) {
            educationInput.setText(currentEducation)
            locationInput.setText(currentLocation)
            phoneInput.setText(currentPhone)
            emailInput.setText(currentEmail)
        }
    }

    private fun setupUI() {
        // Configurar la interfaz inicial
        binding.userName.text = "Nombre del usuario"
    }

    private fun setupViewPager() {
        // Setup ViewPager2 con TabLayout para modo edición
        pagerAdapter = ProfileEditPagerAdapter(requireActivity())
        binding.profileViewPager.adapter = pagerAdapter

        // Conectar TabLayout con ViewPager2
        TabLayoutMediator(binding.profileTabLayout, binding.profileViewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Acerca de mí"
                1 -> "Experiencia"
                2 -> "Habilidades"
                else -> ""
            }
        }.attach()
    }

    private fun setupClickListeners() {
        // Botón Guardar
        binding.saveButton.setOnClickListener {
            saveProfileChanges()
        }

        // Botón Cancelar
        binding.cancelButton.setOnClickListener {
            showCancelConfirmation()
        }

        // Click en imagen de perfil para cambiarla
        binding.profileImage.setOnClickListener {
            openImagePicker()
        }

        binding.profileImageCard.setOnClickListener {
            openImagePicker()
        }
    }

    private fun saveProfileChanges() {
        // Validar campos
        val education = binding.educationInput.text.toString().trim()
        val location = binding.locationInput.text.toString().trim()
        val phone = binding.phoneInput.text.toString().trim()
        val email = binding.emailInput.text.toString().trim()

        // Validaciones básicas
        if (education.isEmpty()) {
            binding.educationInput.error = "La educación es requerida"
            return
        }

        if (location.isEmpty()) {
            binding.locationInput.error = "La ubicación es requerida"
            return
        }

        if (phone.isEmpty()) {
            binding.phoneInput.error = "El teléfono es requerido"
            return
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInput.error = "Email válido es requerido"
            return
        }

        // Guardar cambios
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Guardar Cambios")
        builder.setMessage("¿Estás seguro de que quieres guardar los cambios en tu perfil?")

        builder.setPositiveButton("Guardar") { _, _ ->
            // TODO: Implementar guardado real (SharedPreferences, Room, API)
            saveDataToStorage(education, location, phone, email)

            Toast.makeText(requireContext(), "Perfil actualizado exitosamente", Toast.LENGTH_LONG).show()

            // Regresar al perfil principal
            findNavController().popBackStack()
        }

        builder.setNegativeButton("Continuar editando") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun saveDataToStorage(education: String, location: String, phone: String, email: String) {
        // Implementar guardado real de datos
        val sharedPrefs = requireContext().getSharedPreferences("user_profile", Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            putString("education", education)
            putString("location", location)
            putString("phone", phone)
            putString("email", email)
            if (profileImageUri != null) {
                putString("profile_image_uri", profileImageUri.toString())
            }
            apply()
        }
    }

    private fun showCancelConfirmation() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Cancelar Edición")
        builder.setMessage("¿Estás seguro de que quieres cancelar? Se perderán todos los cambios no guardados.")

        builder.setPositiveButton("Sí, cancelar") { _, _ ->
            findNavController().popBackStack()
        }

        builder.setNegativeButton("Continuar editando") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }

        try {
            imagePickerLauncher.launch(Intent.createChooser(intent, "Seleccionar imagen de perfil"))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "No se pudo abrir el selector de imágenes", Toast.LENGTH_SHORT).show()
        }
    }

    // Métodos para obtener datos actuales de las tabs
    fun getCurrentAboutMe(): String {
        // Obtener texto actual de "Acerca de mí" desde el fragment correspondiente
        return "Lorem ipsum dolor sit amet, consectetur adipiscing elit..."
    }

    fun getCurrentExperiences(): List<Experience> {
        // Obtener experiencias actuales
        return listOf(
            Experience(
                position = "Analista",
                company = "Universidad Nacional Del Santo",
                description = "Desarrollador técnico en la empresa x trabajando 2 años.",
                years = 2
            )
        )
    }

    fun getCurrentSkills(): List<String> {
        // Obtener habilidades actuales
        return listOf(
            "Programador en Python",
            "Manejo de Power BI"
        )
    }

    // Data class para experiencias
    data class Experience(
        val position: String,
        val company: String,
        val description: String,
        val years: Int
    )
}

// NOTA: El ProfileEditPagerAdapter está en un archivo separado:
// ProfileEditPagerAdapter.kt en el paquete com.example.hirelink_2025.view.adapter