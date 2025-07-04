package com.example.hirelink_2025.ui.fragments.common

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.hirelink_2025.R
import com.example.hirelink_2025.databinding.FragmentJobDescriptionDetailBinding

class JobDescriptionDetailFragment : Fragment() {

    private lateinit var binding: FragmentJobDescriptionDetailBinding
    private var isJobSaved = false

    // Datos del trabajo
    private var jobId: Int = 0
    private var jobTitle: String = ""
    private var jobCompany: String = ""
    private var jobLocation: String = ""
    private var jobSalary: String = ""
    private var jobType: String = ""
    private var jobDescription: String = ""
    private var jobPublishedDate: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentJobDescriptionDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtener argumentos
        getArgumentsData()

        // Configurar UI
        setupUI()
        setupClickListeners()

        // Mostrar datos del trabajo
        displayJobData()

        // Actualizar título después de cargar los datos
        updateToolbarTitle()
    }

    private fun getArgumentsData() {
        arguments?.let { bundle ->
            jobId = bundle.getInt("job_id", 0)
            jobTitle = bundle.getString("job_title", "")
            jobCompany = bundle.getString("job_company", "")
            jobLocation = bundle.getString("job_location", "")
            jobSalary = bundle.getString("job_salary", "")
            jobType = bundle.getString("job_type", "")
            jobDescription = bundle.getString("job_description", "")
            jobPublishedDate = bundle.getString("job_published_date", "")
        }
    }

    private fun setupUI() {
        // Configuración inicial de la UI
        // El título se actualiza después de cargar los datos
    }

    private fun updateToolbarTitle() {
        val title = if (jobTitle.isNotEmpty()) {
            "$jobTitle - ${parseJobType()}"
        } else {
            "Título - Tipo de Trabajo"
        }
        binding.toolbarTitle.text = title
    }

    private fun parseJobType(): String {
        return if (jobType.isNotEmpty()) {
            val parts = jobType.split(" - ")
            if (parts.isNotEmpty()) parts[0] else "Trabajo"
        } else {
            "Trabajo"
        }
    }

    private fun setupClickListeners() {
        // Botón back
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        // Botón compartir
        binding.shareButton.setOnClickListener {
            shareJob()
        }

        // Botón bookmark en el header
        binding.bookmarkButton.setOnClickListener {
            toggleBookmark()
        }

        // Botón guardar trabajo
        binding.saveJobButton.setOnClickListener {
            toggleSaveJob()
        }

        // Botón aplicar
        binding.applyJobButton.setOnClickListener {
            applyToJob()
        }
    }

    private fun displayJobData() {
        with(binding) {
            // Información básica
            jobTitle.text = this@JobDescriptionDetailFragment.jobTitle.ifEmpty { "Desarrollador Frontend" }
            companyName.text = this@JobDescriptionDetailFragment.jobCompany.ifEmpty { "Google Inc." }
            jobLocation.text = this@JobDescriptionDetailFragment.jobLocation.ifEmpty { "Lima, Perú" }
            publishedDate.text = this@JobDescriptionDetailFragment.jobPublishedDate.ifEmpty { "Publicado hace 2 días" }

            // Chips
            salaryChip.text = this@JobDescriptionDetailFragment.jobSalary.ifEmpty { "S/. 3,000 - 5,000" }

            // Parsear el tipo de trabajo para modalidad y tipo de empleo
            val typeparts = this@JobDescriptionDetailFragment.jobType.split(" - ")
            if (typeparts.size >= 2) {
                employmentTypeChip.text = typeparts[0]
                modalityChip.text = typeparts[1]
            } else {
                employmentTypeChip.text = "Tiempo completo"
                modalityChip.text = "Remoto"
            }

            experienceChip.text = "Intermedio" // Por defecto

            // Información detallada del trabajo
            vacanciesCount.text = "1" // Por defecto
            jobDate.text = getCurrentDate()
            employmentTypeDetail.text = if (typeparts.isNotEmpty()) typeparts[0] else "Tiempo completo"
            jobPosition.text = extractJobPosition()
            jobModalityDetail.text = if (typeparts.size >= 2) typeparts[1] else "Remoto"
            jobStatus.text = "Disponible"

            // Acerca de la empresa
            aboutCompany.text = getCompanyDescription()

            // Descripción del trabajo
            jobDescription.text = this@JobDescriptionDetailFragment.jobDescription.ifEmpty {
                getDefaultJobDescription()
            }

            // Información de contacto
            contactPhone.text = "+51 956842976"
            contactEmail.text = "usuario@gmail.com"
            contactWebsite.text = "sitio web"

            // Configurar click listeners para contacto
            setupContactClickListeners()
        }
    }

    private fun getCurrentDate(): String {
        val calendar = java.util.Calendar.getInstance()
        val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
        val month = calendar.get(java.util.Calendar.MONTH) + 1
        val year = calendar.get(java.util.Calendar.YEAR)
        return String.format("%02d/%02d/%d", day, month, year)
    }

    private fun extractJobPosition(): String {
        // Extraer el tipo de cargo desde el título del trabajo
        return when {
            jobTitle.contains("Desarrollador", ignoreCase = true) -> "Desarrollador"
            jobTitle.contains("Diseñador", ignoreCase = true) -> "Diseñador"
            jobTitle.contains("Gerente", ignoreCase = true) -> "Gerente"
            jobTitle.contains("Analista", ignoreCase = true) -> "Analista"
            jobTitle.contains("Marketing", ignoreCase = true) -> "Marketing"
            jobTitle.contains("Ventas", ignoreCase = true) -> "Ventas"
            else -> "Profesional"
        }
    }

    private fun setupContactClickListeners() {
        // Click en teléfono
        binding.contactPhone.setOnClickListener {
            dialPhoneNumber(binding.contactPhone.text.toString())
        }

        // Click en email
        binding.contactEmail.setOnClickListener {
            sendEmail(binding.contactEmail.text.toString())
        }

        // Click en website
        binding.contactWebsite.setOnClickListener {
            openWebsite("https://www.google.com") // URL por defecto
        }
    }

    private fun dialPhoneNumber(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = android.net.Uri.parse("tel:$phoneNumber")
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "No se pudo abrir el marcador", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendEmail(email: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = android.net.Uri.parse("mailto:$email")
            putExtra(Intent.EXTRA_SUBJECT, "Consulta sobre: $jobTitle")
            putExtra(Intent.EXTRA_TEXT, "Hola, me interesa la oferta laboral para $jobTitle en $jobCompany.")
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "No se pudo abrir el cliente de email", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openWebsite(url: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = android.net.Uri.parse(url)
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "No se pudo abrir el sitio web", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCompanyDescription(): String {
        return when (jobCompany.lowercase()) {
            "google", "google inc.", "google inc" ->
                "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum."
            "microsoft" ->
                "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book."
            "amazon" ->
                "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book."
            else ->
                "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum."
        }
    }

    private fun getDefaultJobDescription(): String {
        return "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum."
    }

    private fun shareJob() {
        val shareText = """
            ¡Mira esta oportunidad laboral!
            
            Puesto: $jobTitle
            Empresa: $jobCompany
            Ubicación: $jobLocation
            Salario: $jobSalary
            
            Enviado desde HireLink
        """.trimIndent()

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, "Oportunidad laboral: $jobTitle")
        }

        try {
            startActivity(Intent.createChooser(shareIntent, "Compartir trabajo"))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "No se pudo compartir", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleBookmark() {
        isJobSaved = !isJobSaved
        updateBookmarkIcon()

        val message = if (isJobSaved) {
            "Trabajo guardado en favoritos"
        } else {
            "Trabajo removido de favoritos"
        }
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun toggleSaveJob() {
        isJobSaved = !isJobSaved
        updateSaveButtonState()

        val message = if (isJobSaved) {
            "Trabajo guardado"
        } else {
            "Trabajo removido de guardados"
        }
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun updateBookmarkIcon() {
        val iconRes = if (isJobSaved) {
            R.drawable.ic_bookmark_filled
        } else {
            R.drawable.ic_bookmark_border
        }
        binding.bookmarkButton.setIconResource(iconRes)
    }

    private fun updateSaveButtonState() {
        val iconRes = if (isJobSaved) {
            R.drawable.ic_bookmark_filled
        } else {
            R.drawable.ic_bookmark_border
        }

        val text = if (isJobSaved) {
            "Guardado"
        } else {
            "Guardar"
        }

        binding.saveJobButton.setIconResource(iconRes)
        binding.saveJobButton.text = text
    }

    private fun applyToJob() {
        // Mostrar diálogo de confirmación o navegar a formulario de aplicación
        showApplyDialog()
    }

    private fun showApplyDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Aplicar a este trabajo")
        builder.setMessage("¿Estás seguro de que quieres aplicar a este puesto en $jobCompany?")

        builder.setPositiveButton("Aplicar") { _, _ ->
            // Aquí implementarías la lógica de aplicación
            processJobApplication()
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        builder.create().show()
    }

    private fun processJobApplication() {
        // Simular proceso de aplicación
        Toast.makeText(requireContext(), "¡Aplicación enviada exitosamente!", Toast.LENGTH_LONG).show()

        // Cambiar el estado del botón
        binding.applyJobButton.apply {
            text = "Aplicado ✓"
            isEnabled = false
            alpha = 0.6f
        }

        // Opcional: navegar de vuelta o mostrar pantalla de confirmación
        // findNavController().popBackStack()
    }

    companion object {
        @JvmStatic
        fun newInstance(
            jobId: Int,
            jobTitle: String,
            jobCompany: String,
            jobLocation: String,
            jobSalary: String,
            jobType: String,
            jobDescription: String,
            jobPublishedDate: String
        ) = JobDescriptionDetailFragment().apply {
            arguments = Bundle().apply {
                putInt("job_id", jobId)
                putString("job_title", jobTitle)
                putString("job_company", jobCompany)
                putString("job_location", jobLocation)
                putString("job_salary", jobSalary)
                putString("job_type", jobType)
                putString("job_description", jobDescription)
                putString("job_published_date", jobPublishedDate)
            }
        }
    }
}