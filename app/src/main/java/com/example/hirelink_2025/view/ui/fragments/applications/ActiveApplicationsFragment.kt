package com.example.hirelink_2025.view.ui.fragments.applications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hirelink_2025.R
import com.example.hirelink_2025.databinding.FragmentActiveApplicationsBinding
import com.example.hirelink_2025.models.Application
import com.example.hirelink_2025.models.ApplicationStatus
import com.example.hirelink_2025.view.adapter.ApplicationAdapter

class ActiveApplicationsFragment : Fragment() {

    private var _binding: FragmentActiveApplicationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ApplicationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActiveApplicationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Datos mock usando modelo simplificado
        val sampleList = listOf(
            Application(
                applicationId = "app1",
                jobId = "job1",
                applicantId = "user1",
                appliedAt = System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000, // Hace 3 días
                status = ApplicationStatus.PENDING
            ),
            Application(
                applicationId = "app2",
                jobId = "job2",
                applicantId = "user1",
                appliedAt = System.currentTimeMillis() - 24 * 60 * 60 * 1000, // Ayer
                status = ApplicationStatus.PENDING
            ),
            Application(
                applicationId = "app3",
                jobId = "job3",
                applicantId = "user1",
                appliedAt = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000, // Hace 1 semana
                status = ApplicationStatus.ACCEPTED
            )
        )

        adapter = ApplicationAdapter(
            apps = sampleList,
            onCancelClicked = { item ->
                Toast.makeText(requireContext(), "Cancelaste: ${item.applicationId}", Toast.LENGTH_SHORT).show()
            },
            onItemClicked = { item ->
                navigateToApplicationDetail(item)
            },
            getJobInfo = { jobId -> getMockJobInfo(jobId) },
            getCompanyInfo = { companyId -> getMockCompanyInfo(companyId) }
        )

        binding.applicationsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.applicationsRecyclerView.adapter = adapter
    }

    private fun navigateToApplicationDetail(application: Application) {
        // Obtener información relacionada
        val job = getMockJobInfo(application.jobId)
        val company = job?.let { getMockCompanyInfo(it.companyId) }
        
        val bundle = Bundle().apply {
            putString("application_id", application.applicationId)
            putString("job_id", application.jobId)
            putString("job_title", job?.title ?: "Trabajo no encontrado")
            putString("company_name", company?.name ?: "Compañía no encontrada")
            putLong("applied_at", application.appliedAt)
            putString("status", application.status.name)
            putString("job_description", job?.aboutJob ?: "")
            putString("job_requirements", job?.requirements?.joinToString(", ") ?: "")
            putString("employment_type", job?.employmentType ?: "")
            putString("modality", job?.modality ?: "")
        }

        findNavController().navigate(
            R.id.action_myApplicationsFragment_to_applicationDetailFragment,
            bundle
        )
    }
    
    // Métodos mock para obtener datos relacionados
    private fun getMockJobInfo(jobId: String): com.example.hirelink_2025.models.Job? {
        return when (jobId) {
            "job1" -> com.example.hirelink_2025.models.Job(
                id = "job1",
                title = "Desarrollador Android Senior",
                companyId = "company1",
                modality = "Remoto",
                salary = "S/ 5000 - 6000",
                aboutJob = "Desarrollo de aplicaciones Android",
                requirements = listOf("Kotlin", "Android", "MVVM")
            )
            "job2" -> com.example.hirelink_2025.models.Job(
                id = "job2",
                title = "Backend Developer",
                companyId = "company2",
                modality = "Híbrido",
                salary = "S/ 4500 - 5500",
                aboutJob = "Desarrollo de APIs y microservicios",
                requirements = listOf("Java", "Spring", "Docker")
            )
            else -> null
        }
    }
    
    private fun getMockCompanyInfo(companyId: String): com.example.hirelink_2025.models.Company? {
        return when (companyId) {
            "company1" -> com.example.hirelink_2025.models.Company(
                id = "company1",
                name = "Tech Solutions S.A.C.",
                city = "Lima"
            )
            "company2" -> com.example.hirelink_2025.models.Company(
                id = "company2",
                name = "GlobalSoft",
                city = "Arequipa"
            )
            else -> null
        }
    }
}