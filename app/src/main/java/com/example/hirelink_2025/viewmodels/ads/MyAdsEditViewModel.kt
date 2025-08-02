package com.example.hirelink_2025.viewmodels.ads

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hirelink_2025.models.Job
import com.example.hirelink_2025.models.JobStatus
import com.example.hirelink_2025.repository.MyAdsRepository
import com.example.hirelink_2025.view.ui.utils.isValidString
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel para MyAdsEditFragment siguiendo arquitectura MVVM
 * Maneja la lógica de edición de anuncios laborales
 */
class MyAdsEditViewModel : ViewModel() {

    private val repository = MyAdsRepository()

    // Job actual que se está editando
    private val _currentJob = MutableLiveData<Job?>()
    val currentJob: LiveData<Job?> = _currentJob

    // Estados de los campos del formulario
    private val _title = MutableLiveData<String>()
    val title: LiveData<String> = _title

    private val _aboutJob = MutableLiveData<String>()
    val aboutJob: LiveData<String> = _aboutJob

    private val _modality = MutableLiveData<String>()
    val modality: LiveData<String> = _modality


    // Estados de validación
    private val _titleError = MutableLiveData<String?>()
    val titleError: LiveData<String?> = _titleError

    private val _aboutJobError = MutableLiveData<String?>()
    val aboutJobError: LiveData<String?> = _aboutJobError

    private val _modalityError = MutableLiveData<String?>()
    val modalityError: LiveData<String?> = _modalityError


    // Estados de UI
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isFormValid = MutableLiveData<Boolean>()
    val isFormValid: LiveData<Boolean> = _isFormValid

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    // Estados de éxito
    private val _saveSuccess = MutableLiveData<String>()
    val saveSuccess: LiveData<String> = _saveSuccess

    private val _navigationEvent = MutableLiveData<Boolean>()
    val navigationEvent: LiveData<Boolean> = _navigationEvent

    // Opciones para dropdowns
    private val _modalityOptions = MutableLiveData<List<String>>()
    val modalityOptions: LiveData<List<String>> = _modalityOptions

    init {
        initializeModalityOptions()
    }

    /**
     * Inicializa las opciones de modalidad
     */
    private fun initializeModalityOptions() {
        _modalityOptions.value = listOf(
            "Presencial",
            "Remoto",
            "Híbrido"
        )
    }

    /**
     * Carga un job para editar
     */
    fun loadJob(jobId: String) {
        _isLoading.value = true
        _errorMessage.value = ""

        viewModelScope.launch {
            repository.getJobById(jobId)
                .catch { exception ->
                    _isLoading.value = false
                    _errorMessage.value = "Error al cargar el anuncio: ${exception.message}"
                }
                .collect { job ->
                    _currentJob.value = job
                    if (job != null) {
                        populateFields(job)
                    } else {
                        _errorMessage.value = "No se encontró el anuncio"
                    }
                    _isLoading.value = false
                }
        }
    }

    /**
     * Llena los campos del formulario con los datos del job
     */
    private fun populateFields(job: Job) {
        _title.value = job.title
        _aboutJob.value = job.aboutJob
        _modality.value = job.modality
        validateForm()
    }

    /**
     * Actualiza el título
     */
    fun updateTitle(newTitle: String) {
        _title.value = newTitle
        validateTitle(newTitle)
        validateForm()
    }

    /**
     * Actualiza la descripción del trabajo
     */
    fun updateAboutJob(newAboutJob: String) {
        _aboutJob.value = newAboutJob
        validateAboutJob(newAboutJob)
        validateForm()
    }

    /**
     * Actualiza la modalidad
     */
    fun updateModality(newModality: String) {
        _modality.value = newModality
        validateModality(newModality)
        validateForm()
    }


    /**
     * Valida el título
     */
    private fun validateTitle(title: String) {
        _titleError.value = when {
            !title.isValidString() -> "El título es obligatorio"
            title.length < 5 -> "El título debe tener al menos 5 caracteres"
            title.length > 100 -> "El título no puede tener más de 100 caracteres"
            else -> null
        }
    }

    /**
     * Valida la descripción del trabajo
     */
    private fun validateAboutJob(aboutJob: String) {
        _aboutJobError.value = when {
            !aboutJob.isValidString() -> "La descripción del trabajo es obligatoria"
            aboutJob.length < 20 -> "La descripción debe tener al menos 20 caracteres"
            aboutJob.length > 1000 -> "La descripción no puede tener más de 1000 caracteres"
            else -> null
        }
    }

    /**
     * Valida la modalidad
     */
    private fun validateModality(modality: String) {
        val validModalities = _modalityOptions.value ?: emptyList()
        _modalityError.value = when {
            !modality.isValidString() -> "La modalidad es obligatoria"
            modality !in validModalities -> "Selecciona una modalidad válida"
            else -> null
        }
    }


    /**
     * Valida todo el formulario
     */
    private fun validateForm() {
        val titleValid = _titleError.value.isNullOrEmpty()
        val aboutJobValid = _aboutJobError.value.isNullOrEmpty()
        val modalityValid = _modalityError.value.isNullOrEmpty()

        _isFormValid.value = titleValid && aboutJobValid && modalityValid
    }

