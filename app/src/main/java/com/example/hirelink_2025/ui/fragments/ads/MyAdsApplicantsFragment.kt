package com.example.hirelink_2025.ui.fragments.ads

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.hirelink_2025.R
import com.example.hirelink_2025.databinding.FragmentMyAdsApplicantsBinding
import com.example.hirelink_2025.models.Applicant
import com.example.hirelink_2025.ui.adapters.ApplicantsPagerAdapter
import com.example.hirelink_2025.viewmodels.ads.MyAdsApplicantsViewModel
import com.google.android.material.tabs.TabLayoutMediator

class MyAdsApplicantsFragment : Fragment() {

    private var _binding: FragmentMyAdsApplicantsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MyAdsApplicantsViewModel by viewModels()
    private lateinit var pagerAdapter: ApplicantsPagerAdapter

    // Parámetros del fragment
    private var jobId: String? = null
    private var jobTitle: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            jobId = it.getString(ARG_JOB_ID)
            jobTitle = it.getString(ARG_JOB_TITLE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyAdsApplicantsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupViewPager()
        setupObservers()

        // Cargar postulantes
        jobId?.let { viewModel.loadApplicants(it) }
    }

    private fun setupUI() {
        // Configurar título si se pasó el título del trabajo
        jobTitle?.let { title ->
            binding.applicantsTitle.text = "Postulantes - $title"
        }
    }

    private fun setupViewPager() {
        jobId?.let { id ->
            pagerAdapter = ApplicantsPagerAdapter(requireActivity(), id)
            binding.applicantsViewPager.adapter = pagerAdapter

            // Conectar TabLayout con ViewPager2
            TabLayoutMediator(binding.applicantsTabLayout, binding.applicantsViewPager) { tab, position ->
                tab.text = when (position) {
                    0 -> "Aceptados"
                    1 -> "Pendientes"
                    else -> ""
                }
            }.attach()
        }
    }

    private fun setupObservers() {
        // Observar estado de carga
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.applicantsProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observar errores
        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error.isNotEmpty()) {
                showToast(error)
            }
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
        private const val ARG_JOB_TITLE = "job_title"

        @JvmStatic
        fun newInstance(jobId: String, jobTitle: String? = null) =
            MyAdsApplicantsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_JOB_ID, jobId)
                    putString(ARG_JOB_TITLE, jobTitle)
                }
            }
    }
}