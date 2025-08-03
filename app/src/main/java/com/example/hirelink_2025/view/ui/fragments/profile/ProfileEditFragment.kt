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
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.hirelink_2025.R
import com.example.hirelink_2025.databinding.FragmentProfileEditBinding
import com.example.hirelink_2025.models.User
import com.example.hirelink_2025.models.UserProfile
import com.example.hirelink_2025.viewmodels.ProfileViewModel
import com.example.hirelink_2025.viewmodels.ViewModelFactory
import kotlinx.coroutines.launch

class ProfileEditFragment : Fragment() {

    private lateinit var binding: FragmentProfileEditBinding
    
    // ViewModel con Factory (MVVM)
    private val viewModel: ProfileViewModel by viewModels {
        ViewModelFactory()
    }

    private var profileImageUri: Uri? = null

    // Launcher para seleccionar imagen
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                profileImageUri = uri
                
                // Preview inmediato con Glide
                try {
                    Glide.with(this)
                        .load(uri)
                        .apply(
                            RequestOptions()
                                .placeholder(R.drawable.ic_profile)
                                .error(R.drawable.ic_profile)
                                .transform(CircleCrop())
                        )
                        .into(binding.profileImage)
                } catch (e: Exception) {
                    android.util.Log.e("ProfileEditFragment", "Error showing image preview", e)
                    binding.profileImage.setImageURI(uri) // Fallback
                }
                
                // Subir imagen inmediatamente al seleccionarla
                uploadProfileImage(uri)
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

        setupAvailabilityDropdown()
        setupClickListeners()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                if (state.isLoading) {
                    // TODO: Mostrar loading
                }
                
                state.error?.let { error ->
                    Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                }

                state.user?.let { user ->
                    loadUserData(user)
                }

                state.userProfile?.let { profile ->
                    loadUserProfileData(profile)
                }
                
                if (state.isProfileUpdated) {
                    Toast.makeText(requireContext(), "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show()
                    // Resetear el flag antes de navegar para que ProfileFragment pueda manejar la actualización
                    viewModel.resetProfileUpdated()
                    findNavController().popBackStack()
                }
            }
        }
    }
    
    private fun loadUserData(user: User) {
        with(binding) {
            userName.text = user.name
            emailInput.setText(user.email)
            phoneInput.setText(user.phone ?: "")
            
            // Cargar imagen de perfil si existe
            user.profileImageUrl?.let { imageUrl ->
                if (imageUrl.isNotEmpty()) {
                    loadProfileImage(imageUrl)
                }
            }
        }
    }
    
    private fun loadUserProfileData(profile: UserProfile) {
        with(binding) {
            professionInput.setText(profile.profession ?: "")
            bioInput.setText(profile.bio)
            skillsInput.setText(profile.skills.joinToString(", "))
            languagesInput.setText(profile.languages.joinToString(", "))
            locationInput.setText(profile.location)
            availabilityInput.setText(profile.availability)
            salaryInput.setText(profile.salaryExpectation)
            socialInput.setText(profile.socialNetworkUrl)
            portfolioInput.setText(profile.portfolioUrl)
        }
    }

    private fun setupAvailabilityDropdown() {
        // Setup dropdown for availability
        val availabilityOptions = arrayOf("Inmediato", "2 semanas", "1 mes", "2 meses", "3+ meses")
        val adapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, availabilityOptions)
        binding.availabilityInput.setAdapter(adapter)
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
        // Obtener datos de todos los campos
        val profession = binding.professionInput.text.toString().trim()
        val bio = binding.bioInput.text.toString().trim()
        val skills = binding.skillsInput.text.toString().trim()
        val languages = binding.languagesInput.text.toString().trim()
        val location = binding.locationInput.text.toString().trim()
        val phone = binding.phoneInput.text.toString().trim()
        val email = binding.emailInput.text.toString().trim()
        val availability = binding.availabilityInput.text.toString().trim()
        val salary = binding.salaryInput.text.toString().trim()
        val social = binding.socialInput.text.toString().trim()
        val portfolio = binding.portfolioInput.text.toString().trim()

        // Validaciones básicas (solo campos requeridos)
        if (email.isEmpty()) {
            binding.emailInput.error = "El email es requerido"
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
            saveToViewModel(profession, bio, skills, languages, location, phone, email, availability, salary, social, portfolio)
        }

        builder.setNegativeButton("Continuar editando") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun saveToViewModel(
        profession: String, 
        bio: String, 
        skills: String, 
        languages: String, 
        location: String, 
        phone: String, 
        email: String, 
        availability: String, 
        salary: String, 
        social: String,
        portfolio: String
    ) {
        val currentUser = viewModel.getCurrentUser()
        val currentProfile = viewModel.getCurrentUserProfile()
        
        if (currentUser == null || currentProfile == null) {
            Toast.makeText(requireContext(), "Error: No se pudo cargar los datos del usuario", Toast.LENGTH_LONG).show()
            return
        }
        
        // Actualizar User si cambió teléfono o email
        val updatedUser = currentUser.copy(
            phone = phone.ifBlank { null }
        )
        
        // Actualizar UserProfile
        val skillsList = if (skills.isBlank()) emptyList() else skills.split(",").map { it.trim() }.filter { it.isNotBlank() }
        val languagesList = if (languages.isBlank()) emptyList() else languages.split(",").map { it.trim() }.filter { it.isNotBlank() }
        
        val updatedProfile = currentProfile.copy(
            profession = profession.ifBlank { null },
            bio = bio,
            skills = skillsList,
            languages = languagesList,
            location = location,
            availability = availability,
            salaryExpectation = salary,
            socialNetworkUrl = social,
            portfolioUrl = portfolio,
            lastUpdated = System.currentTimeMillis()
        )
        
        // Guardar a través del ViewModel
        viewModel.updateProfile(updatedUser, updatedProfile)
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
    
    /**
     * Subir imagen de perfil usando el ViewModel
     */
    private fun uploadProfileImage(imageUri: Uri) {
        Toast.makeText(requireContext(), "Subiendo imagen...", Toast.LENGTH_SHORT).show()
        viewModel.updateProfileImage(imageUri)
    }
    
    /**
     * Cargar imagen de perfil desde URL usando Glide
     */
    private fun loadProfileImage(imageUrl: String) {
        android.util.Log.d("ProfileEditFragment", "Loading profile image: $imageUrl")
        
        try {
            Glide.with(this)
                .load(imageUrl)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.profile_random) // Imagen por defecto mientras carga
                        .error(R.drawable.profile_random) // Imagen por defecto si hay error
                        .transform(CircleCrop()) // Hacer la imagen circular
                )
                .into(binding.profileImage)
        } catch (e: Exception) {
            android.util.Log.e("ProfileEditFragment", "Error loading profile image", e)
            // Fallback a imagen por defecto
            binding.profileImage.setImageResource(R.drawable.profile_random)
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
                startDate = "Enero 2022",
                endDate = "Diciembre 2023",
                isCurrent = false
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
        val startDate: String,
        val endDate: String,
        val isCurrent: Boolean
    )
}

// NOTA: El ProfileEditPagerAdapter está en un archivo separado:
// ProfileEditPagerAdapter.kt en el paquete com.example.hirelink_2025.view.adapter