package com.example.hirelink_2025.ui.fragments.applications

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hirelink_2025.R
import com.example.hirelink_2025.databinding.FragmentApplicantProfileBinding
import com.example.hirelink_2025.models.ApplicationStatus

class ApplicantProfileFragment : Fragment() {

    private var _binding: FragmentApplicantProfileBinding? = null
    private val binding get() = _binding!!

    // Datos del aplicante
    private var applicantId: String? = null
    private var applicantName: String? = null
    private var applicantEmail: String? = null
    private var applicantPhone: String? = null
    private var applicantProfession: String? = null
    private var applicantExperience: String? = null
    private var applicantSkills: ArrayList<String>? = null
    private var applicationDate: String? = null
    private var applicationStatus: String? = null
    private var profileImage: String? = null
    private var jobId: String? = null
    private var coverLetter: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let { bundle ->
            applicantId = bundle.getString("applicant_id")
            applicantName = bundle.getString("applicant_name")
            applicantEmail = bundle.getString("applicant_email")
            applicantPhone = bundle.getString("applicant_phone")
            applicantProfession = bundle.getString("applicant_profession")
            applicantExperience = bundle.getString("applicant_experience")
            applicantSkills = bundle.getStringArrayList("applicant_skills")
            applicationDate = bundle.getString("application_date")
            applicationStatus = bundle.getString("application_status")
            profileImage = bundle.getString("profile_image")
            jobId = bundle.getString("job_id")
            coverLetter = bundle.getString("cover_letter")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentApplicantProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupClickListeners()
    }

    private fun setupUI() {
        binding.apply {
            // Información básica
            applicantName.text = this@ApplicantProfileFragment.applicantName
            applicantProfession.text = this@ApplicantProfileFragment.applicantProfession
            applicantEmail.text = this@ApplicantProfileFragment.applicantEmail
            applicantPhone.text = this@ApplicantProfileFragment.applicantPhone
            applicantExperience.text = this@ApplicantProfileFragment.applicantExperience

            // ✅ CORREGIDO: Habilidades - acceso directo al TextView
            this@ApplicantProfileFragment.applicantSkills?.let { skills ->
                applicantSkills.text = skills.joinToString(", ")
            }

            // ✅ CORREGIDO: Carta de presentación - acceso directo al TextView
            this@ApplicantProfileFragment.coverLetter?.let { letter ->
                coverLetter.text = letter
            } ?: run {
                coverLetterCard.visibility = View.GONE
            }

            // Estado del aplicante
            applicationStatus?.let { status ->
                val statusEnum = try {
                    ApplicationStatus.valueOf(status)
                } catch (e: Exception) {
                    ApplicationStatus.PENDING
                }

                setupStatusIndicator(statusEnum)
                setupActionButtons(statusEnum)
            }

            // Imagen de perfil (placeholder por ahora)
            applicantPhoto.setImageResource(R.drawable.profile_random)
        }
    }

    private fun setupStatusIndicator(status: ApplicationStatus) {
        val (backgroundColor, textColor, statusText) = when (status) {
            ApplicationStatus.PENDING -> Triple(R.color.orange_pending, R.color.white, "Pendiente")
            ApplicationStatus.ACCEPTED -> Triple(R.color.green_accept, R.color.white, "Aceptado")
            ApplicationStatus.REJECTED -> Triple(R.color.red_reject, R.color.white, "Rechazado")
        }

        binding.applicantStatus.apply {
            text = statusText
            setBackgroundColor(ContextCompat.getColor(requireContext(), backgroundColor))
            setTextColor(ContextCompat.getColor(requireContext(), textColor))
        }
    }

    private fun setupActionButtons(status: ApplicationStatus) {
        binding.apply {
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
    }

    private fun setupClickListeners() {
        binding.apply {
            acceptButton.setOnClickListener {
                applicantId?.let { id ->
                    showAcceptDialog()
                }
            }

            rejectButton.setOnClickListener {
                applicantId?.let { id ->
                    showRejectDialog()
                }
            }

            contactButton.setOnClickListener {
                contactApplicant()
            }

            // Click en email para copiar o abrir app de email
            applicantEmail.setOnClickListener {
                contactApplicant()
            }

            // Click en teléfono para llamar
            applicantPhone.setOnClickListener {
                callApplicant()
            }
        }
    }

    private fun showAcceptDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Aceptar postulante")
            .setMessage("¿Quieres aceptar a $applicantName para este trabajo?")
            .setPositiveButton("Sí, aceptar") { _, _ ->
                // Aquí podrías llamar al ViewModel para actualizar el estado
                Toast.makeText(requireContext(), "$applicantName ha sido aceptado", Toast.LENGTH_LONG).show()

                // Actualizar UI local
                setupStatusIndicator(ApplicationStatus.ACCEPTED)
                setupActionButtons(ApplicationStatus.ACCEPTED)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showRejectDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Rechazar postulante")
            .setMessage("¿Quieres rechazar a $applicantName?")
            .setPositiveButton("Sí, rechazar") { _, _ ->
                // Aquí podrías llamar al ViewModel para actualizar el estado
                Toast.makeText(requireContext(), "$applicantName ha sido rechazado", Toast.LENGTH_SHORT).show()

                // Actualizar UI local
                setupStatusIndicator(ApplicationStatus.REJECTED)
                setupActionButtons(ApplicationStatus.REJECTED)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun contactApplicant() {
        applicantEmail?.let { email ->
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$email")
                putExtra(Intent.EXTRA_SUBJECT, "Respuesta a tu aplicación")
                putExtra(Intent.EXTRA_TEXT, "Hola $applicantName,\n\n")
            }

            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "No se pudo abrir el email", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun callApplicant() {
        applicantPhone?.let { phone ->
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phone")
            }

            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "No se pudo abrir el marcador", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}