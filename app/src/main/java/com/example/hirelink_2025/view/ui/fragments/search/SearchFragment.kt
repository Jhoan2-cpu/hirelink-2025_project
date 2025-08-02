package com.example.hirelink_2025.view.ui.fragments.search

import android.animation.ValueAnimator
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hirelink_2025.R
import com.example.hirelink_2025.models.Company
import com.example.hirelink_2025.models.Job
import com.example.hirelink_2025.view.adapter.JobAdapter
import com.example.hirelink_2025.viewmodels.SearchViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class SearchFragment : Fragment() {

    // ViewModel
    private val searchViewModel: SearchViewModel by viewModels()
    
    // Referencias a las vistas
    private lateinit var jobTypeEditText: TextInputEditText
    private lateinit var locationEditText: TextInputEditText
    private lateinit var jobTypeInputLayout: TextInputLayout
    private lateinit var locationInputLayout: TextInputLayout
    private lateinit var searchButton: MaterialButton
    private lateinit var jobsRecyclerView: RecyclerView
    private lateinit var loadingProgressBar: ProgressBar
    
    // Collapsed search views
    private lateinit var collapsedSearchBar: MaterialCardView
    private lateinit var searchCard: MaterialCardView
    private lateinit var headerBackground: View
    private lateinit var appLogo: TextView
    private lateinit var jobOffersTitle: TextView
    
    // Adapter
    private lateinit var jobAdapter: JobAdapter
    
    // Gesture detector for swipe
    private lateinit var gestureDetector: GestureDetectorCompat
    
    // State
    private var isSearchCollapsed = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupRecyclerView()
        setupSearchButton()
        setupTextWatchers()
        setupGestureDetector()
        setupCollapsedSearchBar()
        observeViewModel()
    }

    private fun initViews(view: View) {
        jobTypeEditText = view.findViewById(R.id.jobTypeEditText)
        locationEditText = view.findViewById(R.id.locationEditText)
        jobTypeInputLayout = view.findViewById(R.id.jobTypeInputLayout)
        locationInputLayout = view.findViewById(R.id.locationInputLayout)
        searchButton = view.findViewById(R.id.searchButton)
        jobsRecyclerView = view.findViewById(R.id.jobsRecyclerView)
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar)
        
        // Collapsed search views
        collapsedSearchBar = view.findViewById(R.id.collapsedSearchBar)
        searchCard = view.findViewById(R.id.searchCard)
        headerBackground = view.findViewById(R.id.headerBackground)
        appLogo = view.findViewById(R.id.appLogo)
        jobOffersTitle = view.findViewById(R.id.jobOffersTitle)
    }

    private fun setupRecyclerView() {
        jobAdapter = JobAdapter(
            onJobClick = { job -> onJobClicked(job) },
            onApplyClick = { job -> onApplyClicked(job) },
            onBookmarkClick = { job -> onBookmarkClicked(job)},
            getCompanyInfo = { companyId -> getCompanyInfo(companyId) }
        )

        jobsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = jobAdapter
            
            // Auto-collapse on scroll
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    // Colapsar cuando se hace scroll hacia arriba
                    if (dy > 20 && !isSearchCollapsed) {
                        collapseSearchForm()
                    }
                }
            })
        }
    }
    
    private fun setupSearchButton() {
        searchButton.setOnClickListener {
            performSearch()
        }
    }
    
    private fun setupGestureDetector() {
        gestureDetector = GestureDetectorCompat(requireContext(), object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                // Detectar swipe hacia arriba en el área del formulario de búsqueda
                if (distanceY > 20 && !isSearchCollapsed) {
                    collapseSearchForm()
                    return true
                }
                return false
            }
        })
        
        // Aplicar gesture detector al área del formulario
        searchCard.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            false
        }
    }
    
    private fun setupCollapsedSearchBar() {
        collapsedSearchBar.setOnClickListener {
            expandSearchForm()
        }
        
        // Update hint text based on current search
        updateCollapsedSearchHint()
    }
    
    private fun updateCollapsedSearchHint() {
        val searchHint = view?.findViewById<TextView>(R.id.searchHint)
        val jobType = jobTypeEditText.text.toString().trim()
        val location = locationEditText.text.toString().trim()
        
        val hintText = when {
            jobType.isNotEmpty() && location.isNotEmpty() -> "$jobType en $location"
            jobType.isNotEmpty() -> jobType
            location.isNotEmpty() -> "Trabajos en $location"
            else -> "Buscar trabajos..."
        }
        
        searchHint?.text = hintText
    }

    private fun setupTextWatchers() {
        // Opcional: Limpiar errores cuando el usuario empiece a escribir
        jobTypeEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) jobTypeInputLayout.error = null
        }

        locationEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) locationInputLayout.error = null
        }
    }

    private fun performSearch() {
        val jobType = jobTypeEditText.text.toString().trim()
        val location = locationEditText.text.toString().trim()

        // Limpiar errores previos
        clearErrors()

        // Si ambos campos están vacíos, cargar todos los trabajos
        if (jobType.isEmpty() && location.isEmpty()) {
            searchViewModel.loadAllJobs()
        } else {
            // Realizar búsqueda con filtros
            searchViewModel.searchJobs(jobType, location)
        }
        
        // Actualizar hint en la barra colapsada
        updateCollapsedSearchHint()
    }

    private fun observeViewModel() {
        searchViewModel.jobs.observe(viewLifecycleOwner) { jobs ->
            jobAdapter.submitList(jobs)
        }
        
        searchViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        searchViewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                searchViewModel.clearError()
            }
        }
    }

    private fun clearErrors() {
        jobTypeInputLayout.error = null
        locationInputLayout.error = null
    }

    private fun onJobClicked(job: Job) {
        // Navegar al detalle del trabajo
        val bundle = Bundle().apply {
            putString("jobId", job.id)
        }
        
        try {
            findNavController().navigate(
                R.id.action_searchFragment_to_jobDetailFragment,
                bundle
            )
        } catch (e: Exception) {
            Toast.makeText(context, "Error al abrir detalle del trabajo", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun onApplyClicked(job: Job) {
        Toast.makeText(context, "Aplicar a: ${job.title}", Toast.LENGTH_SHORT).show()
    }
    
    private fun onBookmarkClicked(job: Job) {
        Toast.makeText(context, "Trabajo guardado: ${job.title}", Toast.LENGTH_SHORT).show()
    }

    //TEMPORAL
    fun getCompanyInfo(companyId: String): Company? {
        return cachedCompanyMap[companyId]
    }
    //TEMPORAL
    private val cachedCompanyMap: Map<String, Company> = mapOf(
        "1" to Company("1", "Tech Solutions S.A.C.", "Lima"),
        "2" to Company("2", "GlobalSoft", "Cusco")
    )

    private fun collapseSearchForm() {
        if (isSearchCollapsed) return
        
        isSearchCollapsed = true
        
        // Animar la ocultación del formulario completo
        val collapseAnimator = ValueAnimator.ofFloat(1f, 0f)
        collapseAnimator.duration = 300
        collapseAnimator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Float
            
            // Ocultar elementos del header
            headerBackground.alpha = animatedValue
            appLogo.alpha = animatedValue
            searchCard.alpha = animatedValue
            
            // Escalar elementos
            searchCard.scaleY = animatedValue
            headerBackground.scaleY = animatedValue
            
            if (animatedValue == 0f) {
                // Mostrar barra colapsada
                collapsedSearchBar.visibility = View.VISIBLE
                collapsedSearchBar.alpha = 0f
                collapsedSearchBar.animate()
                    .alpha(1f)
                    .setDuration(200)
                    .start()
                
                // Ocultar elementos completos
                headerBackground.visibility = View.GONE
                appLogo.visibility = View.GONE
                searchCard.visibility = View.GONE
                
                // Actualizar constraint del título
                updateJobsTitleConstraint(true)
            }
        }
        collapseAnimator.start()
    }
    
    private fun expandSearchForm() {
        if (!isSearchCollapsed) return
        
        isSearchCollapsed = false
        
        // Ocultar barra colapsada
        collapsedSearchBar.animate()
            .alpha(0f)
            .setDuration(200)
            .withEndAction {
                collapsedSearchBar.visibility = View.GONE
                
                // Mostrar elementos del header
                headerBackground.visibility = View.VISIBLE
                appLogo.visibility = View.VISIBLE
                searchCard.visibility = View.VISIBLE
                
                // Animar la aparición
                val expandAnimator = ValueAnimator.ofFloat(0f, 1f)
                expandAnimator.duration = 300
                expandAnimator.addUpdateListener { animation ->
                    val animatedValue = animation.animatedValue as Float
                    
                    headerBackground.alpha = animatedValue
                    appLogo.alpha = animatedValue
                    searchCard.alpha = animatedValue
                    searchCard.scaleY = animatedValue
                    headerBackground.scaleY = animatedValue
                }
                expandAnimator.start()
                
                // Actualizar constraint del título
                updateJobsTitleConstraint(false)
            }
            .start()
    }
    
    private fun updateJobsTitleConstraint(collapsed: Boolean) {
        val layoutParams = jobOffersTitle.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.topToBottom = if (collapsed) {
            R.id.collapsedSearchBar
        } else {
            R.id.searchCard
        }
        jobOffersTitle.layoutParams = layoutParams
    }
}