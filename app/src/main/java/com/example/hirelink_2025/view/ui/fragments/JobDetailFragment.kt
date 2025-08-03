package com.example.hirelink_2025.view.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.hirelink_2025.R
import com.example.hirelink_2025.models.Job
import com.example.hirelink_2025.models.Company
import com.example.hirelink_2025.view.ui.MapFragment
import com.example.hirelink_2025.viewmodels.JobDetailUiState
import com.example.hirelink_2025.viewmodels.JobDetailViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class JobDetailFragment : Fragment() {

    private val viewModel: JobDetailViewModel by viewModels()

    // Views
    private lateinit var progressBar: ProgressBar
    private lateinit var backButton: ImageButton
    private lateinit var bookmarkButton: ImageButton
    private lateinit var titleText: TextView
    private lateinit var companyText: TextView
    private lateinit var locationText: TextView
    private lateinit var salaryText: TextView
    private lateinit var modalityText: TextView
    private lateinit var employmentTypeText: TextView
    private lateinit var descriptionText: TextView
    private lateinit var requirementsText: TextView
    private lateinit var applyButton: Button
    private lateinit var companyInfoContainer: View
    private lateinit var companyLogoImageView: ImageView
    private lateinit var companyDescriptionText: TextView
    private lateinit var companyPhoneText: TextView
    private lateinit var companyEmailText: TextView
    private lateinit var companyWebsiteText: TextView
    private lateinit var postedDateText: TextView
    private lateinit var vacanciesText: TextView
    private lateinit var mapContainer: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_job_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupClickListeners()
        observeViewModel()

        // Obtener jobId del Bundle (temporal hasta que Safe Args funcione)
        val jobId = arguments?.getString("jobId") ?: ""
        if (jobId.isNotEmpty()) {
            viewModel.loadJobDetail(jobId)
        } else {
            Toast.makeText(requireContext(), "Error: ID de empleo no encontrado", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    private fun initViews(view: View) {
        progressBar = view.findViewById(R.id.progressBar)
        backButton = view.findViewById(R.id.backButton)
        bookmarkButton = view.findViewById(R.id.bookmarkButton)
        titleText = view.findViewById(R.id.titleText)
        companyText = view.findViewById(R.id.companyText)
        locationText = view.findViewById(R.id.locationText)
        salaryText = view.findViewById(R.id.salaryText)
        modalityText = view.findViewById(R.id.modalityText)
        descriptionText = view.findViewById(R.id.descriptionText)
        requirementsText = view.findViewById(R.id.requirementsText)
        applyButton = view.findViewById(R.id.applyButton)
        
        // Views adicionales que pueden existir o no en el layout
        try {
            employmentTypeText = view.findViewById(R.id.employmentTypeText)
            companyInfoContainer = view.findViewById(R.id.companyInfoContainer)
            companyLogoImageView = view.findViewById(R.id.companyLogoImageView)
            companyDescriptionText = view.findViewById(R.id.companyDescriptionText)
            companyPhoneText = view.findViewById(R.id.companyPhoneText)
            companyEmailText = view.findViewById(R.id.companyEmailText)
            companyWebsiteText = view.findViewById(R.id.companyWebsiteText)
            postedDateText = view.findViewById(R.id.postedDateText)
            vacanciesText = view.findViewById(R.id.vacanciesText)
            mapContainer = view.findViewById(R.id.mapContainer)
        } catch (e: Exception) {
            // Views opcionales, si no existen no hay problema
        }
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        bookmarkButton.setOnClickListener {
            viewModel.toggleBookmark()
        }

        applyButton.setOnClickListener {
            viewModel.applyToJob()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateUI(state)
                }
            }
        }
    }

    private fun updateUI(state: JobDetailUiState) {
        // Loading state
        progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE

        // Job data
        state.job?.let { job ->
            populateJobData(job, state.company)
        }

        // Bookmark button
        updateBookmarkButton(state.bookmarked)

        // Apply button
        applyButton.isEnabled = !state.isApplying
        applyButton.text = if (state.isApplying) "Aplicando..." else "Aplicar"

        // Error handling
        state.error?.let { error ->
            Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }

        // Success
        if (state.applicationSuccess) {
            Toast.makeText(requireContext(), "¡Aplicación enviada exitosamente!", Toast.LENGTH_LONG).show()
            viewModel.clearApplicationSuccess()
        }
    }

    private fun populateJobData(job: Job, company: Company?) {
        // Datos del trabajo
        titleText.text = job.title
        
        // Información de la empresa
        companyText.text = company?.name ?: "Empresa no disponible"
        
        // Ubicación de la empresa
        locationText.text = when {
            !company?.city.isNullOrBlank() && !company?.country.isNullOrBlank() -> 
                "${company.city}, ${company.country}"
            !company?.city.isNullOrBlank() -> company.city
            !company?.country.isNullOrBlank() -> company.country
            else -> "Ubicación no especificada"
        }
        
        // Datos del trabajo
        salaryText.text = if (job.salary.isNotBlank()) job.salary else "Salario a convenir"
        modalityText.text = job.modality.ifBlank { "No especificado" }
        
        // Tipo de empleo si existe el campo
        try {
            employmentTypeText.text = job.employmentType.ifBlank { "No especificado" }
        } catch (e: Exception) {
            // Campo no existe en el layout
        }
        
        // Descripción del trabajo
        descriptionText.text = if (job.aboutJob.isNotBlank()) {
            job.aboutJob
        } else {
            "Descripción no disponible"
        }
        
        // Requisitos
        requirementsText.text = if (job.requirements.isNotEmpty()) {
            job.requirements.joinToString("\n• ", "• ")
        } else {
            "• No se especifican requisitos"
        }
        
        // Fecha de publicación si existe el campo
        try {
            postedDateText.text = formatDate(job.createdAt)
        } catch (e: Exception) {
            // Campo no existe en el layout
        }
        
        // Número de vacantes si existe el campo
        try {
            vacanciesText.text = if (job.vacancies > 0) {
                "${job.vacancies} vacante${if (job.vacancies > 1) "s" else ""}"
            } else {
                "Vacantes no especificadas"
            }
        } catch (e: Exception) {
            // Campo no existe en el layout
        }
        
        // Información adicional de la empresa si existen los campos
        populateCompanyInfo(company)
    }

    private fun populateCompanyInfo(company: Company?) {
        if (company == null) return
        
        try {
            // Logo de la empresa
            if (!company.logoUrl.isNullOrBlank()) {
                Glide.with(this)
                    .load(company.logoUrl)
                    .placeholder(R.drawable.ic_placeholder_company)
                    .error(R.drawable.ic_placeholder_company)
                    .circleCrop()
                    .into(companyLogoImageView)
            } else {
                companyLogoImageView.setImageResource(R.drawable.ic_placeholder_company)
            }
            
            // Descripción de la empresa
            companyDescriptionText.text = if (company.description.isNotBlank()) {
                company.description
            } else {
                "Descripción de la empresa no disponible"
            }
            
            // Teléfono de la empresa
            companyPhoneText.text = if (company.phone.isNotBlank()) {
                company.phone
            } else {
                "Teléfono no disponible"
            }
            
            // Email de la empresa
            companyEmailText.text = if (company.email.isNotBlank()) {
                company.email
            } else {
                "Email no disponible"
            }
            
            // Website de la empresa
            companyWebsiteText.text = if (company.website.isNotBlank()) {
                company.website
            } else {
                "Sitio web no disponible"
            }
            
        } catch (e: Exception) {
            // Campos opcionales no existen en el layout actual
        }
    }

    private fun formatDate(timestamp: Long): String {
        return try {
            val date = Date(timestamp)
            val format = SimpleDateFormat("dd MMM yyyy", Locale("es", "ES"))
            "Publicado el ${format.format(date)}"
        } catch (e: Exception) {
            "Fecha no disponible"
        }
    }

    private fun updateBookmarkButton(bookmarked: Boolean) {
        val iconRes = if (bookmarked) {
            R.drawable.ic_bookmark_filled
        } else {
            R.drawable.ic_bookmark_border
        }

        try {
            bookmarkButton.setImageResource(iconRes)
        } catch (e: Exception) {
            bookmarkButton.setImageResource(android.R.drawable.ic_menu_save)
        }
    }
}