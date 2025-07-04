package com.example.hirelink_2025.ui.fragments.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.hirelink_2025.databinding.FragmentApplicantExperienceBinding
import com.example.hirelink_2025.models.Applicant
import com.example.hirelink_2025.models.ApplicationStatus

class ApplicantExperienceFragment : Fragment() {

    private var _binding: FragmentApplicantExperienceBinding? = null
    private val binding get() = _binding!!

    private lateinit var applicant: Applicant

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            applicant = Applicant(
                id = args.getString(ARG_APPLICANT_ID) ?: "",
                name = args.getString(ARG_APPLICANT_NAME) ?: "",
                email = args.getString(ARG_APPLICANT_EMAIL) ?: "",
                phone = args.getString(ARG_APPLICANT_PHONE) ?: "",
                profession = args.getString(ARG_APPLICANT_PROFESSION) ?: "",
                experience = args.getString(ARG_APPLICANT_EXPERIENCE) ?: "",
                skills = args.getStringArrayList(ARG_APPLICANT_SKILLS) ?: emptyList(),
                applicationDate = args.getString(ARG_APPLICATION_DATE) ?: "",
                status = ApplicationStatus.valueOf(args.getString(ARG_APPLICATION_STATUS) ?: "PENDING"),
                profileImage = args.getString(ARG_PROFILE_IMAGE),
                jobId = args.getString(ARG_JOB_ID) ?: "",
                coverLetter = args.getString(ARG_COVER_LETTER)
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentApplicantExperienceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupExperienceInfo()
    }

    private fun setupExperienceInfo() {
        with(binding) {
            // Información de experiencia
            experienceTitle.text = applicant.profession
            experienceDescription.text = applicant.experience

            // Información adicional simulada (en una app real, esto vendría de la BD)
            val experienceDetailsText = buildString {
                append("Profesión: ${applicant.profession}\n")
                append("Años de experiencia: ${applicant.experience}\n\n")
                append("Áreas de especialización:\n")
                append("• Desarrollo de aplicaciones móviles\n")
                append("• Arquitectura de software\n")
                append("• Metodologías ágiles\n")
                append("• Trabajo en equipo\n")
                append("• Resolución de problemas\n\n")
                append("Proyectos destacados:\n")
                append("• Aplicación de comercio electrónico\n")
                append("• Sistema de gestión empresarial\n")
                append("• Aplicación de servicios financieros")
            }

            experienceDetails.text = experienceDetailsText
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_APPLICANT_ID = "applicant_id"
        private const val ARG_APPLICANT_NAME = "applicant_name"
        private const val ARG_APPLICANT_EMAIL = "applicant_email"
        private const val ARG_APPLICANT_PHONE = "applicant_phone"
        private const val ARG_APPLICANT_PROFESSION = "applicant_profession"
        private const val ARG_APPLICANT_EXPERIENCE = "applicant_experience"
        private const val ARG_APPLICANT_SKILLS = "applicant_skills"
        private const val ARG_APPLICATION_DATE = "application_date"
        private const val ARG_APPLICATION_STATUS = "application_status"
        private const val ARG_PROFILE_IMAGE = "profile_image"
        private const val ARG_JOB_ID = "job_id"
        private const val ARG_COVER_LETTER = "cover_letter"

        @JvmStatic
        fun newInstance(applicant: Applicant) = ApplicantExperienceFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_APPLICANT_ID, applicant.id)
                putString(ARG_APPLICANT_NAME, applicant.name)
                putString(ARG_APPLICANT_EMAIL, applicant.email)
                putString(ARG_APPLICANT_PHONE, applicant.phone)
                putString(ARG_APPLICANT_PROFESSION, applicant.profession)
                putString(ARG_APPLICANT_EXPERIENCE, applicant.experience)
                putStringArrayList(ARG_APPLICANT_SKILLS, ArrayList(applicant.skills))
                putString(ARG_APPLICATION_DATE, applicant.applicationDate)
                putString(ARG_APPLICATION_STATUS, applicant.status.name)
                putString(ARG_PROFILE_IMAGE, applicant.profileImage)
                putString(ARG_JOB_ID, applicant.jobId)
                putString(ARG_COVER_LETTER, applicant.coverLetter)
            }
        }
    }
}