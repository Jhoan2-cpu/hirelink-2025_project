package com.example.hirelink_2025.view.ui.fragments.ads

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hirelink_2025.R
import com.example.hirelink_2025.models.Application
import com.example.hirelink_2025.models.ApplicationStatus
import com.example.hirelink_2025.models.Job
import com.example.hirelink_2025.view.adapter.ApplicationsAdapter
import com.example.hirelink_2025.viewmodels.ApplicationsViewModel
import com.example.hirelink_2025.viewmodels.CompanyViewModel
import com.example.hirelink_2025.viewmodels.ViewModelFactory
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.launch

/**
 * Fragment para mostrar la lista de postulantes de un trabajo
 */
class JobApplicantsFragment : Fragment() {
    
    private lateinit var toolbar: MaterialToolbar
    private lateinit var jobTitle: TextView
    private lateinit var companyName: TextView
    private lateinit var applicantsCount: TextView
    private lateinit var statusChip: Chip
    private lateinit var applicantsRecyclerView: RecyclerView
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var applicationsAdapter: ApplicationsAdapter
    
    // ViewModels
    private val applicationsViewModel: ApplicationsViewModel by viewModels { ViewModelFactory() }
    private val companyViewModel: CompanyViewModel by viewModels { ViewModelFactory() }
    
    private var currentJob: Job? = null
    private var jobId: String? = null
    private var currentJobTitle: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_job_applicants, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        Log.d("JobApplicantsFragment", "Fragment created")
        
        initViews(view)
        setupRecyclerView()
        setupClickListeners()
        observeViewModels()
        
