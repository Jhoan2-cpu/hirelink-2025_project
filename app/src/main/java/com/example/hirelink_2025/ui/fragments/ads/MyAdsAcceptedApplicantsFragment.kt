package com.example.hirelink_2025.ui.fragments.ads

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hirelink_2025.databinding.FragmentMyAdsAcceptedApplicantsBinding
import com.example.hirelink_2025.models.ApplicationStatus
import com.example.hirelink_2025.ui.adapters.ApplicantsAdapter

class MyAdsAcceptedApplicantsFragment : Fragment() {

    private var _binding: FragmentMyAdsAcceptedApplicantsBinding? = null
    private val binding get() = _binding!!

    private var applicantsAdapter: ApplicantsAdapter? = null
    private var jobId: String? = null

    companion object {
        const val ARG_JOB_ID = "job_id"

        fun newInstance(jobId: String): MyAdsAcceptedApplicantsFragment {
            return MyAdsAcceptedApplicantsFragment().apply {
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
        _binding = FragmentMyAdsAcceptedApplicantsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        binding.acceptedApplicantsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        applicantsAdapter?.let { adapter ->
            // Crear un nuevo adapter filtrado para este fragment
            val filteredAdapter = ApplicantsAdapter(
                onItemClicked = { applicant ->
                    // Propagar el click al adapter padre
                    (parentFragment as? MyAdsApplicantsFragment)?.let { parent ->
                        // Llamar directamente al método de navegación del fragment padre
                        parent.navigateToApplicantProfile(applicant)
                    }
                },
                onAcceptClicked = { applicant ->
                    (parentFragment as? MyAdsApplicantsFragment)?.handleAcceptApplicant(applicant)
                },
                onRejectClicked = { applicant ->
                    (parentFragment as? MyAdsApplicantsFragment)?.handleRejectApplicant(applicant)
                }
            )

            binding.acceptedApplicantsRecyclerView.adapter = filteredAdapter

            // Filtrar solo aplicantes aceptados
            val acceptedApplicants = adapter.currentList.filter {
                it.status == ApplicationStatus.ACCEPTED
            }
            filteredAdapter.submitList(acceptedApplicants)
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
     * Actualizar lista filtrada
     */
    fun updateFilteredList() {
        setupRecyclerView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}