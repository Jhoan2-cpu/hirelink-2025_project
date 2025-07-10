package com.example.hirelink_2025.ui.fragments.applications

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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hirelink_2025.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip

class ApplicationDetailFragment : Fragment() {

    // UI Components
    private lateinit var backButton: MaterialButton
    private lateinit var shareButton: MaterialButton
    private lateinit var toolbarTitle: TextView
    
    // Application Header
    private lateinit var companyLogo: ImageView
    private lateinit var jobTitle: TextView
    private lateinit var companyName: TextView
    private lateinit var applicationDate: TextView
    
    // Status
    private lateinit var statusChip: Chip
    private lateinit var step1Indicator: View
    private lateinit var step2Indicator: View
    private lateinit var step3Indicator: View
    
    // Job Details
    private lateinit var modalityChip: Chip
    private lateinit var employmentTypeChip: Chip
    private lateinit var salaryChip: Chip
    private lateinit var vacanciesChip: Chip
    private lateinit var jobDescription: TextView
    private lateinit var jobRequirements: TextView
    
    // Contact
    private lateinit var phoneLayout: LinearLayout
    private lateinit var emailLayout: LinearLayout
    private lateinit var websiteLayout: LinearLayout
    private lateinit var companyPhone: TextView
    private lateinit var companyEmail: TextView
    private lateinit var companyWebsite: TextView
    
    // Action Button
    private lateinit var cancelApplicationButton: MaterialButton
    
    // Application data
    private var applicationData: ApplicationData? = null
    
    data class ApplicationData(
        val jobTitle: String = "",
        val companyName: String = "",
        val applicationDate: String = "",
        val status: String = "",
        val modality: String = "",
        val employmentType: String = "",
        val salary: String = "",
        val vacancies: String = "",
        val jobDescription: String = "",
        val jobRequirements: String = "",
        val companyPhone: String = "",
        val companyEmail: String = "",
        val companyWebsite: String = "",
        val logoResId: Int = 0
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_application_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        getApplicationDataFromArguments()
        setupClickListeners()
        displayApplicationData()
    }

    private fun initViews(view: View) {
        // Toolbar
        backButton = view.findViewById(R.id.backButton)
        shareButton = view.findViewById(R.id.shareButton)
        toolbarTitle = view.findViewById(R.id.toolbarTitle)
        
        // Application Header
        companyLogo = view.findViewById(R.id.companyLogo)
        jobTitle = view.findViewById(R.id.jobTitle)
        companyName = view.findViewById(R.id.companyName)
        applicationDate = view.findViewById(R.id.applicationDate)
        
        // Status
        statusChip = view.findViewById(R.id.statusChip)
        step1Indicator = view.findViewById(R.id.step1Indicator)
        step2Indicator = view.findViewById(R.id.step2Indicator)
        step3Indicator = view.findViewById(R.id.step3Indicator)
        
        // Job Details
        modalityChip = view.findViewById(R.id.modalityChip)
        employmentTypeChip = view.findViewById(R.id.employmentTypeChip)
        salaryChip = view.findViewById(R.id.salaryChip)
        vacanciesChip = view.findViewById(R.id.vacanciesChip)
        jobDescription = view.findViewById(R.id.jobDescription)
        jobRequirements = view.findViewById(R.id.jobRequirements)
        
        // Contact
        phoneLayout = view.findViewById(R.id.phoneLayout)
        emailLayout = view.findViewById(R.id.emailLayout)
        websiteLayout = view.findViewById(R.id.websiteLayout)
        companyPhone = view.findViewById(R.id.companyPhone)
        companyEmail = view.findViewById(R.id.companyEmail)
        companyWebsite = view.findViewById(R.id.companyWebsite)
        
        // Action Button
        cancelApplicationButton = view.findViewById(R.id.cancelApplicationButton)
    }
    
