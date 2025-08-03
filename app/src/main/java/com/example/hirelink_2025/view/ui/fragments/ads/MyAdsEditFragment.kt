package com.example.hirelink_2025.view.ui.fragments.ads

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.hirelink_2025.databinding.FragmentMyAdsEditBinding
import com.example.hirelink_2025.models.Job
import com.example.hirelink_2025.models.JobStatus
import com.example.hirelink_2025.view.ui.utils.Constants
import com.example.hirelink_2025.view.ui.utils.showErrorSnackbar
import com.example.hirelink_2025.view.ui.utils.showSuccessSnackbar
import com.example.hirelink_2025.viewmodels.ads.MyAdsEditViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Fragment para editar anuncios laborales
 * Siguiendo arquitectura MVVM - Solo maneja la UI
 */
class MyAdsEditFragment : Fragment() {

    private var _binding: FragmentMyAdsEditBinding? = null
    private val binding get() = _binding!!

    // ViewModel usando by viewModels() delegate
    private val viewModel: MyAdsEditViewModel by viewModels()

    // Parámetros del fragmento
    private var jobId: String? = null
    private var jobTitle: String? = null

    // Adapters para dropdowns
    private lateinit var modalityAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            jobId = it.getString("job_id")
            jobTitle = it.getString("job_title")
            android.util.Log.d("MyAdsEditFragment", "Received job_id: $jobId, job_title: $jobTitle")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyAdsEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupObservers()
        loadJob()
    }

    /**
     * Configura la UI inicial
     */
    private fun setupUI() {
        setupToolbar()
        setupFormFields()
        setupClickListeners()
        setupDropdowns()
    }

    /**
     * Configura el toolbar
     */
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            // Por ahora, simplemente navegar hacia atrás
            // TODO: Implementar hasUnsavedChanges() en ViewModel si no existe
            findNavController().navigateUp()
        }
    }

    /**
     * Configura los campos del formulario - Solo campos editables
     */
    private fun setupFormFields() {
        // Configurar listeners para validación en tiempo real
        
        // Job Information Fields
        binding.titleEditText.doOnTextChanged { text, _, _, _ ->
            viewModel.updateTitle(text.toString())
        }

        binding.aboutJobEditText.doOnTextChanged { text, _, _, _ ->
            viewModel.updateAboutJob(text.toString())
        }

        binding.skillsEditText.doOnTextChanged { text, _, _, _ ->
            viewModel.updateRequirements(text.toString())
        }

        // Job Details Fields
        binding.vacanciesEditText.doOnTextChanged { text, _, _, _ ->
            viewModel.updateVacancies(text.toString())
        }

        binding.employmentTypeEditText.doOnTextChanged { text, _, _, _ ->
            viewModel.updateEmploymentType(text.toString())
        }

        // Configurar dropdown de modalidad
        binding.modalityDropdown.setOnItemClickListener { _, _, position, _ ->
            val selectedModality = modalityAdapter.getItem(position) ?: ""
            viewModel.updateModality(selectedModality)
        }

        // Salario
        binding.salaryEditText.doOnTextChanged { text, _, _, _ ->
            viewModel.updateSalary(text.toString())
        }

        // Fecha límite
        binding.deadlineEditText.setOnClickListener {
            showDatePicker()
        }
    }


    /**
     * Configura los listeners de clicks
     */
    private fun setupClickListeners() {
        // Botón guardar
        binding.saveAdButton.setOnClickListener {
            viewModel.saveJob()
        }

        // Botón publicar (si está en estado DRAFT)
        binding.publishButton.setOnClickListener {
            showPublishConfirmationDialog()
        }

        // Botón cambiar estado
        binding.statusButton.setOnClickListener {
            showStatusChangeDialog()
        }

        // Botón descartar cambios
        binding.discardButton.setOnClickListener {
            // Por ahora, mostrar diálogo directamente
            // TODO: Verificar hasUnsavedChanges() cuando esté implementado
            showDiscardChangesDialog()
        }
    }

    /**
     * Configura los dropdowns
     */
    private fun setupDropdowns() {
        // Adapter para modalidad
        modalityAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            mutableListOf<String>()
        )
        binding.modalityDropdown.setAdapter(modalityAdapter)

        // El tipo de empleo es campo de texto libre, no necesita adapter
    }

    /**
     * Configura los observadores del ViewModel
     */
    private fun setupObservers() {
        // Observar el job actual
        viewModel.currentJob.observe(viewLifecycleOwner) { job ->
            job?.let {
                updateJobInfo(it)
            }
        }

        // Observar campos del formulario - Solo campos editables
        viewModel.title.observe(viewLifecycleOwner) { title ->
            if (binding.titleEditText.text.toString() != title) {
                binding.titleEditText.setText(title)
            }
        }

        viewModel.aboutJob.observe(viewLifecycleOwner) { description ->
            if (binding.aboutJobEditText.text.toString() != description) {
                binding.aboutJobEditText.setText(description)
            }
        }

        viewModel.requirements.observe(viewLifecycleOwner) { requirements ->
            if (binding.skillsEditText.text.toString() != requirements) {
                binding.skillsEditText.setText(requirements)
            }
        }

        viewModel.vacancies.observe(viewLifecycleOwner) { vacancies ->
            if (binding.vacanciesEditText.text.toString() != vacancies) {
                binding.vacanciesEditText.setText(vacancies)
            }
        }

        viewModel.employmentType.observe(viewLifecycleOwner) { employmentType ->
            if (binding.employmentTypeEditText.text.toString() != employmentType) {
                binding.employmentTypeEditText.setText(employmentType)
            }
        }

        viewModel.modality.observe(viewLifecycleOwner) { modality ->
            if (binding.modalityDropdown.text.toString() != modality) {
                binding.modalityDropdown.setText(modality, false)
            }
        }

        viewModel.salary.observe(viewLifecycleOwner) { salary ->
            if (binding.salaryEditText.text.toString() != salary) {
                binding.salaryEditText.setText(salary)
            }
        }

        viewModel.deadline.observe(viewLifecycleOwner) { deadline ->
            if (binding.deadlineEditText.text.toString() != deadline) {
                binding.deadlineEditText.setText(deadline)
            }
        }

        // Observar errores de validación - Solo campos editables
        viewModel.titleError.observe(viewLifecycleOwner) { error ->
            binding.titleInputLayout.error = error
        }

        viewModel.aboutJobError.observe(viewLifecycleOwner) { error ->
            binding.aboutJobInputLayout.error = error
        }

        viewModel.requirementsError.observe(viewLifecycleOwner) { error ->
            binding.skillsInputLayout.error = error
        }

        viewModel.vacanciesError.observe(viewLifecycleOwner) { error ->
            binding.vacanciesInputLayout.error = error
        }

        viewModel.employmentTypeError.observe(viewLifecycleOwner) { error ->
            binding.employmentTypeInputLayout.error = error
        }

        viewModel.modalityError.observe(viewLifecycleOwner) { error ->
            binding.modalityInputLayout.error = error
        }

        viewModel.salaryError.observe(viewLifecycleOwner) { error ->
            binding.salaryInputLayout.error = error
        }

        viewModel.deadlineError.observe(viewLifecycleOwner) { error ->
            binding.deadlineInputLayout.error = error
        }

        // Observar validez del formulario
        viewModel.isFormValid.observe(viewLifecycleOwner) { isValid ->
            binding.saveAdButton.isEnabled = isValid && (viewModel.isLoading.value != true)
            binding.publishButton.isEnabled = isValid
        }

        // Observar estado de carga
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            updateLoadingState(isLoading)
        }

        // Observar opciones de modalidad
        viewModel.modalityOptions.observe(viewLifecycleOwner) { options ->
            modalityAdapter.clear()
            modalityAdapter.addAll(options)
        }

        // Observar mensajes de error
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                showErrorSnackbar(message) {
                    viewModel.clearErrorMessage()
                }
            }
        }

        // Observar mensajes de éxito
        viewModel.saveSuccess.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                showSuccessSnackbar(message)
                viewModel.clearSuccessMessage()
            }
        }

        // Observar evento de navegación
        viewModel.navigationEvent.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate) {
                findNavController().navigateUp()
                viewModel.onNavigationHandled()
            }
        }
    }

    /**
     * Carga el job para editar
     */
    private fun loadJob() {
        jobId?.let { id ->
            android.util.Log.d("MyAdsEditFragment", "Loading job with ID: $id")
            
            // Primera opción: Intentar usar datos del Bundle
            if (loadJobFromBundle()) {
                android.util.Log.d("MyAdsEditFragment", "Loaded job from Bundle")
                return
            }
            
            // Segunda opción: Cargar desde Firestore
            android.util.Log.d("MyAdsEditFragment", "Loading from Firestore")
            viewModel.loadJob(id)
        } ?: run {
            android.util.Log.e("MyAdsEditFragment", "Job ID is null")
            showErrorSnackbar("Error: ID del anuncio no encontrado")
            findNavController().navigateUp()
        }
    }
    
    /**
     * Intenta cargar el Job desde los datos del Bundle - Solo campos editables
     */
    private fun loadJobFromBundle(): Boolean {
        return arguments?.let { bundle ->
            try {
                val job = Job(
                    id = bundle.getString("job_id") ?: return false,
                    title = bundle.getString("job_title") ?: "",
                    modality = bundle.getString("job_modality") ?: "",
                    salary = bundle.getString("job_salary") ?: "",
                    requirements = bundle.getString("job_requirements")?.split("\n")?.filter { it.isNotBlank() } ?: emptyList(),
                    employmentType = bundle.getString("job_employment_type") ?: "",
                    aboutJob = bundle.getString("job_about_job") ?: "",
                    deadline = bundle.getString("job_deadline") ?: "",
                    vacancies = bundle.getInt("job_vacancies", 0),
                    companyId = bundle.getString("company_id") ?: "",
                    status = try {
                        JobStatus.valueOf(bundle.getString("job_status") ?: "ACTIVE")
                    } catch (e: Exception) {
                        JobStatus.ACTIVE
                    },
                    // Campos no editables con valores por defecto
                    postedDate = "",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    offerSalary = "" // No editable
                )
                
                // Simular que el Job se cargó exitosamente
                viewModel.setCurrentJob(job)
                true
            } catch (e: Exception) {
                android.util.Log.e("MyAdsEditFragment", "Error loading from bundle: ${e.message}")
                false
            }
        } ?: false
    }

    /**
     * Actualiza la información del job en la UI
     */
    private fun updateJobInfo(job: Job) {
        // Actualizar toolbar title
        binding.toolbar.title = "Editar: ${job.title}"

        // Mostrar estado actual
        binding.currentStatusText.text = "Estado: ${job.status.getDisplayText()}"

        // Configurar botones según el estado
        setupButtonsForStatus(job.status)
    }

    /**
     * Configura los botones según el estado del job
     */
    private fun setupButtonsForStatus(status: JobStatus) {
        binding.publishButton.apply {
            visibility = if (status == JobStatus.DRAFT) View.VISIBLE else View.GONE
        }

        binding.statusButton.apply {
            // Por ahora, ocultar hasta que se implemente getValidStatusTransitions()
            visibility = View.GONE
        }
    }

    /**
     * Actualiza el estado de carga - Solo campos editables
     */
    private fun updateLoadingState(isLoading: Boolean) {
        binding.apply {
            // Deshabilitar campos editables durante carga
            titleEditText.isEnabled = !isLoading
            aboutJobEditText.isEnabled = !isLoading
            skillsEditText.isEnabled = !isLoading
            vacanciesEditText.isEnabled = !isLoading
            employmentTypeEditText.isEnabled = !isLoading
            modalityDropdown.isEnabled = !isLoading
            salaryEditText.isEnabled = !isLoading
            deadlineEditText.isEnabled = !isLoading

            // Mostrar/ocultar indicador de carga
            loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE

            // Deshabilitar botones durante carga
            saveAdButton.isEnabled = !isLoading && (viewModel.isFormValid.value == true)
            publishButton.isEnabled = !isLoading
        }
    }

    /**
     * Muestra diálogo de confirmación para publicar
     */
    private fun showPublishConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Publicar anuncio")
            .setMessage("¿Estás seguro de que deseas publicar este anuncio? Una vez publicado será visible para todos los usuarios.")
            .setPositiveButton("Publicar") { _, _ ->
                viewModel.publishJob()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    //mark
    /**
     * Muestra diálogo para cambiar estado
     */
    private fun showStatusChangeDialog() {
        // TODO: Implementar cuando getValidStatusTransitions() y changeJobStatus() estén disponibles
        showErrorSnackbar("Función de cambio de estado - Por implementar")
    }

    /**
     * Muestra diálogo para descartar cambios
     */
    private fun showDiscardChangesDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Descartar cambios")
            .setMessage("¿Estás seguro de que deseas salir sin guardar?")
            .setPositiveButton("Descartar") { _, _ ->
                // Por ahora, simplemente navegar hacia atrás
                findNavController().navigateUp()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * Muestra selector de fecha para deadline
     */
    private fun showDatePicker() {
        // TODO: Implementar DatePickerDialog para deadline
        // Por ahora, mostrar un placeholder
        showErrorSnackbar("Selector de fecha límite - Por implementar")
    }

    /**
     * Maneja el botón atrás del sistema
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        /**
         * Crea una nueva instancia del fragmento
         */
        fun newInstance(jobId: String, jobTitle: String? = null): MyAdsEditFragment {
            return MyAdsEditFragment().apply {
                arguments = Bundle().apply {
                    putString(Constants.KEY_JOB_ID, jobId)
                    putString(Constants.KEY_JOB_TITLE, jobTitle)
                }
            }
        }
    }
}