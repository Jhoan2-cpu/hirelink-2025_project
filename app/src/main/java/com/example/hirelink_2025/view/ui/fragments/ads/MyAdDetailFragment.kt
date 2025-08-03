package com.example.hirelink_2025.view.ui.fragments.ads

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.hirelink_2025.R
import androidx.navigation.fragment.findNavController
import com.example.hirelink_2025.databinding.FragmentMyAdDetailBinding
import com.example.hirelink_2025.models.Company
import com.example.hirelink_2025.viewmodels.CompanyViewModel
import com.example.hirelink_2025.viewmodels.ViewModelFactory

/**
 * Fragment específico para mostrar detalles de anuncios desde MyAdsFragment
 */
class MyAdDetailFragment : Fragment() {

    private var _binding: FragmentMyAdDetailBinding? = null
    private val binding get() = _binding!!
    
    // ViewModel para obtener información de la compañía
    private val companyViewModel: CompanyViewModel by viewModels { ViewModelFactory() }
    
    private var currentCompany: Company? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyAdDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        Log.d("MyAdDetailFragment", "Fragment created")
        
        setupUI()
        setupBackButton()
        loadJobData()
        loadCompanyData()
    }

    private fun setupUI() {
        
        // Configurar botón de cerrar
        binding.closeButton.setOnClickListener {
            navigateBack()
        }

        // Configurar botón para ver postulantes
        binding.viewApplicantsButton?.setOnClickListener {
            navigateToApplicants()
        }
    }

    private fun loadJobData() {
        arguments?.let { args ->
            Log.d("MyAdDetailFragment", "Loading job data from arguments")
            
            binding.apply {
                // Información básica del trabajo
                jobTitleText.text = args.getString("job_title", "Título no disponible")
                
                // Descripción del trabajo
                val aboutJob = args.getString("job_about_job", "")
                aboutJobText.text = if (aboutJob.isNotEmpty()) aboutJob else "Descripción no disponible"
                
                // Requisitos/habilidades
                val requirements = args.getString("job_requirements", "")
                skillsText.text = if (requirements.isNotEmpty()) requirements else "Requisitos no especificados"
                
                // Información laboral
                val employmentType = args.getString("job_employment_type", "")
                val modality = args.getString("job_modality", "")
                val vacancies = args.getString("job_vacancies", "1")
                val salary = args.getString("job_salary", "")
                val offerSalary = args.getString("job_offer_salary", "")
                val deadline = args.getString("job_deadline", "")
                
                employmentTypeText.text = if (employmentType.isNotEmpty()) employmentType else "Tipo no especificado"
                modalityText.text = if (modality.isNotEmpty()) modality else "Modalidad no especificada"
                vacanciesText.text = "$vacancies vacante(s)"
                
                // Mostrar información adicional en los campos de texto disponibles
                // Como no hay salaryText ni deadlineText en el layout, usar otros campos
                // Agregar esta información al final de employmentTypeText o skillsText
                val additionalInfo = buildString {
                    if (salary.isNotEmpty() || offerSalary.isNotEmpty()) {
                        append("\n\nSalario: ")
                        append(if (salary.isNotEmpty()) salary else offerSalary)
                    }
                    if (deadline.isNotEmpty()) {
                        append("\n\nFecha límite: $deadline")
                    }
                }
                
                if (additionalInfo.isNotEmpty()) {
                    skillsText.text = skillsText.text.toString() + additionalInfo
                }
                
                // Fecha de publicación
                val postedDate = args.getString("job_posted_date", "")
                dateText.text = if (postedDate.isNotEmpty()) "Publicado: $postedDate" else "Fecha no disponible"
                
                // Estado del trabajo
                val status = args.getString("job_status", "ACTIVE")
                jobStatusText.text = formatJobStatus(status)
            }
        } ?: run {
            Log.w("MyAdDetailFragment", "No arguments found")
            Toast.makeText(requireContext(), "Error al cargar datos del anuncio", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun loadCompanyData() {
        val companyId = arguments?.getString("company_id") ?: return
        
        Log.d("MyAdDetailFragment", "Loading company data for ID: $companyId")
        
        companyViewModel.getCompanyById(companyId) { company ->
            if (company != null) {
                Log.d("MyAdDetailFragment", "Company loaded: ${company.name}")
                currentCompany = company
                displayCompanyData(company)
            } else {
                Log.w("MyAdDetailFragment", "Company not found for ID: $companyId")
                displayDefaultCompanyData()
            }
        }
    }
    
    private fun displayCompanyData(company: Company) {
        binding.apply {
            // Ubicación de la compañía
            val location = buildString {
                if (company.address.isNotEmpty()) {
                    append(company.address)
                    if (company.city.isNotEmpty() || company.country.isNotEmpty()) {
                        append(", ")
                    }
                }
                if (company.city.isNotEmpty()) {
                    append(company.city)
                    if (company.country.isNotEmpty()) {
                        append(", ")
                    }
                }
                if (company.country.isNotEmpty()) {
                    append(company.country)
                }
            }
            
            // Información completa de la compañía en aboutCompanyText
            val companyInfo = buildString {
                append("Empresa: ${company.name}\n\n")
                if (company.description.isNotEmpty()) {
                    append(company.description)
                    append("\n\n")
                }
                append("Ubicación: ")
                append(if (location.isNotEmpty()) location else "Ubicación no especificada")
            }
            aboutCompanyText.text = companyInfo
            
            // Información de contacto
            phoneText.text = if (company.phone.isNotEmpty()) company.phone else "Teléfono no disponible"
            emailText.text = if (company.email.isNotEmpty()) company.email else "Email no disponible"
            websiteText.text = if (company.website.isNotEmpty()) company.website else "Sitio web no disponible"
            
            // Configurar click listeners para contacto
            setupContactClickListeners(company)
        }
    }
    
    private fun displayDefaultCompanyData() {
        binding.apply {
            aboutCompanyText.text = "Empresa: No encontrada\n\nInformación de la empresa no disponible\n\nUbicación: Ubicación no disponible"
            phoneText.text = "Teléfono no disponible"
            emailText.text = "Email no disponible"
            websiteText.text = "Sitio web no disponible"
        }
    }
    
    private fun setupContactClickListeners(company: Company) {
        binding.apply {
            // Click en teléfono
            phoneText.setOnClickListener {
                if (company.phone.isNotEmpty()) {
                    try {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:${company.phone}")
                        }
                        startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "No se pudo abrir el marcador", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            
            // Click en email
            emailText.setOnClickListener {
                if (company.email.isNotEmpty()) {
                    try {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:${company.email}")
                        }
                        startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "No se pudo abrir el cliente de email", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            
            // Click en sitio web
            websiteText.setOnClickListener {
                if (company.website.isNotEmpty()) {
                    try {
                        val url = if (!company.website.startsWith("http://") && !company.website.startsWith("https://")) {
                            "https://${company.website}"
                        } else {
                            company.website
                        }
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "No se pudo abrir el sitio web", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    
    private fun formatJobStatus(status: String): String {
        return when (status.uppercase()) {
            "ACTIVE" -> "Activo"
            "CLOSED" -> "Cerrado"
            "DRAFT" -> "Borrador"
            else -> status.lowercase().replaceFirstChar { it.uppercase() }
        }
    }


    private fun navigateToApplicants() {
        arguments?.let { args ->
            val bundle = Bundle().apply {
                putString("jobId", args.getString("job_id"))
                putString("jobTitle", args.getString("job_title"))
            }
            
            findNavController().navigate(
                R.id.action_myAdDetailFragment_to_myAdsApplicantsFragment,
                bundle
            )
        }
    }

    private fun setupBackButton() {
        // Manejar el botón atrás del sistema
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateBack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun navigateBack() {
        findNavController().navigateUp()
    }


    companion object {
        fun newInstance(
            jobId: String,
            titulo: String,
            descripcion: String,
            habilidades: String,
            fecha: String,
            tipoEmpleo: String,
            modalidad: String,
            estado: String,
            telefono: String,
            email: String
        ): MyAdDetailFragment {
            val fragment = MyAdDetailFragment()
            val args = Bundle().apply {
                putString("job_id", jobId)
                putString("job_title", titulo)
                putString("job_description", descripcion)
                putString("job_requirements", habilidades)
                putString("job_posted_date", fecha)
                putString("job_employment_type", tipoEmpleo)
                putString("job_modality", modalidad)
                putString("job_status", estado)
                putString("job_phone", telefono)
                putString("job_email", email)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}