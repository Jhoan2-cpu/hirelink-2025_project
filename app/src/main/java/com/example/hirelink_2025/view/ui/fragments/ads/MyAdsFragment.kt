package com.example.hirelink_2025.view.ui.fragments.ads

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hirelink_2025.R
import com.example.hirelink_2025.databinding.FragmentMyAdsBinding
import com.example.hirelink_2025.models.Job
import com.example.hirelink_2025.models.JobStatus
import com.example.hirelink_2025.repository.AdsStats
import com.example.hirelink_2025.view.adapter.MyAdAdapter
import com.example.hirelink_2025.viewmodels.ads.MyAdsViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

/**
 * Fragment para mostrar los anuncios laborales del usuario
 * Siguiendo arquitectura MVVM - Solo maneja la UI
 */
class MyAdsFragment : Fragment() {

    private var _binding: FragmentMyAdsBinding? = null
    private val binding get() = _binding!!

    // ViewModel usando by viewModels() delegate
    private val viewModel: MyAdsViewModel by viewModels()

    private lateinit var adapter: MyAdAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyAdsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupObservers()
    }

    /**
     * Configura la UI inicial
     */
    private fun setupUI() {
        setupRecyclerView()
        setupClickListeners()
    }

    /**
     * Configura el RecyclerView y su adapter
     */
    private fun setupRecyclerView() {
        adapter = MyAdAdapter(
            onJobClick = { job -> handleJobClick(job) },
            onEditClick = { job -> handleEditClick(job) },
            onDeleteClick = { job -> handleDeleteClick(job) },
            onApplicantsClick = { job -> handleApplicantsClick(job) }
        )

        binding.adsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@MyAdsFragment.adapter
        }
    }

    /**
     * Configura los listeners de clicks
     */
    private fun setupClickListeners() {
        // FAB para crear nuevo anuncio
        binding.createAdFab.setOnClickListener {
            findNavController().navigate(R.id.action_myAdsFragment_to_myAdsRegisterFragment)
        }

        binding.companyButton.setOnClickListener {
            findNavController().navigate(R.id.action_myAdsFragment_to_companyFragment)
        }

        // Botones de filtro (si existen en el layout)
        binding.apply {
            // Asumiendo que hay botones de filtro en el layout
            // Si no existen, estos se pueden omitir
            /*
            activeAdsButton?.setOnClickListener {
                viewModel.loadActiveAds()
            }

            closedAdsButton?.setOnClickListener {
                viewModel.loadClosedAds()
            }

            draftAdsButton?.setOnClickListener {
                viewModel.loadDraftAds()
            }

            allAdsButton?.setOnClickListener {
                viewModel.loadMyAds()
            }
            */
        }
    }

    /**
     * Configura los observadores del ViewModel
     */
    private fun setupObservers() {
        // Observar lista de anuncios
        viewModel.myAds.observe(viewLifecycleOwner) { ads ->
            adapter.submitList(ads)
            updateEmptyState(ads.isEmpty())
        }

        // Observar estadísticas
        viewModel.adsStats.observe(viewLifecycleOwner) { stats ->
            updateStatsUI(stats)
        }

        // Observar estado de carga
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            updateLoadingState(isLoading)
        }

        // Observar estado vacío
        viewModel.isEmpty.observe(viewLifecycleOwner) { isEmpty ->
            updateEmptyState(isEmpty)
        }

        // Observar mensajes de error
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                showErrorMessage(message)
                viewModel.clearErrorMessage()
            }
        }

        // Observar éxito en eliminación
        viewModel.deleteSuccess.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                showSuccessMessage(message)
                viewModel.clearSuccessMessages()
            }
        }

        // Observar éxito en actualización
        viewModel.updateSuccess.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                showSuccessMessage(message)
                viewModel.clearSuccessMessages()
            }
        }
    }

    /**
     * Maneja el click en un anuncio para ver detalles
     */
    private fun handleJobClick(job: Job) {
        // ✅ USAR NAVIGATION COMPONENT
        val bundle = Bundle().apply {
            putString("job_id", job.id)
            putString("job_title", job.title)
            putString("job_description", job.description)
            putString("job_requirements", job.requirements.joinToString(", "))
            putString("job_posted_date", job.postedDate)
            putString("job_employment_type", job.employmentType)
            putString("job_modality", job.modality)
            putString("job_status", job.status.name)
            putString("job_phone", "123456789") // Placeholder
            putString("job_email", "contact@company.com") // Placeholder
        }

        findNavController().navigate(
            R.id.action_myAdsFragment_to_myAdDetailFragment,
            bundle
        )
    }

    /**
     * Maneja el click para editar un anuncio
     */
    private fun handleEditClick(job: Job) {
        findNavController().navigate(
            R.id.action_myAdsFragment_to_myAdsEditFragment,
            Bundle().apply {
                putString("job_id", job.id)
                putString("job_title", job.title)
            }
        )
    }

    /**
     * Maneja el click para eliminar un anuncio
     */
    private fun handleDeleteClick(job: Job) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar anuncio")
            .setMessage("¿Estás seguro que deseas eliminar '${job.title}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.deleteAd(job)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * Maneja el click para ver postulantes
     */
    private fun handleApplicantsClick(job: Job) {
        findNavController().navigate(
            R.id.action_myAdsFragment_to_myAdsApplicantsFragment,
            Bundle().apply {
                putString("job_id", job.id)
                putString("job_title", job.title)
            }
        )
    }

    /**
     * Actualiza la UI con las estadísticas
     */
    private fun updateStatsUI(stats: AdsStats) {
        binding.apply {
            // Asumiendo que hay views para mostrar estadísticas
            // Si no existen en el layout, se pueden omitir
            /*
            totalAdsText?.text = stats.totalAds.toString()
            activeAdsText?.text = stats.activeAds.toString()
            closedAdsText?.text = stats.closedAds.toString()
            draftAdsText?.text = stats.draftAds.toString()
            totalViewsText?.text = stats.totalViews.toString()
            totalApplicantsText?.text = stats.totalApplicants.toString()
            */
        }
    }

    /**
     * Actualiza el estado de carga
     */
    private fun updateLoadingState(isLoading: Boolean) {
        binding.apply {
            if (isLoading) {
                // Mostrar loading - puede ser un ProgressBar
                // progressBar?.visibility = View.VISIBLE
                // adsRecyclerView.visibility = View.GONE
            } else {
                // Ocultar loading
                // progressBar?.visibility = View.GONE
                // adsRecyclerView.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Actualiza el estado vacío
     */
    private fun updateEmptyState(isEmpty: Boolean) {
        binding.apply {
            if (isEmpty) {
                // Mostrar estado vacío
                // emptyStateLayout?.visibility = View.VISIBLE
                // adsRecyclerView.visibility = View.GONE
            } else {
                // Ocultar estado vacío
                // emptyStateLayout?.visibility = View.GONE
                // adsRecyclerView.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Muestra un mensaje de error
     */
    private fun showErrorMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction("Reintentar") {
                viewModel.refreshData()
            }
            .show()
    }

    /**
     * Muestra un mensaje de éxito
     */
    private fun showSuccessMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(resources.getColor(R.color.success, null))
            .show()
    }

    /**
     * Método público para actualizar datos desde el exterior
     */
    fun refreshData() {
        viewModel.refreshData()
    }

    /**
     * Método público para filtrar por estado
     */
    fun filterByStatus(status: JobStatus) {
        viewModel.filterAdsByStatus(status)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}