package com.example.hirelink_2025.view.ui.fragments.applications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.hirelink_2025.R
import com.example.hirelink_2025.databinding.FragmentApplicantSkillsBinding
import com.example.hirelink_2025.models.Applicant
import com.example.hirelink_2025.models.ApplicationStatus
import com.google.android.material.chip.Chip

class ApplicantSkillsFragment : Fragment() {

    private var _binding: FragmentApplicantSkillsBinding? = null
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
        _binding = FragmentApplicantSkillsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSkillsInfo()
    }

    private fun setupSkillsInfo() {
        with(binding) {
            // Mostrar habilidades como chips
            skillsChipGroup.removeAllViews()

            applicant.skills.forEach { skill ->
                val chip = Chip(requireContext()).apply {
                    text = skill
                    isClickable = false
                    isCheckable = false
                    setChipBackgroundColorResource(R.color.primary)
                    setTextColor(resources.getColor(android.R.color.white, null))
                    chipStrokeWidth = 0f
                }
                skillsChipGroup.addView(chip)
            }

            // Resumen de habilidades
            val skillsCount = applicant.skills.size
            skillsSummary.text = if (skillsCount > 0) {
                "El candidato tiene $skillsCount habilidades técnicas que demuestran su experiencia en el área de desarrollo."
            } else {
                "No se han especificado habilidades técnicas."
            }
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
        fun newInstance(applicant: Applicant) = ApplicantSkillsFragment().apply {
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