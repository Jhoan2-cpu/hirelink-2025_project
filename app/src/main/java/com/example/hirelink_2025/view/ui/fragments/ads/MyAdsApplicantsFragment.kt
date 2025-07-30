package com.example.hirelink_2025.view.ui.fragments.ads

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.hirelink_2025.R
import com.example.hirelink_2025.databinding.FragmentMyAdsApplicantsBinding
import com.example.hirelink_2025.models.Applicant
import com.example.hirelink_2025.models.ApplicationStatus
import com.example.hirelink_2025.view.adapter.ApplicantsAdapter
import com.example.hirelink_2025.view.adapter.ApplicantsPagerAdapter
import com.example.hirelink_2025.viewmodels.ViewModelFactory
import com.example.hirelink_2025.viewmodels.ads.MyAdsApplicantsViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AlertDialog

class MyAdsApplicantsFragment : Fragment() {

    private var _binding: FragmentMyAdsApplicantsBinding? = null
    private val binding get() = _binding!!

    // ViewModel con Factory (MVVM)
    private val viewModel: MyAdsApplicantsViewModel by viewModels {
        ViewModelFactory()
    }

    // Adapters
    private lateinit var pagerAdapter: ApplicantsPagerAdapter
    private lateinit var applicantsAdapter: ApplicantsAdapter

    // Variables del job
    private var jobId: String? = null
    private var jobTitle: String? = null

    companion object {
        const val ARG_JOB_ID = "job_id"
        const val ARG_JOB_TITLE = "job_title"

        fun newInstance(jobId: String, jobTitle: String): MyAdsApplicantsFragment {
            return MyAdsApplicantsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_JOB_ID, jobId)
                    putString(ARG_JOB_TITLE, jobTitle)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            jobId = it.getString(ARG_JOB_ID)
            jobTitle = it.getString(ARG_JOB_TITLE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyAdsApplicantsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        setupTabLayout()
        setupObservers()
        loadTestData()

        // Inicializar con job ID si está disponible
        jobId?.let { viewModel.initializeWithJob(it) }
    }

    private fun setupToolbar() {
        jobTitle?.let { title ->
            binding.applicantsTitle?.text = "Postulantes - $title"
        }
    }

    private fun setupRecyclerView() {
        applicantsAdapter = createApplicantsAdapter()

        // Pasar el adapter a los fragments del ViewPager
        if (::pagerAdapter.isInitialized) {
            pagerAdapter.setApplicantsAdapter(applicantsAdapter)
        }

        Log.d("MyAdsApplicants", "ApplicantsAdapter creado con callbacks para botones de acción")
    }

    private fun setupTabLayout() {
        jobId?.let { id ->
            pagerAdapter = ApplicantsPagerAdapter(requireActivity(), id)

            // Pasar el adapter a los fragments del pager
            pagerAdapter.setApplicantsAdapter(applicantsAdapter)

            binding.applicantsViewPager?.adapter = pagerAdapter

            binding.applicantsTabLayout?.let { tabLayout ->
                binding.applicantsViewPager?.let { viewPager ->
                    TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                        tab.text = when (position) {
                            0 -> "Aceptados"
                            1 -> "Pendientes"
                            else -> "Tab $position"
                        }
                    }.attach()
                }
            }
        }
    }

    private fun createApplicantsAdapter(): ApplicantsAdapter {
        return ApplicantsAdapter(
            onItemClicked = { applicant ->
                // Click en toda la tarjeta → Ver perfil
                navigateToApplicantProfile(applicant)
            },
            onAcceptClicked = { applicant ->
                // Click en botón Aceptar
                handleAcceptApplicant(applicant)
            },
            onRejectClicked = { applicant ->
                // Click en botón Rechazar
                handleRejectApplicant(applicant)
            }
        )
    }
    /**
     * Acepta a un postulante
     */
    internal fun handleAcceptApplicant(applicant: Applicant) {
        AlertDialog.Builder(requireContext())
            .setTitle("Aceptar postulante")
            .setMessage("¿Quieres aceptar a ${applicant.name} para este trabajo?")
            .setPositiveButton("Sí, aceptar") { _, _ ->
                // Actualizar estado a ACCEPTED
                viewModel.updateApplicantStatus(applicant.id, ApplicationStatus.ACCEPTED)

                // Mostrar confirmación
                Snackbar.make(binding.root, "${applicant.name} ha sido aceptado", Snackbar.LENGTH_LONG)
                    .setAction("Contactar") {
                        contactApplicant(applicant)
                    }
                    .show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * Rechaza a un postulante
     */
    internal fun handleRejectApplicant(applicant: Applicant) {
        AlertDialog.Builder(requireContext())
            .setTitle("Rechazar postulante")
            .setMessage("¿Quieres rechazar a ${applicant.name}?")
            .setPositiveButton("Sí, rechazar") { _, _ ->
                // Actualizar estado a REJECTED
                viewModel.updateApplicantStatus(applicant.id, ApplicationStatus.REJECTED)

                // Mostrar confirmación
                Snackbar.make(binding.root, "${applicant.name} ha sido rechazado", Snackbar.LENGTH_SHORT)
                    .show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }


    private fun loadTestData() {
        // Datos de prueba
        val mockApplicants = listOf(
            Applicant(
                id = "1",
                name = "Ana García",
                email = "ana.garcia@email.com",
                phone = "+34 666 123 456",
                profession = "Desarrolladora Frontend",
                experience = "3 años de experiencia en React y Vue.js",
                skills = listOf("JavaScript", "React", "Vue.js", "CSS3", "HTML5"),
                applicationDate = "15/01/2025",
                status = ApplicationStatus.PENDING,
                profileImage = null,
                jobId = jobId ?: "job123",
                coverLetter = "Me interesa mucho esta posición porque..."
            ),
            Applicant(
                id = "2",
                name = "Carlos Rodríguez",
                email = "carlos.rodriguez@email.com",
                phone = "+34 677 654 321",
                profession = "Diseñador UX/UI",
                experience = "5 años diseñando interfaces de usuario",
                skills = listOf("Figma", "Adobe XD", "Sketch", "Prototyping"),
                applicationDate = "14/01/2025",
                status = ApplicationStatus.ACCEPTED,
                profileImage = null,
                jobId = jobId ?: "job123",
                coverLetter = "Mi experiencia en diseño de interfaces..."
            ),
            Applicant(
                id = "3",
                name = "María López",
                email = "maria.lopez@email.com",
                phone = "+34 688 987 654",
                profession = "Backend Developer",
                experience = "4 años con Java y Spring Boot",
                skills = listOf("Java", "Spring Boot", "MySQL", "Docker"),
                applicationDate = "13/01/2025",
                status = ApplicationStatus.REJECTED,
                profileImage = null,
                jobId = jobId ?: "job123",
                coverLetter = "Creo que mi experiencia en backend..."
            )
        )

        // Actualizar adapter principal
        applicantsAdapter.submitList(mockApplicants)

        // Notificar al ViewModel para que actualice los observables
        viewModel.updateApplicantsList(mockApplicants)
    }

    private fun setupObservers() {
        // Observar estado de la UI
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateLoadingState(state.isLoading)
                state.error?.let { error ->
                    showErrorMessage(error)
                    viewModel.clearError()
                }
            }
        }

        // Observar contadores para badges
        observeApplicantCounts()
    }

    private fun observeApplicantCounts() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.pendingApplicants.collect { pendingList ->
                updateTabBadge(1, pendingList.size)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.acceptedApplicants.collect { acceptedList ->
                updateTabBadge(0, acceptedList.size)
            }
        }
    }

