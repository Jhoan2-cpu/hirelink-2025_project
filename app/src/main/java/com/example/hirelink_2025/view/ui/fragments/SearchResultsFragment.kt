package com.example.hirelink_2025.view.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hirelink_2025.R
import com.example.hirelink_2025.databinding.FragmentSearchResultsBinding
import com.example.hirelink_2025.models.Job
import com.example.hirelink_2025.models.JobFilters
import com.example.hirelink_2025.models.SalaryRange
import com.example.hirelink_2025.view.adapter.SearchResultsAdapter
import com.example.hirelink_2025.view.ui.utils.showErrorSnackbar
import com.example.hirelink_2025.view.ui.utils.showSuccessSnackbar
import com.example.hirelink_2025.viewmodels.SearchViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import android.widget.ArrayAdapter
import android.widget.ListView

/**
 * Fragment para mostrar resultados de búsqueda de empleos
 * Siguiendo arquitectura MVVM
 */
class SearchResultsFragment : Fragment() {

    private var _binding: FragmentSearchResultsBinding? = null
    private val binding get() = _binding!!

    // ViewModel compartido con SearchFragment a través de la Activity
    private val viewModel: SearchViewModel by activityViewModels()

    // Adapter para resultados
    private lateinit var searchResultsAdapter: SearchResultsAdapter
    
    // Estado de expansión de filtros
    private var isFiltersExpanded = false

    // Navigation args para recibir parámetros de búsqueda (se generará automáticamente)
    // private val args: SearchResultsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchResultsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        setupRecyclerView()
        setupObservers()
        
