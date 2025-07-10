package com.example.hirelink_2025.ui.fragments.ads

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.hirelink_2025.R
import com.example.hirelink_2025.models.Company
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CompanyDetailFragment : Fragment() {
    
    private lateinit var toolbar: MaterialToolbar
    private lateinit var companyLogo: ImageView
    private lateinit var companyName: TextView
    private lateinit var companyType: TextView
    private lateinit var companyDescription: TextView
    private lateinit var companyAddress: TextView
    private lateinit var companyCityCountry: TextView
    private lateinit var companyPhone: TextView
    private lateinit var companyEmail: TextView
    private lateinit var companyWebsite: TextView
    private lateinit var employeeCountChip: Chip
    private lateinit var activeJobsChip: Chip
    private lateinit var foundedYearChip: Chip
    private lateinit var phoneLayout: LinearLayout
    private lateinit var emailLayout: LinearLayout
    private lateinit var websiteLayout: LinearLayout
    private lateinit var editCompanyButton: MaterialButton
    private lateinit var deleteCompanyButton: MaterialButton
    
    private var currentCompany: Company? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_company_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupClickListeners()
        loadCompanyData()
    }
    
    private fun initViews(view: View) {
        toolbar = view.findViewById(R.id.toolbar)
        companyLogo = view.findViewById(R.id.companyLogo)
        companyName = view.findViewById(R.id.companyName)
        companyType = view.findViewById(R.id.companyType)
        companyDescription = view.findViewById(R.id.companyDescription)
        companyAddress = view.findViewById(R.id.companyAddress)
        companyCityCountry = view.findViewById(R.id.companyCityCountry)
        companyPhone = view.findViewById(R.id.companyPhone)
        companyEmail = view.findViewById(R.id.companyEmail)
        companyWebsite = view.findViewById(R.id.companyWebsite)
        employeeCountChip = view.findViewById(R.id.employeeCountChip)
        activeJobsChip = view.findViewById(R.id.activeJobsChip)
        foundedYearChip = view.findViewById(R.id.foundedYearChip)
        phoneLayout = view.findViewById(R.id.phoneLayout)
        emailLayout = view.findViewById(R.id.emailLayout)
        websiteLayout = view.findViewById(R.id.websiteLayout)
        editCompanyButton = view.findViewById(R.id.editCompanyButton)
        deleteCompanyButton = view.findViewById(R.id.deleteCompanyButton)
    }
    
    private fun setupClickListeners() {
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        
        phoneLayout.setOnClickListener {
            currentCompany?.phone?.let { phone ->
                if (phone.isNotEmpty()) {
                    dialPhone(phone)
                }
            }
        }
        
        emailLayout.setOnClickListener {
            currentCompany?.email?.let { email ->
                if (email.isNotEmpty()) {
                    sendEmail(email)
                }
            }
        }
        
        websiteLayout.setOnClickListener {
            currentCompany?.website?.let { website ->
                if (website.isNotEmpty()) {
                    openWebsite(website)
                }
            }
        }
        
        editCompanyButton.setOnClickListener {
            currentCompany?.let { company ->
                editCompany(company)
            }
        }
        
        deleteCompanyButton.setOnClickListener {
            currentCompany?.let { company ->
                confirmDeleteCompany(company)
            }
        }
    }
    
    private fun loadCompanyData() {
        // Get company ID from navigation arguments
        val companyId = arguments?.getString("companyId") ?: ""
        
        // TODO: Load company from repository using companyId
        // For now, get sample company data based on ID
        currentCompany = getSampleCompany(companyId)
        
        populateViews()
    }
    
    private fun getSampleCompany(companyId: String): Company {
        // Sample company data (replace with actual repository call)
        return when (companyId) {
            "1" -> Company(
                id = "1",
                name = "TechSolutions S.A.C.",
                type = "Tecnología",
                description = "Empresa líder en desarrollo de software y soluciones tecnológicas innovadoras para el mercado peruano y latinoamericano. Nos especializamos en crear aplicaciones móviles, sistemas web y soluciones de inteligencia artificial que transforman la manera en que las empresas hacen negocios.",
                size = "50-100",
                foundedYear = 2020,
                address = "Av. Javier Prado Este 123, San Isidro",
                city = "Lima",
                country = "Perú",
                phone = "987654321",
                email = "info@techsolutions.com",
                website = "www.techsolutions.com",
                logoUrl = "",
                employeeCount = 75,
                activeJobsCount = 3,
                ownerId = "user1"
            )
            "2" -> Company(
                id = "2",
                name = "Innovate Corp",
                type = "Consultoría",
                description = "Consultoría especializada en transformación digital y gestión empresarial. Ayudamos a las empresas a modernizar sus procesos, implementar nuevas tecnologías y optimizar su rendimiento operativo para alcanzar el éxito en la era digital.",
                size = "10-50",
                foundedYear = 2018,
                address = "Calle Los Incas 456, Cercado",
                city = "Arequipa",
                country = "Perú",
                phone = "123456789",
                email = "contact@innovate.com",
                website = "www.innovate.com",
                logoUrl = "",
                employeeCount = 25,
                activeJobsCount = 1,
                ownerId = "user1"
            )
            "3" -> Company(
                id = "3",
                name = "DigitalWorks",
                type = "Marketing Digital",
                description = "Agencia de marketing digital especializada en estrategias de crecimiento online, gestión de redes sociales, publicidad digital y desarrollo de marca. Creamos campañas efectivas que generan resultados medibles para nuestros clientes.",
                size = "1-10",
                foundedYear = 2022,
                address = "Jr. Junín 789, Centro Histórico",
                city = "Cusco",
                country = "Perú",
                phone = "555666777",
                email = "hello@digitalworks.pe",
                website = "www.digitalworks.pe",
                logoUrl = "",
                employeeCount = 8,
                activeJobsCount = 2,
                ownerId = "user1"
            )
            else -> Company(
                id = companyId,
                name = "Compañía de Ejemplo",
                type = "General",
                description = "Descripción de ejemplo para la compañía.",
                size = "1-10",
                foundedYear = 2023,
                address = "Dirección de ejemplo",
                city = "Lima",
                country = "Perú",
                phone = "",
                email = "",
                website = "",
                logoUrl = "",
                employeeCount = 0,
                activeJobsCount = 0,
                ownerId = "user1"
            )
        }
    }
    
    private fun populateViews() {
        currentCompany?.let { company ->
            // Basic info
            companyName.text = company.name
            companyType.text = "${company.type} • ${company.city}, ${company.country}"
            companyDescription.text = company.description.ifEmpty { "Sin descripción disponible" }
            
            // Location
            val fullAddress = if (company.address.isNotEmpty()) {
                "${company.address}, ${company.city}, ${company.country}"
            } else {
                "${company.city}, ${company.country}"
            }
            companyAddress.text = fullAddress
            companyCityCountry.text = "${company.city}, ${company.country}"
            
            // Contact info
            setupContactInfo(company)
            
            // Stats chips
            setupStatsChips(company)
            
            // TODO: Load company logo if available
            // If logoUrl is not empty, load the image using Glide or similar
        }
    }
    
    private fun setupContactInfo(company: Company) {
        // Phone
        if (company.phone.isNotEmpty()) {
            companyPhone.text = "+51 ${company.phone}"
            phoneLayout.visibility = View.VISIBLE
        } else {
            phoneLayout.visibility = View.GONE
        }
        
        // Email
        if (company.email.isNotEmpty()) {
            companyEmail.text = company.email
            emailLayout.visibility = View.VISIBLE
        } else {
            emailLayout.visibility = View.GONE
        }
        
        // Website
        if (company.website.isNotEmpty()) {
            companyWebsite.text = company.website
            websiteLayout.visibility = View.VISIBLE
        } else {
            websiteLayout.visibility = View.GONE
        }
    }
    
    private fun setupStatsChips(company: Company) {
        // Employee count
        val employeeText = when {
            company.employeeCount == 0 -> company.size.ifEmpty { "No especificado" }
            company.employeeCount == 1 -> "1 empleado"
            else -> "${company.employeeCount} empleados"
        }
        employeeCountChip.text = employeeText
        
        // Active jobs
        val jobsText = when (company.activeJobsCount) {
            0 -> "Sin empleos activos"
            1 -> "1 empleo activo"
            else -> "${company.activeJobsCount} empleos activos"
        }
        activeJobsChip.text = jobsText
        
        // Founded year
        val foundedText = if (company.foundedYear > 0) {
            "Fundada en ${company.foundedYear}"
        } else {
            "Año no especificado"
        }
        foundedYearChip.text = foundedText
    }
    
    private fun dialPhone(phone: String) {
        try {
            val formattedPhone = if (phone.startsWith("+")) phone else "+51$phone"
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$formattedPhone")
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "No se puede realizar la llamada", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun sendEmail(email: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$email")
                putExtra(Intent.EXTRA_SUBJECT, "Consulta sobre ${currentCompany?.name}")
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "No se puede enviar el email", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun openWebsite(website: String) {
        try {
            val url = if (website.startsWith("http://") || website.startsWith("https://")) {
                website
            } else {
                "https://$website"
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "No se puede abrir el sitio web", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun editCompany(company: Company) {
        val bundle = Bundle().apply {
            putString("companyId", company.id)
        }
        findNavController().navigate(R.id.action_companyDetailFragment_to_companyEditFragment, bundle)
    }
    
    private fun confirmDeleteCompany(company: Company) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar Compañía")
            .setMessage("¿Estás seguro de que deseas eliminar \"${company.name}\"? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                deleteCompany(company)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun deleteCompany(company: Company) {
        // TODO: Delete company from repository/database
        Toast.makeText(requireContext(), "Compañía eliminada", Toast.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }
}