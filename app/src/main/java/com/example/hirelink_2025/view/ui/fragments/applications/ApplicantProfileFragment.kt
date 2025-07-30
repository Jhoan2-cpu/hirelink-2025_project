package com.example.hirelink_2025.view.ui.fragments.applications

import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import androidx.navigation.fragment.findNavController
import com.example.hirelink_2025.R
import com.example.hirelink_2025.models.ApplicationStatus
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

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
    
    // Cover Letter
    private lateinit var coverLetter: TextView
    
    // Action Buttons
    private lateinit var acceptButton: MaterialButton
    private lateinit var rejectButton: MaterialButton
    private lateinit var contactButton: MaterialButton

    // Applicant data
    private var applicantData: ApplicantData? = null
    
    data class ApplicantData(
        val id: String = "",
        val name: String = "",
        val email: String = "",
        val phone: String = "",
        val profession: String = "",
        val experience: String = "",
        val skills: List<String> = emptyList(),
        val applicationDate: String = "",
        val status: ApplicationStatus = ApplicationStatus.PENDING,
        val profileImage: String = "",
        val jobId: String = "",
        val coverLetterText: String = ""
    )


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
        getApplicantDataFromArguments()
        setupClickListeners()
        displayApplicantData()
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
        
        // Cover Letter
        coverLetter = view.findViewById(R.id.coverLetter)
        
        // Action Buttons
        acceptButton = view.findViewById(R.id.acceptButton)
        rejectButton = view.findViewById(R.id.rejectButton)
        contactButton = view.findViewById(R.id.contactButton)
    }
    
    private fun getApplicantDataFromArguments() {
        // For now, we create static data regardless of arguments
        // Later this will be replaced with dynamic data from arguments
        
        applicantData = ApplicantData(
            id = "static_user_001",
            name = "Juan Carlos Pérez",
            email = "juan.perez@email.com",
            phone = "+51 987654321",
            profession = "Desarrollador Android Senior",
            experience = "5 años de experiencia",
            skills = listOf("Kotlin", "Android", "MVVM", "Clean Architecture", "Git", "Firebase", "Room", "Retrofit"),
            applicationDate = "16/01/2025",
            status = ApplicationStatus.PENDING,
            profileImage = "",
            jobId = "job_001",
            coverLetterText = "Carta de presentación estática"
        )
    }

    private fun displayApplicantData() {
        // Display static data regardless of arguments received
        
        // Profile header - Always show static data
        applicantName.text = "Juan Carlos Pérez"
        applicantProfession.text = "Desarrollador Android Senior"
        experienceYears.text = "5 años de experiencia"
        
        // Status - Always show as pending
        updateStatusAppearance(ApplicationStatus.PENDING)
        
        // Contact information - Static data
        applicantEmail.text = "juan.perez@email.com"
        applicantPhone.text = "+51 987654321"
        
        // Experience - Static description
        applicantExperience.text = "5 años de experiencia en desarrollo móvil con especialización en Android. He trabajado en equipos ágiles desarrollando aplicaciones nativas utilizando las mejores prácticas de la industria."
        
        // Skills - Static skills list
        val staticSkills = listOf("Kotlin", "Android", "MVVM", "Clean Architecture", "Git", "Firebase", "Room", "Retrofit")
        setupSkillsChips(staticSkills)
        
        // Cover letter - Static content
        coverLetter.text = "Estimado equipo de reclutamiento,\n\nMe dirijo a ustedes con gran interés en la posición de Desarrollador Android. Mi experiencia de 5 años en el desarrollo de aplicaciones móviles, combinada con mi pasión por la tecnología y la innovación, me convierten en un candidato ideal para este rol.\n\nDurante mi carrera, he trabajado con tecnologías como Kotlin, Java, y he implementado arquitecturas modernas como MVVM y Clean Architecture. Estoy siempre dispuesto a aprender nuevas tecnologías y contribuir al crecimiento del equipo.\n\nEspero tener la oportunidad de discutir cómo mis habilidades pueden contribuir al éxito de su empresa.\n\nSaludos cordiales,\nJuan Carlos Pérez"
        
        // Action buttons - Always show as pending status
        setupActionButtons(ApplicationStatus.PENDING)
    }
    
    private fun setupSkillsChips(skills: List<String>) {
        skillsChipGroup.removeAllViews()
        
        val skillsToShow = if (skills.isNotEmpty()) skills else listOf("Kotlin", "Android", "MVVM", "Clean Architecture", "Git", "Firebase")
        
        skillsToShow.forEach { skill ->
            val chip = Chip(requireContext())
            chip.text = skill
            chip.isClickable = false
            chip.setChipBackgroundColorResource(R.color.surface_variant)
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
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

        contactButton.setOnClickListener {
            contactApplicant()
        }

        // Contact click listeners
        emailLayout.setOnClickListener {
            contactApplicant()
        }

        phoneLayout.setOnClickListener {
            callApplicant()
        }
    }

    private fun shareApplicantProfile() {
        val shareText = """
            Perfil de postulante - HireLink:
            
            Nombre: Juan Carlos Pérez
            Profesión: Desarrollador Android Senior
            Experiencia: 5 años de experiencia
            Email: juan.perez@email.com
            Teléfono: +51 987654321
            Estado: Pendiente
            
            Enviado desde HireLink
        """.trimIndent()

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, "Perfil de Juan Carlos Pérez")
        }

        try {
            startActivity(Intent.createChooser(shareIntent, "Compartir perfil"))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "No se pudo compartir", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showAcceptDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Aceptar postulante")
            .setMessage("¿Quieres aceptar a Juan Carlos Pérez para este trabajo?")
            .setPositiveButton("Sí, aceptar") { _, _ ->
                Toast.makeText(requireContext(), "Juan Carlos Pérez ha sido aceptado", Toast.LENGTH_LONG).show()
                
                // Update UI to show accepted status
                updateStatusAppearance(ApplicationStatus.ACCEPTED)
                setupActionButtons(ApplicationStatus.ACCEPTED)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showRejectDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Rechazar postulante")
            .setMessage("¿Quieres rechazar a Juan Carlos Pérez?")
            .setPositiveButton("Sí, rechazar") { _, _ ->
                Toast.makeText(requireContext(), "Juan Carlos Pérez ha sido rechazado", Toast.LENGTH_SHORT).show()
                
                // Update UI to show rejected status
                updateStatusAppearance(ApplicationStatus.REJECTED)
                setupActionButtons(ApplicationStatus.REJECTED)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun contactApplicant() {
        // Static email contact
        val email = "juan.perez@email.com"
        
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
            putExtra(Intent.EXTRA_SUBJECT, "Respuesta a tu aplicación")
            putExtra(Intent.EXTRA_TEXT, "Hola Juan Carlos,\n\nGracias por tu interés en nuestra empresa.\n\n")
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "No se pudo abrir el email", Toast.LENGTH_SHORT).show()
        }
    }

    private fun callApplicant() {
        // Static phone contact
        val phone = "+51 987654321"
        
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phone")
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "No se pudo abrir el marcador", Toast.LENGTH_SHORT).show()
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