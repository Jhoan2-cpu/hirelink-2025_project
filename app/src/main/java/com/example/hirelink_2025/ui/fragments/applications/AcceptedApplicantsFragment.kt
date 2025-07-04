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
import com.example.hirelink_2025.databinding.FragmentAcceptedApplicantsBinding
import com.example.hirelink_2025.models.Applicant
import com.example.hirelink_2025.models.ApplicationStatus
import com.example.hirelink_2025.ui.adapters.ApplicantAdapter
import com.example.hirelink_2025.viewmodels.ads.MyAdsApplicantsViewModel

class AcceptedApplicantsFragment : Fragment() {

    private var _binding: FragmentAcceptedApplicantsBinding? = null
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
        _binding = FragmentAcceptedApplicantsBinding.inflate(inflater, container, false)
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
                // Ya está aceptado, no hacer nada o cambiar funcionalidad
                showToast("${applicant.name} ya está contratado")
            },
            onRejectClick = { applicant ->
                // Despedir al postulante
                viewModel.rejectApplicant(applicant.id)
                showToast("${applicant.name} ha sido despedido")
            }
        )

        binding.acceptedApplicantsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = applicantAdapter
        }
    }

    private fun setupObservers() {
        // Observar solo postulantes aceptados
        viewModel.applicants.observe(viewLifecycleOwner) { applicants ->
            val acceptedApplicants = applicants.filter { it.status == ApplicationStatus.ACCEPTED }
            applicantAdapter.submitList(acceptedApplicants)
            updateEmptyState(acceptedApplicants.isEmpty())
        }

        // Observar estado de carga
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.acceptedProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.emptyAcceptedApplicantsMessage.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.acceptedApplicantsRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
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
        fun newInstance(jobId: String) = AcceptedApplicantsFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_JOB_ID, jobId)
            }
        }
    }
}