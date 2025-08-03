package com.example.hirelink_2025.view.ui.fragments.ads

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hirelink_2025.R
import com.example.hirelink_2025.databinding.FragmentMyAdsBinding
import com.example.hirelink_2025.models.Job
import com.example.hirelink_2025.models.JobStatus
import com.example.hirelink_2025.view.adapter.MyAdAdapter
import com.example.hirelink_2025.viewmodels.ads.MyAdsViewModel
import com.example.hirelink_2025.viewmodels.ApplicationsViewModel
import com.example.hirelink_2025.viewmodels.CompanyViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

/**
 * Fragment para mostrar los anuncios laborales del usuario
 * Adaptado para trabajar con los modelos actualizados:
 * - Job: modelo simplificado con companyId
 * - Company: información obtenida por separado vía CompanyViewModel
 * - Application: gestión de postulaciones vía ApplicationsViewModel
 * 
 * Siguiendo arquitectura MVVM - Solo maneja la UI
 */
class MyAdsFragment : Fragment() {

    private var _binding: FragmentMyAdsBinding? = null
    private val binding get() = _binding!!

    // ViewModels usando by viewModels() delegate
    private val viewModel: MyAdsViewModel by viewModels { 
        com.example.hirelink_2025.viewmodels.ViewModelFactory() 
    }
    
    private val applicationsViewModel: ApplicationsViewModel by viewModels { 
        com.example.hirelink_2025.viewmodels.ViewModelFactory() 
    }
    
    private val companyViewModel: CompanyViewModel by viewModels { 
        com.example.hirelink_2025.viewmodels.ViewModelFactory() 
    }

    private lateinit var adapter: MyAdAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyAdsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupObservers()
        
