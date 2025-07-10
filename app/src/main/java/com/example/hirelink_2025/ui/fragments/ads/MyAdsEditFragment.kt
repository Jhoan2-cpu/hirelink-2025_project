package com.example.hirelink_2025.ui.fragments.ads

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.hirelink_2025.R
import com.example.hirelink_2025.databinding.FragmentMyAdsEditBinding
import com.example.hirelink_2025.models.JobStatus
import com.example.hirelink_2025.ui.utils.Constants
import com.example.hirelink_2025.ui.utils.showErrorSnackbar
import com.example.hirelink_2025.ui.utils.showSuccessSnackbar
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
            jobId = it.getString(Constants.KEY_JOB_ID)
            jobTitle = it.getString(Constants.KEY_JOB_TITLE)
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
     * Configura los campos del formulario
     */
    private fun setupFormFields() {
        // Configurar listeners para validación en tiempo real
        
        // Job Information Fields - Solo usar campos existentes en ViewModel por ahora
        binding.titleEditText.doOnTextChanged { text, _, _, _ ->
            viewModel.updateTitle(text.toString())
        }

        // Por ahora, mapear aboutJobEditText a description del ViewModel existente
        binding.aboutJobEditText.doOnTextChanged { text, _, _, _ ->
            viewModel.updateDescription(text.toString())
        }

        // Configurar dropdown de modalidad
        binding.modalityDropdown.setOnItemClickListener { _, _, position, _ ->
            val selectedModality = modalityAdapter.getItem(position) ?: ""
            viewModel.updateModality(selectedModality)
        }

        // Configurar click listeners para campos de fecha y selección
        binding.dateEditText.setOnClickListener {
            showDatePicker()
        }

        binding.uploadImageButton.setOnClickListener {
            showImagePicker()
        }

        binding.selectLocationButton.setOnClickListener {
            showLocationPicker()
        }

        // Listeners para campos que serán implementados después
        setupPlaceholderListeners()
    }

    /**
     * Configura listeners placeholder para campos nuevos
     */
    private fun setupPlaceholderListeners() {
        // Estos campos mostrarán mensajes placeholder hasta que se implementen en el ViewModel
        binding.aboutCompanyEditText.doOnTextChanged { _, _, _, _ ->
            // TODO: Implementar en ViewModel
        }

        binding.skillsEditText.doOnTextChanged { _, _, _, _ ->
            // TODO: Implementar en ViewModel
        }

        binding.vacanciesEditText.doOnTextChanged { _, _, _, _ ->
            // TODO: Implementar en ViewModel
        }

        binding.employmentTypeEditText.doOnTextChanged { _, _, _, _ ->
            // TODO: Implementar en ViewModel
        }

        binding.positionEditText.doOnTextChanged { _, _, _, _ ->
            // TODO: Implementar en ViewModel
        }

        binding.phoneEditText.doOnTextChanged { _, _, _, _ ->
            // TODO: Implementar en ViewModel
        }

        binding.emailEditText.doOnTextChanged { _, _, _, _ ->
            // TODO: Implementar en ViewModel
        }

        binding.websiteEditText.doOnTextChanged { _, _, _, _ ->
            // TODO: Implementar en ViewModel
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

        // Observar campos del formulario - Solo campos existentes en ViewModel
        viewModel.title.observe(viewLifecycleOwner) { title ->
            if (binding.titleEditText.text.toString() != title) {
                binding.titleEditText.setText(title)
            }
        }

        viewModel.description.observe(viewLifecycleOwner) { description ->
            if (binding.aboutJobEditText.text.toString() != description) {
                binding.aboutJobEditText.setText(description)
            }
        }

        viewModel.modality.observe(viewLifecycleOwner) { modality ->
            if (binding.modalityDropdown.text.toString() != modality) {
                binding.modalityDropdown.setText(modality, false)
            }
        }

        // Observar errores de validación - Solo campos existentes
        viewModel.titleError.observe(viewLifecycleOwner) { error ->
            binding.titleInputLayout.error = error
        }

        viewModel.descriptionError.observe(viewLifecycleOwner) { error ->
            binding.aboutJobInputLayout.error = error
        }

        viewModel.modalityError.observe(viewLifecycleOwner) { error ->
            binding.modalityInputLayout.error = error
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
            viewModel.loadJob(id)
        } ?: run {
            showErrorSnackbar("Error: ID del anuncio no encontrado")
            findNavController().navigateUp()
        }
    }

    /**
     * Actualiza la información del job en la UI
     */
    private fun updateJobInfo(job: com.example.hirelink_2025.models.Job) {
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
     * Actualiza el estado de carga
     */
    private fun updateLoadingState(isLoading: Boolean) {
        binding.apply {
            // Deshabilitar campos principales durante carga
            titleEditText.isEnabled = !isLoading
            aboutJobEditText.isEnabled = !isLoading
            modalityDropdown.isEnabled = !isLoading
            
            // Campos adicionales (funcionarán independientemente del ViewModel por ahora)
            aboutCompanyEditText.isEnabled = !isLoading
            skillsEditText.isEnabled = !isLoading
            vacanciesEditText.isEnabled = !isLoading
            employmentTypeEditText.isEnabled = !isLoading
            dateEditText.isEnabled = !isLoading
            positionEditText.isEnabled = !isLoading
            phoneEditText.isEnabled = !isLoading
            emailEditText.isEnabled = !isLoading
            websiteEditText.isEnabled = !isLoading
            
            // Media & Location
            uploadImageButton.isEnabled = !isLoading
            selectLocationButton.isEnabled = !isLoading

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
     * Muestra selector de fecha
     */
    private fun showDatePicker() {
        // TODO: Implementar DatePickerDialog
        // Por ahora, mostrar un placeholder
        showErrorSnackbar("Selector de fecha - Por implementar")
    }

    /**
     * Muestra selector de imagen
     */
    private fun showImagePicker() {
        // TODO: Implementar selector de imagen desde galería/cámara
        // Por ahora, mostrar un placeholder
        showErrorSnackbar("Selector de imagen - Por implementar")
    }

    /**
     * Muestra selector de ubicación
     */
    private fun showLocationPicker() {
        // TODO: Implementar selector de ubicación (mapa/lista)
        // Por ahora, mostrar un placeholder
        showErrorSnackbar("Selector de ubicación - Por implementar")
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