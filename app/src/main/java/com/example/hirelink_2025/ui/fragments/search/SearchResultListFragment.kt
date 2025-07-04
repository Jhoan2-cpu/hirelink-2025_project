package com.example.hirelink_2025.ui.fragments.search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hirelink_2025.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import android.widget.TextView
import android.widget.ProgressBar
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.example.hirelink_2025.models.JobResult
import com.example.hirelink_2025.ui.adapters.JobResultsAdapter
import com.google.android.material.card.MaterialCardView


class SearchResultListFragment : Fragment() {

    // Referencias a las vistas
    private lateinit var backButton: MaterialButton
    private lateinit var titleText: TextView
    private lateinit var filterMenuButton: MaterialButton
    private lateinit var resultsRecyclerView: RecyclerView
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var resultsCountText: TextView
    private lateinit var filterMenuCard: MaterialCardView

    // Estado del menú de filtros
    private var isFilterMenuVisible = false

    // Adapter para el RecyclerView
    private lateinit var jobResultsAdapter: JobResultsAdapter

    // Variables para almacenar los datos de búsqueda
    private var jobType: String? = null
    private var location: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search_result_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar vistas
        initViews(view)

        // Recibir argumentos de navegación
        getSearchArguments()

        // Configurar UI
        setupUI()

        // Configurar RecyclerView
        setupRecyclerView()

        // Configurar botones
        setupButtons()

        // Configurar manejo del botón atrás
        setupBackPressedCallback()

