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
import com.example.hirelink_2025.models.Application
import com.example.hirelink_2025.models.ApplicationStatus
import com.example.hirelink_2025.models.User
import com.example.hirelink_2025.view.adapter.ApplicationsAdapter
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
import java.text.SimpleDateFormat
import java.util.*

class MyAdsApplicantsFragment : Fragment() {

    private var _binding: FragmentMyAdsApplicantsBinding? = null
    private val binding get() = _binding!!

    // ViewModel con Factory (MVVM)
    val viewModel: MyAdsApplicantsViewModel by viewModels {
        ViewModelFactory()
    }

    // Adapters
    private lateinit var pagerAdapter: ApplicantsPagerAdapter
    private lateinit var applicationsAdapter: ApplicationsAdapter

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
        applicationsAdapter = createApplicationsAdapter()

        // Pasar el adapter a los fragments del ViewPager
        if (::pagerAdapter.isInitialized) {
            pagerAdapter.setApplicationsAdapter(applicationsAdapter)
        }

        Log.d("MyAdsApplicants", "ApplicationsAdapter creado con callbacks para botones de acción")
    }

    private fun setupTabLayout() {
        jobId?.let { id ->
            pagerAdapter = ApplicantsPagerAdapter(requireActivity(), id)

            // Pasar el adapter a los fragments del pager
            pagerAdapter.setApplicationsAdapter(applicationsAdapter)

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

    private fun createApplicationsAdapter(): ApplicationsAdapter {
        return ApplicationsAdapter(
            onAcceptClick = { application ->
                // Click en botón Aceptar
                handleAcceptApplication(application)
            },
            onRejectClick = { application ->
                // Click en botón Rechazar
                handleRejectApplication(application)
            },
            onApplicantClick = { application ->
                // Click en toda la tarjeta → Ver perfil del postulante
                navigateToApplicantProfile(application)
            },
            getUserInfo = { userId ->
                // Obtener información del usuario desde el ViewModel
                viewModel.getUserById(userId)
            }
        )
    }
    /**
     * Acepta una aplicación
     */
    internal fun handleAcceptApplication(application: Application) {
        val user = viewModel.getUserById(application.applicantId)
        val userName = user?.name ?: "el postulante"
        
        AlertDialog.Builder(requireContext())
            .setTitle("Aceptar postulante")
            .setMessage("¿Quieres aceptar a $userName para este trabajo?")
            .setPositiveButton("Sí, aceptar") { _, _ ->
                // Actualizar estado a ACCEPTED
                viewModel.updateApplicationStatus(application.applicationId, ApplicationStatus.ACCEPTED)

                // Mostrar confirmación
                Snackbar.make(binding.root, "$userName ha sido aceptado", Snackbar.LENGTH_LONG)
                    .setAction("Contactar") {
                        user?.let { contactApplicant(it) }
                    }
                    .show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * Rechaza una aplicación
     */
    internal fun handleRejectApplication(application: Application) {
        val user = viewModel.getUserById(application.applicantId)
        val userName = user?.name ?: "el postulante"
        
        AlertDialog.Builder(requireContext())
            .setTitle("Rechazar postulante")
            .setMessage("¿Quieres rechazar a $userName?")
            .setPositiveButton("Sí, rechazar") { _, _ ->
                // Actualizar estado a REJECTED
                viewModel.updateApplicationStatus(application.applicationId, ApplicationStatus.REJECTED)

                // Mostrar confirmación
                Snackbar.make(binding.root, "$userName ha sido rechazado", Snackbar.LENGTH_SHORT)
                    .show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }


    private fun loadTestData() {
        val currentJobId = jobId ?: "job123"
        
        // Crear datos de prueba para aplicaciones
        val mockApplications = viewModel.createTestApplications(currentJobId)
        
        // Crear usuarios de prueba correspondientes
        val mockUsers = mapOf(
            "user1" to User(
                id = "user1",
                name = "Ana García",
                email = "ana.garcia@email.com",
                phone = "+34 666 123 456",
                createdAt = System.currentTimeMillis()
            ),
            "user2" to User(
                id = "user2", 
                name = "Carlos Rodríguez",
                email = "carlos.rodriguez@email.com",
                phone = "+34 677 654 321",
                createdAt = System.currentTimeMillis()
            ),
            "user3" to User(
                id = "user3",
                name = "María López",
                email = "maria.lopez@email.com",
                phone = "+34 688 987 654",
                createdAt = System.currentTimeMillis()
            )
        )

        // Actualizar cache de usuarios en el ViewModel
        mockUsers.forEach { (userId, user) ->
            viewModel.updateUserCache(userId, user)
        }

        // Actualizar adapter principal
        applicationsAdapter.submitList(mockApplications)

        // Notificar al ViewModel para que actualice los observables
        viewModel.updateApplicationsList(mockApplications)
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
            viewModel.pendingApplications.collect { pendingList ->
                updateTabBadge(1, pendingList.size)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.acceptedApplications.collect { acceptedList ->
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

    internal fun navigateToApplicantProfile(application: Application) {
        val user = viewModel.getUserById(application.applicantId)
        Log.d("MyAdsApplicants", "Navegando a perfil de: ${user?.name ?: "Usuario desconocido"}")

        try {
            val bundle = Bundle().apply {
                putString("application_id", application.applicationId)
                putString("applicant_id", application.applicantId)
                putString("applicant_name", user?.name ?: "")
                putString("applicant_email", user?.email ?: "")
                putString("applicant_phone", user?.phone ?: "")
                putString("application_date", formatDate(application.appliedAt))
                putString("application_status", application.status.name)
                putString("job_id", application.jobId)
                putString("cover_letter", application.coverLetter)
            }

            findNavController().navigate(
                R.id.action_myAdsApplicantsFragment_to_applicantProfileFragment,
                bundle
            )
            
            Log.d("MyAdsApplicants", "Navegación exitosa al perfil de ${user?.name}")

        } catch (e: Exception) {
            Log.e("MyAdsApplicants", "Error en navegación: ${e.message}")
            Toast.makeText(requireContext(), "Error al abrir perfil: ${e.message}", Toast.LENGTH_SHORT).show()
            // Fallback: mostrar información en un dialog
            showApplicationDetailsDialog(application)
        }
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    private fun showApplicationDetailsDialog(application: Application) {
        val user = viewModel.getUserById(application.applicantId)
        
        if (user != null) {
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle("Perfil de ${user.name}")
                .setMessage(buildApplicationDetails(application, user))
                .setPositiveButton("Cerrar") { dialog, _ -> dialog.dismiss() }
                .setNeutralButton("Contactar") { _, _ ->
                    contactApplicant(user)
                }
                .create()

            dialog.show()
        } else {
            Toast.makeText(requireContext(), "No se pudo cargar el perfil", Toast.LENGTH_SHORT).show()
        }
    }

    private fun buildApplicationDetails(application: Application, user: User): String {
        return """
        Nombre: ${user.name}
        Email: ${user.email}
        Teléfono: ${user.phone}
        Fecha de aplicación: ${formatDate(application.appliedAt)}
        Estado: ${application.status.name}
        Carta de presentación: ${application.coverLetter}
        """.trimIndent()
    }

    private fun contactApplicant(user: User) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:${user.email}")
            putExtra(Intent.EXTRA_SUBJECT, "Respuesta a tu aplicación - ${jobTitle ?: "Trabajo"}")
            putExtra(Intent.EXTRA_TEXT, "Hola ${user.name},\n\n")
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