    private fun updateTabBadge(tabIndex: Int, count: Int) {
        val tabLayout = binding.applicantsTabLayout
        val tab = tabLayout.getTabAt(tabIndex)

        tab?.let {
            val tabText = when (tabIndex) {
                0 -> "Aceptados"
                1 -> "Pendientes"
                else -> "Tab $tabIndex"
            }
            it.text = if (count > 0) "$tabText ($count)" else tabText
        }
    }

    private fun updateLoadingState(isLoading: Boolean) {
        binding.applicantsProgressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.applicantsViewPager?.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun showErrorMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction("Reintentar") {
                jobId?.let { viewModel.initializeWithJob(it) }
            }
            .show()
    }

    internal fun navigateToApplicantProfile(applicant: Applicant) {
        Log.d("MyAdsApplicants", "Navegando a perfil de: ${applicant.name}")

        try {
            val bundle = Bundle().apply {
                putString("applicant_id", applicant.id)
                putString("applicant_name", applicant.name)
                putString("applicant_email", applicant.email ?: "")
                putString("applicant_phone", applicant.phone ?: "")
                putString("applicant_profession", applicant.profession)
                putString("applicant_experience", applicant.experience)
                putStringArrayList("applicant_skills", ArrayList(applicant.skills))
                putString("application_date", applicant.applicationDate)
                putString("application_status", applicant.status.name)
                putString("profile_image", applicant.profileImage ?: "")
                putString("job_id", applicant.jobId ?: jobId ?: "")
                putString("cover_letter", applicant.coverLetter ?: "")
            }

            findNavController().navigate(
                R.id.action_myAdsApplicantsFragment_to_applicantProfileFragment,
                bundle
            )
            
            Log.d("MyAdsApplicants", "Navegación exitosa al perfil de ${applicant.name}")

        } catch (e: Exception) {
            Log.e("MyAdsApplicants", "Error en navegación: ${e.message}")
            Toast.makeText(requireContext(), "Error al abrir perfil: ${e.message}", Toast.LENGTH_SHORT).show()
            // Fallback: mostrar información en un dialog
            showApplicantDetailsDialog(applicant)
        }
    }

    private fun showApplicantDetailsDialog(applicantId: String) {
        val applicant = viewModel.getApplicantById(applicantId)
        showApplicantDetailsDialog(applicant)
    }

    private fun showApplicantDetailsDialog(applicant: Applicant?) {
        if (applicant != null) {
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle("Perfil de ${applicant.name}")
                .setMessage(buildApplicantDetails(applicant))
                .setPositiveButton("Cerrar") { dialog, _ -> dialog.dismiss() }
                .setNeutralButton("Contactar") { _, _ ->
                    contactApplicant(applicant)
                }
                .create()

            dialog.show()
        } else {
            Toast.makeText(requireContext(), "No se pudo cargar el perfil", Toast.LENGTH_SHORT).show()
        }
    }

    private fun buildApplicantDetails(applicant: Applicant): String {
        return """
        Profesión: ${applicant.profession}
        Experiencia: ${applicant.experience}
        Email: ${applicant.email}
        Teléfono: ${applicant.phone}
        Habilidades: ${applicant.skills.joinToString(", ")}
        Fecha de aplicación: ${applicant.applicationDate}
        Estado: ${applicant.status.name}
        """.trimIndent()
    }

    private fun contactApplicant(applicant: Applicant) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:${applicant.email}")
            putExtra(Intent.EXTRA_SUBJECT, "Respuesta a tu aplicación - ${jobTitle ?: "Trabajo"}")
            putExtra(Intent.EXTRA_TEXT, "Hola ${applicant.name},\n\n")
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "No se pudo abrir el email", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}