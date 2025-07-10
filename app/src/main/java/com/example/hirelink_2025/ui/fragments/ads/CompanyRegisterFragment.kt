package com.example.hirelink_2025.ui.fragments.ads

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.example.hirelink_2025.R
import com.example.hirelink_2025.models.Company
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.UUID

class CompanyRegisterFragment : Fragment() {
    
    private lateinit var toolbar: MaterialToolbar
    private lateinit var companyNameInputLayout: TextInputLayout
    private lateinit var companyNameEditText: TextInputEditText
    private lateinit var companyTypeInputLayout: TextInputLayout
    private lateinit var companyTypeEditText: TextInputEditText
    private lateinit var companyDescriptionInputLayout: TextInputLayout
    private lateinit var companyDescriptionEditText: TextInputEditText
    private lateinit var companySizeInputLayout: TextInputLayout
    private lateinit var companySizeEditText: TextInputEditText
    private lateinit var foundedYearInputLayout: TextInputLayout
    private lateinit var foundedYearEditText: TextInputEditText
    private lateinit var addressInputLayout: TextInputLayout
    private lateinit var addressEditText: TextInputEditText
    private lateinit var cityInputLayout: TextInputLayout
    private lateinit var cityEditText: TextInputEditText
    private lateinit var countryInputLayout: TextInputLayout
    private lateinit var countryEditText: TextInputEditText
    private lateinit var phoneInputLayout: TextInputLayout
    private lateinit var phoneEditText: TextInputEditText
    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var emailEditText: TextInputEditText
    private lateinit var websiteInputLayout: TextInputLayout
    private lateinit var websiteEditText: TextInputEditText
    private lateinit var uploadLogoButton: MaterialButton
    private lateinit var registerCompanyButton: MaterialButton
    
