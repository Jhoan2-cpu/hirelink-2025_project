package com.example.hirelink_2025.ui.fragments.ads

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.hirelink_2025.R
import com.example.hirelink_2025.databinding.FragmentMyAdsApplicantsBinding
import com.example.hirelink_2025.ui.adapters.ApplicantsPagerAdapter
import com.example.hirelink_2025.viewmodels.ads.MyAdsApplicantsViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

/**
 * Fragment principal para mostrar los aplicantes de un trabajo
 * Sigue el patrón MVVM: solo maneja la lógica de presentación
 */
class MyAdsApplicantsFragment : Fragment() {

    private var _binding: FragmentMyAdsApplicantsBinding? = null
    private val binding get() = _binding!!

    // ViewModel: delegación lazy para obtener la instancia
    private val viewModel: MyAdsApplicantsViewModel by viewModels()

    // Adapter para el ViewPager
    private lateinit var pagerAdapter: ApplicantsPagerAdapter

    // Parámetros del fragment
    private var jobId: String? = null
    private var jobTitle: String? = null

    companion object {
        const val ARG_JOB_ID = "job_id"
        const val ARG_JOB_TITLE = "job_title"

        fun newInstance(jobId: String, jobTitle: String): MyAdsApplicantsFragment {
            return MyAdsApplicantsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_JOB_ID, jobId)
                    putString(ARG_JOB_TITLE, jobTitle)
                }
            }
        }
    }

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

        try {
            val jobId = arguments?.getString("job_id")
            val jobTitle = arguments?.getString("job_title", "Postulantes")

            if (jobId.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "ID de trabajo no válido", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
                return
            }

            // Configurar UI
            setupToolbar(jobTitle.toString())
            setupTabLayout()
            setupObservers()

            // Cargar datos
            viewModel.loadApplicants(jobId)

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }

    /**
     * Configurar toolbar con título y navegación
     */
    private fun setupToolbar(jobTitle: String) {
        // Si tienes un toolbar en el layout, configurarlo aquí
        binding.applicantsTitle?.text = "Postulantes - $jobTitle"

        // Si hay botón de back, configurarlo
        // binding.backButton?.setOnClickListener {
        //     findNavController().popBackStack()
        // }
    }

    /**
     * Configurar TabLayout con ViewPager
     */
    private fun setupTabLayout() {
        jobId?.let { id ->
            pagerAdapter = ApplicantsPagerAdapter(requireActivity(), id)
            binding.applicantsViewPager.adapter = pagerAdapter

            // Configurar TabLayout con ViewPager
            TabLayoutMediator(binding.applicantsTabLayout, binding.applicantsViewPager) { tab, position ->
                tab.text = when (position) {
                    0 -> "Aceptados"
                    1 -> "Pendientes"
                    else -> "Tab $position"
                }
            }.attach()
        }
    }

    /**
     * Configurar la interfaz de usuario
     */
    private fun setupUI() {
        // Configurar título
        jobTitle?.let { title ->
            binding.applicantsTitle?.text = "Postulantes - $title"
        }

        // Configurar ViewPager y Tabs
        setupViewPager()
    }

    /**
     * Configurar ViewPager con los tabs
     */
    private fun setupViewPager() {
        jobId?.let { id ->
            pagerAdapter = ApplicantsPagerAdapter(requireActivity(), id)
            binding.applicantsViewPager.adapter = pagerAdapter

            // Configurar TabLayout con ViewPager
            TabLayoutMediator(binding.applicantsTabLayout, binding.applicantsViewPager) { tab, position ->
                tab.text = when (position) {
                    0 -> "Aceptados"
                    1 -> "Pendientes"
                    else -> "Tab $position"
                }
            }.attach()
        }
    }

    /**
     * Configurar observers para los estados del ViewModel
     */
    private fun setupObservers() {
        // Observar estado general de la UI
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateLoadingState(state.isLoading)
                state.error?.let { error ->
                    showErrorMessage(error)
                }
            }
        }

        // Observar mensajes de error
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorMessage.collect { message ->
                message?.let {
                    showErrorMessage(it)
                    viewModel.clearErrorMessage()
                }
            }
        }

        // Observar mensajes de éxito
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.successMessage.collect { message ->
                message?.let {
                    showSuccessMessage(it)
                    viewModel.clearSuccessMessage()
                }
            }
        }

        // Observar contadores para actualizar badges en tabs si es necesario
        observeApplicantCounts()
    }

    /**
     * Observar contadores de aplicantes
     */
    private fun observeApplicantCounts() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.pendingApplicants.collect { pendingList ->
                updateTabBadge(1, pendingList.size) // Tab de pendientes
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.acceptedApplicants.collect { acceptedList ->
                updateTabBadge(0, acceptedList.size) // Tab de aceptados
            }
        }
    }

    /**
     * Actualizar badges en los tabs
     */
    private fun updateTabBadge(tabIndex: Int, count: Int) {
        val tabLayout = binding.applicantsTabLayout
        val tab = tabLayout.getTabAt(tabIndex)

        tab?.let {
            val tabText = when (tabIndex) {
                0 -> "Aceptados"
                1 -> "Pendientes"
                else -> "Tab $tabIndex"
            }
            it.text = if (count > 0) "$tabText ($count)" else tabText
        }
    }

    /**
     * Actualizar estado de carga
     */
    private fun updateLoadingState(isLoading: Boolean) {
        binding.applicantsProgressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.applicantsViewPager.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    /**
     * Mostrar mensaje de error
     */
    private fun showErrorMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction("Reintentar") {
                jobId?.let { viewModel.loadApplicants(it) }
            }
            .show()
    }

    /**
     * Mostrar mensaje de éxito
     */
    private fun showSuccessMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(resources.getColor(R.color.success, null))
            .show()
    }

    /**
     * Navegación a perfil de aplicante
     */
    fun navigateToApplicantProfile(applicantId: String) {
        viewModel.getApplicantById(applicantId) { applicant ->
            applicant?.let {
                // Crear bundle con datos del aplicante
                val args = Bundle().apply {
                    putString("applicant_id", it.id)
                    putString("applicant_name", it.name)
                    putString("applicant_email", it.email)
                    putString("applicant_phone", it.phone)
                    putString("applicant_profession", it.profession)
                    putString("applicant_experience", it.experience)
                    putStringArrayList("applicant_skills", ArrayList(it.skills))
                    putString("application_date", it.applicationDate)
                    putString("application_status", it.status.name)
                    putString("profile_image", it.profileImage)
                    putString("job_id", it.jobId)
                    putString("cover_letter", it.coverLetter)
                }

                findNavController().navigate(
                    R.id.action_myAdsApplicantsFragment_to_applicantProfileFragment,
                    args
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}