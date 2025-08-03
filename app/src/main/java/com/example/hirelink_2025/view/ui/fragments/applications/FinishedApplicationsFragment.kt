package com.example.hirelink_2025.view.ui.fragments.applications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hirelink_2025.R
import com.example.hirelink_2025.databinding.FragmentActiveApplicationsBinding
import com.example.hirelink_2025.models.Application
import com.example.hirelink_2025.view.adapter.ApplicationAdapter
import com.example.hirelink_2025.viewmodels.ApplicationsViewModel
import com.example.hirelink_2025.viewmodels.ViewModelFactory
import kotlinx.coroutines.launch

class FinishedApplicationsFragment : Fragment() {

    private var _binding: FragmentActiveApplicationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ApplicationAdapter
    private val applicationsViewModel: ApplicationsViewModel by viewModels { ViewModelFactory() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActiveApplicationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        loadUserApplications()
    }

    private fun setupRecyclerView() {
        adapter = ApplicationAdapter(
            apps = emptyList(),
            onItemClicked = { application ->
                navigateToApplicationDetail(application)
            },
            getJobInfo = { jobId -> applicationsViewModel.getJobInfo(jobId) },
            getCompanyInfo = { companyId -> applicationsViewModel.getCompanyInfo(companyId) }
        )

        binding.applicationsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.applicationsRecyclerView.adapter = adapter
        
        // Configurar pull-to-refresh
        binding.swipeRefreshLayout.setOnRefreshListener {
            applicationsViewModel.refreshUserApplications()
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            applicationsViewModel.applications.collect { _ ->
                // Filtrar solo las postulaciones finalizadas
                val finishedApplications = applicationsViewModel.getFinishedApplications()
                updateAdapter(finishedApplications)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            applicationsViewModel.isLoading.collect { isLoading ->
                // Controlar SwipeRefreshLayout
                binding.swipeRefreshLayout.isRefreshing = isLoading
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            applicationsViewModel.error.collect { error ->
                error?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                    applicationsViewModel.clearError()
                }
            }
        }
    }

    private fun loadUserApplications() {
        // ✅ CAMBIAR - usar Firebase Auth directamente
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid

        if (userId != null) {
            applicationsViewModel.loadApplicationsForUser(userId)
        } else {
            Toast.makeText(requireContext(), "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateAdapter(applications: List<Application>) {
        adapter = ApplicationAdapter(
            apps = applications,
            onItemClicked = { application ->
                navigateToApplicationDetail(application)
            },
            getJobInfo = { jobId -> applicationsViewModel.getJobInfo(jobId) },
            getCompanyInfo = { companyId -> applicationsViewModel.getCompanyInfo(companyId) }
        )
        binding.applicationsRecyclerView.adapter = adapter
    }

    private fun navigateToApplicationDetail(application: Application) {
        val job = applicationsViewModel.getJobInfo(application.jobId)
        val company = job?.let { applicationsViewModel.getCompanyInfo(it.companyId) }
        
        val bundle = Bundle().apply {
            putString("application_id", application.applicationId)
            putString("job_id", application.jobId)
            putString("job_title", job?.title ?: "Trabajo no encontrado")
            putString("company_name", company?.name ?: "Compañía no encontrada")
            putLong("applied_at", application.appliedAt)
            putString("status", application.status.name)
            putString("job_description", job?.aboutJob ?: "")
            putString("job_requirements", job?.requirements?.joinToString(", ") ?: "")
            putString("employment_type", job?.employmentType ?: "")
            putString("modality", job?.modality ?: "")
        }

        findNavController().navigate(
            R.id.action_myApplicationsFragment_to_applicationDetailFragment,
            bundle
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}