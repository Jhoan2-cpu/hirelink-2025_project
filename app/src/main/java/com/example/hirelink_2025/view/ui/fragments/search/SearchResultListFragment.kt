package com.example.hirelink_2025.view.ui.fragments.search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import com.example.hirelink_2025.R
import com.example.hirelink_2025.databinding.FragmentSearchResultListBinding
import com.example.hirelink_2025.models.JobResult
import com.example.hirelink_2025.view.adapter.JobResultsAdapter
import com.google.android.material.button.MaterialButton

class SearchResultListFragment : Fragment() {

    private lateinit var binding: FragmentSearchResultListBinding
    private var isFilterMenuVisible = false
    private lateinit var jobResultsAdapter: JobResultsAdapter

    // Lista completa de trabajos (datos de prueba)
    private val allJobs = mutableListOf<JobResult>()

    // Lista filtrada que se muestra
    private var filteredJobs = mutableListOf<JobResult>()

    // Variables para almacenar los filtros seleccionados
    private var selectedStatus: String = "Todos"
    private var selectedExperience: String = "Todos"
    private var selectedDate: String = "Cualquier fecha"
    private var selectedPosition: String = "Todos"
    private var selectedEmploymentType: String = "Todos"
    private var selectedModality: String = "Todos"
    private var selectedLocation: String = "Todas"

    // Variables para los criterios de búsqueda
    private var jobType: String? = null
    private var location: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchResultListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtener argumentos de navegación
        arguments?.let {
            jobType = it.getString("job_type")
            location = it.getString("location")
        }

