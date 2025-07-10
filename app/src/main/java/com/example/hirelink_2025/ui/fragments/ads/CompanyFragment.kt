package com.example.hirelink_2025.ui.fragments.ads

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hirelink_2025.R
import com.example.hirelink_2025.models.Company
import com.example.hirelink_2025.ui.adapters.CompanyAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CompanyFragment : Fragment() {
    
    private lateinit var companiesRecyclerView: RecyclerView
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var addCompanyFab: FloatingActionButton
    private lateinit var companyAdapter: CompanyAdapter
    
    private val companies = mutableListOf<Company>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_company, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupRecyclerView()
        setupClickListeners()
        loadCompanies()
    }
    
    private fun initViews(view: View) {
        companiesRecyclerView = view.findViewById(R.id.companiesRecyclerView)
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout)
        addCompanyFab = view.findViewById(R.id.addCompanyFab)
    }
    
    private fun setupRecyclerView() {
        companyAdapter = CompanyAdapter(
            companies = companies,
            onEditClick = { company -> editCompany(company) },
            onDeleteClick = { company -> confirmDeleteCompany(company) },
            onItemClick = { company -> viewCompanyDetails(company) }
        )
        
        companiesRecyclerView.apply {
            adapter = companyAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }
    
    private fun setupClickListeners() {
        addCompanyFab.setOnClickListener {
            navigateToRegisterCompany()
        }
    }
    
    private fun loadCompanies() {
        // TODO: Load companies from repository/database
        // For now, add some sample data
        loadSampleData()
        updateUI()
    }
    
    private fun loadSampleData() {
        val sampleCompanies = listOf(
            Company(
                id = "1",
                name = "TechSolutions S.A.C.",
                type = "Tecnología",
                description = "Empresa líder en desarrollo de software y soluciones tecnológicas innovadoras.",
                size = "50-100",
                foundedYear = 2020,
                city = "Lima",
                country = "Perú",
                phone = "987654321",
                email = "info@techsolutions.com",
                website = "www.techsolutions.com",
                employeeCount = 75,
                activeJobsCount = 3
            ),
            Company(
                id = "2",
                name = "Innovate Corp",
                type = "Consultoría",
                description = "Consultoría especializada en transformación digital.",
                size = "10-50",
                foundedYear = 2018,
                city = "Arequipa",
                country = "Perú",
                phone = "123456789",
                email = "contact@innovate.com",
                website = "www.innovate.com",
                employeeCount = 25,
                activeJobsCount = 1
            )
        )
        
        companies.clear()
        companies.addAll(sampleCompanies)
    }
    
    private fun updateUI() {
        if (companies.isEmpty()) {
            companiesRecyclerView.visibility = View.GONE
            emptyStateLayout.visibility = View.VISIBLE
        } else {
            companiesRecyclerView.visibility = View.VISIBLE
            emptyStateLayout.visibility = View.GONE
        }
        companyAdapter.notifyDataSetChanged()
    }
    
    private fun navigateToRegisterCompany() {
        findNavController().navigate(R.id.action_companyFragment_to_companyRegisterFragment)
    }
    
    private fun editCompany(company: Company) {
        val bundle = Bundle().apply {
            putString("companyId", company.id)
        }
        findNavController().navigate(R.id.action_companyFragment_to_companyEditFragment, bundle)
    }
    
    private fun confirmDeleteCompany(company: Company) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar Compañía")
            .setMessage("¿Estás seguro de que deseas eliminar \"${company.name}\"?")
            .setPositiveButton("Eliminar") { _, _ ->
                deleteCompany(company)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun deleteCompany(company: Company) {
        // TODO: Delete company from repository/database
        companies.remove(company)
        companyAdapter.removeCompany(company)
        updateUI()
    }
    
    private fun viewCompanyDetails(company: Company) {
        val bundle = Bundle().apply {
            putString("companyId", company.id)
        }
        findNavController().navigate(R.id.action_companyFragment_to_companyDetailFragment, bundle)
    }
}