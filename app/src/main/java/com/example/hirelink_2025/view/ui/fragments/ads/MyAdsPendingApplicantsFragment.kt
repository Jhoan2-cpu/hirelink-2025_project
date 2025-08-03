package com.example.hirelink_2025.view.ui.fragments.ads

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hirelink_2025.databinding.FragmentMyAdsPendingApplicantsBinding
import com.example.hirelink_2025.models.ApplicationStatus
import com.example.hirelink_2025.view.adapter.ApplicantsAdapter
import com.example.hirelink_2025.view.adapter.ApplicationsAdapter

class MyAdsPendingApplicantsFragment : Fragment() {

    private var _binding: FragmentMyAdsPendingApplicantsBinding? = null
    private val binding get() = _binding!!

    private var applicantsAdapter: ApplicantsAdapter? = null
    private var applicationsAdapter: ApplicationsAdapter? = null
    private var jobId: String? = null

    companion object {
        const val ARG_JOB_ID = "job_id"

        fun newInstance(jobId: String): MyAdsPendingApplicantsFragment {
            return MyAdsPendingApplicantsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_JOB_ID, jobId)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            jobId = it.getString(ARG_JOB_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyAdsPendingApplicantsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Priorizar el nuevo adapter de aplicaciones si está disponible
        applicationsAdapter?.let { 
            setupApplicationsRecyclerView() 
        } ?: run {
            // Fallback al adapter legacy si no hay ApplicationsAdapter
            applicantsAdapter?.let { setupRecyclerView() }
        }
    }

    private fun setupRecyclerView() {
        binding.pendingApplicantsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        applicantsAdapter?.let { adapter ->
            // Crear un nuevo adapter filtrado para este fragment
            val filteredAdapter = ApplicantsAdapter(
                onItemClicked = { applicant ->
                    // Legacy adapter - log para debugging
                    android.util.Log.d("PendingApplicants", "Legacy applicant clicked: ${applicant.name}")
                },
                onAcceptClicked = { applicant ->
                    // Legacy adapter - log para debugging
                    android.util.Log.d("PendingApplicants", "Legacy accept clicked: ${applicant.name}")
                },
                onRejectClicked = { applicant ->
                    // Legacy adapter - log para debugging
                    android.util.Log.d("PendingApplicants", "Legacy reject clicked: ${applicant.name}")
                }
            )

            binding.pendingApplicantsRecyclerView.adapter = filteredAdapter

            // Filtrar solo aplicantes pendientes
            val pendingApplicants = adapter.currentList.filter {
                it.status == ApplicationStatus.PENDING
            }
            filteredAdapter.submitList(pendingApplicants)
        }
    }

    /**
     * Método llamado desde el fragment PADRE para pasar el adapter
     */
    fun setApplicantsAdapter(adapter: ApplicantsAdapter) {
        this.applicantsAdapter = adapter
        if (_binding != null) {
            setupRecyclerView()
        }
    }

    /**
     * Método para recibir el adapter de aplicaciones actualizado
     */
    fun setApplicationsAdapter(adapter: ApplicationsAdapter) {
        this.applicationsAdapter = adapter
        if (_binding != null) {
            setupApplicationsRecyclerView()
        }
    }

    /**
     * Configurar RecyclerView para ApplicationsAdapter
     */
    private fun setupApplicationsRecyclerView() {
        applicationsAdapter?.let { adapter ->
            binding.pendingApplicantsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            
            // Crear adapter filtrado para solo aplicaciones pendientes
            val filteredAdapter = ApplicationsAdapter(
                onAcceptClick = { application ->
                    (parentFragment as? MyAdsApplicantsFragment)?.handleAcceptApplication(application)
                },
                onRejectClick = { application ->
                    (parentFragment as? MyAdsApplicantsFragment)?.handleRejectApplication(application)
                },
                onApplicantClick = { application ->
                    (parentFragment as? MyAdsApplicantsFragment)?.navigateToApplicantProfile(application)
                },
                getUserInfo = { userId ->
                    // Obtener info del usuario desde el fragment padre
                    (parentFragment as? MyAdsApplicantsFragment)?.viewModel?.getUserById(userId)
                }
            )

            binding.pendingApplicantsRecyclerView.adapter = filteredAdapter

            // Filtrar solo aplicaciones pendientes
            val pendingApplications = adapter.currentList.filter {
                it.status == ApplicationStatus.PENDING
            }
            filteredAdapter.submitList(pendingApplications)
        }
    }

    /**
     * Actualizar lista filtrada
     */
    fun updateFilteredList() {
        applicantsAdapter?.let { setupRecyclerView() }
        applicationsAdapter?.let { setupApplicationsRecyclerView() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}