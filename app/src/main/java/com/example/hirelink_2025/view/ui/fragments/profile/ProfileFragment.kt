package com.example.hirelink_2025.view.ui.fragments.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.hirelink_2025.R
import com.example.hirelink_2025.databinding.FragmentProfileBinding
import com.example.hirelink_2025.models.User
import com.example.hirelink_2025.models.UserProfile
import com.example.hirelink_2025.models.WorkExperience
import com.example.hirelink_2025.models.Education
import com.example.hirelink_2025.view.adapter.ExperienceAdapter
import com.example.hirelink_2025.view.adapter.EducationAdapter
import com.example.hirelink_2025.view.ui.activities.AuthActivity
import com.example.hirelink_2025.viewmodels.ProfileViewModel
import com.example.hirelink_2025.viewmodels.ViewModelFactory
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    // ViewModel con Factory (MVVM)
    private val viewModel: ProfileViewModel by viewModels {
        ViewModelFactory()
    }

    private lateinit var experienceAdapter: ExperienceAdapter
    private lateinit var educationAdapter: EducationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        // Recargar datos cuando regresamos de la edición
        viewModel.loadUserData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        experienceAdapter = ExperienceAdapter(
            experiences = emptyList(),
            onEditClick = { experience ->
                showEditExperienceDialog(experience)
            },
            onDeleteClick = { experience ->
                showDeleteExperienceConfirmation(experience)
            }
        )

        binding.experienceRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = experienceAdapter
        }

        educationAdapter = EducationAdapter(
            educationList = emptyList(),
            onEditClick = { education ->
                showEditEducationDialog(education)
            },
            onDeleteClick = { education ->
                showDeleteEducationConfirmation(education)
            }
        )

        binding.educationRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = educationAdapter
        }
    }

    private fun setupClickListeners() {
        // Settings button - Solo cerrar sesión
        binding.settingsButton.setOnClickListener {
            showLogoutConfirmation()
        }
        
        // Botones para agregar experiencia y educación
        binding.addExperienceButton.setOnClickListener {
            showAddExperienceDialog()
        }
        
        binding.addEducationButton.setOnClickListener {
            showAddEducationDialog()
        }

        // Contact actions
        binding.callButton.setOnClickListener {
            val phone = viewModel.getCurrentUser()?.phone
            if (!phone.isNullOrBlank()) {
                dialPhoneNumber(phone)
            } else {
                Toast.makeText(requireContext(), "No se ha especificado un teléfono", Toast.LENGTH_SHORT).show()
            }
        }

        binding.emailButton.setOnClickListener {
            val email = viewModel.getCurrentUser()?.email
            if (!email.isNullOrBlank()) {
                sendEmail(email)
            }
        }

        binding.linkedinButton.setOnClickListener {
            val socialNetworkUrl = viewModel.getCurrentUserProfile()?.socialNetworkUrl
            if (!socialNetworkUrl.isNullOrBlank()) {
                openUrl(socialNetworkUrl)
            }
        }

        binding.portfolioButton.setOnClickListener {
            val portfolioUrl = viewModel.getCurrentUserProfile()?.portfolioUrl
            if (!portfolioUrl.isNullOrBlank()) {
                openUrl(portfolioUrl)
            }
        }

        // Edit FAB - Editar perfil completo
        binding.editFab.setOnClickListener {
            // Navigate to profile edit fragment
            findNavController().navigate(R.id.action_profileFragment_to_profileEditFragment)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateLoadingState(state.isLoading)
                state.error?.let { error ->
                    showErrorMessage(error)
                    viewModel.clearError()
                }

                state.user?.let { user ->
                    updateUserBasicInfo(user)
                }

                state.userProfile?.let { profile ->
                    updateUserProfile(profile)
                }

                if (state.isProfileUpdated) {
                    showSuccessMessage("Perfil actualizado correctamente")
                    viewModel.resetProfileUpdated()
                }
            }
        }
    }

    private fun updateUserBasicInfo(user: User) {
        with(binding) {
            userName.text = user.name
            userEmail.text = user.email
            userPhone.text = user.phone ?: "No especificado"
            
            // Cargar imagen de perfil si existe
            user.profileImageUrl?.let { imageUrl ->
                if (imageUrl.isNotEmpty()) {
                    loadProfileImage(imageUrl)
                }
            }
        }
    }

    private fun updateUserProfile(profile: UserProfile) {
        with(binding) {
            // Profesión en header
            userProfession.text = profile.profession ?: "Agrega tu profesión"

            // Bio/About me
            userBio.text = if (profile.bio.isBlank()) {
                "Agrega una descripción sobre ti, tu experiencia y objetivos profesionales."
            } else {
                profile.bio
            }

            // Disponibilidad y salario
            userAvailability.text = profile.availability.ifBlank { "No especificado" }
            userSalaryExpectation.text = profile.salaryExpectation.ifBlank { "No especificado" }

            // LinkedIn y Portfolio
            if (profile.socialNetworkUrl.isNotBlank()) {
                linkedinLayout.visibility = View.VISIBLE
                userLinkedin.text = profile.socialNetworkUrl
            } else {
                linkedinLayout.visibility = View.GONE
            }

            if (profile.portfolioUrl.isNotBlank()) {
                portfolioLayout.visibility = View.VISIBLE
                userPortfolio.text = profile.portfolioUrl
            } else {
                portfolioLayout.visibility = View.GONE
            }

            // Skills
            updateSkillsDisplay(profile.skills)

            // Experience
            updateExperienceDisplay(profile.experience)

            // Education
            updateEducationDisplay(profile.education)

            // Languages
            updateLanguagesDisplay(profile.languages)

            // Location
            updateLocationDisplay(profile.location)
        }
    }

    private fun updateSkillsDisplay(skills: List<String>) {
        binding.skillsChipGroup.removeAllViews()

        if (skills.isEmpty()) {
            binding.noSkillsText.visibility = View.VISIBLE
            binding.skillsChipGroup.visibility = View.GONE
        } else {
            binding.noSkillsText.visibility = View.GONE
            binding.skillsChipGroup.visibility = View.VISIBLE

            skills.forEach { skill ->
                val chip = Chip(requireContext())
                chip.text = skill
                chip.isClickable = false
                chip.setChipBackgroundColorResource(R.color.surface_variant)
                binding.skillsChipGroup.addView(chip)
            }
        }
    }

    private fun updateExperienceDisplay(experiences: List<WorkExperience>) {
        if (experiences.isEmpty()) {
            binding.noExperienceText.visibility = View.VISIBLE
            binding.experienceRecyclerView.visibility = View.GONE
        } else {
            binding.noExperienceText.visibility = View.GONE
            binding.experienceRecyclerView.visibility = View.VISIBLE
            experienceAdapter.updateExperiences(experiences)
        }
    }

    private fun updateEducationDisplay(educationList: List<Education>) {
        if (educationList.isEmpty()) {
            binding.noEducationText.visibility = View.VISIBLE
            binding.educationRecyclerView.visibility = View.GONE
        } else {
            binding.noEducationText.visibility = View.GONE
            binding.educationRecyclerView.visibility = View.VISIBLE
            educationAdapter.updateEducation(educationList)
        }
    }

    private fun updateLanguagesDisplay(languages: List<String>) {
        binding.languagesChipGroup.removeAllViews()

        if (languages.isEmpty()) {
            binding.noLanguagesText.visibility = View.VISIBLE
            binding.languagesChipGroup.visibility = View.GONE
        } else {
            binding.noLanguagesText.visibility = View.GONE
            binding.languagesChipGroup.visibility = View.VISIBLE

            languages.forEach { language ->
                val chip = Chip(requireContext())
                chip.text = language
                chip.isClickable = false
                chip.setChipBackgroundColorResource(R.color.surface_variant)
                binding.languagesChipGroup.addView(chip)
            }
        }
    }

    private fun updateLocationDisplay(location: String) {
        with(binding) {
            val displayText = if (location.isBlank()) {
                "Agregar ubicación"
            } else {
                location
            }
            userLocationDisplay.text = displayText
            userLocationText.text = displayText
        }
    }


    private fun showDeleteExperienceConfirmation(experience: WorkExperience) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar experiencia")
            .setMessage("¿Quieres eliminar la experiencia en ${experience.company}?")
            .setPositiveButton("Eliminar") { _, _ ->
                deleteExperience(experience)
                Toast.makeText(requireContext(), "Experiencia eliminada", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showDeleteEducationConfirmation(education: Education) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar educación")
            .setMessage("¿Quieres eliminar la educación en ${education.institution}?")
            .setPositiveButton("Eliminar") { _, _ ->
                deleteEducation(education)
                Toast.makeText(requireContext(), "Educación eliminada", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showAddExperienceDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_experience, null)
        
        val positionInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.positionInput)
        val companyInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.companyInput)
        val descriptionInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.descriptionInput)
        val startDateInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.startDateInput)
        val endDateInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.endDateInput)
        val currentJobCheckbox = dialogView.findViewById<com.google.android.material.checkbox.MaterialCheckBox>(R.id.currentJobCheckbox)

        // Handle current job checkbox
        currentJobCheckbox.setOnCheckedChangeListener { _, isChecked ->
            endDateInput.isEnabled = !isChecked
            if (isChecked) {
                endDateInput.setText("")
            }
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Agregar experiencia laboral")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val position = positionInput.text.toString().trim()
                val company = companyInput.text.toString().trim()
                val description = descriptionInput.text.toString().trim()
                val startDate = startDateInput.text.toString().trim()
                val endDate = if (currentJobCheckbox.isChecked) "" else endDateInput.text.toString().trim()
                val isCurrent = currentJobCheckbox.isChecked

                if (position.isNotBlank() && company.isNotBlank() && startDate.isNotBlank()) {
                    val newExperience = WorkExperience(
                        id = java.util.UUID.randomUUID().toString(),
                        position = position,
                        company = company,
                        description = description,
                        startDate = startDate,
                        endDate = endDate,
                        isCurrent = isCurrent
                    )
                    addExperience(newExperience)
                } else {
                    Toast.makeText(requireContext(), "Por favor completa los campos requeridos", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.show()
    }

    private fun showAddEducationDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_education, null)
        
        val degreeInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.degreeInput)
        val institutionInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.institutionInput)
        val fieldInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.fieldInput)
        val startYearInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.startYearInput)
        val endYearInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.endYearInput)
        val descriptionInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.descriptionInput)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Agregar educación")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val degree = degreeInput.text.toString().trim()
                val institution = institutionInput.text.toString().trim()
                val field = fieldInput.text.toString().trim()
                val startYear = startYearInput.text.toString().trim()
                val endYear = endYearInput.text.toString().trim()
                val description = descriptionInput.text.toString().trim()

                if (degree.isNotBlank() && institution.isNotBlank()) {
                    val newEducation = Education(
                        id = java.util.UUID.randomUUID().toString(),
                        degree = degree,
                        institution = institution,
                        field = field,
                        startYear = startYear,
                        endYear = endYear,
                        description = description
                    )
                    addEducation(newEducation)
                } else {
                    Toast.makeText(requireContext(), "Por favor completa los campos requeridos", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.show()
    }

    private fun showEditExperienceDialog(experience: WorkExperience) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_experience, null)
        
        val positionInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.positionInput)
        val companyInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.companyInput)
        val descriptionInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.descriptionInput)
        val startDateInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.startDateInput)
        val endDateInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.endDateInput)
        val currentJobCheckbox = dialogView.findViewById<com.google.android.material.checkbox.MaterialCheckBox>(R.id.currentJobCheckbox)

        // Llenar los campos con los datos actuales
        positionInput.setText(experience.position)
        companyInput.setText(experience.company)
        descriptionInput.setText(experience.description)
        startDateInput.setText(experience.startDate)
        endDateInput.setText(experience.endDate)
        currentJobCheckbox.isChecked = experience.isCurrent

        // Handle current job checkbox
        currentJobCheckbox.setOnCheckedChangeListener { _, isChecked ->
            endDateInput.isEnabled = !isChecked
            if (isChecked) {
                endDateInput.setText("")
            }
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Editar experiencia laboral")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val position = positionInput.text.toString().trim()
                val company = companyInput.text.toString().trim()
                val description = descriptionInput.text.toString().trim()
                val startDate = startDateInput.text.toString().trim()
                val endDate = if (currentJobCheckbox.isChecked) "" else endDateInput.text.toString().trim()
                val isCurrent = currentJobCheckbox.isChecked

                if (position.isNotBlank() && company.isNotBlank() && startDate.isNotBlank()) {
                    val updatedExperience = experience.copy(
                        position = position,
                        company = company,
                        description = description,
                        startDate = startDate,
                        endDate = endDate,
                        isCurrent = isCurrent
                    )
                    updateExperience(experience, updatedExperience)
                } else {
                    Toast.makeText(requireContext(), "Por favor completa los campos requeridos", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.show()
    }

    private fun showEditEducationDialog(education: Education) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_education, null)
        
        val degreeInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.degreeInput)
        val institutionInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.institutionInput)
        val fieldInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.fieldInput)
        val startYearInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.startYearInput)
        val endYearInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.endYearInput)
        val descriptionInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.descriptionInput)

        // Llenar los campos con los datos actuales
        degreeInput.setText(education.degree)
        institutionInput.setText(education.institution)
        fieldInput.setText(education.field)
        startYearInput.setText(education.startYear)
        endYearInput.setText(education.endYear)
        descriptionInput.setText(education.description)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Editar educación")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val degree = degreeInput.text.toString().trim()
                val institution = institutionInput.text.toString().trim()
                val field = fieldInput.text.toString().trim()
                val startYear = startYearInput.text.toString().trim()
                val endYear = endYearInput.text.toString().trim()
                val description = descriptionInput.text.toString().trim()

                if (degree.isNotBlank() && institution.isNotBlank()) {
                    val updatedEducation = education.copy(
                        degree = degree,
                        institution = institution,
                        field = field,
                        startYear = startYear,
                        endYear = endYear,
                        description = description
                    )
                    updateEducation(education, updatedEducation)
                } else {
                    Toast.makeText(requireContext(), "Por favor completa los campos requeridos", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.show()
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Cerrar sesión")
            .setMessage("¿Estás seguro de que quieres cerrar sesión?")
            .setPositiveButton("Sí") { _, _ ->
                viewModel.signOut()
                // Redirigir a AuthActivity
                val intent = Intent(requireContext(), AuthActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun addExperience(experience: WorkExperience) {
        val currentProfile = viewModel.getCurrentUserProfile() ?: return
        val updatedExperiences = currentProfile.experience.toMutableList().apply { add(experience) }
        val updatedProfile = currentProfile.copy(experience = updatedExperiences)
        viewModel.updateUserProfile(updatedProfile)
    }

    private fun addEducation(education: Education) {
        val currentProfile = viewModel.getCurrentUserProfile() ?: return
        val updatedEducation = currentProfile.education.toMutableList().apply { add(education) }
        val updatedProfile = currentProfile.copy(education = updatedEducation)
        viewModel.updateUserProfile(updatedProfile)
    }

    private fun updateExperience(oldExperience: WorkExperience, newExperience: WorkExperience) {
        val currentProfile = viewModel.getCurrentUserProfile() ?: return
        val updatedExperiences = currentProfile.experience.toMutableList()
        val index = updatedExperiences.indexOf(oldExperience)
        if (index != -1) {
            updatedExperiences[index] = newExperience
            val updatedProfile = currentProfile.copy(experience = updatedExperiences)
            viewModel.updateUserProfile(updatedProfile)
        }
    }

    private fun updateEducation(oldEducation: Education, newEducation: Education) {
        val currentProfile = viewModel.getCurrentUserProfile() ?: return
        val updatedEducation = currentProfile.education.toMutableList()
        val index = updatedEducation.indexOf(oldEducation)
        if (index != -1) {
            updatedEducation[index] = newEducation
            val updatedProfile = currentProfile.copy(education = updatedEducation)
            viewModel.updateUserProfile(updatedProfile)
        }
    }

    private fun deleteExperience(experience: WorkExperience) {
        val currentProfile = viewModel.getCurrentUserProfile() ?: return
        val updatedExperiences = currentProfile.experience.toMutableList().apply { remove(experience) }
        val updatedProfile = currentProfile.copy(experience = updatedExperiences)
        viewModel.updateUserProfile(updatedProfile)
    }

    private fun deleteEducation(education: Education) {
        val currentProfile = viewModel.getCurrentUserProfile() ?: return
        val updatedEducation = currentProfile.education.toMutableList().apply { remove(education) }
        val updatedProfile = currentProfile.copy(education = updatedEducation)
        viewModel.updateUserProfile(updatedProfile)
    }

    private fun updateLoadingState(isLoading: Boolean) {
        // TODO: Mostrar/ocultar loading indicator
    }

    private fun showErrorMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showSuccessMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
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

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "No se pudo abrir el enlace", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatDate(timestamp: Long): String {
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault())
        return format.format(date)
    }
    
    /**
     * Cargar imagen de perfil desde URL usando Glide
     */
    private fun loadProfileImage(imageUrl: String) {
        android.util.Log.d("ProfileFragment", "Loading profile image: $imageUrl")
        
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
            android.util.Log.e("ProfileFragment", "Error loading profile image", e)
            // Fallback a imagen por defecto
            binding.profileImage.setImageResource(R.drawable.profile_random)
        }
    }
}