package com.example.hirelink_2025.view.ui.fragments.applications

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.hirelink_2025.R
import com.example.hirelink_2025.models.Application
import com.example.hirelink_2025.models.ApplicationStatus
import com.example.hirelink_2025.models.User
import com.example.hirelink_2025.models.UserProfile
import com.example.hirelink_2025.viewmodels.ApplicationsViewModel
import com.example.hirelink_2025.viewmodels.ViewModelFactory
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.launch

class ApplicantProfileFragment : Fragment() {

    // UI Components
    private lateinit var backButton: MaterialButton
    private lateinit var shareButton: MaterialButton
    private lateinit var toolbarTitle: TextView
    
    // Profile Header
    private lateinit var applicantPhoto: ImageView
    private lateinit var applicantName: TextView
    private lateinit var applicantProfession: TextView
    private lateinit var experienceYears: TextView
    private lateinit var statusChip: Chip
    
    // Contact
    private lateinit var emailLayout: LinearLayout
    private lateinit var phoneLayout: LinearLayout
    private lateinit var applicantEmail: TextView
    private lateinit var applicantPhone: TextView
    
    // Experience and Skills
    private lateinit var applicantExperience: TextView
    private lateinit var skillsChipGroup: ChipGroup
    
    // Additional Information
    private lateinit var applicantLocation: TextView
    private lateinit var applicantBio: TextView
    
    // Action Buttons
    private lateinit var acceptButton: MaterialButton
    private lateinit var rejectButton: MaterialButton

    // ViewModels
    private val applicationsViewModel: ApplicationsViewModel by viewModels { ViewModelFactory() }
    
    // Data
    private var userId: String? = null
    private var applicationId: String? = null
    private var jobId: String? = null
    private var currentUser: User? = null
    private var currentUserProfile: UserProfile? = null
    private var currentApplication: Application? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_applicant_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        getArgumentsData()
        setupObservers()
        setupClickListeners()
        loadUserData()
        
