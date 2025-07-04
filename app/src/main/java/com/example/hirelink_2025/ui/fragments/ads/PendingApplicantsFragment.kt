package com.example.hirelink_2025.ui.fragments.ads

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hirelink_2025.databinding.FragmentPendingApplicantsBinding
import com.example.hirelink_2025.models.Applicant
import com.example.hirelink_2025.ui.adapters.ApplicantAdapter
import com.example.hirelink_2025.viewmodels.ads.MyAdsApplicantsViewModel
import kotlinx.coroutines.launch

class PendingApplicantsFragment : Fragment() {

    private var _binding: FragmentPendingApplicantsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MyAdsApplicantsViewModel by activityViewModels()
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

        setupRecyclerView()
        setupObservers()
    }

    private fun setupRecyclerView() {
        applicantAdapter = ApplicantAdapter(
            onViewProfileClick = { applicant -> navigateToProfile(applicant) },
            onAcceptClick = { applicant -> acceptApplicant(applicant) },
            onRejectClick = { applicant -> rejectApplicant(applicant) }
        )

        binding.pendingApplicantsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = applicantAdapter
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.pendingApplicants.collect { applicants ->
                updateApplicantsList(applicants)
                updateEmptyState(applicants.isEmpty())
            }
        }
    }

    private fun updateApplicantsList(applicants: List<Applicant>) {
        applicantAdapter.submitList(applicants)
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.pendingApplicantsRecyclerView.visibility = View.GONE
        } else {
            binding.pendingApplicantsRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun acceptApplicant(applicant: Applicant) {
        // Solo pasar el applicantId, no el jobId
        viewModel.acceptApplicant(applicant.id)
    }

    private fun rejectApplicant(applicant: Applicant) {
        // Solo pasar el applicantId, no el jobId
        viewModel.rejectApplicant(applicant.id)
    }

    private fun navigateToProfile(applicant: Applicant) {
        var parentFrag = parentFragment
        while (parentFrag != null && parentFrag !is MyAdsApplicantsFragment) {
            parentFrag = parentFrag.parentFragment
        }
        (parentFrag as? MyAdsApplicantsFragment)?.navigateToApplicantProfile(applicant.id)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}