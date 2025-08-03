package com.example.hirelink_2025.view.ui.fragments.ads

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.hirelink_2025.R
import com.example.hirelink_2025.models.Company
import com.example.hirelink_2025.models.CompanySize
import com.example.hirelink_2025.viewmodels.CompanyViewModel
import com.example.hirelink_2025.viewmodels.ViewModelFactory
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.Calendar

class CompanyEditFragment : Fragment() {
    
    private lateinit var toolbar: MaterialToolbar
    private lateinit var loadingProgressBar: ProgressBar
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
    private lateinit var updateCompanyButton: MaterialButton
    private lateinit var discardChangesButton: MaterialButton
    
    // ViewModel
    private val viewModel: CompanyViewModel by viewModels { ViewModelFactory() }
    
    private var currentCompany: Company? = null
    private var selectedLogoUri: Uri? = null
    private var originalCompany: Company? = null
    private var currentUserId: String? = null
    
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
        return inflater.inflate(R.layout.fragment_company_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        Log.d("CompanyEditFragment", "Fragment created")
        
        initViews(view)
        setupClickListeners()
        setupObservers()
        getCurrentUserAndLoadCompany()
    }
    
    private fun initViews(view: View) {
        toolbar = view.findViewById(R.id.toolbar)
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar)
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
        updateCompanyButton = view.findViewById(R.id.updateCompanyButton)
        discardChangesButton = view.findViewById(R.id.discardChangesButton)
    }
    
    private fun setupClickListeners() {
        toolbar.setNavigationOnClickListener {
            if (hasUnsavedChanges()) {
                showDiscardChangesDialog()
            } else {
                findNavController().navigateUp()
            }
        }
        
        uploadLogoButton.setOnClickListener {
            selectLogo()
        }
        
        updateCompanyButton.setOnClickListener {
            if (validateForm()) {
                updateCompany()
            }
        }
        
        discardChangesButton.setOnClickListener {
            showDiscardChangesDialog()
        }
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                
                // Observar estado de operaciones
                launch {
                    viewModel.operationInProgress.collect { inProgress ->
                        showLoading(inProgress)
                    }
                }
                
                // Observar errores
                launch {
                    viewModel.error.collect { error ->
                        error?.let {
                            Log.e("CompanyEditFragment", "Error: $it")
                            showError(it)
                            viewModel.clearError()
                        }
                    }
                }
                
                // Observar resultados de operaciones
                launch {
                    viewModel.operationResult.collect { result ->
                        result?.let {
                            Log.d("CompanyEditFragment", "Operation result: $it")
                            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                            viewModel.clearOperationResult()
                        }
                    }
                }
            }
        }
    }
    
    private fun getCurrentUserAndLoadCompany() {
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        
        if (currentUserId == null) {
            Log.w("CompanyEditFragment", "No authenticated user found")
            showError("Usuario no autenticado")
            findNavController().navigateUp()
            return
        }
        
        // Obtener ID de la compañía desde argumentos
        val companyId = arguments?.getString("companyId") ?: ""
        Log.d("CompanyEditFragment", "Received companyId from arguments: '$companyId'")
        
        if (companyId.isNotEmpty()) {
            Log.d("CompanyEditFragment", "Loading company: $companyId")
            loadCompanyById(companyId)
        } else {
            Log.e("CompanyEditFragment", "No company ID provided in arguments")
            showError("ID de compañía no encontrado")
            findNavController().navigateUp()
        }
    }
    
    private fun loadCompanyById(companyId: String) {
        viewModel.getCompanyById(companyId) { company ->
            if (company != null) {
                Log.d("CompanyEditFragment", "Company loaded: ${company.name}")
                currentCompany = company
                originalCompany = company.copy()
                try {
                    populateFields()
                } catch (e: Exception) {
                    Log.e("CompanyEditFragment", "Error populating fields: ${e.message}")
                    showError("Error al cargar los datos de la compañía")
                    findNavController().navigateUp()
                }
            } else {
                Log.e("CompanyEditFragment", "Company not found")
                showError("Compañía no encontrada")
                findNavController().navigateUp()
            }
        }
    }
    
    
    private fun populateFields() {
        currentCompany?.let { company ->
            companyNameEditText.setText(company.name)
            companyTypeEditText.setText(company.type)
            companyDescriptionEditText.setText(company.description)
            
            // Convert CompanySize enum to display string
            val sizeText = when (company.size) {
                com.example.hirelink_2025.models.CompanySize.STARTUP -> "1-10"
                com.example.hirelink_2025.models.CompanySize.SMALL -> "11-50"
                com.example.hirelink_2025.models.CompanySize.MEDIUM -> "51-200"
                com.example.hirelink_2025.models.CompanySize.LARGE -> "201-1000"
                com.example.hirelink_2025.models.CompanySize.ENTERPRISE -> "1000+"
            }
            companySizeEditText.setText(sizeText)
            
            if (company.foundedYear > 0) {
                foundedYearEditText.setText(company.foundedYear.toString())
            }
            addressEditText.setText(company.address)
            cityEditText.setText(company.city)
            countryEditText.setText(company.country)
            phoneEditText.setText(company.phone)
            emailEditText.setText(company.email)
            websiteEditText.setText(company.website)
            
            // Set logo if available
            if (company.logoUrl.isNotEmpty()) {
                selectedLogoUri = Uri.parse(company.logoUrl)
                updateLogoButton()
            }
        }
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
            uploadLogoButton.text = "Cambiar logo de la compañía"
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
        if (!email.isNullOrBlank() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.error = "El formato del email no es válido"
            isValid = false
        }
        
        // Validate website if provided
        val website = websiteEditText.text?.toString()
        if (!website.isNullOrBlank() && !Patterns.WEB_URL.matcher(website).matches()) {
            websiteInputLayout.error = "El formato del sitio web no es válido"
            isValid = false
        }
        
        // Validate founded year if provided
        val foundedYearText = foundedYearEditText.text?.toString()
        if (!foundedYearText.isNullOrBlank()) {
            try {
                val year = foundedYearText.toInt()
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
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
    
    private fun updateCompany() {
        currentCompany?.let { company ->
            val updatedCompany = company.copy(
                name = companyNameEditText.text.toString().trim(),
                type = companyTypeEditText.text.toString().trim(),
                description = companyDescriptionEditText.text.toString().trim(),
                size = convertStringToCompanySize(companySizeEditText.text.toString().trim()),
                foundedYear = foundedYearEditText.text?.toString()?.toIntOrNull() ?: 0,
                address = addressEditText.text.toString().trim(),
                city = cityEditText.text.toString().trim(),
                country = countryEditText.text.toString().trim(),
                phone = phoneEditText.text.toString().trim(),
                email = emailEditText.text.toString().trim(),
                website = websiteEditText.text.toString().trim(),
                logoUrl = selectedLogoUri?.toString() ?: company.logoUrl,
                updatedAt = System.currentTimeMillis()
            )
            
            // Usar ViewModel para actualizar la compañía
            Log.d("CompanyEditFragment", "Updating company: ${updatedCompany.name}")
            viewModel.updateCompany(updatedCompany) { success ->
                if (success) {
                    Log.d("CompanyEditFragment", "Company updated successfully")
                    findNavController().navigateUp()
                } else {
                    Log.e("CompanyEditFragment", "Failed to update company")
                }
            }
        }
    }
    
    private fun hasUnsavedChanges(): Boolean {
        val current = getCurrentCompanyFromForm()
        return current != originalCompany
    }
    
    private fun getCurrentCompanyFromForm(): Company? {
        return currentCompany?.copy(
            name = companyNameEditText.text.toString().trim(),
            type = companyTypeEditText.text.toString().trim(),
            description = companyDescriptionEditText.text.toString().trim(),
            size = convertStringToCompanySize(companySizeEditText.text.toString().trim()),
            foundedYear = foundedYearEditText.text?.toString()?.toIntOrNull() ?: 0,
            address = addressEditText.text.toString().trim(),
            city = cityEditText.text.toString().trim(),
            country = countryEditText.text.toString().trim(),
            phone = phoneEditText.text.toString().trim(),
            email = emailEditText.text.toString().trim(),
            website = websiteEditText.text.toString().trim(),
            logoUrl = selectedLogoUri?.toString() ?: currentCompany?.logoUrl ?: ""
        )
    }
    
    private fun showDiscardChangesDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Descartar cambios")
            .setMessage("¿Estás seguro de que deseas descartar los cambios realizados?")
            .setPositiveButton("Descartar") { _, _ ->
                findNavController().navigateUp()
            }
            .setNegativeButton("Continuar editando", null)
            .show()
    }
    
    private fun showLoading(show: Boolean) {
        loadingProgressBar.visibility = if (show) View.VISIBLE else View.GONE
        updateCompanyButton.isEnabled = !show
        discardChangesButton.isEnabled = !show
    }
    
    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
    
    private fun convertStringToCompanySize(sizeText: String): CompanySize {
        return when (sizeText) {
            "1-10" -> CompanySize.STARTUP
            "11-50" -> CompanySize.SMALL
            "51-200" -> CompanySize.MEDIUM
            "201-1000" -> CompanySize.LARGE
            "1000+" -> CompanySize.ENTERPRISE
            else -> CompanySize.SMALL // Default fallback
        }
    }
}