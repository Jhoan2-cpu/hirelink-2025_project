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
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.hirelink_2025.R
import com.example.hirelink_2025.models.Company
import com.example.hirelink_2025.viewmodels.CompanyRegisterViewModel
import com.example.hirelink_2025.viewmodels.CompanyRegisterUiState
import com.example.hirelink_2025.viewmodels.ViewModelFactory
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import android.widget.AutoCompleteTextView
import com.google.firebase.auth.FirebaseAuth
import com.bumptech.glide.Glide
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import java.util.Calendar

class CompanyRegisterFragment : Fragment() {
    private var selectedLatLng: LatLng? = null

    private lateinit var toolbar: MaterialToolbar
    private lateinit var companyNameInputLayout: TextInputLayout
    private lateinit var companyNameEditText: TextInputEditText
    private lateinit var companyTypeInputLayout: TextInputLayout
    private lateinit var companyTypeEditText: TextInputEditText
    private lateinit var companyDescriptionInputLayout: TextInputLayout
    private lateinit var companyDescriptionEditText: TextInputEditText
    private lateinit var companySizeInputLayout: TextInputLayout
    private lateinit var companySizeEditText: AutoCompleteTextView
    private lateinit var foundedYearInputLayout: TextInputLayout
    private lateinit var foundedYearEditText: TextInputEditText
    private lateinit var ubicationInputLayout: TextInputLayout//NEW
    private lateinit var ubicationEditText: TextInputEditText//NEW
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
    private lateinit var logoPreviewImageView: ImageView
    private lateinit var registerCompanyButton: MaterialButton
    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var openMapButton: MaterialButton
    
    // ViewModel con factory
    private val viewModel: CompanyRegisterViewModel by viewModels { ViewModelFactory() }
    
    private var selectedLogoUri: Uri? = null
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
        return inflater.inflate(R.layout.fragment_company_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        Log.d("CompanyRegisterFragment", "Fragment created")
        
        initViews(view)
        setupClickListeners()
        setupValidation()
        observeViewModel()
        
        // Obtener usuario actual
        getCurrentUser()

        val navBackStackEntry = findNavController().currentBackStackEntry
        val savedStateHandle = navBackStackEntry?.savedStateHandle

        savedStateHandle?.getLiveData<LatLng>("selected_location")
            ?.observe(viewLifecycleOwner) { latLng ->
                selectedLatLng = latLng
                ubicationEditText.setText("Lat: ${latLng.latitude}, Lng: ${latLng.longitude}")
            }

    }
    
    private fun initViews(view: View) {
        openMapButton = view.findViewById(R.id.openMapButton)//Nuevo

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
        ubicationInputLayout = view.findViewById(R.id.ubicationInputLayout)
        ubicationEditText = view.findViewById(R.id.ubicationEditText)
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
        logoPreviewImageView = view.findViewById(R.id.logoPreviewImageView)
        registerCompanyButton = view.findViewById(R.id.registerCompanyButton)
        
        // Buscar progress indicator, crear uno si no existe en el XML  
        progressIndicator = view.findViewById<CircularProgressIndicator>(R.id.progressIndicator) 
            ?: CircularProgressIndicator(requireContext()).apply {
                visibility = View.GONE
                // El progressIndicator no existe en el XML, usar este fallback
            }
    }
    
    private fun setupClickListeners() {
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        
        uploadLogoButton.setOnClickListener {
            selectLogo()
        }
        
        registerCompanyButton.setOnClickListener {
            if (validateBasicForm()) {
                registerCompany()
            }
        }

        openMapButton.setOnClickListener {
            findNavController().navigate(R.id.mapPickerFragment)
        }


    }
    
    private fun setupValidation() {
        // Set default country
        countryEditText.setText("Perú")
        
        // Setup company size dropdown
        setupCompanySizeDropdown()
    }
    
