package com.example.hirelink_2025.view.ui.fragments.ads

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.hirelink_2025.R
import com.example.hirelink_2025.models.Company
import com.example.hirelink_2025.view.adapter.CompanySelectionAdapter
import com.example.hirelink_2025.viewmodels.ads.JobRegisterViewModel
import com.example.hirelink_2025.viewmodels.ViewModelFactory
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.*

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
    
    // Additional fields
    private lateinit var vacanciesEditText: TextInputEditText
    private lateinit var employmentTypeEditText: TextInputEditText
    private lateinit var modalityDropdown: MaterialAutoCompleteTextView
    private lateinit var dateEditText: TextInputEditText
    private lateinit var positionEditText: TextInputEditText
    private lateinit var uploadImageButton: MaterialButton
    private lateinit var selectLocationButton: MaterialButton
    
    private lateinit var publishButton: MaterialButton
    
    // ViewModel con FirestoreService directo (sin repositories)
    private val viewModel: JobRegisterViewModel by viewModels { ViewModelFactory() }
    
    private var userCompanies: List<Company> = emptyList()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_ads_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupClickListeners()
        setupObservers()
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
        
        // Additional fields
        vacanciesEditText = view.findViewById(R.id.vacanciesEditText)
        employmentTypeEditText = view.findViewById(R.id.employmentTypeEditText)
        modalityDropdown = view.findViewById(R.id.modalityDropdown)
        dateEditText = view.findViewById(R.id.dateEditText)
        positionEditText = view.findViewById(R.id.positionEditText)
        uploadImageButton = view.findViewById(R.id.uploadImageButton)
        selectLocationButton = view.findViewById(R.id.selectLocationButton)
        
        publishButton = view.findViewById(R.id.publishButton)
    }
    
    private fun setupObservers() {
        // Observar compañías del usuario
        viewModel.userCompanies.observe(viewLifecycleOwner, Observer { companies ->
            userCompanies = companies
            setupCompanyDropdown()
        })
        
        // Observar compañía seleccionada
        viewModel.selectedCompany.observe(viewLifecycleOwner, Observer { company ->
            if (company != null) {
                autoFillCompanyFields(company)
                autoFillNoticeLayout.visibility = View.VISIBLE
                Toast.makeText(requireContext(), "Campos completados automáticamente", Toast.LENGTH_SHORT).show()
            } else {
                clearCompanyFields()
                autoFillNoticeLayout.visibility = View.GONE
            }
        })
        
        // Observar estado de carga
        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            publishButton.isEnabled = !isLoading
            publishButton.text = if (isLoading) "Publicando..." else "Publicar Anuncio"
        })
        
        // Observar errores
        viewModel.errorMessage.observe(viewLifecycleOwner, Observer { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
            }
        })
        
        // Observar errores de validación
        viewModel.validationErrors.observe(viewLifecycleOwner, Observer { errors ->
            if (errors.isNotEmpty()) {
                val errorMessage = errors.joinToString("\n")
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
            }
        })
        
        // Observar éxito en el registro
        viewModel.isSuccess.observe(viewLifecycleOwner, Observer { isSuccess ->
            if (isSuccess) {
                Toast.makeText(requireContext(), "Anuncio publicado exitosamente", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
                viewModel.resetSuccess()
            }
        })
        
        // Configurar dropdowns
        setupModalityDropdown()
        setupEmploymentTypeDropdown()
    }
    
    private fun setupClickListeners() {
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        
        companySelectDropdown.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val company = userCompanies[position]
            viewModel.selectCompany(company)
            companySelectDropdown.setText(company.name, false)
        }
        
        clearSelectionButton.setOnClickListener {
            viewModel.selectCompany(null)
            companySelectDropdown.setText("", false)
            Toast.makeText(requireContext(), "Selección de compañía eliminada", Toast.LENGTH_SHORT).show()
        }
        
        publishButton.setOnClickListener {
            publishJobAd()
        }
        
        // Date picker
        dateEditText.setOnClickListener {
            showDatePicker()
        }
        
        // Image upload (placeholder)
        uploadImageButton.setOnClickListener {
            Toast.makeText(requireContext(), "Selección de imagen - Próximamente", Toast.LENGTH_SHORT).show()
        }
        
        // Location selection (placeholder)
        selectLocationButton.setOnClickListener {
            Toast.makeText(requireContext(), "Selección de ubicación - Próximamente", Toast.LENGTH_SHORT).show()
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
    
    private fun setupModalityDropdown() {
        val modalityOptions = viewModel.getModalityOptions()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, modalityOptions)
        modalityDropdown.setAdapter(adapter)
        
        modalityDropdown.setOnItemClickListener { _, _, position, _ ->
            modalityDropdown.setText(modalityOptions[position], false)
        }
    }
    
    private fun setupEmploymentTypeDropdown() {
        val employmentOptions = viewModel.getEmploymentTypeOptions()
        // Para este campo usaremos un EditText normal, pero podríamos convertirlo a dropdown
        // employmentTypeEditText puede mostrar sugerencias si es necesario
    }
    
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                dateEditText.setText(selectedDate)
            },
            year, month, day
        )
        
        // Set minimum date to today
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
    }
    
    
    private fun autoFillCompanyFields(company: Company) {
        nameCompanyEditText.setText(company.name)
        phoneEditText.setText(company.phone)
        emailEditText.setText(company.email)
        websiteEditText.setText(company.website)
        aboutCompanyEditText.setText(company.description)
    }
    
    private fun clearCompanyFields() {
        nameCompanyEditText.setText("")
        phoneEditText.setText("")
        emailEditText.setText("")
        websiteEditText.setText("")
        aboutCompanyEditText.setText("")
    }
    
    
    private fun publishJobAd() {
        viewModel.clearErrors()
        
        // Obtener datos del formulario
        val title = titleEditText.text.toString()
        val aboutCompany = aboutCompanyEditText.text.toString()
        val aboutJob = aboutJobEditText.text.toString()
        val skills = skillsEditText.text.toString()
        val vacancies = try {
            vacanciesEditText.text.toString().toIntOrNull() ?: 1
        } catch (e: Exception) { 1 }
        val employmentType = employmentTypeEditText.text.toString().ifBlank { "Tiempo Completo" }
        val modality = modalityDropdown.text.toString().ifBlank { "Presencial" }
        val deadline = dateEditText.text.toString()
        val offerSalary = positionEditText.text.toString().ifBlank { "Por negociar" }
        val phone = phoneEditText.text.toString()
        val email = emailEditText.text.toString()
        val website = websiteEditText.text.toString()
        val selectedLocation = "Lima, Perú" // Por ahora fijo, luego se puede mejorar
        
        viewModel.registerJob(
            title = title,
            aboutCompany = aboutCompany,
            aboutJob = aboutJob,
            skills = skills,
            vacancies = vacancies,
            employmentType = employmentType,
            modality = modality,
            deadline = deadline,
            offerSalary = offerSalary,
            phone = phone,
            email = email,
            website = website,
            selectedLocation = selectedLocation,
            imageUrl = null // Por ahora null, luego se puede agregar upload de imagen
        )
    }
}