    private fun getApplicationDataFromArguments() {
        arguments?.let { args ->
            applicationData = ApplicationData(
                jobTitle = args.getString("job_title", ""),
                companyName = args.getString("company_name", ""),
                applicationDate = args.getString("application_date", ""),
                status = args.getString("status", ""),
                modality = args.getString("modality", ""),
                employmentType = args.getString("employment_type", ""),
                salary = args.getString("salary", ""),
                vacancies = args.getString("vacancies", ""),
                jobDescription = args.getString("job_description", ""),
                jobRequirements = args.getString("job_requirements", ""),
                companyPhone = args.getString("company_phone", ""),
                companyEmail = args.getString("company_email", ""),
                companyWebsite = args.getString("company_website", ""),
                logoResId = args.getInt("company_logo", 0)
            )
        }
    }
    
    private fun setupClickListeners() {
        backButton.setOnClickListener {
            findNavController().navigateUp()
        }
        
        shareButton.setOnClickListener {
            shareApplication()
        }
        
        // Contact click listeners
        phoneLayout.setOnClickListener {
            dialPhone(applicationData?.companyPhone ?: "")
        }
        
        emailLayout.setOnClickListener {
            sendEmail(applicationData?.companyEmail ?: "")
        }
        
        websiteLayout.setOnClickListener {
            openWebsite(applicationData?.companyWebsite ?: "")
        }
        
        cancelApplicationButton.setOnClickListener {
            showCancelConfirmation()
        }
    }

    private fun displayApplicationData() {
        val data = applicationData ?: return
        
        // Application header
        jobTitle.text = data.jobTitle.ifEmpty { "Desarrollador Android Senior" }
        companyName.text = data.companyName.ifEmpty { "TechSolutions S.A.C." }
        applicationDate.text = data.applicationDate.ifEmpty { "Postulado hace 3 días" }
        
        // Company logo
        if (data.logoResId != 0) {
            companyLogo.setImageResource(data.logoResId)
        }
        
        // Status and progress
        updateStatusAndProgress(data.status)
        
        // Job detail chips
        modalityChip.text = data.modality.ifEmpty { "Remoto" }
        employmentTypeChip.text = data.employmentType.ifEmpty { "Tiempo completo" }
        salaryChip.text = data.salary.ifEmpty { "S/. 5000 - 7000" }
        vacanciesChip.text = if (data.vacancies.isNotEmpty()) "${data.vacancies} vacantes" else "5 vacantes"
        
        // Job content
        jobDescription.text = data.jobDescription.ifEmpty {
            "Buscamos un desarrollador Android con experiencia en Kotlin, arquitectura MVVM y desarrollo de aplicaciones nativas."
        }
        
        jobRequirements.text = data.jobRequirements.ifEmpty {
            "• 3+ años de experiencia en desarrollo Android\n• Conocimiento en Kotlin y Java\n• Experiencia con MVVM y LiveData\n• Conocimiento de APIs REST y bases de datos"
        }
        
        // Contact information - Always show default data
        companyPhone.text = data.companyPhone.ifEmpty { "+51 987654321" }
        companyEmail.text = data.companyEmail.ifEmpty { "rrhh@techsolutions.com" }
        companyWebsite.text = data.companyWebsite.ifEmpty { "www.techsolutions.com" }
        
        // Always show contact sections with default data
        phoneLayout.visibility = View.VISIBLE
        emailLayout.visibility = View.VISIBLE
        websiteLayout.visibility = View.VISIBLE
    }

    private fun updateStatusAndProgress(status: String) {
        val statusText = status.ifEmpty { "En revisión" }
        statusChip.text = statusText
        
        // Set status chip color and icon
        when (statusText) {
            "En revisión" -> {
                statusChip.setChipBackgroundColorResource(R.color.secondary)
                statusChip.setChipIconResource(R.drawable.ic_pending)
                updateProgressIndicators(1)
            }
            "Aceptado" -> {
                statusChip.setChipBackgroundColorResource(R.color.success)
                statusChip.setChipIconResource(R.drawable.ic_check_circle)
                updateProgressIndicators(3)
            }
            "Rechazado" -> {
                statusChip.setChipBackgroundColorResource(R.color.error)
                statusChip.setChipIconResource(R.drawable.ic_rejected)
                updateProgressIndicators(3)
            }
            else -> {
                statusChip.setChipBackgroundColorResource(R.color.text_hint)
                statusChip.setChipIconResource(R.drawable.ic_pending)
                updateProgressIndicators(1)
            }
        }
        
        statusChip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
    }
    