        // Aplicar los filtros de búsqueda recibidos
        applySearchFilters()
    }

    /**
     * Configura la UI inicial
     */
    private fun setupUI() {
        // Configurar toolbar
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Configurar botones
        binding.refreshButton.setOnClickListener {
            viewModel.refreshResults()
        }

        binding.newSearchButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.retryButton.setOnClickListener {
            viewModel.refreshResults()
        }

        // Configurar filtros
        setupFilterChips()
        setupFilterExpansion()
    }

    /**
     * Configura el RecyclerView con el adapter
     */
    private fun setupRecyclerView() {
        searchResultsAdapter = SearchResultsAdapter(
            onJobClick = { job -> navigateToJobDetail(job) },
            getCompanyForJob = { job -> viewModel.getCompanyForJob(job) }
        )

        binding.resultsRecyclerView.apply {
            adapter = searchResultsAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    /**
     * Configura los observadores del ViewModel
     */
    private fun setupObservers() {
        // Observar resultados de búsqueda
        viewModel.searchResults.observe(viewLifecycleOwner) { results ->
            android.util.Log.d("SearchResultsFragment", "Received search results: ${results.size} jobs")
            results.forEachIndexed { index, job ->
                android.util.Log.d("SearchResultsFragment", "Job $index: ${job.title} (ID: ${job.id})")
            }
            searchResultsAdapter.submitList(results)
            updateContentVisibility()
        }

        // Observar estado de carga
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            // ✅ CRUCIAL: También actualizar la visibilidad del contenido cuando cambie isLoading
            updateContentVisibility()
        }

        // Observar si hay resultados vacíos
        viewModel.isEmpty.observe(viewLifecycleOwner) { isEmpty ->
            updateContentVisibility()
        }

        // Observar si ya se realizó búsqueda
        viewModel.hasSearched.observe(viewLifecycleOwner) { hasSearched ->
            updateContentVisibility()
        }

        // Observar mensajes de error
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                binding.errorMessageText.text = errorMessage
                updateContentVisibility()
            }
        }

        // Observar resumen de búsqueda
        viewModel.searchCount.observe(viewLifecycleOwner) { count ->
            updateSearchSummary(count)
        }

        // Observar cache de compañías para refrescar adapter
        viewModel.companiesCache.observe(viewLifecycleOwner) { companies ->
            if (companies.isNotEmpty()) {
                searchResultsAdapter.notifyDataSetChanged()
            }
        }

        // Observar filtros activos
        viewModel.activeFilters.observe(viewLifecycleOwner) { filters ->
            updateFilterChips(filters)
        }
    }

    /**
     * Aplica los filtros de búsqueda recibidos por navigation
     */
    private fun applySearchFilters() {
        // Obtener argumentos del Bundle directamente hasta que se generen las clases de navegación
        val jobType = arguments?.getString("jobType") ?: ""
        val location = arguments?.getString("location") ?: ""

        android.util.Log.d("SearchResultsFragment", "Received search params - jobType: '$jobType', location: '$location'")

        // Verificar si ya hay resultados de búsqueda disponibles
        val currentResults = viewModel.searchResults.value
        val hasResults = currentResults != null && currentResults.isNotEmpty()
        
        if (hasResults) {
            android.util.Log.d("SearchResultsFragment", "Using existing search results: ${currentResults?.size} jobs")
            // Ya hay resultados, no ejecutar nueva búsqueda
            return
        }

        // Solo ejecutar búsqueda si no hay resultados previos
        android.util.Log.d("SearchResultsFragment", "No existing results, executing new search")
        
        // Aplicar filtros al ViewModel
        if (jobType.isNotBlank()) {
            viewModel.updateJobType(jobType)
        }
        if (location.isNotBlank()) {
            viewModel.updateLocation(location)
        }

        // Ejecutar búsqueda solo si es necesario
        viewModel.searchJobs()
    }

    /**
     * Actualiza la visibilidad del contenido según el estado
     */
    private fun updateContentVisibility() {
        val hasSearched = viewModel.hasSearched.value ?: false
        val isEmpty = viewModel.isEmpty.value ?: false
        val hasError = viewModel.errorMessage.value?.isNotEmpty() ?: false
        val isLoading = viewModel.isLoading.value ?: false
        val resultsCount = viewModel.searchCount.value ?: 0

        android.util.Log.d("SearchResultsFragment", "updateContentVisibility - hasSearched: $hasSearched, isEmpty: $isEmpty, hasError: $hasError, isLoading: $isLoading, resultsCount: $resultsCount")

        when {
            isLoading -> {
                android.util.Log.d("SearchResultsFragment", "Showing loading state")
                binding.resultsRecyclerView.visibility = View.GONE
                binding.emptyStateLayout.visibility = View.GONE
                binding.errorStateLayout.visibility = View.GONE
            }
            hasError -> {
                android.util.Log.d("SearchResultsFragment", "Showing error state: ${viewModel.errorMessage.value}")
                binding.resultsRecyclerView.visibility = View.GONE
                binding.emptyStateLayout.visibility = View.GONE
                binding.errorStateLayout.visibility = View.VISIBLE
            }
            hasSearched && isEmpty -> {
                android.util.Log.d("SearchResultsFragment", "Showing empty state")
                binding.resultsRecyclerView.visibility = View.GONE
                binding.emptyStateLayout.visibility = View.VISIBLE
                binding.errorStateLayout.visibility = View.GONE
            }
            hasSearched && !isEmpty -> {
                android.util.Log.d("SearchResultsFragment", "Showing results: $resultsCount jobs")
                binding.resultsRecyclerView.visibility = View.VISIBLE
                binding.emptyStateLayout.visibility = View.GONE
                binding.errorStateLayout.visibility = View.GONE
            }
            else -> {
                android.util.Log.d("SearchResultsFragment", "Showing default state (nothing)")
                binding.resultsRecyclerView.visibility = View.GONE
                binding.emptyStateLayout.visibility = View.GONE
                binding.errorStateLayout.visibility = View.GONE
            }
        }
    }

    /**
     * Actualiza el resumen de búsqueda
     */
    private fun updateSearchSummary(resultCount: Int) {
        binding.searchQueryText.text = viewModel.getSearchSummary()
        binding.searchResultsCount.text = when (resultCount) {
            0 -> "Sin resultados"
            1 -> "1 empleo encontrado"
            else -> "$resultCount empleos encontrados"
        }
    }

    /**
     * Navega al detalle del empleo
     */
    private fun navigateToJobDetail(job: Job) {
        android.util.Log.d("SearchResultsFragment", "Navigating to job detail: ${job.title}")
        
        try {
            // Crear Bundle con argumentos del job
            val bundle = Bundle().apply {
                putString("jobId", job.id)
            }
            
            // Navegar al JobDetailFragment
            findNavController().navigate(R.id.action_searchResultsFragment_to_jobDetailFragment, bundle)
            
        } catch (e: Exception) {
            android.util.Log.e("SearchResultsFragment", "Error navigating to job detail: ${e.message}")
            
            // Fallback: navegación directa
            try {
                findNavController().navigate(R.id.jobDetailFragment, Bundle().apply {
                    putString("jobId", job.id)
                })
            } catch (e2: Exception) {
                android.util.Log.e("SearchResultsFragment", "Fallback navigation failed: ${e2.message}")
                showErrorSnackbar("Error navegando a detalles del empleo")
            }
        }
    }

    /**
     * Toggle bookmark para un empleo
     */
    private fun toggleBookmark(job: Job) {
        android.util.Log.d("SearchResultsFragment", "Toggle bookmark for job: ${job.title}")
        
        // TODO: Implementar lógica de bookmarks
        // viewModel.toggleBookmark(job)
        
        showSuccessSnackbar("Marcador actualizado para: ${job.title}")
    }

    /**
     * Configura los chips de filtros
     */
    private fun setupFilterChips() {
        // Configurar limpiar filtros
        binding.clearFiltersButton.setOnClickListener {
            viewModel.clearFilters()
        }

        // Configurar filtro de modalidad
        binding.modalityFilterChip.setOnClickListener {
            showModalityFilterDialog()
        }


        // Configurar filtro de rango salarial
        binding.salaryRangeFilterChip.setOnClickListener {
            showSalaryRangeFilterDialog()
        }
    }

    /**
     * Actualiza la apariencia de los chips según los filtros activos
     */
    private fun updateFilterChips(filters: JobFilters) {
        // Actualizar chip de modalidad
        binding.modalityFilterChip.isChecked = filters.modality.isNotEmpty()
        binding.modalityFilterChip.text = if (filters.modality.isNotEmpty()) {
            "Modalidad: ${filters.modality}"
        } else {
            "Modalidad"
        }


        // Actualizar chip de rango salarial
        binding.salaryRangeFilterChip.isChecked = filters.salaryRange != SalaryRange.ALL
        binding.salaryRangeFilterChip.text = if (filters.salaryRange != SalaryRange.ALL) {
            "Salario: ${filters.salaryRange.displayName}"
        } else {
            "Rango salarial"
        }

        // Mostrar/ocultar botón de limpiar filtros
        binding.clearFiltersButton.visibility = if (filters.hasActiveFilters()) {
            View.VISIBLE
        } else {
            View.GONE
        }

        // Actualizar contador de filtros activos
        updateActiveFiltersCount(filters)
    }

    /**
     * Muestra dialog de filtro de modalidad
     */
    private fun showModalityFilterDialog() {
        val modalityOptions = listOf("", "Presencial", "Remoto", "Híbrido")
        val modalityNames = listOf("Todos", "Presencial", "Remoto", "Híbrido")

        val currentFilters = viewModel.getCurrentFilters()
        val selectedIndex = modalityOptions.indexOf(currentFilters.modality)

        val dialog = BottomSheetDialog(requireContext())
        val listView = ListView(requireContext()).apply {
            adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_single_choice, modalityNames)
            choiceMode = ListView.CHOICE_MODE_SINGLE
            setItemChecked(selectedIndex, true)
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedModality = modalityOptions[position]
            val newFilters = currentFilters.copy(modality = selectedModality)
            viewModel.updateFilters(newFilters)
            dialog.dismiss()
        }

        dialog.setContentView(listView)
        dialog.show()
    }


    /**
     * Muestra dialog de filtro de rango salarial
     */
    private fun showSalaryRangeFilterDialog() {
        val salaryRanges = SalaryRange.values()
        val salaryNames = salaryRanges.map { it.displayName }

        val currentFilters = viewModel.getCurrentFilters()
        val selectedIndex = salaryRanges.indexOf(currentFilters.salaryRange)

        val dialog = BottomSheetDialog(requireContext())
        val listView = ListView(requireContext()).apply {
            adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_single_choice, salaryNames)
            choiceMode = ListView.CHOICE_MODE_SINGLE
            setItemChecked(selectedIndex, true)
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedSalaryRange = salaryRanges[position]
            val newFilters = currentFilters.copy(salaryRange = selectedSalaryRange)
            viewModel.updateFilters(newFilters)
            dialog.dismiss()
        }

        dialog.setContentView(listView)
        dialog.show()
    }

    /**
     * Configura la expansión/colapso de filtros
     */
    private fun setupFilterExpansion() {
        binding.filterHeader.setOnClickListener {
            toggleFiltersExpansion()
        }
    }

    /**
     * Alterna entre expandir y colapsar los filtros
     */
    private fun toggleFiltersExpansion() {
        isFiltersExpanded = !isFiltersExpanded
        
        binding.filterContent.visibility = if (isFiltersExpanded) {
            View.VISIBLE
        } else {
            View.GONE
        }

        // Rotar el ícono de flecha
        val rotation = if (isFiltersExpanded) 180f else 0f
        binding.filterExpandIcon.animate()
            .rotation(rotation)
            .setDuration(200)
            .start()
    }

    /**
     * Actualiza el contador de filtros activos
     */
    private fun updateActiveFiltersCount(filters: JobFilters) {
        var activeCount = 0
        
        if (filters.modality.isNotEmpty()) activeCount++
        if (filters.salaryRange != SalaryRange.ALL) activeCount++

        if (activeCount > 0) {
            binding.activeFiltersCount.visibility = View.VISIBLE
            binding.activeFiltersCount.text = "$activeCount activo${if (activeCount > 1) "s" else ""}"
        } else {
            binding.activeFiltersCount.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        /**
         * Factory method para crear instancia con parámetros
         */
        fun newInstance(jobType: String, location: String): SearchResultsFragment {
            return SearchResultsFragment().apply {
                arguments = Bundle().apply {
                    putString("job_type", jobType)
                    putString("location", location)
                }
            }
        }
    }
}