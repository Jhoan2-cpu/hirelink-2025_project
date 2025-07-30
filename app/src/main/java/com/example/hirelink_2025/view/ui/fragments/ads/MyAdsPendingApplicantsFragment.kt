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

class MyAdsPendingApplicantsFragment : Fragment() {

    private var _binding: FragmentMyAdsPendingApplicantsBinding? = null
    private val binding get() = _binding!!

    private var applicantsAdapter: ApplicantsAdapter? = null
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
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        binding.pendingApplicantsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        applicantsAdapter?.let { adapter ->
            // Crear un nuevo adapter filtrado para este fragment
            val filteredAdapter = ApplicantsAdapter(
                onItemClicked = { applicant ->
                    // Propagar el click al adapter padre
                    (parentFragment as? MyAdsApplicantsFragment)?.let { parent ->
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