        // Inicializar búsqueda de Application
        findCurrentApplication()
    }
    
    private fun initViews(view: View) {
        // Toolbar
        backButton = view.findViewById(R.id.backButton)
        shareButton = view.findViewById(R.id.shareButton)
        toolbarTitle = view.findViewById(R.id.toolbarTitle)
        
        // Profile Header
        applicantPhoto = view.findViewById(R.id.applicantPhoto)
        applicantName = view.findViewById(R.id.applicantName)
        applicantProfession = view.findViewById(R.id.applicantProfession)
        experienceYears = view.findViewById(R.id.experienceYears)
        statusChip = view.findViewById(R.id.statusChip)
        
        // Contact
        emailLayout = view.findViewById(R.id.emailLayout)
        phoneLayout = view.findViewById(R.id.phoneLayout)
        applicantEmail = view.findViewById(R.id.applicantEmail)
        applicantPhone = view.findViewById(R.id.applicantPhone)
        
        // Experience and Skills
        applicantExperience = view.findViewById(R.id.applicantExperience)
        skillsChipGroup = view.findViewById(R.id.skillsChipGroup)
        
        // Additional Information
        applicantLocation = view.findViewById(R.id.applicantLocation)
        applicantBio = view.findViewById(R.id.applicantBio)
        
        // Action Buttons
        acceptButton = view.findViewById(R.id.acceptButton)
        rejectButton = view.findViewById(R.id.rejectButton)
    }
    
    private fun getArgumentsData() {
        userId = arguments?.getString("userId") ?: arguments?.getString("applicantId")
        applicationId = arguments?.getString("applicationId")
        jobId = arguments?.getString("jobId")
        
        Log.d("ApplicantProfileFragment", "User ID: $userId, Application ID: $applicationId, Job ID: $jobId")
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Observar cache de usuarios
            applicationsViewModel.usersCache.collect { usersCache ->
                userId?.let { id ->
                    usersCache[id]?.let { user ->
                        currentUser = user
                        updateUI()
                    }
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            // Observar cache de perfiles
            applicationsViewModel.userProfilesCache.collect { profilesCache ->
                userId?.let { id ->
                    profilesCache[id]?.let { profile ->
                        currentUserProfile = profile
                        updateUI()
                    }
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            // Observar aplicaciones para obtener el estado actual
            applicationsViewModel.applications.collect { applications ->
                Log.d("ApplicantProfileFragment", "Applications updated: ${applications.size}")
                
                val application = if (applicationId != null) {
                    // Buscar por applicationId si está disponible
                    applications.find { it.applicationId == applicationId }
                } else {
                    // Buscar por userId y jobId como fallback
                    applications.find { it.applicantId == userId && it.jobId == jobId }
                }
                
                Log.d("ApplicantProfileFragment", "Found application: ${application?.applicationId}, status: ${application?.status}")
                
                if (application != null && application != currentApplication) {
                    currentApplication = application
                    updateUI()
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            // Observar operaciones
            applicationsViewModel.operationResult.collect { result ->
                result?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                    applicationsViewModel.clearOperationResult()
                }
            }
        }
    }
    
    private fun loadUserData() {
        userId?.let { id ->
            // Cargar usuario y perfil desde FirestoreService
            val firestoreService = com.example.hirelink_2025.network.FirestoreService()
            
            firestoreService.getUserById(id, object : com.example.hirelink_2025.network.Callback<User?> {
                override fun onSuccess(user: User?) {
                    currentUser = user
                    updateUI()
                }
                
                override fun onError(exception: Exception) {
                    Log.e("ApplicantProfileFragment", "Error loading user", exception)
                    Toast.makeText(requireContext(), "Error al cargar datos del usuario", Toast.LENGTH_SHORT).show()
                }
            })
            
            firestoreService.getUserProfile(id, object : com.example.hirelink_2025.network.Callback<UserProfile?> {
                override fun onSuccess(profile: UserProfile?) {
                    currentUserProfile = profile
                    updateUI()
                }
                
                override fun onError(exception: Exception) {
                    Log.e("ApplicantProfileFragment", "Error loading user profile", exception)
                }
            })
        }
        
        // Cargar applications del job para obtener el estado correcto
        jobId?.let { jId ->
            Log.d("ApplicantProfileFragment", "Loading applications for job: $jId")
            applicationsViewModel.loadApplicationsForJob(jId)
        }
    }
    
    private fun updateUI() {
        val user = currentUser
        val profile = currentUserProfile
        
        if (user == null) return
        
        // Profile header
        applicantName.text = user.name
        applicantProfession.text = profile?.profession?.ifEmpty { "Profesión no especificada" } ?: "Profesión no especificada"
        
        // Experience years - basado en experiencia laboral
        if (profile?.experience?.isNotEmpty() == true) {
            experienceYears.text = "${profile.experience.size} trabajos registrados"
        } else {
            experienceYears.text = "Sin experiencia registrada"
        }
        
        // Status - usar estado real de la application
        val currentStatus = currentApplication?.status ?: ApplicationStatus.PENDING
        Log.d("ApplicantProfileFragment", "Updating UI with status: $currentStatus")
        updateStatusAppearance(currentStatus)
        
        // Contact information
        applicantEmail.text = user.email
        applicantPhone.text = user.phone?.ifEmpty { "No disponible" } ?: "No disponible"
        
        // Experience description
        if (profile?.experience?.isNotEmpty() == true) {
            val experienceText = StringBuilder()
            profile.experience.take(3).forEach { exp ->
                experienceText.append("• ${exp.position} en ${exp.company}")
                if (exp.isCurrent) experienceText.append(" (Actual)")
                experienceText.append("\n")
            }
            applicantExperience.text = experienceText.toString().trim()
        } else {
            applicantExperience.text = "Sin experiencia registrada"
        }
        
        // Skills
        setupSkillsChips(profile?.skills ?: emptyList())
        
        // Additional information
        applicantLocation.text = profile?.location?.ifEmpty { "Ubicación no especificada" } ?: "Ubicación no especificada"
        applicantBio.text = profile?.bio?.ifEmpty { "Sin biografía disponible" } ?: "Sin biografía disponible"
        
        // Load profile image
        loadProfileImage(user.profileImageUrl)
        
        // Action buttons - usar estado real de la application
        val actionStatus = currentApplication?.status ?: ApplicationStatus.PENDING
        setupActionButtons(actionStatus)
    }
    
    private fun loadProfileImage(imageUrl: String?) {
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .circleCrop()
                .into(applicantPhoto)
        } else {
            applicantPhoto.setImageResource(R.drawable.ic_person)
        }
    }
    
    private fun setupSkillsChips(skills: List<String>) {
        skillsChipGroup.removeAllViews()
        
        if (skills.isEmpty()) {
            val chip = Chip(requireContext())
            chip.text = "Sin habilidades registradas"
            chip.isClickable = false
            chip.setChipBackgroundColorResource(R.color.surface_variant)
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_hint))
            chip.textSize = 14f
            skillsChipGroup.addView(chip)
            return
        }
        
        skills.forEach { skill ->
            val chip = Chip(requireContext())
            chip.text = skill
            chip.isClickable = false
            chip.setChipBackgroundColorResource(R.color.primary)
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            chip.textSize = 14f
            skillsChipGroup.addView(chip)
        }
    }

    private fun updateStatusAppearance(status: ApplicationStatus) {
        statusChip.apply {
            when (status) {
                ApplicationStatus.PENDING -> {
                    text = "Pendiente"
                    setChipBackgroundColorResource(R.color.secondary)
                    setChipIconResource(R.drawable.ic_pending)
                }
                ApplicationStatus.ACCEPTED -> {
                    text = "Aceptado"
                    setChipBackgroundColorResource(R.color.success)
                    setChipIconResource(R.drawable.ic_check_circle)
                }
                ApplicationStatus.REJECTED -> {
                    text = "Rechazado"
                    setChipBackgroundColorResource(R.color.error)
                    setChipIconResource(R.drawable.ic_rejected)
                }
            }
            setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        }
    }

    private fun setupActionButtons(status: ApplicationStatus) {
        when (status) {
            ApplicationStatus.PENDING -> {
                acceptButton.isEnabled = true
                rejectButton.isEnabled = true
                acceptButton.alpha = 1.0f
                rejectButton.alpha = 1.0f
            }
            ApplicationStatus.ACCEPTED -> {
                acceptButton.isEnabled = false
                rejectButton.isEnabled = true
                acceptButton.alpha = 0.5f
                rejectButton.alpha = 1.0f
            }
            ApplicationStatus.REJECTED -> {
                acceptButton.isEnabled = true
                rejectButton.isEnabled = false
                acceptButton.alpha = 1.0f
                rejectButton.alpha = 0.5f
            }
        }
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            findNavController().navigateUp()
        }
        
        shareButton.setOnClickListener {
            shareApplicantProfile()
        }
        
        acceptButton.setOnClickListener {
            showAcceptDialog()
        }

        rejectButton.setOnClickListener {
            showRejectDialog()
        }

        // Contact click listeners
        emailLayout.setOnClickListener {
            contactApplicantByEmail()
        }

        phoneLayout.setOnClickListener {
            callApplicant()
        }
    }

    private fun shareApplicantProfile() {
        val user = currentUser
        val profile = currentUserProfile
        
        if (user == null) {
            Toast.makeText(requireContext(), "Datos no disponibles para compartir", Toast.LENGTH_SHORT).show()
            return
        }
        
        val shareText = """
            Perfil de postulante - HireLink:
            
            Nombre: ${user.name}
            Profesión: ${profile?.profession ?: "No especificada"}
            Email: ${user.email}
            Teléfono: ${user.phone ?: "No disponible"}
            Ubicación: ${profile?.location ?: "No especificada"}
            Experiencia: ${if (profile?.experience?.isNotEmpty() == true) "${profile.experience.size} trabajos registrados" else "Sin experiencia registrada"}
            Estado: Pendiente
            
            Enviado desde HireLink
        """.trimIndent()

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, "Perfil de ${user.name}")
        }

        try {
            startActivity(Intent.createChooser(shareIntent, "Compartir perfil"))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "No se pudo compartir", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showAcceptDialog() {
        val userName = currentUser?.name ?: "este postulante"
        AlertDialog.Builder(requireContext())
            .setTitle("Aceptar postulante")
            .setMessage("¿Quieres aceptar a $userName para este trabajo?")
            .setPositiveButton("Sí, aceptar") { _, _ ->
                updateApplicationStatus(ApplicationStatus.ACCEPTED)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showRejectDialog() {
        val userName = currentUser?.name ?: "este postulante"
        AlertDialog.Builder(requireContext())
            .setTitle("Rechazar postulante")
            .setMessage("¿Quieres rechazar a $userName?")
            .setPositiveButton("Sí, rechazar") { _, _ ->
                updateApplicationStatus(ApplicationStatus.REJECTED)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun updateApplicationStatus(newStatus: ApplicationStatus) {
        val appId = applicationId
        if (appId == null) {
            Toast.makeText(requireContext(), "Error: ID de postulación no disponible", Toast.LENGTH_SHORT).show()
            return
        }
        
        val userName = currentUser?.name ?: "el postulante"
        
        applicationsViewModel.updateApplicationStatus(appId, newStatus) { success ->
            if (success) {
                // Actualizar la application local
                currentApplication = currentApplication?.copy(status = newStatus)
                
                // Actualizar UI
                updateStatusAppearance(newStatus)
                setupActionButtons(newStatus)
                
                val statusText = when (newStatus) {
                    ApplicationStatus.ACCEPTED -> "aceptado"
                    ApplicationStatus.REJECTED -> "rechazado"
                    else -> "actualizado"
                }
                Toast.makeText(requireContext(), "$userName ha sido $statusText", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(requireContext(), "Error al actualizar el estado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun contactApplicantByEmail() {
        val email = currentUser?.email
        val userName = currentUser?.name ?: "postulante"
        
        if (email.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Email no disponible", Toast.LENGTH_SHORT).show()
            return
        }
        
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
            putExtra(Intent.EXTRA_SUBJECT, "Respuesta a tu aplicación")
            putExtra(Intent.EXTRA_TEXT, "Hola $userName,\n\nGracias por tu interés en nuestra empresa.\n\n")
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "No se pudo abrir el email", Toast.LENGTH_SHORT).show()
        }
    }

    private fun callApplicant() {
        val phone = currentUser?.phone
        
        if (phone.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Teléfono no disponible", Toast.LENGTH_SHORT).show()
            return
        }
        
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phone")
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "No se pudo abrir el marcador", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun findCurrentApplication() {
        // Buscar inmediatamente en el cache de applications disponible
        val currentApplications = applicationsViewModel.applications.value
        
        val application = if (applicationId != null) {
            // Buscar por applicationId si está disponible
            currentApplications.find { it.applicationId == applicationId }
        } else {
            // Buscar por userId y jobId como fallback
            currentApplications.find { it.applicantId == userId && it.jobId == jobId }
        }
        
        if (application != null) {
            Log.d("ApplicantProfileFragment", "Found application in cache: ${application.applicationId}, status: ${application.status}")
            currentApplication = application
            updateUI()
        } else {
            Log.d("ApplicantProfileFragment", "Application not found in cache, waiting for observer...")
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(
            applicantId: String,
            applicantName: String,
            applicantEmail: String,
            applicantPhone: String,
            applicantProfession: String,
            applicantExperience: String,
            applicantSkills: ArrayList<String>,
            applicationDate: String,
            applicationStatus: String,
            profileImage: String,
            jobId: String,
            coverLetter: String
        ) = ApplicantProfileFragment().apply {
            arguments = Bundle().apply {
                putString("applicant_id", applicantId)
                putString("applicant_name", applicantName)
                putString("applicant_email", applicantEmail)
                putString("applicant_phone", applicantPhone)
                putString("applicant_profession", applicantProfession)
                putString("applicant_experience", applicantExperience)
                putStringArrayList("applicant_skills", applicantSkills)
                putString("application_date", applicationDate)
                putString("application_status", applicationStatus)
                putString("profile_image", profileImage)
                putString("job_id", jobId)
                putString("cover_letter", coverLetter)
            }
        }
    }
}