        setupUI()
        setupRecyclerView()
        setupFilterMenuButtons()
        generateTestData()
        showResults()
    }

    private fun setupUI() {
        // Actualizar título con los criterios de búsqueda
        binding.titleText.text = buildSearchTitle()

        // Botón back
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        // Botón menú de filtros
        binding.filterMenuButton.setOnClickListener {
            toggleFilterMenu()
        }

        // Actualizar texto de chips con filtros por defecto
        updateChipsText()
    }

    private fun buildSearchTitle(): String {
        return when {
            !jobType.isNullOrEmpty() && !location.isNullOrEmpty() ->
                "$jobType - $location"
            !jobType.isNullOrEmpty() ->
                "$jobType - Todas las ubicaciones"
            !location.isNullOrEmpty() ->
                "Todos los trabajos - $location"
            else -> "Resultados de búsqueda"
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

        binding.resultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = jobResultsAdapter
        }
    }

    private fun generateTestData() {
        allJobs.clear()

        val companies = listOf(
            "Google", "Microsoft", "Amazon", "Apple", "Netflix", "Spotify",
            "Uber", "Airbnb", "Facebook", "Twitter", "LinkedIn", "Adobe",
            "Oracle", "SAP", "Salesforce", "Dropbox", "Slack", "Zoom",
            "TikTok", "Snapchat", "Pinterest", "Reddit", "Discord", "Twitch"
        )

        val jobTitles = listOf(
            "Desarrollador Frontend", "Desarrollador Backend", "Desarrollador Full Stack",
            "Ingeniero de Software", "Arquitecto de Software", "DevOps Engineer",
            "Analista de Datos", "Científico de Datos", "Ingeniero de Machine Learning",
            "Diseñador UX/UI", "Product Manager", "Scrum Master",
            "Especialista en Marketing Digital", "Community Manager", "Content Creator",
            "Gerente de Ventas", "Ejecutivo Comercial", "Business Analyst",
            "Consultor IT", "Administrador de Sistemas", "Especialista en Ciberseguridad",
            "QA Engineer", "Tester de Software", "Ingeniero de Calidad"
        )

        val locations = listOf(
            "Lima, Perú", "Arequipa, Perú", "Trujillo, Perú", "Chiclayo, Perú",
            "Piura, Perú", "Cusco, Perú", "Huancayo, Perú", "Iquitos, Perú",
            "Remoto", "Híbrido - Lima", "Híbrido - Arequipa", "Nacional"
        )

        val experienceLevels = listOf("Principiante", "Intermedio", "Avanzado", "Senior")
        val employmentTypes = listOf("Tiempo completo", "Tiempo parcial", "Contrato", "Freelance", "Práctica")
        val modalities = listOf("Presencial", "Remoto", "Híbrido")
        val statuses = listOf("Activo", "Finalizado", "Pausado")

        // Generar 50 trabajos de prueba
        for (i in 1..50) {
            val company = companies.random()
            val title = jobTitles.random()
            val locationJob = locations.random()
            val experienceLevel = experienceLevels.random()
            val employmentType = employmentTypes.random()
            val modality = modalities.random()
            val status = statuses.random()

            val baseSalary = when (experienceLevel) {
                "Principiante" -> (2000..3500).random()
                "Intermedio" -> (3500..5500).random()
                "Avanzado" -> (5500..8000).random()
                "Senior" -> (8000..12000).random()
                else -> (2000..5000).random()
            }

            val maxSalary = baseSalary + (500..2000).random()
            val daysAgo = (1..30).random()

            val job = JobResult(
                id = i,
                title = title,
                company = company,
                location = locationJob,
                salary = "S/. $baseSalary - $maxSalary",
                type = "$employmentType - $modality",
                publishedDate = "Hace ${daysAgo} día${if (daysAgo > 1) "s" else ""}",
                description = "Buscamos un $title para unirse a nuestro equipo en $company. Trabajo $employmentType en modalidad $modality en $locationJob. Nivel de experiencia requerido: $experienceLevel.",
                isBookmarked = (i % 4 == 0), // Algunos trabajos marcados como favoritos
                // Campos adicionales para filtros
                status = status,
                experienceLevel = experienceLevel,
                employmentType = employmentType,
                modality = modality
            )

            allJobs.add(job)
        }
    }

    private fun setupFilterMenuButtons() {
        // Configurar cada botón de filtro
        binding.filterMenuCard.findViewById<MaterialButton>(R.id.filterDateButton)?.setOnClickListener {
            showDatePopupMenu(it)
        }

        binding.filterMenuCard.findViewById<MaterialButton>(R.id.filterPositionButton)?.setOnClickListener {
            showPositionPopupMenu(it)
        }

        binding.filterMenuCard.findViewById<MaterialButton>(R.id.filterEmploymentTypeButton)?.setOnClickListener {
            showEmploymentTypePopupMenu(it)
        }

        binding.filterMenuCard.findViewById<MaterialButton>(R.id.filterModalityButton)?.setOnClickListener {
            showModalityPopupMenu(it)
        }

        binding.filterMenuCard.findViewById<MaterialButton>(R.id.filterLocationButton)?.setOnClickListener {
            showLocationPopupMenu(it)
        }

        // Botón "Mostrar resultados"
        binding.filterMenuCard.findViewById<MaterialButton>(R.id.showResultsButton)?.setOnClickListener {
            applyFiltersAndShowResults()
        }
    }

    private fun showDatePopupMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.filter_date_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            selectedDate = when (item.itemId) {
                R.id.date_last_week -> "Última semana"
                R.id.date_last_month -> "Último mes"
                R.id.date_last_3_months -> "Últimos 3 meses"
                R.id.date_all -> "Cualquier fecha"
                else -> "Cualquier fecha"
            }
            updateChipsText()
            applyFilters()
            true
        }

        popupMenu.show()
    }

    private fun showPositionPopupMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.filter_position_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            selectedPosition = when (item.itemId) {
                R.id.pos_developer -> "Desarrollador"
                R.id.pos_designer -> "Diseñador"
                R.id.pos_manager -> "Gerente"
                R.id.pos_analyst -> "Analista"
                R.id.pos_marketing -> "Marketing"
                R.id.pos_sales -> "Ventas"
                R.id.pos_other -> "Otros"
                else -> "Todos"
            }
            updateChipsText()
            applyFilters()
            true
        }

        popupMenu.show()
    }

    private fun showEmploymentTypePopupMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.filter_employment_type_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            selectedEmploymentType = when (item.itemId) {
                R.id.emp_full_time -> "Tiempo completo"
                R.id.emp_part_time -> "Tiempo parcial"
                R.id.emp_contract -> "Contrato"
                R.id.emp_freelance -> "Freelance"
                R.id.emp_internship -> "Práctica"
                R.id.emp_all -> "Todos"
                else -> "Todos"
            }
            updateChipsText()
            applyFilters()
            true
        }

        popupMenu.show()
    }

    private fun showModalityPopupMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.filter_modality_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            selectedModality = when (item.itemId) {
                R.id.mod_presencial -> "Presencial"
                R.id.mod_remote -> "Remoto"
                R.id.mod_hybrid -> "Híbrido"
                R.id.mod_all -> "Todos"
                else -> "Todos"
            }
            updateChipsText()
            applyFilters()
            true
        }

        popupMenu.show()
    }

    private fun showLocationPopupMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.filter_location_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            selectedLocation = when (item.itemId) {
                R.id.loc_same_city -> "Misma ciudad"
                R.id.loc_same_region -> "Misma región"
                R.id.loc_national -> "Nacional"
                R.id.loc_international -> "Internacional"
                R.id.loc_all -> "Todas"
                else -> "Todas"
            }
            updateChipsText()
            applyFilters()
            true
        }

        popupMenu.show()
    }

    private fun updateChipsText() {
        // Actualizar el texto de los chips con los filtros seleccionados
        binding.statusChip.text = selectedStatus
        binding.dateChip.text = selectedDate

        // Actualizar el contador de resultados
        updateResultsCount()
    }

    private fun updateResultsCount() {
        val count = filteredJobs.size
        binding.resultsCountText.text = when (count) {
            0 -> "Sin resultados"
            1 -> "1 Resultado"
            else -> "$count Resultados"
        }

        // Actualizar el botón "Mostrar resultados"
        binding.filterMenuCard.findViewById<MaterialButton>(R.id.showResultsButton)?.text =
            "Mostrar $count resultados"
    }

    private fun applyFilters() {
        filteredJobs.clear()

        filteredJobs.addAll(allJobs.filter { job ->
            // Filtrar por estado
            val statusMatch = selectedStatus == "Todos" || job.status == selectedStatus

            // Filtrar por experiencia
            val experienceMatch = selectedExperience == "Todos" || job.experienceLevel == selectedExperience

            // Filtrar por fecha (simplificado)
            val dateMatch = selectedDate == "Cualquier fecha" || isJobWithinDateRange(job)

            // Filtrar por cargo
            val positionMatch = selectedPosition == "Todos" || job.title.contains(selectedPosition, ignoreCase = true)

            // Filtrar por tipo de empleo
            val employmentMatch = selectedEmploymentType == "Todos" || job.employmentType == selectedEmploymentType

            // Filtrar por modalidad
            val modalityMatch = selectedModality == "Todos" || job.modality == selectedModality

            // Filtrar por ubicación (simplificado)
            val locationMatch = selectedLocation == "Todas" ||
                    (selectedLocation == "Nacional" && job.location.contains("Perú", ignoreCase = true)) ||
                    (selectedLocation == "Remoto" && job.location.contains("Remoto", ignoreCase = true))

            // Debe cumplir todos los filtros
            statusMatch && experienceMatch && dateMatch && positionMatch &&
                    employmentMatch && modalityMatch && locationMatch
        })

        showResults()
    }

    private fun isJobWithinDateRange(job: JobResult): Boolean {
        // Extraer el número de días del campo publishedDate
        val publishedText = job.publishedDate
        val daysAgo = publishedText.replace("Hace ", "").replace(" días", "").replace(" día", "").toIntOrNull() ?: 0

        return when (selectedDate) {
            "Última semana" -> daysAgo <= 7
            "Último mes" -> daysAgo <= 30
            "Últimos 3 meses" -> daysAgo <= 90
            else -> true
        }
    }

    private fun showResults() {
        if (filteredJobs.isEmpty() && allJobs.isNotEmpty()) {
            showEmptyState()
        } else {
            jobResultsAdapter.submitList(filteredJobs.toList())
            binding.resultsRecyclerView.visibility = View.VISIBLE
            binding.emptyStateLayout.visibility = View.GONE
        }

        updateResultsCount()
    }

    private fun showEmptyState() {
        binding.resultsRecyclerView.visibility = View.GONE
        binding.emptyStateLayout.visibility = View.VISIBLE
    }

    private fun toggleFilterMenu() {
        isFilterMenuVisible = !isFilterMenuVisible

        if (isFilterMenuVisible) {
            showFilterMenu()
        } else {
            hideFilterMenu()
        }
    }

    private fun showFilterMenu() {
        binding.filterMenuCard.visibility = View.VISIBLE
        binding.filterMenuCard.animate()
            .alpha(1f)
            .setDuration(200)
            .start()
    }

    private fun hideFilterMenu() {
        binding.filterMenuCard.animate()
            .alpha(0f)
            .setDuration(200)
            .withEndAction {
                binding.filterMenuCard.visibility = View.GONE
            }
            .start()
    }

    private fun applyFiltersAndShowResults() {
        // Aplicar filtros
        applyFilters()

        // Ocultar el menú de filtros
        hideFilterMenu()
        isFilterMenuVisible = false

        // Mostrar mensaje de confirmación
        Toast.makeText(requireContext(), "Filtros aplicados - ${filteredJobs.size} resultados", Toast.LENGTH_SHORT).show()
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
        // Actualizar el estado del bookmark en la lista
        val index = filteredJobs.indexOfFirst { it.id == job.id }
        if (index != -1) {
            filteredJobs[index].isBookmarked = !filteredJobs[index].isBookmarked
            jobResultsAdapter.notifyItemChanged(index)
        }

        // También actualizar en la lista completa
        val allIndex = allJobs.indexOfFirst { it.id == job.id }
        if (allIndex != -1) {
            allJobs[allIndex].isBookmarked = !allJobs[allIndex].isBookmarked
        }

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

    // Clase para encapsular los criterios de filtro
    data class FilterCriteria(
        val status: String,
        val experience: String,
        val date: String,
        val position: String,
        val employmentType: String,
        val modality: String,
        val location: String
    )
}