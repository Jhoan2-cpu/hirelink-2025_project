package com.example.hirelink_2025.ui.fragments.ads

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hirelink_2025.R
import com.example.hirelink_2025.databinding.FragmentPendingApplicantsBinding
import com.example.hirelink_2025.models.Applicant
import com.example.hirelink_2025.models.ApplicationStatus
import com.example.hirelink_2025.ui.adapters.ApplicantAdapter
import com.example.hirelink_2025.viewmodels.ads.MyAdsApplicantsViewModel

class PendingApplicantsFragment : Fragment() {

    private var _binding: FragmentPendingApplicantsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MyAdsApplicantsViewModel by viewModels()
    private lateinit var applicantAdapter: ApplicantAdapter

    private var jobId: String? = null

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

        // Cargar postulantes
        jobId?.let { viewModel.loadApplicants(it) }
    }

    private fun setupRecyclerView() {
        applicantAdapter = ApplicantAdapter(
            onViewProfileClick = { applicant ->
                navigateToProfile(applicant)
            },
            onAcceptClick = { applicant ->
                viewModel.acceptApplicant(applicant.id)
                showToast("${applicant.name} ha sido contratado")
            },
            onRejectClick = { applicant ->
                viewModel.rejectApplicant(applicant.id)
                showToast("Postulación de ${applicant.name} rechazada")
            }
        )

        binding.pendingApplicantsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = applicantAdapter
        }
    }

    private fun setupObservers() {
        // Observar solo postulantes pendientes
        viewModel.applicants.observe(viewLifecycleOwner) { applicants ->
            val pendingApplicants = applicants.filter { it.status == ApplicationStatus.PENDING }
            applicantAdapter.submitList(pendingApplicants)
            updateEmptyState(pendingApplicants.isEmpty())
        }

        // Observar estado de carga
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.pendingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.emptyPendingApplicantsMessage.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.pendingApplicantsRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun navigateToProfile(applicant: Applicant) {
        try {
            findNavController().navigate(
                R.id.action_myAdsApplicantsFragment_to_applicantProfileFragment,
                Bundle().apply {
                    putString("applicant_id", applicant.id)
                    putString("applicant_name", applicant.name)
                    putString("applicant_email", applicant.email)
                    putString("applicant_phone", applicant.phone)
                    putString("applicant_profession", applicant.profession)
                    putString("applicant_experience", applicant.experience)
                    putStringArrayList("applicant_skills", ArrayList(applicant.skills))
                    putString("application_date", applicant.applicationDate)
                    putString("application_status", applicant.status.name)
                    putString("profile_image", applicant.profileImage)
                    putString("job_id", applicant.jobId)
                    putString("cover_letter", applicant.coverLetter)
                }
            )
        } catch (e: Exception) {
            showToast("Error al navegar al perfil")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_JOB_ID = "job_id"

        @JvmStatic
        fun newInstance(jobId: String) = PendingApplicantsFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_JOB_ID, jobId)
            }
        }
    }
}