    private fun updateProgressIndicators(currentStep: Int) {
        // Update step indicators based on current status
        val primaryColor = ContextCompat.getColor(requireContext(), R.color.primary)
        val secondaryColor = ContextCompat.getColor(requireContext(), R.color.secondary)
        val hintColor = ContextCompat.getColor(requireContext(), R.color.text_hint)
        
        step1Indicator.setBackgroundColor(primaryColor) // Always completed
        step2Indicator.setBackgroundColor(if (currentStep >= 2) secondaryColor else hintColor)
        step3Indicator.setBackgroundColor(if (currentStep >= 3) primaryColor else hintColor)
    }

    private fun dialPhone(phone: String) {
        val phoneToUse = phone.ifEmpty { "+51 987654321" }
        
        val formattedPhone = if (phoneToUse.startsWith("+")) phoneToUse else "+51$phoneToUse"
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$formattedPhone")
        }
        
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "No se pudo abrir el marcador", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun sendEmail(email: String) {
        val emailToUse = email.ifEmpty { "rrhh@techsolutions.com" }
        
        val jobTitle = applicationData?.jobTitle ?: "empleo"
        val companyName = applicationData?.companyName ?: "empresa"
        
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$emailToUse")
            putExtra(Intent.EXTRA_SUBJECT, "Consulta sobre mi postulación: $jobTitle")
            putExtra(Intent.EXTRA_TEXT, "Hola, me gustaría consultar sobre el estado de mi postulación para $jobTitle en $companyName.")
        }
        
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "No se pudo abrir el cliente de email", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun openWebsite(website: String) {
        val websiteToUse = website.ifEmpty { "www.techsolutions.com" }
        
        val url = if (websiteToUse.startsWith("http://") || websiteToUse.startsWith("https://")) {
            websiteToUse
        } else {
            "https://$websiteToUse"
        }
        
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
        }
        
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "No se pudo abrir el sitio web", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun shareApplication() {
        val data = applicationData ?: return
        
        val shareText = """
            Mi postulación en HireLink:
            
            Puesto: ${data.jobTitle}
            Empresa: ${data.companyName}
            Estado: ${data.status}
            Fecha de postulación: ${data.applicationDate}
            
            Enviado desde HireLink
        """.trimIndent()

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, "Mi postulación: ${data.jobTitle}")
        }

        try {
            startActivity(Intent.createChooser(shareIntent, "Compartir postulación"))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "No se pudo compartir", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showCancelConfirmation() {
        val data = applicationData ?: return
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Cancelar postulación")
            .setMessage("¿Estás seguro de que quieres cancelar tu postulación para ${data.jobTitle} en ${data.companyName}?")
            .setPositiveButton("Sí, cancelar") { _, _ ->
                cancelApplication()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun cancelApplication() {
        Toast.makeText(requireContext(), "Postulación cancelada exitosamente", Toast.LENGTH_LONG).show()
        findNavController().navigateUp()
    }

    companion object {
        @JvmStatic
        fun newInstance(
            jobTitle: String,
            companyName: String,
            applicationDate: String,
            status: String,
            modality: String,
            employmentType: String,
            salary: String,
            vacancies: String,
            jobDescription: String,
            jobRequirements: String,
            companyPhone: String,
            companyEmail: String,
            companyWebsite: String,
            logoResId: Int
        ) = ApplicationDetailFragment().apply {
            arguments = Bundle().apply {
                putString("job_title", jobTitle)
                putString("company_name", companyName)
                putString("application_date", applicationDate)
                putString("status", status)
                putString("modality", modality)
                putString("employment_type", employmentType)
                putString("salary", salary)
                putString("vacancies", vacancies)
                putString("job_description", jobDescription)
                putString("job_requirements", jobRequirements)
                putString("company_phone", companyPhone)
                putString("company_email", companyEmail)
                putString("company_website", companyWebsite)
                putInt("company_logo", logoResId)
            }
        }
    }
}