    private fun setupCompanySizeDropdown() {
        val companySizes = viewModel.getCompanySizes()
        val adapter = android.widget.ArrayAdapter(
            requireContext(), 
            android.R.layout.simple_dropdown_item_1line, 
            companySizes
        )
        companySizeEditText.apply {
            setAdapter(adapter)
            setText(companySizes[0], false) // Set default to first option (1-10)
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                
                // Observar estado de la UI
                launch {
                    viewModel.uiState.collect { uiState ->
                        Log.d("CompanyRegisterFragment", "UI State updated: loading=${uiState.isLoading}, success=${uiState.isSuccess}")
                        updateUI(uiState)
                    }
                }
            }
        }
    }
    
    private fun getCurrentUser() {
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserId == null) {
            Log.w("CompanyRegisterFragment", "No authenticated user found")
            Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_LONG).show()
            findNavController().navigateUp()
        } else {
            Log.d("CompanyRegisterFragment", "Current user ID: $currentUserId")
        }
    }
    
    private fun updateUI(uiState: CompanyRegisterUiState) {
        // Mostrar/ocultar loading
        if (uiState.isLoading) {
            progressIndicator.visibility = View.VISIBLE
            registerCompanyButton.isEnabled = false
        } else {
            progressIndicator.visibility = View.GONE
            registerCompanyButton.isEnabled = true
        }
        
        // Manejar errores de validación
        if (uiState.validationErrors.isNotEmpty()) {
            displayValidationErrors(uiState.validationErrors)
        }
        
        // Manejar errores generales
        uiState.error?.let { error ->
            Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
            viewModel.clearErrors()
        }
        
        // Manejar éxito
        if (uiState.isSuccess) {
            Log.d("CompanyRegisterFragment", "Company registered successfully")
            Toast.makeText(requireContext(), "Compañía registrada exitosamente", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }
    
    private fun displayValidationErrors(errors: List<String>) {
        clearErrors()
        
        errors.forEach { error ->
            when {
                error.contains("nombre", ignoreCase = true) -> {
                    companyNameInputLayout.error = error
                }
                error.contains("tipo", ignoreCase = true) || error.contains("industria", ignoreCase = true) -> {
                    companyTypeInputLayout.error = error
                }
                error.contains("descripción", ignoreCase = true) -> {
                    companyDescriptionInputLayout.error = error
                }
                error.contains("dirección", ignoreCase = true) -> {
                    addressInputLayout.error = error
                }
                error.contains("ubicación", ignoreCase = true) -> {
                    ubicationInputLayout.error = error
                }
                error.contains("ciudad", ignoreCase = true) -> {
                    cityInputLayout.error = error
                }
                error.contains("email", ignoreCase = true) -> {
                    emailInputLayout.error = error
                }
                error.contains("teléfono", ignoreCase = true) -> {
                    phoneInputLayout.error = error
                }
                error.contains("sitio web", ignoreCase = true) || error.contains("website", ignoreCase = true) -> {
                    websiteInputLayout.error = error
                }
                else -> {
                    // Mostrar error general
                    Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                }
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
            
            // Mostrar vista previa del logo
            logoPreviewImageView.visibility = View.VISIBLE
            Glide.with(this)
                .load(selectedLogoUri)
                .fitCenter()
                .placeholder(R.drawable.ic_group)
                .error(R.drawable.ic_group)
                .into(logoPreviewImageView)
        } else {
            uploadLogoButton.text = "Seleccionar logo de la compañía"
            uploadLogoButton.setIconResource(R.drawable.ic_image)
            logoPreviewImageView.visibility = View.GONE
        }
    }
    
    private fun validateBasicForm(): Boolean {
        var isValid = true
        
        // Clear previous errors
        clearErrors()
        
        // Validate required fields only (let ViewModel handle comprehensive validation)
        if (companyNameEditText.text.isNullOrBlank()) {
            companyNameInputLayout.error = "El nombre de la compañía es obligatorio"
            isValid = false
        }
        
        if (companyTypeEditText.text.isNullOrBlank()) {
            companyTypeInputLayout.error = "El tipo de industria es obligatorio"
            isValid = false
        }
        
        if (companyDescriptionEditText.text.isNullOrBlank()) {
            companyDescriptionInputLayout.error = "La descripción es obligatoria"
            isValid = false
        }
        
        // Validate founded year format if provided
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
        listOf(
            companyNameInputLayout,
            companyTypeInputLayout,
            companyDescriptionInputLayout,
            companySizeInputLayout,
            foundedYearInputLayout,
            addressInputLayout,
            ubicationInputLayout,
            cityInputLayout,
            countryInputLayout,
            emailInputLayout,
            phoneInputLayout,
            websiteInputLayout
        ).forEach { it.error = null }
    }
    
    private fun registerCompany() {
        currentUserId?.let { ownerId ->
            Log.d("CompanyRegisterFragment", "Registering company for user: $ownerId")
            
            // Preparar datos del formulario
            val name = companyNameEditText.text.toString().trim()
            val type = companyTypeEditText.text.toString().trim()
            val description = companyDescriptionEditText.text.toString().trim()
            val size = companySizeEditText.text.toString().trim().ifEmpty { "1-10" }
            val foundedYear = foundedYearEditText.text?.toString()?.toIntOrNull() ?: 0
            val address = addressEditText.text.toString().trim()
            val ubication = selectedLatLng?.let { "Lat: ${it.latitude}, Lng: ${it.longitude}" } ?: ""
            val city = cityEditText.text.toString().trim()
            val country = countryEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val phone = phoneEditText.text.toString().trim()
            val website = websiteEditText.text.toString().trim()
            
            // Usar el ViewModel para registrar la compañía
            viewModel.registerCompany(
                name = name,
                type = type,
                description = description,
                size = size,
                foundedYear = foundedYear,
                address = address,
                ubication = ubication,
                city = city,
                country = country,
                email = email,
                phone = phone,
                website = website,
                logoUri = selectedLogoUri,
                ownerId = ownerId
            )
        } ?: run {
            Log.e("CompanyRegisterFragment", "No user ID available")
            Toast.makeText(requireContext(), "Error: Usuario no identificado", Toast.LENGTH_LONG).show()
        }
    }
}