package com.example.hirelink_2025.view.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.hirelink_2025.R
import com.example.hirelink_2025.models.JobAd
import com.example.hirelink_2025.viewmodels.JobDetailUiState
import com.example.hirelink_2025.viewmodels.JobDetailViewModel
import kotlinx.coroutines.launch

class JobDetailFragment : Fragment() {

    private val viewModel: JobDetailViewModel by viewModels()

    // Views
    private lateinit var progressBar: ProgressBar
    private lateinit var backButton: ImageButton
    private lateinit var bookmarkButton: ImageButton
    private lateinit var titleText: TextView
    private lateinit var companyText: TextView
    private lateinit var locationText: TextView
    private lateinit var salaryText: TextView
    private lateinit var modalityText: TextView
    private lateinit var descriptionText: TextView
    private lateinit var requirementsText: TextView
    private lateinit var applyButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_job_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupClickListeners()
        observeViewModel()

        // Obtener jobId del Bundle (temporal hasta que Safe Args funcione)
        val jobId = arguments?.getString("jobId") ?: ""
        if (jobId.isNotEmpty()) {
            viewModel.loadJobDetail(jobId)
        } else {
            Toast.makeText(requireContext(), "Error: ID de empleo no encontrado", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    private fun initViews(view: View) {
        progressBar = view.findViewById(R.id.progressBar)
        backButton = view.findViewById(R.id.backButton)
        bookmarkButton = view.findViewById(R.id.bookmarkButton)
        titleText = view.findViewById(R.id.titleText)
        companyText = view.findViewById(R.id.companyText)
        locationText = view.findViewById(R.id.locationText)
        salaryText = view.findViewById(R.id.salaryText)
        modalityText = view.findViewById(R.id.modalityText)
        descriptionText = view.findViewById(R.id.descriptionText)
        requirementsText = view.findViewById(R.id.requirementsText)
        applyButton = view.findViewById(R.id.applyButton)
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        bookmarkButton.setOnClickListener {
            viewModel.toggleBookmark()
        }

        applyButton.setOnClickListener {
            viewModel.applyToJob()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateUI(state)
                }
            }
        }
    }

    private fun updateUI(state: JobDetailUiState) {
        // Loading state
        progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE

        // Job data
        state.job?.let { job ->
            populateJobData(job)
        }

        // Bookmark button
        updateBookmarkButton(state.bookmarked)

        // Apply button
        applyButton.isEnabled = !state.isApplying
        applyButton.text = if (state.isApplying) "Aplicando..." else "Aplicar"

        // Error handling
        state.error?.let { error ->
            Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }

        // Success
        if (state.applicationSuccess) {
            Toast.makeText(requireContext(), "¡Aplicación enviada exitosamente!", Toast.LENGTH_LONG).show()
            viewModel.clearApplicationSuccess()
        }
    }

    private fun populateJobData(job: JobAd) {
        titleText.text = job.titulo
        companyText.text = job.empresa
        locationText.text = job.ubicacion
        salaryText.text = job.salario
        modalityText.text = job.modalidad
        descriptionText.text = job.descripcion
        requirementsText.text = job.habilidades.joinToString("\n• ", "• ")
    }

    private fun updateBookmarkButton(bookmarked: Boolean) {
        val iconRes = if (bookmarked) {
            R.drawable.ic_bookmark_filled
        } else {
            R.drawable.ic_bookmark_border
        }

        try {
            bookmarkButton.setImageResource(iconRes)
        } catch (e: Exception) {
            bookmarkButton.setImageResource(android.R.drawable.ic_menu_save)
        }
    }
}