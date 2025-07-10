package com.example.hirelink_2025.ui.fragments.common

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.hirelink_2025.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

class JobDescriptionDetailFragment : Fragment() {

    // UI Components
    private lateinit var backButton: MaterialButton
    private lateinit var shareButton: MaterialButton
    private lateinit var toolbarTitle: TextView
    
    // Job Header Components
    private lateinit var companyLogo: ImageView
    private lateinit var jobTitle: TextView
    private lateinit var companyName: TextView
    private lateinit var locationAndDate: TextView
    
    // Chips
    private lateinit var modalityChip: Chip
    private lateinit var vacanciesChip: Chip
    private lateinit var employmentTypeChip: Chip
    private lateinit var salaryChip: Chip
    
    // Content sections
    private lateinit var aboutCompany: TextView
    private lateinit var aboutJob: TextView
    private lateinit var jobRequirements: TextView
    
    // Contact information
    private lateinit var phoneLayout: LinearLayout
    private lateinit var emailLayout: LinearLayout
    private lateinit var websiteLayout: LinearLayout
    private lateinit var companyPhone: TextView
    private lateinit var companyEmail: TextView
    private lateinit var companyWebsite: TextView
    
    // Apply button
    private lateinit var applyJobButton: MaterialButton

    // Job data
    private var jobData: JobData? = null
    
    data class JobData(
        val id: Int = 0,
        val title: String = "",
        val company: String = "",
        val location: String = "",
        val salary: String = "",
        val modality: String = "",
        val vacancies: String = "",
        val employmentType: String = "",
        val companyDescription: String = "",
        val jobDescription: String = "",
        val requirements: String = "",
        val phone: String = "",
        val email: String = "",
        val website: String = "",
        val publishedDate: String = ""
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_job_description_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        getJobDataFromArguments()
        setupClickListeners()
        displayJobData()
    }

    private fun initViews(view: View) {
        // Toolbar
        backButton = view.findViewById(R.id.backButton)
        shareButton = view.findViewById(R.id.shareButton)
        toolbarTitle = view.findViewById(R.id.toolbarTitle)
        
        // Job Header
        companyLogo = view.findViewById(R.id.companyLogo)
        jobTitle = view.findViewById(R.id.jobTitle)
        companyName = view.findViewById(R.id.companyName)
        locationAndDate = view.findViewById(R.id.locationAndDate)
        
        // Chips
        modalityChip = view.findViewById(R.id.modalityChip)
        vacanciesChip = view.findViewById(R.id.vacanciesChip)
        employmentTypeChip = view.findViewById(R.id.employmentTypeChip)
        salaryChip = view.findViewById(R.id.salaryChip)
        
        // Content sections
        aboutCompany = view.findViewById(R.id.aboutCompany)
        aboutJob = view.findViewById(R.id.aboutJob)
        jobRequirements = view.findViewById(R.id.jobRequirements)
        
        // Contact information
        phoneLayout = view.findViewById(R.id.phoneLayout)
        emailLayout = view.findViewById(R.id.emailLayout)
        websiteLayout = view.findViewById(R.id.websiteLayout)
        companyPhone = view.findViewById(R.id.companyPhone)
        companyEmail = view.findViewById(R.id.companyEmail)
        companyWebsite = view.findViewById(R.id.companyWebsite)
        
        // Apply button
        applyJobButton = view.findViewById(R.id.applyJobButton)
    }
    
    private fun getJobDataFromArguments() {
        arguments?.let { bundle ->
            jobData = JobData(
                id = bundle.getInt("job_id", 0),
                title = bundle.getString("job_title", ""),
                company = bundle.getString("job_company", ""),
                location = bundle.getString("job_location", ""),
                salary = bundle.getString("job_salary", ""),
                modality = bundle.getString("job_modality", ""),
                vacancies = bundle.getString("job_vacancies", ""),
                employmentType = bundle.getString("job_employment_type", ""),
                companyDescription = bundle.getString("company_description", ""),
                jobDescription = bundle.getString("job_description", ""),
                requirements = bundle.getString("job_requirements", ""),
                phone = bundle.getString("company_phone", ""),
                email = bundle.getString("company_email", ""),
                website = bundle.getString("company_website", ""),
                publishedDate = bundle.getString("job_published_date", "")
            )
        }
    }


    private fun setupClickListeners() {
        backButton.setOnClickListener {
            findNavController().navigateUp()
        }
        
        shareButton.setOnClickListener {
            shareJob()
        }
        
        // Contact click listeners
        phoneLayout.setOnClickListener {
            dialPhone(jobData?.phone ?: "")
        }
        
        emailLayout.setOnClickListener {
            sendEmail(jobData?.email ?: "")
        }
        
        websiteLayout.setOnClickListener {
            openWebsite(jobData?.website ?: "")
        }
        
        applyJobButton.setOnClickListener {
            applyToJob()
        }
    }