    /**
     * Guarda los cambios del anuncio
     */
    fun saveJob() {
        if (_isFormValid.value != true) {
            _errorMessage.value = "Por favor corrige los errores del formulario"
            return
        }

        val currentJob = _currentJob.value
        if (currentJob == null) {
            _errorMessage.value = "No se pudo guardar: anuncio no encontrado"
            return
        }

        _isLoading.value = true
        _errorMessage.value = ""

        viewModelScope.launch {
            try {
                val updatedJob = currentJob.copy(
                    title = _title.value ?: "",
                    aboutJob = _aboutJob.value ?: "",
                    modality = _modality.value ?: "",
                    updatedAt = System.currentTimeMillis()
                )

                val success = repository.updateJob(updatedJob)
                if (success) {
                    _saveSuccess.value = "Anuncio actualizado exitosamente"
                    _navigationEvent.value = true
                } else {
                    _errorMessage.value = "Error al guardar los cambios"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al guardar: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Cambia el estado del anuncio
     */
    fun changeJobStatus(newStatus: JobStatus) {
        val currentJob = _currentJob.value
        if (currentJob == null) {
            _errorMessage.value = "No se pudo cambiar el estado: anuncio no encontrado"
            return
        }

        if (!currentJob.status.canTransitionTo(newStatus)) {
            _errorMessage.value = "No se puede cambiar al estado seleccionado"
            return
        }

        _isLoading.value = true

        viewModelScope.launch {
            try {
                val success = repository.updateJobStatus(currentJob.id, newStatus)
                if (success) {
                    _currentJob.value = currentJob.copy(status = newStatus)
                    _saveSuccess.value = "Estado cambiado a ${newStatus.getDisplayText()}"
                } else {
                    _errorMessage.value = "Error al cambiar el estado"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cambiar estado: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Publica el anuncio (cambiar de DRAFT a ACTIVE)
     */
    fun publishJob() {
        if (_isFormValid.value != true) {
            _errorMessage.value = "Completa todos los campos antes de publicar"
            return
        }

        val currentJob = _currentJob.value
        if (currentJob?.status != JobStatus.DRAFT) {
            _errorMessage.value = "Solo se pueden publicar anuncios en estado de borrador"
            return
        }

        // Primero guardar cambios, luego cambiar estado
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Guardar cambios primero
                val updatedJob = currentJob.copy(
                    title = _title.value ?: "",
                    aboutJob = _aboutJob.value ?: "",
                    modality = _modality.value ?: "",
                    updatedAt = System.currentTimeMillis()
                )

                val saveSuccess = repository.updateJob(updatedJob)
                if (saveSuccess) {
                    // Cambiar estado a ACTIVE
                    val statusSuccess = repository.updateJobStatus(currentJob.id, JobStatus.ACTIVE)
                    if (statusSuccess) {
                        _currentJob.value = updatedJob.copy(status = JobStatus.ACTIVE)
                        _saveSuccess.value = "Anuncio publicado exitosamente"
                        _navigationEvent.value = true
                    } else {
                        _errorMessage.value = "Error al publicar el anuncio"
                    }
                } else {
                    _errorMessage.value = "Error al guardar los cambios"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al publicar: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Descarta los cambios y navega hacia atrás
     */
    fun discardChanges() {
        _navigationEvent.value = true
    }

    /**
     * Limpia los mensajes de error
     */
    fun clearErrorMessage() {
        _errorMessage.value = ""
    }

    /**
     * Limpia los mensajes de éxito
     */
    fun clearSuccessMessage() {
        _saveSuccess.value = ""
    }

    /**
     * Maneja el evento de navegación
     */
    fun onNavigationHandled() {
        _navigationEvent.value = false
    }

    /**
     * Verifica si hay cambios sin guardar
     */
    fun hasUnsavedChanges(): Boolean {
        val currentJob = _currentJob.value ?: return false
        return currentJob.title != _title.value ||
                currentJob.aboutJob != _aboutJob.value ||
                currentJob.modality != _modality.value
    }

    /**
     * Obtiene los estados válidos para transición
     */
    fun getValidStatusTransitions(): List<JobStatus> {
        return _currentJob.value?.status?.getValidTransitions() ?: emptyList()
    }

    /**
     * Verifica si el job puede ser eliminado
     */
    fun canDeleteJob(): Boolean {
        return _currentJob.value?.status?.canDelete() == true
    }

    /**
     * Verifica si el job puede ser publicado
     */
    fun canPublishJob(): Boolean {
        return _currentJob.value?.status == JobStatus.DRAFT && _isFormValid.value == true
    }

    /**
     * Valida todos los campos actuales
     */
    fun validateAllFields() {
        _title.value?.let { validateTitle(it) }
        _aboutJob.value?.let { validateAboutJob(it) }
        _modality.value?.let { validateModality(it) }
        validateForm()
    }
}