    private var selectedLogoUri: Uri? = null
    
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedLogoUri = result.data?.data
            updateLogoButton()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_company_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupClickListeners()
        setupValidation()
    }
    
    private fun initViews(view: View) {
        toolbar = view.findViewById(R.id.toolbar)
        companyNameInputLayout = view.findViewById(R.id.companyNameInputLayout)
        companyNameEditText = view.findViewById(R.id.companyNameEditText)
        companyTypeInputLayout = view.findViewById(R.id.companyTypeInputLayout)
        companyTypeEditText = view.findViewById(R.id.companyTypeEditText)
        companyDescriptionInputLayout = view.findViewById(R.id.companyDescriptionInputLayout)
        companyDescriptionEditText = view.findViewById(R.id.companyDescriptionEditText)
        companySizeInputLayout = view.findViewById(R.id.companySizeInputLayout)
        companySizeEditText = view.findViewById(R.id.companySizeEditText)
        foundedYearInputLayout = view.findViewById(R.id.foundedYearInputLayout)
        foundedYearEditText = view.findViewById(R.id.foundedYearEditText)
        addressInputLayout = view.findViewById(R.id.addressInputLayout)
        addressEditText = view.findViewById(R.id.addressEditText)
        cityInputLayout = view.findViewById(R.id.cityInputLayout)
        cityEditText = view.findViewById(R.id.cityEditText)
        countryInputLayout = view.findViewById(R.id.countryInputLayout)
        countryEditText = view.findViewById(R.id.countryEditText)
        phoneInputLayout = view.findViewById(R.id.phoneInputLayout)
        phoneEditText = view.findViewById(R.id.phoneEditText)
        emailInputLayout = view.findViewById(R.id.emailInputLayout)
        emailEditText = view.findViewById(R.id.emailEditText)
        websiteInputLayout = view.findViewById(R.id.websiteInputLayout)
        websiteEditText = view.findViewById(R.id.websiteEditText)
        uploadLogoButton = view.findViewById(R.id.uploadLogoButton)
        registerCompanyButton = view.findViewById(R.id.registerCompanyButton)
    }
    
    private fun setupClickListeners() {
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        
        uploadLogoButton.setOnClickListener {
            selectLogo()
        }
        
        registerCompanyButton.setOnClickListener {
            if (validateForm()) {
                registerCompany()
            }
        }
    }
    
    private fun setupValidation() {
        // Set default country
        countryEditText.setText("Perú")
    }
    
    private fun selectLogo() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        imagePickerLauncher.launch(intent)
    }
    
    private fun updateLogoButton() {
        if (selectedLogoUri != null) {
            uploadLogoButton.text = "Logo seleccionado"
            uploadLogoButton.setIconResource(R.drawable.ic_check)
        } else {
            uploadLogoButton.text = "Seleccionar logo de la compañía"
            uploadLogoButton.setIconResource(R.drawable.ic_image)
        }
    }
    
    private fun validateForm(): Boolean {
        var isValid = true
        
        // Clear previous errors
        clearErrors()
        
        // Validate company name
        if (companyNameEditText.text.isNullOrBlank()) {
            companyNameInputLayout.error = "El nombre de la compañía es obligatorio"
            isValid = false
        }
        
        // Validate company type
        if (companyTypeEditText.text.isNullOrBlank()) {
            companyTypeInputLayout.error = "El tipo de industria es obligatorio"
            isValid = false
        }
        
        // Validate company description
        if (companyDescriptionEditText.text.isNullOrBlank()) {
            companyDescriptionInputLayout.error = "La descripción es obligatoria"
            isValid = false
        }
        
        // Validate city
        if (cityEditText.text.isNullOrBlank()) {
            cityInputLayout.error = "La ciudad es obligatoria"
            isValid = false
        }
        
        // Validate country
        if (countryEditText.text.isNullOrBlank()) {
            countryInputLayout.error = "El país es obligatorio"
            isValid = false
        }
        
        // Validate email if provided
        val email = emailEditText.text?.toString()
        if (!email.isNullOrBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.error = "El formato del email no es válido"
            isValid = false
        }
        
        // Validate website if provided
        val website = websiteEditText.text?.toString()
        if (!website.isNullOrBlank() && !android.util.Patterns.WEB_URL.matcher(website).matches()) {
            websiteInputLayout.error = "El formato del sitio web no es válido"
            isValid = false
        }
        
        // Validate founded year if provided
        val foundedYearText = foundedYearEditText.text?.toString()
        if (!foundedYearText.isNullOrBlank()) {
            try {
                val year = foundedYearText.toInt()
                val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                if (year < 1800 || year > currentYear) {
                    foundedYearInputLayout.error = "El año debe estar entre 1800 y $currentYear"
                    isValid = false
                }
            } catch (e: NumberFormatException) {
                foundedYearInputLayout.error = "El año debe ser un número válido"
                isValid = false
            }
        }
        
        return isValid
    }
    
    private fun clearErrors() {
        companyNameInputLayout.error = null
        companyTypeInputLayout.error = null
        companyDescriptionInputLayout.error = null
        companySizeInputLayout.error = null
        foundedYearInputLayout.error = null
        addressInputLayout.error = null
        cityInputLayout.error = null
        countryInputLayout.error = null
        phoneInputLayout.error = null
        emailInputLayout.error = null
        websiteInputLayout.error = null
    }
    
    private fun registerCompany() {
        val company = Company(
            id = UUID.randomUUID().toString(),
            name = companyNameEditText.text.toString().trim(),
            type = companyTypeEditText.text.toString().trim(),
            description = companyDescriptionEditText.text.toString().trim(),
            size = companySizeEditText.text.toString().trim(),
            foundedYear = foundedYearEditText.text?.toString()?.toIntOrNull() ?: 0,
            address = addressEditText.text.toString().trim(),
            city = cityEditText.text.toString().trim(),
            country = countryEditText.text.toString().trim(),
            phone = phoneEditText.text.toString().trim(),
            email = emailEditText.text.toString().trim(),
            website = websiteEditText.text.toString().trim(),
            logoUrl = selectedLogoUri?.toString() ?: "",
            employeeCount = 0,
            activeJobsCount = 0,
            ownerId = "current_user_id" // TODO: Get from user session
        )
        
        // TODO: Save company to repository/database
        saveCompany(company)
    }
    
    private fun saveCompany(company: Company) {
        // TODO: Implement actual save logic with repository
        // For now, just show success message and navigate back
        
        Toast.makeText(requireContext(), "Compañía registrada exitosamente", Toast.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }
}