        // Cargar datos iniciales
        refreshAllData()
    }

    override fun onResume() {
        super.onResume()
        // Recargar datos cada vez que el fragment se hace visible
        // Esto captura nuevos anuncios registrados
        refreshAllData()
    }

    /**
     * Configura la UI inicial
     */
    private fun setupUI() {
        setupRecyclerView()
        setupClickListeners()
    }

    /**
     * Configura el RecyclerView y su adapter
     */
    private fun setupRecyclerView() {
        adapter = MyAdAdapter(
            onJobClick = { job -> handleJobClick(job) },
            onEditClick = { job -> handleEditClick(job) },
            onDeleteClick = { job -> handleDeleteClick(job) },
            onApplicantsClick = { job -> navigateToApplicants(job) },
            getCompanyInfo = { companyId -> 
                getCompanyInfoById(companyId)
            },
            getApplicationsCount = { jobId ->
                // Return cached count or 0 as default
                getApplicationsCountForJob(jobId)
            }
        )

        binding.adsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@MyAdsFragment.adapter
        }
    }
    
    /**
     * Obtiene el número de postulaciones para un trabajo
     * Usa el ViewModel para obtener el conteo cacheado
     */
    private fun getApplicationsCountForJob(jobId: String): Int {
        return viewModel.getApplicationsCount(jobId)
    }
    
    // Cache de compañías para acceso rápido
    private val companiesCache = mutableMapOf<String, com.example.hirelink_2025.models.Company>()

    /**
     * Obtiene información de una compañía por ID
     * Usa cache local y carga desde ViewModel si es necesario
     */
    private fun getCompanyInfoById(companyId: String): com.example.hirelink_2025.models.Company? {
        // Primero buscar en cache
        companiesCache[companyId]?.let { return it }
        
        // Si no está en cache, cargar desde CompanyViewModel
        companyViewModel.getCompanyById(companyId) { company ->
            company?.let {
                companiesCache[companyId] = it
                // Actualizar adapter después de cargar la compañía
                if (::adapter.isInitialized) {
                    adapter.notifyDataSetChanged()
                }
            }
        }
        
        return companiesCache[companyId]
    }

    /**
     * Configura los listeners de clicks
     */
    private fun setupClickListeners() {
        // FAB para crear nuevo anuncio
        binding.createAdFab.setOnClickListener {
            findNavController().navigate(R.id.action_myAdsFragment_to_myAdsRegisterFragment)
        }

        binding.companyButton.setOnClickListener {
            findNavController().navigate(R.id.action_myAdsFragment_to_companyFragment)
        }

        // Botones de filtro (si existen en el layout)
        binding.apply {
            // Asumiendo que hay botones de filtro en el layout
            // Si no existen, estos se pueden omitir
            /*
            activeAdsButton?.setOnClickListener {
                viewModel.loadActiveAds()
            }

            closedAdsButton?.setOnClickListener {
                viewModel.loadClosedAds()
            }

            draftAdsButton?.setOnClickListener {
                viewModel.loadDraftAds()
            }

            allAdsButton?.setOnClickListener {
                viewModel.loadMyAds()
            }
            */
        }
    }

    /**
     * Configura los observadores del ViewModel
     */
    private fun setupObservers() {
        // Observar lista de anuncios
        viewModel.myAds.observe(viewLifecycleOwner) { ads ->
            adapter.submitList(ads)
            updateEmptyState(ads.isEmpty())
        }

        // Observar estadísticas
        viewModel.adsStats.observe(viewLifecycleOwner) { stats ->
            updateStatsUI(stats)
        }

        // Observar estado de carga
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            updateLoadingState(isLoading)
        }

        // Observar estado vacío
        viewModel.isEmpty.observe(viewLifecycleOwner) { isEmpty ->
            updateEmptyState(isEmpty)
        }

        // Observar mensajes de error
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                showErrorMessage(message)
                viewModel.clearErrorMessage()
            }
        }

        // Observar conteo de postulaciones
        viewModel.applicationsCount.observe(viewLifecycleOwner) { counts ->
            // Actualizar adapter cuando cambien los conteos
            if (::adapter.isInitialized) {
                adapter.notifyDataSetChanged()
            }
        }

        // Observar éxito en eliminación
        viewModel.deleteSuccess.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                showSuccessMessage(message)
                viewModel.clearSuccessMessages()
            }
        }

        // Observar éxito en actualización
        viewModel.updateSuccess.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                showSuccessMessage(message)
                viewModel.clearSuccessMessages()
            }
        }
        
        // TODO: Agregar observadores cuando los ViewModels tengan estas propiedades
        // Por ahora comentado hasta que se implementen los métodos correspondientes
        /*
        // Observar cambios en las compañías para actualizar el adapter
        companyViewModel.companies.observe(viewLifecycleOwner) { companies ->
            // Cuando las compañías se actualizan, refrescar el adapter
            // para mostrar información de compañía actualizada
            if (::adapter.isInitialized && companies.isNotEmpty()) {
                adapter.notifyDataSetChanged()
            }
        }
        
        // Observar cambios en los conteos de aplicaciones
        applicationsViewModel.applicationsCount.observe(viewLifecycleOwner) { counts ->
            // Cuando los conteos de aplicaciones cambian, actualizar adapter
            if (::adapter.isInitialized && counts.isNotEmpty()) {
                adapter.notifyDataSetChanged()
            }
        }
        */
    }

    /**
     * Maneja el click en un anuncio para ver detalles
     * Adaptado al modelo Job simplificado
     */
    private fun handleJobClick(job: Job) {
        // Obtener información de la compañía para el detalle
        val company = getCompanyInfoById(job.companyId)
        
        val bundle = Bundle().apply {
            // Datos del Job (modelo simplificado)
            putString("job_id", job.id)
            putString("job_title", job.title)
            putString("job_about_job", job.aboutJob)
            putString("job_requirements", job.requirements.joinToString(", "))
            putString("job_posted_date", job.postedDate)
            putString("job_employment_type", job.employmentType)
            putString("job_modality", job.modality)
            putString("job_salary", job.salary)
            putString("job_offer_salary", job.offerSalary)
            putString("job_deadline", job.deadline)
            putString("job_vacancies", job.vacancies.toString())
            putString("job_status", job.status.name)
            putString("company_id", job.companyId)
            
            // Datos de la compañía (si están disponibles)
            company?.let { comp ->
                putString("company_name", comp.name)
                putString("company_phone", comp.phone)
                putString("company_email", comp.email)
                putString("company_website", comp.website)
                putString("company_address", comp.address)
                putString("company_city", comp.city)
            }
        }

        findNavController().navigate(
            R.id.action_myAdsFragment_to_myAdDetailFragment,
            bundle
        )
    }

    /**
     * Maneja el click para editar un anuncio
     * Pasa todos los datos necesarios del modelo Job simplificado
     */
    private fun handleEditClick(job: Job) {
        val bundle = Bundle().apply {
            // Todos los campos del modelo Job simplificado
            putString("job_id", job.id)
            putString("job_title", job.title)
            putString("job_modality", job.modality)
            putString("job_salary", job.salary)
            putString("job_requirements", job.requirements.joinToString("\n"))
            putString("job_employment_type", job.employmentType)
            putString("job_about_job", job.aboutJob)
            putString("job_deadline", job.deadline)
            putString("job_offer_salary", job.offerSalary)
            putInt("job_vacancies", job.vacancies)
            putString("company_id", job.companyId)
            putString("job_status", job.status.name)
        }
        
        findNavController().navigate(
            R.id.action_myAdsFragment_to_myAdsEditFragment,
            bundle
        )
    }

    /**
     * Maneja el click para eliminar un anuncio
     */
    private fun handleDeleteClick(job: Job) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar anuncio")
            .setMessage("¿Estás seguro que deseas eliminar '${job.title}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.deleteAd(job)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * Maneja el click para ver postulantes
     */
    private fun handleApplicantsClick(job: Job) {
        val bundle = Bundle().apply {
            putString("jobId", job.id)
            putString("jobTitle", job.title)
        }
        
        findNavController().navigate(
            R.id.action_myAdsFragment_to_jobApplicantsFragment,
            bundle
        )
    }

    /**
     * Actualiza la UI con las estadísticas
     */
    private fun updateStatsUI(stats: Map<String, Int>) {
        binding.apply {
            activeAdsCount.text = stats["active"]?.toString() ?: "0"
            totalApplicantsCount.text = stats["totalApplications"]?.toString() ?: "0"
        }
    }

    /**
     * Actualiza el estado de carga
     */
    private fun updateLoadingState(isLoading: Boolean) {
        binding.apply {
            if (isLoading) {
                progressBar.visibility = View.VISIBLE
                adsRecyclerView.visibility = View.GONE
                emptyStateLayout.visibility = View.GONE
            } else {
                progressBar.visibility = View.GONE
                adsRecyclerView.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Actualiza el estado vacío
     */
    private fun updateEmptyState(isEmpty: Boolean) {
        binding.apply {
            if (isEmpty && progressBar.visibility != View.VISIBLE) {
                emptyStateLayout.visibility = View.VISIBLE
                adsRecyclerView.visibility = View.GONE
            } else {
                emptyStateLayout.visibility = View.GONE
                if (progressBar.visibility != View.VISIBLE) {
                    adsRecyclerView.visibility = View.VISIBLE
                }
            }
        }
    }

    /**
     * Muestra un mensaje de error
     */
    private fun showErrorMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction("Reintentar") {
                viewModel.refreshData()
            }
            .show()
    }

    /**
     * Muestra un mensaje de éxito
     */
    private fun showSuccessMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(resources.getColor(R.color.success, null))
            .show()
    }

    /**
     * Método para refrescar todos los datos relacionados
     * Incluye trabajos, compañías y conteos de aplicaciones
     */
    private fun refreshAllData() {
        // Refrescar trabajos del usuario
        viewModel.refreshData()
        
        // TODO: Refrescar información de compañías cuando el ViewModel tenga el método
        // companyViewModel.refreshCompanies()
        
        // TODO: Precargar conteos de aplicaciones cuando esté implementado
        // preloadApplicationsCounts()
    }
    
    /**
     * Precarga los conteos de aplicaciones para todos los trabajos
     */
    private fun preloadApplicationsCounts() {
        // TODO: Implementar precarga cuando ApplicationsViewModel tenga el método
        // viewModel.myAds.value?.forEach { job ->
        //     applicationsViewModel.loadApplicationsCountForJob(job.id)
        // }
    }

    /**
     * Método público para actualizar datos desde el exterior
     */
    fun refreshData() {
        refreshAllData()
    }

    /**
     * Método público para filtrar por estado
     */
    fun filterByStatus(status: JobStatus) {
        // TODO: Implementar método filterAdsByStatus en MyAdsViewModel
        // viewModel.filterAdsByStatus(status)
    }
    
    /**
     * Maneja la navegación a la gestión de postulantes con validación
     */
    private fun navigateToApplicants(job: Job) {
        // TODO: Verificar que la compañía existe antes de navegar
        // Por ahora navegar directamente hasta que se implemente cache
        handleApplicantsClick(job)
    }
    
    /**
     * Obtiene información completa del trabajo con datos de compañía
     */
    private fun getJobWithCompanyInfo(job: Job): Pair<Job, com.example.hirelink_2025.models.Company?> {
        // TODO: Implementar cache de compañías para acceso directo
        val company = getCompanyInfoById(job.companyId)
        return Pair(job, company)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}