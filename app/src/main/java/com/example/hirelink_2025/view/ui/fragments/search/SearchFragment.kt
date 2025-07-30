package com.example.hirelink_2025.view.ui.fragments.search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.hirelink_2025.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class SearchFragment : Fragment() {

    // Referencias a las vistas
    private lateinit var jobTypeEditText: TextInputEditText
    private lateinit var locationEditText: TextInputEditText
    private lateinit var jobTypeInputLayout: TextInputLayout
    private lateinit var locationInputLayout: TextInputLayout
    private lateinit var searchButton: MaterialButton

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
        setupSearchButton()
        setupTextWatchers() // Opcional: para limpiar errores al escribir
    }

    private fun initViews(view: View) {
        jobTypeEditText = view.findViewById(R.id.jobTypeEditText)
        locationEditText = view.findViewById(R.id.locationEditText)
        jobTypeInputLayout = view.findViewById(R.id.jobTypeInputLayout)
        locationInputLayout = view.findViewById(R.id.locationInputLayout)
        searchButton = view.findViewById(R.id.searchButton)
    }

    private fun setupSearchButton() {
        searchButton.setOnClickListener {
            performSearch()
        }
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

        // Validar campos
        if (!validateFields(jobType, location)) {
            return
        }

        // Deshabilitar botón mientras navega (opcional)
        searchButton.isEnabled = false

        // Navegar al fragment de resultados
        navigateToSearchResults(jobType, location)
    }

    private fun validateFields(jobType: String, location: String): Boolean {
        var isValid = true

        if (jobType.isEmpty() && location.isEmpty()) {
            jobTypeInputLayout.error = "Ingresa al menos un criterio"
            locationInputLayout.error = "Ingresa al menos un criterio"
            isValid = false
        }

        // Validaciones adicionales si las necesitas
        // if (jobType.length < 3) {
        //     jobTypeInputLayout.error = "Mínimo 3 caracteres"
        //     isValid = false
        // }

        return isValid
    }

    private fun clearErrors() {
        jobTypeInputLayout.error = null
        locationInputLayout.error = null
    }

    private fun navigateToSearchResults(jobType: String, location: String) {
        val bundle = Bundle().apply {
            putString("job_type", jobType)
            putString("location", location)
        }

        try {
            findNavController().navigate(
                R.id.action_searchFragment_to_searchResultListFragment,
                bundle
            )
        } catch (e: Exception) {
            // Manejar error de navegación
            searchButton.isEnabled = true
            // Mostrar mensaje de error si es necesario
        }
    }

    override fun onResume() {
        super.onResume()
        // Rehabilitar botón cuando regrese a la pantalla
        searchButton.isEnabled = true
    }
}