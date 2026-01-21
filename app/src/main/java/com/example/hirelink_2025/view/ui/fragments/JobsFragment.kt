package com.example.hirelink_2025.view.ui.fragments

import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hirelink_2025.R
import com.example.hirelink_2025.models.JobAd // CAMBIAR Job por JobAd
import com.example.hirelink_2025.view.adapter.CategoriesAdapter
import com.example.hirelink_2025.view.adapter.JobsAdapter
import com.example.hirelink_2025.viewmodels.JobsUiState
import com.example.hirelink_2025.viewmodels.JobsViewModel

class JobsFragment : Fragment() {

    private val viewModel: JobsViewModel by viewModels()
    private lateinit var categoriesAdapter: CategoriesAdapter
    private lateinit var jobsAdapter: JobsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_jobs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        setupSearchView()
        observeViewModel()
    }

    private fun setupRecyclerViews() {
        // Adapter para categorías
        categoriesAdapter = CategoriesAdapter { category ->
            viewModel.selectCategory(category)
        }

        // Adapter para empleos - CORREGIR el tipo de parámetro
        jobsAdapter = JobsAdapter { job ->
            navigateToJobDetail(job)
        }

        // RecyclerView para categorías
        view?.findViewById<RecyclerView>(R.id.categoriesRecyclerView)?.apply {
            adapter = categoriesAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }

        // RecyclerView principal para empleos
        view?.findViewById<RecyclerView>(R.id.recyclerView)?.apply {
            adapter = jobsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupSearchView() {
        view?.findViewById<SearchView>(R.id.searchView)?.apply {
            setOnQueryTextListener(object :
                SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let { viewModel.searchJobs(it) }
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText?.let { viewModel.searchJobs(it) }
                    return true
                }
            })
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

    private fun updateUI(state: JobsUiState) {
        // Actualizar categorías - CAMBIAR submitList por updateCategories
        categoriesAdapter.updateCategories(state.categories)
        categoriesAdapter.setSelectedCategory(state.selectedCategory)

        // Actualizar empleos - CAMBIAR submitList por updateJobs
        jobsAdapter.updateJobs(state.filteredJobs)

        // Manejar estado de carga
        view?.findViewById<View>(R.id.progressBar)?.visibility =
            if (state.isLoading) View.VISIBLE else View.GONE

        // Mostrar errores
        state.error?.let { error ->
            Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    private fun navigateToJobDetail(job: JobAd) {
        try {
            val bundle = Bundle().apply {
                putString("jobId", job.id)
                putString("job_title", job.titulo)
                putString("job_company", job.empresa)
                putString("job_location", job.ubicacion)
                putString("job_salary", job.salario)
                putString("job_description", job.descripcion)
                putString("job_requirements", job.habilidades.joinToString("\n• ", "• "))
                putString("job_work_type", job.modalidad)
                // Eliminamos job_posted_date ya que no existe en el modelo
            }

            findNavController().navigate(
                R.id.action_jobsFragment_to_jobDetailFragment,
                bundle
            )
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error al abrir detalles", Toast.LENGTH_SHORT).show()
            Log.e("JobsFragment", "Navigation error", e)
        }
    }
}