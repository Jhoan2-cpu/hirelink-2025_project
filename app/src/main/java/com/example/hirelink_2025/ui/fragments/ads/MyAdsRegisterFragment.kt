package com.example.hirelink_2025.ui.fragments.ads

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.hirelink_2025.R
import com.example.hirelink_2025.models.Company
import com.example.hirelink_2025.repository.CompanyRepository
import com.example.hirelink_2025.ui.adapters.CompanySelectionAdapter
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyAdsRegisterFragment : Fragment() {
    
    private lateinit var toolbar: MaterialToolbar
    private lateinit var companySelectInputLayout: TextInputLayout
    private lateinit var companySelectDropdown: MaterialAutoCompleteTextView
    private lateinit var autoFillNoticeLayout: LinearLayout
    private lateinit var clearSelectionButton: MaterialButton
    
    // Company fields that will be auto-filled
    private lateinit var nameCompanyEditText: TextInputEditText
    private lateinit var phoneEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var websiteEditText: TextInputEditText
    
    // Job fields
    private lateinit var titleEditText: TextInputEditText
    private lateinit var aboutCompanyEditText: TextInputEditText
    private lateinit var aboutJobEditText: TextInputEditText
    private lateinit var skillsEditText: TextInputEditText
    
    private lateinit var publishButton: MaterialButton
    
    private var selectedCompany: Company? = null
    private var userCompanies: List<Company> = emptyList()
    private lateinit var companyRepository: CompanyRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_ads_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupRepository()
        setupClickListeners()
        loadUserCompanies()
    }
    
    private fun initViews(view: View) {
        toolbar = view.findViewById(R.id.toolbar)
        companySelectInputLayout = view.findViewById(R.id.companySelectInputLayout)
        companySelectDropdown = view.findViewById(R.id.companySelectDropdown)
        autoFillNoticeLayout = view.findViewById(R.id.autoFillNoticeLayout)
        clearSelectionButton = view.findViewById(R.id.clearSelectionButton)
        
        // Company fields
        nameCompanyEditText = view.findViewById(R.id.nameCompany)
        phoneEditText = view.findViewById(R.id.phoneEditText)
        emailEditText = view.findViewById(R.id.emailEditText)
        websiteEditText = view.findViewById(R.id.websiteEditText)
        
        // Job fields
        titleEditText = view.findViewById(R.id.titleEditText)
        aboutCompanyEditText = view.findViewById(R.id.aboutCompanyEditText)
        aboutJobEditText = view.findViewById(R.id.aboutJobEditText)
        skillsEditText = view.findViewById(R.id.skillsEditText)
        
        publishButton = view.findViewById(R.id.publishButton)
    }
    
    private fun setupRepository() {
        companyRepository = CompanyRepository()
    }
    
    private fun setupClickListeners() {
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        
        companySelectDropdown.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val company = userCompanies[position]
            selectCompany(company)
        }
        
        clearSelectionButton.setOnClickListener {
            clearCompanySelection()
        }
        
        publishButton.setOnClickListener {
            if (validateForm()) {
                publishJobAd()
            }
        }
    }
    
    private fun loadUserCompanies() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // TODO: Filter by current user ID
                val companies = companyRepository.getCompaniesByOwner("user1")
                userCompanies = companies
                
                withContext(Dispatchers.Main) {
                    setupCompanyDropdown()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error al cargar compañías", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun setupCompanyDropdown() {
        if (userCompanies.isEmpty()) {
            companySelectInputLayout.visibility = View.GONE
            return
        }
        
        val adapter = CompanySelectionAdapter(requireContext(), userCompanies)
        companySelectDropdown.setAdapter(adapter)
        
        companySelectInputLayout.visibility = View.VISIBLE
    }
    
    private fun selectCompany(company: Company) {
        selectedCompany = company
        companySelectDropdown.setText(company.name, false)
        
        // Auto-fill company fields
        autoFillCompanyFields(company)
        
        // Show auto-fill notice
        autoFillNoticeLayout.visibility = View.VISIBLE
        
        Toast.makeText(requireContext(), "Campos completados automáticamente", Toast.LENGTH_SHORT).show()
    }
    
    private fun autoFillCompanyFields(company: Company) {
        nameCompanyEditText.setText(company.name)
        phoneEditText.setText(company.phone)
        emailEditText.setText(company.email)
        websiteEditText.setText(company.website)
        aboutCompanyEditText.setText(company.description)
    }
    
    private fun clearCompanySelection() {
        selectedCompany = null
        companySelectDropdown.setText("", false)
        
        // Clear auto-filled fields
        nameCompanyEditText.setText("")
        phoneEditText.setText("")
        emailEditText.setText("")
        websiteEditText.setText("")
        aboutCompanyEditText.setText("")
        
        // Hide auto-fill notice
        autoFillNoticeLayout.visibility = View.GONE
        
        Toast.makeText(requireContext(), "Selección de compañía eliminada", Toast.LENGTH_SHORT).show()
    }
    
    private fun validateForm(): Boolean {
        var isValid = true
        
        // Validate job title
        if (titleEditText.text.isNullOrBlank()) {
            Toast.makeText(requireContext(), "El puesto es obligatorio", Toast.LENGTH_SHORT).show()
            isValid = false
        }
        
        // Validate company name (either selected or manually entered)
        if (nameCompanyEditText.text.isNullOrBlank()) {
            Toast.makeText(requireContext(), "El nombre de la empresa es obligatorio", Toast.LENGTH_SHORT).show()
            isValid = false
        }
        
        // Validate job description
        if (aboutJobEditText.text.isNullOrBlank()) {
            Toast.makeText(requireContext(), "La descripción del empleo es obligatoria", Toast.LENGTH_SHORT).show()
            isValid = false
        }
        
        return isValid
    }
    
    private fun publishJobAd() {
        // TODO: Create JobAd object and save to repository
        val jobTitle = titleEditText.text.toString().trim()
        val companyName = nameCompanyEditText.text.toString().trim()
        val jobDescription = aboutJobEditText.text.toString().trim()
        
        // Simulate publishing
        Toast.makeText(requireContext(), "Anuncio publicado exitosamente", Toast.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }
}