        // Obtener ID del trabajo desde argumentos
        getJobIdAndLoadData()
    }
    
    private fun initViews(view: View) {
        toolbar = view.findViewById(R.id.toolbar)
        jobTitle = view.findViewById(R.id.jobTitle)
        companyName = view.findViewById(R.id.companyName)
        applicantsCount = view.findViewById(R.id.applicantsCount)
        statusChip = view.findViewById(R.id.statusChip)
        applicantsRecyclerView = view.findViewById(R.id.applicantsRecyclerView)
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout)
        progressIndicator = view.findViewById(R.id.progressIndicator)
    }
    
    private fun setupRecyclerView() {
        applicationsAdapter = ApplicationsAdapter(
            onAcceptClick = { application ->
                Log.d("JobApplicantsFragment", "Accept application: ${application.applicationId}")
                showStatusConfirmDialog(application, ApplicationStatus.ACCEPTED)
            },
            onRejectClick = { application ->
                Log.d("JobApplicantsFragment", "Reject application: ${application.applicationId}")
                showStatusConfirmDialog(application, ApplicationStatus.REJECTED)
            },
            onApplicantClick = { application ->
                Log.d("JobApplicantsFragment", "View applicant profile: ${application.applicantId}")
                navigateToApplicantProfile(application.applicantId)
            },
            getUserInfo = { userId ->
                applicationsViewModel.getUserInfo(userId)
            },
            getUserProfile = { userId ->
                applicationsViewModel.getUserProfile(userId)
            }
        )
        
        applicantsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = applicationsAdapter
        }
    }
    
    private fun setupClickListeners() {
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun observeViewModels() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                
                // Observar lista de postulaciones
                launch {
                    applicationsViewModel.applications.collect { applications ->
                        Log.d("JobApplicantsFragment", "Applications updated: ${applications.size}")
                        updateUI(applications)
                        updateApplicantsCount(applications.size)
                    }
                }
                
                // Observar estado de carga
                launch {
                    applicationsViewModel.isLoading.collect { isLoading ->
                        Log.d("JobApplicantsFragment", "Loading state: $isLoading")
                        updateLoadingState(isLoading)
                    }
                }
                
                // Observar errores
                launch {
                    applicationsViewModel.error.collect { error ->
                        error?.let {
                            Log.e("JobApplicantsFragment", "Error: $it")
                            showError(it)
                            applicationsViewModel.clearError()
                        }
                    }
                }
                
                // Observar resultados de operaciones
                launch {
                    applicationsViewModel.operationResult.collect { result ->
                        result?.let {
                            Log.d("JobApplicantsFragment", "Operation result: $it")
                            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                            applicationsViewModel.clearOperationResult()
                        }
                    }
                }
                
                // Observar cache de usuarios para actualizar UI
                launch {
                    applicationsViewModel.usersCache.collect { usersCache ->
                        if (usersCache.isNotEmpty()) {
                            Log.d("JobApplicantsFragment", "Users cache updated: ${usersCache.size} users")
                            applicationsAdapter.notifyDataSetChanged()
                        }
                    }
                }
                
                // Observar cache de perfiles para actualizar UI
                launch {
                    applicationsViewModel.userProfilesCache.collect { profilesCache ->
                        if (profilesCache.isNotEmpty()) {
                            Log.d("JobApplicantsFragment", "Profiles cache updated: ${profilesCache.size} profiles")
                            applicationsAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }
        }
    }
    
    private fun getJobIdAndLoadData() {
        jobId = arguments?.getString("jobId")
        currentJobTitle = arguments?.getString("jobTitle")
        
        if (jobId != null) {
            Log.d("JobApplicantsFragment", "Loading applicants for job: $jobId")
            applicationsViewModel.loadApplicationsForJob(jobId!!)
            loadJobInfo(jobId!!)
        } else {
            Log.e("JobApplicantsFragment", "No job ID provided")
            showError("ID de trabajo no encontrado")
            findNavController().navigateUp()
        }
    }
    
    private fun loadJobInfo(jobId: String) {
        // Mostrar título del trabajo desde argumentos
        jobTitle.text = currentJobTitle ?: "Cargando..."
        
        // Cargar información completa del trabajo desde Firestore
        val firestoreService = com.example.hirelink_2025.network.FirestoreService()
        firestoreService.getJobById(jobId, object : com.example.hirelink_2025.network.Callback<Job?> {
            override fun onSuccess(job: Job?) {
                if (job != null) {
                    currentJob = job
                    jobTitle.text = job.title
                    updateJobStatus(job.status)
                    
                    // Cargar información de la empresa
                    if (job.companyId.isNotEmpty()) {
                        loadCompanyInfo(job.companyId)
                    } else {
                        companyName.text = "Empresa no especificada"
                    }
                } else {
                    Log.w("JobApplicantsFragment", "Job not found")
                    jobTitle.text = currentJobTitle ?: "Trabajo no encontrado"
                    companyName.text = "Empresa no disponible"
                }
            }
            
            override fun onError(exception: Exception) {
                Log.e("JobApplicantsFragment", "Error loading job info", exception)
                jobTitle.text = currentJobTitle ?: "Error al cargar"
                companyName.text = "Error al cargar empresa"
            }
        })
    }
    
    private fun loadCompanyInfo(companyId: String) {
        companyViewModel.getCompanyById(companyId) { company ->
            if (company != null) {
                companyName.text = company.name
            } else {
                companyName.text = "Empresa no encontrada"
            }
        }
    }
    
    private fun updateJobStatus(status: com.example.hirelink_2025.models.JobStatus) {
        val statusText = when (status) {
            com.example.hirelink_2025.models.JobStatus.ACTIVE -> "Activo"
            com.example.hirelink_2025.models.JobStatus.CLOSED -> "Cerrado"
            com.example.hirelink_2025.models.JobStatus.DRAFT -> "Borrador"
            com.example.hirelink_2025.models.JobStatus.PAUSED -> "Pausado"
            else -> status.name
        }
        
        statusChip.text = statusText
        
        // Actualizar color del chip según el estado
        val chipColor = when (status) {
            com.example.hirelink_2025.models.JobStatus.ACTIVE -> R.color.success
            com.example.hirelink_2025.models.JobStatus.CLOSED -> R.color.error
            com.example.hirelink_2025.models.JobStatus.DRAFT -> R.color.warning
            com.example.hirelink_2025.models.JobStatus.PAUSED -> R.color.text_secondary
            else -> R.color.text_secondary
        }
        
        statusChip.setChipBackgroundColorResource(chipColor)
    }
    
    private fun updateUI(applications: List<Application>) {
        if (applications.isEmpty()) {
            applicantsRecyclerView.visibility = View.GONE
            emptyStateLayout.visibility = View.VISIBLE
        } else {
            applicantsRecyclerView.visibility = View.VISIBLE
            emptyStateLayout.visibility = View.GONE
            applicationsAdapter.submitList(applications)
        }
    }
    
    private fun updateApplicantsCount(count: Int) {
        applicantsCount.text = "$count postulantes"
    }
    
    private fun updateLoadingState(isLoading: Boolean) {
        if (isLoading) {
            progressIndicator.visibility = View.VISIBLE
        } else {
            progressIndicator.visibility = View.GONE
        }
    }
    
    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
    
    private fun showStatusConfirmDialog(application: Application, newStatus: ApplicationStatus) {
        val statusText = when (newStatus) {
            ApplicationStatus.ACCEPTED -> "aceptar"
            ApplicationStatus.REJECTED -> "rechazar"
            else -> "cambiar el estado de"
        }
        
        val user = applicationsViewModel.getUserInfo(application.applicantId)
        val applicantName = user?.name ?: "este postulante"
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Confirmar acción")
            .setMessage("¿Estás seguro de que deseas $statusText a $applicantName?")
            .setPositiveButton("Confirmar") { _, _ ->
                updateApplicationStatus(application, newStatus)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun updateApplicationStatus(application: Application, newStatus: ApplicationStatus) {
        applicationsViewModel.updateApplicationStatus(application.applicationId, newStatus) { success ->
            if (success) {
                Log.d("JobApplicantsFragment", "Application status updated successfully")
                // Refrescar la lista para mostrar el cambio inmediatamente
                jobId?.let { id ->
                    applicationsViewModel.loadApplicationsForJob(id)
                }
            } else {
                Log.e("JobApplicantsFragment", "Failed to update application status")
            }
        }
    }
    
    private fun navigateToApplicantProfile(applicantId: String) {
        // Buscar la application correspondiente para pasar el applicationId
        val application = applicationsViewModel.applications.value.find { it.applicantId == applicantId }
        
        val bundle = Bundle().apply {
            putString("userId", applicantId)
            putString("applicantId", applicantId)
            application?.let {
                putString("applicationId", it.applicationId)
                putString("jobId", it.jobId)
            }
        }
        
        try {
            findNavController().navigate(
                R.id.applicantProfileFragment,
                bundle
            )
        } catch (e: Exception) {
            Log.e("JobApplicantsFragment", "Error navigating to applicant profile", e)
            // Fallback: mostrar información básica
            val user = applicationsViewModel.getUserInfo(applicantId)
            val message = if (user != null) {
                "Postulante: ${user.name}\nEmail: ${user.email}"
            } else {
                "Información del postulante no disponible"
            }
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }
    }
}