package com.example.hirelink_2025.view.ui.fragments.ads

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hirelink_2025.databinding.FragmentPendingApplicantsBinding
import com.example.hirelink_2025.models.Applicant
import com.example.hirelink_2025.view.adapter.ApplicantAdapter
import com.example.hirelink_2025.view.ui.fragments.ads.MyAdsApplicantsFragment
import com.example.hirelink_2025.viewmodels.ViewModelFactory
import com.example.hirelink_2025.viewmodels.ads.MyAdsApplicantsViewModel
import kotlinx.coroutines.launch

/**
 * Fragment que muestra aplicantes pendientes
 * MVVM: Solo maneja UI, delega lógica al ViewModel
 */
class PendingApplicantsFragment : Fragment() {

    private var _binding: FragmentPendingApplicantsBinding? = null
    private val binding get() = _binding!!

    // ViewModel compartido con el Fragment padre (MVVM)
    private val viewModel: MyAdsApplicantsViewModel by activityViewModels {
        ViewModelFactory()
    }

    private lateinit var applicantAdapter: ApplicantAdapter
    private var jobId: String? = null

    companion object {
        private const val ARG_JOB_ID = "job_id"

        fun newInstance(jobId: String): PendingApplicantsFragment {
            return PendingApplicantsFragment().apply {
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
        _binding = FragmentPendingApplicantsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar ViewModel con jobId
        jobId?.let { id ->
            viewModel.initializeWithJob(id)
        }

        setupRecyclerView()
        setupObservers()
    }

    /**
     * Configurar RecyclerView (UI)
     */
    private fun setupRecyclerView() {
        applicantAdapter = ApplicantAdapter(
            onViewProfileClick = ::onViewProfileClick,
            onAcceptClick = ::onAcceptClick, // Mostrar botón aceptar para pendientes
            onRejectClick = ::onRejectClick  // Mostrar botón rechazar para pendientes
        )

        binding.pendingApplicantsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = applicantAdapter
        }
    }

    /**
     * Configurar observadores (MVVM)
     */
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Observar aplicantes pendientes
            viewModel.pendingApplicants.collect { applicants ->
                updateApplicantsList(applicants)
                updateEmptyState(applicants.isEmpty())
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // Observar estado de la UI
            viewModel.uiState.collect { uiState ->
                updateLoadingState(uiState.isLoading)
                uiState.error?.let { error ->
                    showError(error)
                    viewModel.clearError()
                }
            }
        }
    }

    /**
     * Actualizar lista (UI)
     */
    private fun updateApplicantsList(applicants: List<Applicant>) {
        applicantAdapter.submitList(applicants)
    }

    /**
     * Actualizar estado vacío (UI)
     */
    private fun updateEmptyState(isEmpty: Boolean) {
        binding.pendingApplicantsRecyclerView.visibility =
            if (isEmpty) View.GONE else View.VISIBLE

        // Mostrar mensaje de estado vacío si no hay aplicantes pendientes
        binding.emptyStateText.apply {
            visibility = if (isEmpty) View.VISIBLE else View.GONE
            text = "No hay aplicantes pendientes"
        }
    }

    /**
     * Actualizar estado de carga (UI)
     */
    private fun updateLoadingState(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    /**
     * Mostrar errores (UI)
     */
    private fun showError(error: String) {
        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
    }

    // Acciones delegadas al ViewModel (MVVM)
    private fun onAcceptClick(applicant: Applicant) {
        viewModel.acceptApplicant(applicant.id)
    }

    private fun onRejectClick(applicant: Applicant) {
        viewModel.rejectApplicant(applicant.id)
    }

    private fun onViewProfileClick(applicant: Applicant) {
        // Navegación delegada al Fragment padre
        var parentFrag = parentFragment
        while (parentFrag != null && parentFrag !is MyAdsApplicantsFragment) {
            parentFrag = parentFrag.parentFragment
        }
        (parentFrag as? MyAdsApplicantsFragment)?.navigateToApplicantProfile(applicant)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}