        // Realizar búsqueda
        performSearch()
    }

    private fun setupBackPressedCallback() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isFilterMenuVisible) {
                    hideFilterMenu()
                } else {
                    findNavController().popBackStack()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun initViews(view: View) {
        backButton = view.findViewById(R.id.backButton)
        titleText = view.findViewById(R.id.titleText)
        filterMenuButton = view.findViewById(R.id.filterMenuButton)
        resultsRecyclerView = view.findViewById(R.id.resultsRecyclerView)
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout)
        progressBar = view.findViewById(R.id.progressBar)
        resultsCountText = view.findViewById(R.id.resultsCountText)
        filterMenuCard = view.findViewById(R.id.filterMenuCard)
    }

    private fun getSearchArguments() {
        arguments?.let { bundle ->
            jobType = bundle.getString("job_type")
            location = bundle.getString("location")
        }
    }

    private fun setupUI() {
        // Actualizar título con los criterios de búsqueda
        titleText.text = buildSearchTitle()

        // Configurar touch listener para cerrar menú
        setupTouchListeners()
    }

    private fun setupTouchListeners() {
        // Cerrar menú al tocar el RecyclerView
        resultsRecyclerView.setOnTouchListener { _, _ ->
            if (isFilterMenuVisible) {
                hideFilterMenu()
            }
            false
        }

        // Cerrar menú al tocar fuera del área del menú
        view?.setOnTouchListener { _, _ ->
            if (isFilterMenuVisible) {
                hideFilterMenu()
            }
            false
        }
    }

    private fun buildSearchTitle(): String {
        return when {
            !jobType.isNullOrEmpty() && !location.isNullOrEmpty() ->
                "$jobType - $location"
            !jobType.isNullOrEmpty() ->
                "$jobType - Todas las ubicaciones"
            !location.isNullOrEmpty() ->
                "Todos los trabajos - $location"
            else -> "Cargo, área o empresa - Ubicación"
        }
    }

    private fun setupRecyclerView() {
        jobResultsAdapter = JobResultsAdapter(
            onItemClick = { job ->
                navigateToJobDetail(job)
            },
            onBookmarkClick = { job ->
                handleBookmarkClick(job)
            },
            onApplyClick = { job ->
                handleApplyClick(job)
            }
        )

        resultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = jobResultsAdapter
        }
    }

    private fun setupButtons() {
        // Botón back
        backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        // Botón menú de filtros
        filterMenuButton.setOnClickListener {
            toggleFilterMenu()
        }

        // Configurar botones del menú de filtros
        setupFilterMenuButtons()
    }

    private fun setupFilterMenuButtons() {
        val filterMenuButtons = listOf(
            R.id.filterStatusButton,
            R.id.filterDateButton,
            R.id.filterExperienceButton,
            R.id.filterPositionButton,
            R.id.filterEmploymentTypeButton,
            R.id.filterModalityButton,
            R.id.filterLocationButton
        )

        filterMenuButtons.forEach { buttonId ->
            filterMenuCard.findViewById<MaterialButton>(buttonId)?.setOnClickListener { button ->
                val filterName = (button as MaterialButton).text.toString()
                handleFilterSelection(filterName)
            }
        }

        // Botón "Mostrar resultados"
        filterMenuCard.findViewById<MaterialButton>(R.id.showResultsButton)?.setOnClickListener {
            applyFiltersAndShowResults()
        }
    }

    private fun toggleFilterMenu() {
        isFilterMenuVisible = !isFilterMenuVisible

        if (isFilterMenuVisible) {
            showFilterMenu()
        } else {
            hideFilterMenu()
        }

        // Actualizar icono del botón (opcional)
        updateFilterButtonIcon()
    }

    private fun updateFilterButtonIcon() {
        // Opcional: cambiar el ícono cuando el menú está abierto
        val iconRes = if (isFilterMenuVisible) {
            R.drawable.ic_close  // Crear este ícono si lo necesitas
        } else {
            R.drawable.ic_filter_list
        }
        // filterMenuButton.setIconResource(iconRes)
    }

    private fun showFilterMenu() {
        filterMenuCard.visibility = View.VISIBLE
        filterMenuCard.alpha = 0f
        filterMenuCard.animate()
            .alpha(1f)
            .setDuration(200)
            .start()
    }

    private fun hideFilterMenu() {
        filterMenuCard.animate()
            .alpha(0f)
            .setDuration(200)
            .withEndAction {
                filterMenuCard.visibility = View.GONE
            }
            .start()
    }

    private fun handleFilterSelection(filterName: String) {
        Toast.makeText(requireContext(), "Filtro seleccionado: $filterName", Toast.LENGTH_SHORT).show()
        // Aquí implementarías la lógica específica para cada filtro
    }

    private fun applyFiltersAndShowResults() {
        hideFilterMenu()

        // Mostrar loading mientras se aplican los filtros
        showLoading(true)

        // Simular aplicación de filtros
        view?.postDelayed({
            // Aquí aplicarías los filtros reales
            val filteredJobs = generateSampleJobs(jobType, location)

            showLoading(false)
            showResults(filteredJobs)

            Toast.makeText(requireContext(), "Filtros aplicados correctamente", Toast.LENGTH_SHORT).show()
        }, 800)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Limpiar recursos si es necesario
    }

    private fun performSearch() {
        // Mostrar loading
        showLoading(true)

        // Simular búsqueda de trabajos
        searchJobs(jobType, location)
    }

    private fun searchJobs(jobType: String?, location: String?) {
        // Simular delay de red
        view?.postDelayed({
            // Generar datos de ejemplo
            val jobs = generateSampleJobs(jobType, location)

            // Ocultar loading
            showLoading(false)

            // Mostrar resultados
            showResults(jobs)
        }, 1500) // Simular 1.5 segundos de carga
    }

    private fun generateSampleJobs(jobType: String?, location: String?): List<JobResult> {
        val jobs = mutableListOf<JobResult>()

        // Generar trabajos de ejemplo basados en la búsqueda
        val jobTypeToShow = jobType ?: "Desarrollador"
        val locationToShow = location ?: "Lima"

        val companies = listOf(
            "TechCorp S.A.C.",
            "Innovate Solutions",
            "Digital Peru",
            "Software House",
            "StartupTech",
            "CodeFactory",
            "DevPeru",
            "TechSolutions"
        )

        val jobVariants = listOf("Senior", "Junior", "Mid-level", "Lead", "Principal")
        val workTypes = listOf("Remoto", "Híbrido", "Presencial")

        for (i in 1..8) {
            val variant = jobVariants[i % jobVariants.size]
            val company = companies[i % companies.size]
            val workType = workTypes[i % workTypes.size]
            val baseSalary = 2000 + (i * 800)
            val maxSalary = baseSalary + 1500

            jobs.add(
                JobResult(
                    id = i,
                    title = "$jobTypeToShow $variant",
                    company = company,
                    location = locationToShow,
                    salary = "S/. $baseSalary - $maxSalary",
                    type = workType,
                    publishedDate = "Hace ${i} día${if (i > 1) "s" else ""}",
                    description = "Buscamos un $jobTypeToShow $variant para unirse a nuestro equipo en $company. Trabajo $workType en $locationToShow.",
                    isBookmarked = i % 3 == 0 // Algunos trabajos marcados como favoritos
                )
            )
        }

        return jobs
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        resultsRecyclerView.visibility = if (show) View.GONE else View.VISIBLE
        emptyStateLayout.visibility = View.GONE
    }

    private fun showResults(jobs: List<JobResult>) {
        if (jobs.isEmpty()) {
            showEmptyState()
        } else {
            jobResultsAdapter.submitList(jobs)
            resultsRecyclerView.visibility = View.VISIBLE
            emptyStateLayout.visibility = View.GONE

            // Actualizar contador de resultados
            updateResultsCount(jobs.size)
        }
    }

    private fun updateResultsCount(count: Int) {
        resultsCountText.text = when (count) {
            0 -> "Sin resultados"
            1 -> "1 Resultado"
            else -> "$count Resultados"
        }
    }

    private fun showEmptyState() {
        resultsRecyclerView.visibility = View.GONE
        emptyStateLayout.visibility = View.VISIBLE
    }

    private fun navigateToJobDetail(job: JobResult) {
        val bundle = Bundle().apply {
            putInt("job_id", job.id)
            putString("job_title", job.title)
            putString("job_company", job.company)
            putString("job_location", job.location)
            putString("job_salary", job.salary)
            putString("job_type", job.type)
            putString("job_description", job.description)
            putString("job_published_date", job.publishedDate)
        }

        try {
            findNavController().navigate(
                R.id.action_searchResultListFragment_to_jobDescriptionDetailFragment,
                bundle
            )
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Abriendo detalles de: ${job.title}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleBookmarkClick(job: JobResult) {
        val message = if (job.isBookmarked) {
            "Guardado en favoritos: ${job.title}"
        } else {
            "Removido de favoritos: ${job.title}"
        }
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun handleApplyClick(job: JobResult) {
        Toast.makeText(requireContext(), "Aplicando a: ${job.title}", Toast.LENGTH_SHORT).show()
        // Aquí implementarías la lógica de aplicación
    }
}