    private fun displayJobData() {
        val data = jobData ?: return
        
        // Job header
        jobTitle.text = data.title.ifEmpty { "Desarrollador Android Senior" }
        companyName.text = data.company.ifEmpty { "TechSolutions S.A.C." }
        locationAndDate.text = formatLocationAndDate(data.location, data.publishedDate)
        
        // Job detail chips
        modalityChip.text = data.modality.ifEmpty { "Remoto" }
        vacanciesChip.text = if (data.vacancies.isNotEmpty()) "${data.vacancies} vacantes" else "5 vacantes"
        employmentTypeChip.text = data.employmentType.ifEmpty { "Tiempo completo" }
        salaryChip.text = data.salary.ifEmpty { "S/. 5000 - 7000" }
        
        // Content sections
        aboutCompany.text = data.companyDescription.ifEmpty {
            "Empresa líder en desarrollo de software y soluciones tecnológicas innovadoras para el mercado peruano y latinoamericano."
        }
        
        aboutJob.text = data.jobDescription.ifEmpty {
            "Buscamos un desarrollador Android con experiencia en Kotlin, arquitectura MVVM y desarrollo de aplicaciones nativas."
        }
        
        jobRequirements.text = data.requirements.ifEmpty {
            "• 3+ años de experiencia en desarrollo Android\n• Conocimiento en Kotlin y Java\n• Experiencia con MVVM y LiveData\n• Conocimiento de APIs REST y bases de datos"
        }
        
        // Contact information - Always show default data
        companyPhone.text = data.phone.ifEmpty { "+51 987654321" }
        companyEmail.text = data.email.ifEmpty { "rrhh@techsolutions.com" }
        companyWebsite.text = data.website.ifEmpty { "www.techsolutions.com" }
        
        // Always show contact sections with default data
        phoneLayout.visibility = View.VISIBLE
        emailLayout.visibility = View.VISIBLE
        websiteLayout.visibility = View.VISIBLE
    }
    
    private fun formatLocationAndDate(location: String, publishedDate: String): String {
        val loc = location.ifEmpty { "Lima, Perú" }
        val date = publishedDate.ifEmpty { "Hace 2 días" }
        return "$loc • $date"
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
        
        val jobTitle = jobData?.title ?: "empleo"
        val companyName = jobData?.company ?: "empresa"
        
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$emailToUse")
            putExtra(Intent.EXTRA_SUBJECT, "Consulta sobre: $jobTitle")
            putExtra(Intent.EXTRA_TEXT, "Hola, me interesa la oferta laboral para $jobTitle en $companyName.")
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


    private fun shareJob() {
        val data = jobData ?: return
        
        val shareText = """
            ¡Mira esta oportunidad laboral!
            
            Puesto: ${data.title}
            Empresa: ${data.company}
            Ubicación: ${data.location}
            Salario: ${data.salary}
            Modalidad: ${data.modality}
            
            Enviado desde HireLink
        """.trimIndent()

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, "Oportunidad laboral: ${data.title}")
        }

        try {
            startActivity(Intent.createChooser(shareIntent, "Compartir trabajo"))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "No se pudo compartir", Toast.LENGTH_SHORT).show()
        }
    }



    private fun applyToJob() {
        val data = jobData ?: return
        
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Aplicar a este trabajo")
        builder.setMessage("¿Estás seguro de que quieres aplicar a este puesto en ${data.company}?")

        builder.setPositiveButton("Aplicar") { _, _ ->
            processJobApplication()
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        builder.create().show()
    }

    private fun processJobApplication() {
        Toast.makeText(requireContext(), "¡Aplicación enviada exitosamente!", Toast.LENGTH_LONG).show()

        applyJobButton.apply {
            text = "Aplicado ✓"
            isEnabled = false
            alpha = 0.6f
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(
            jobId: Int,
            jobTitle: String,
            jobCompany: String,
            jobLocation: String,
            jobSalary: String,
            jobModality: String,
            jobVacancies: String,
            jobEmploymentType: String,
            companyDescription: String,
            jobDescription: String,
            jobRequirements: String,
            companyPhone: String,
            companyEmail: String,
            companyWebsite: String,
            jobPublishedDate: String
        ) = JobDescriptionDetailFragment().apply {
            arguments = Bundle().apply {
                putInt("job_id", jobId)
                putString("job_title", jobTitle)
                putString("job_company", jobCompany)
                putString("job_location", jobLocation)
                putString("job_salary", jobSalary)
                putString("job_modality", jobModality)
                putString("job_vacancies", jobVacancies)
                putString("job_employment_type", jobEmploymentType)
                putString("company_description", companyDescription)
                putString("job_description", jobDescription)
                putString("job_requirements", jobRequirements)
                putString("company_phone", companyPhone)
                putString("company_email", companyEmail)
                putString("company_website", companyWebsite)
                putString("job_published_date", jobPublishedDate)
            }
        }
    }
}