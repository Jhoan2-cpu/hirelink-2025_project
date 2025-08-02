package com.example.hirelink_2025.view.ui.fragments.ads

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hirelink_2025.R
import com.example.hirelink_2025.models.Company
import com.example.hirelink_2025.view.adapter.CompanyAdapter
import com.example.hirelink_2025.viewmodels.CompanyViewModel
import com.example.hirelink_2025.viewmodels.ViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

/**
 * Fragment para mostrar las compañías del usuario
 */
class CompanyFragment : Fragment() {
    
    private lateinit var companiesRecyclerView: RecyclerView
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var addCompanyFab: FloatingActionButton
    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var companyAdapter: CompanyAdapter
    
    // ViewModel con factory
    private val viewModel: CompanyViewModel by viewModels { ViewModelFactory() }
    
    // Usuario actual
    private var currentUserId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_company, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        Log.d("CompanyFragment", "Fragment created")
        
        initViews(view)
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
        
        // Obtener usuario actual y cargar compañías
        getCurrentUserAndLoadCompanies()
    }
    
    private fun initViews(view: View) {
        companiesRecyclerView = view.findViewById(R.id.companiesRecyclerView)
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout)
        addCompanyFab = view.findViewById(R.id.addCompanyFab)
        progressIndicator = view.findViewById(R.id.progressIndicator)
    }
    
    private fun setupRecyclerView() {
        companyAdapter = CompanyAdapter(
            companies = mutableListOf(),
            onEditClick = { company ->
                Log.d("CompanyFragment", "Edit company: ${company.name}")
                navigateToEditCompany(company)
            },
            onDeleteClick = { company ->
                Log.d("CompanyFragment", "Delete company requested: ${company.name}")
                showDeleteConfirmation(company)
            },
            onItemClick = { company ->
                Log.d("CompanyFragment", "Company clicked: ${company.name}")
                navigateToCompanyDetail(company)
            }
        )
        
        companiesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = companyAdapter
        }
    }
    
    private fun setupClickListeners() {
        addCompanyFab.setOnClickListener {
            Log.d("CompanyFragment", "Add company FAB clicked")
            navigateToRegisterCompany()
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                
                // Observar lista de compañías
                viewModel.listCompany.observe(viewLifecycleOwner) { companies ->
                    Log.d("CompanyFragment", "Companies updated: ${companies.size}")
                    updateUI(companies)
                }
                
                // Observar estado de carga
                launch {
                    viewModel.isLoading.collect { isLoading ->
                        Log.d("CompanyFragment", "Loading state: $isLoading")
                        updateLoadingState(isLoading)
                    }
                }
                
                // Observar errores
                launch {
                    viewModel.error.collect { error ->
                        error?.let {
                            Log.e("CompanyFragment", "Error: $it")
                            showError(it)
                            viewModel.clearError()
                        }
                    }
                }
                
                // Observar resultados de operaciones
                launch {
                    viewModel.operationResult.collect { result ->
                        result?.let {
                            Log.d("CompanyFragment", "Operation result: $it")
                            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                            viewModel.clearOperationResult()
                        }
                    }
                }
            }
        }
    }
    
   private fun getCurrentUserAndLoadCompanies() {
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        
        if (currentUserId != null) {
            Log.d("CompanyFragment", "Loading companies for user: $currentUserId")
            
            // Primero limpiar URLs de placeholder problemáticas
            viewModel.cleanPlaceholderUrls(currentUserId!!) { success ->
                if (success) {
                    Log.d("CompanyFragment", "Placeholder URLs cleaned successfully")
                } else {
                    Log.w("CompanyFragment", "Failed to clean placeholder URLs")
                }
                // Cargar compañías después de la limpieza
                viewModel.loadUserCompanies(currentUserId!!)
            }
        } else {
            Log.w("CompanyFragment", "No authenticated user found")
            showError("Usuario no autenticado")
        }
    }
    
    private fun updateUI(companies: List<Company>) {
        if (companies.isEmpty()) {
            // Mostrar estado vacío
            companiesRecyclerView.visibility = View.GONE
            emptyStateLayout.visibility = View.VISIBLE
        } else {
            // Mostrar lista de compañías
            companiesRecyclerView.visibility = View.VISIBLE
            emptyStateLayout.visibility = View.GONE
            companyAdapter.updateCompanies(companies)
        }
    }
    
    private fun updateLoadingState(isLoading: Boolean) {
        if (isLoading) {
            progressIndicator.visibility = View.VISIBLE
        } else {
            progressIndicator.visibility = View.GONE
        }
    }
    
    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
    
    private fun showDeleteConfirmation(company: Company) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar Compañía")
            .setMessage("¿Estás seguro de que deseas eliminar '${company.name}'? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                deleteCompany(company)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun deleteCompany(company: Company) {
        currentUserId?.let { ownerId ->
            viewModel.deleteCompany(company.id, ownerId) { success ->
                if (success) {
                    Log.d("CompanyFragment", "Company deleted successfully")
                } else {
                    Log.e("CompanyFragment", "Failed to delete company")
                }
            }
        }
    }
    
    private fun navigateToCompanyDetail(company: Company) {
        Log.d("CompanyFragment", "Navigating to company detail. Company ID: '${company.id}', Company Name: '${company.name}'")
        viewModel.selectCompany(company)
        val bundle = Bundle().apply {
            putString("companyId", company.id)
        }
        Log.d("CompanyFragment", "Bundle created with companyId: '${company.id}'")
        findNavController().navigate(R.id.action_companyFragment_to_companyDetailFragment, bundle)
    }
    
    private fun navigateToEditCompany(company: Company) {
        viewModel.selectCompany(company)
        val bundle = Bundle().apply {
            putString("companyId", company.id)
        }
        findNavController().navigate(R.id.action_companyFragment_to_companyEditFragment, bundle)
    }
    
    private fun navigateToRegisterCompany() {
        findNavController().navigate(R.id.action_companyFragment_to_companyRegisterFragment)
    }
    
    override fun onResume() {
        super.onResume()
        // Recargar compañías cuando el fragment se vuelve visible
        currentUserId?.let { userId ->
            Log.d("CompanyFragment", "Refreshing companies on resume")
            viewModel.refreshCompanies(userId)
        }
    }
}