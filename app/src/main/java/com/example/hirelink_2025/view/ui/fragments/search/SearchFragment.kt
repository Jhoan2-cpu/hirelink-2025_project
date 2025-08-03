package com.example.hirelink_2025.view.ui.fragments.search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.hirelink_2025.R
import com.example.hirelink_2025.databinding.FragmentSearchBinding
import com.example.hirelink_2025.view.ui.utils.showErrorSnackbar
import com.example.hirelink_2025.viewmodels.SearchViewModel

/**
 * Fragment para búsqueda de empleos
 * Siguiendo arquitectura MVVM con navegación a resultados
 */
class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    // ViewModel para manejo de búsqueda compartido con SearchResultsFragment
    private val searchViewModel: SearchViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
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
        setupTextWatchers()
        setupSearchButton()
        setupCollapsedSearchBar()
    }

    /**
     * Configura los listeners para campos de texto
     */
    private fun setupTextWatchers() {
        // Actualizar ViewModel cuando cambian los campos
        binding.jobTypeEditText.doOnTextChanged { text, _, _, _ ->
            searchViewModel.updateJobType(text.toString())
            updateCollapsedSearchHint()
            clearFieldErrors()
        }

        binding.locationEditText.doOnTextChanged { text, _, _, _ ->
            searchViewModel.updateLocation(text.toString())
            updateCollapsedSearchHint()
            clearFieldErrors()
        }

        // Limpiar errores cuando el usuario enfoca los campos
        binding.jobTypeEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) binding.jobTypeInputLayout.error = null
        }

        binding.locationEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) binding.locationInputLayout.error = null
        }
    }

    /**
     * Configura el botón de búsqueda
     */
    private fun setupSearchButton() {
        binding.searchButton.setOnClickListener {
            performSearch()
        }
    }

    /**
     * Configura la barra de búsqueda colapsada
     */
    private fun setupCollapsedSearchBar() {
        binding.collapsedSearchContent.setOnClickListener {
            // TODO: Implementar lógica para expandir la búsqueda
            // Por ahora no usamos la funcionalidad colapsada
        }
    }

    /**
     * Configura los observadores del ViewModel
     */
    private fun setupObservers() {
        // Observar errores
        searchViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                showErrorSnackbar(errorMessage) {
                    searchViewModel.clearErrorMessage()
                }
            }
        }

        // Observar estado de validación para habilitar/deshabilitar búsqueda
        searchViewModel.jobTypeQuery.observe(viewLifecycleOwner) { _ ->
            updateSearchButtonState()
        }

        searchViewModel.locationQuery.observe(viewLifecycleOwner) { _ ->
            updateSearchButtonState()
        }
    }

    /**
     * Realiza la búsqueda y navega a resultados
     */
    private fun performSearch() {
        val jobType = binding.jobTypeEditText.text.toString().trim()
        val location = binding.locationEditText.text.toString().trim()

        // Validar que al menos un campo tenga contenido
        if (jobType.isBlank() && location.isBlank()) {
            binding.jobTypeInputLayout.error = "Ingresa al menos un criterio de búsqueda"
            binding.locationInputLayout.error = "Ingresa al menos un criterio de búsqueda"
            return
        }

        // Limpiar errores
        clearFieldErrors()

        android.util.Log.d("SearchFragment", "Performing search - jobType: '$jobType', location: '$location'")

        // Aplicar filtros al ViewModel
        searchViewModel.updateJobType(jobType)
        searchViewModel.updateLocation(location)
        
        // Ejecutar búsqueda antes de navegar
        searchViewModel.searchJobs()

        // Navegar a resultados de búsqueda
        navigateToSearchResults(jobType, location)
    }

    /**
     * Navega al fragmento de resultados de búsqueda
     */
    private fun navigateToSearchResults(jobType: String, location: String) {
        try {
            android.util.Log.d("SearchFragment", "Navegando a resultados de búsqueda - jobType: '$jobType', location: '$location'")
            
            // Crear Bundle con argumentos
            val bundle = Bundle().apply {
                putString("jobType", jobType)
                putString("location", location)
            }
            
            // Navegar usando el ID del destino
            findNavController().navigate(R.id.action_searchFragment_to_searchResultsFragment, bundle)
            
        } catch (e: Exception) {
            android.util.Log.e("SearchFragment", "Error navigating to search results: ${e.message}")
            
            // Fallback: navegación manual si falla la navegación por ID
            try {
                findNavController().navigate(R.id.searchResultsFragment, Bundle().apply {
                    putString("jobType", jobType)
                    putString("location", location)
                })
            } catch (e2: Exception) {
                android.util.Log.e("SearchFragment", "Fallback navigation also failed: ${e2.message}")
                showErrorSnackbar("Error en la navegación: ${e2.message}")
            }
        }
    }

    /**
     * Actualiza el hint de la barra de búsqueda colapsada
     */
    private fun updateCollapsedSearchHint() {
        val jobType = binding.jobTypeEditText.text.toString().trim()
        val location = binding.locationEditText.text.toString().trim()
        
        val hintText = when {
            jobType.isNotEmpty() && location.isNotEmpty() -> "$jobType en $location"
            jobType.isNotEmpty() -> jobType
            location.isNotEmpty() -> "Trabajos en $location"
            else -> "Buscar trabajos..."
        }
        
        binding.searchHint.text = hintText
    }

    /**
     * Actualiza el estado del botón de búsqueda
     */
    private fun updateSearchButtonState() {
        binding.searchButton.isEnabled = searchViewModel.canSearch()
    }

    /**
     * Limpia errores de los campos
     */
    private fun clearFieldErrors() {
        binding.jobTypeInputLayout.error = null
        binding.locationInputLayout.error = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}