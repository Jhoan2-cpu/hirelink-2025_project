package com.example.hirelink_2025.ui.fragments.applications

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
import com.example.hirelink_2025.ui.adapters.ApplicationAdapter

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

        val sampleList = listOf(
            Application(
                jobTitle = "Desarrollador Android Senior",
                companyName = "Tech Solutions S.A.C.",
                applicationDate = "Postulado hace 3 días",
                status = "Programada",
                logoResId = R.drawable.ic_title
            ),
            Application(
                jobTitle = "Backend Developer",
                companyName = "GlobalSoft",
                applicationDate = "Postulado ayer",
                status = "Programada",
                logoResId = R.drawable.ic_profile
            ),
            Application(
                jobTitle = "Backend Developer",
                companyName = "GlobalSoft",
                applicationDate = "Postulado ayer",
                status = "Programada",
                logoResId = R.drawable.ic_profile
            ),
            Application(
                jobTitle = "Backend Developer",
                companyName = "GlobalSoft",
                applicationDate = "Postulado ayer",
                status = "Programada",
                logoResId = R.drawable.ic_profile
            ),
            Application(
                jobTitle = "Backend Developer",
                companyName = "GlobalSoft",
                applicationDate = "Postulado ayer",
                status = "Programada",
                logoResId = R.drawable.ic_profile
            ),
            Application(
                jobTitle = "Backend Developer",
                companyName = "GlobalSoft",
                applicationDate = "Postulado ayer",
                status = "Programada",
                logoResId = R.drawable.ic_profile
            ),
            Application(
                jobTitle = "Backend Developer",
                companyName = "GlobalSoft",
                applicationDate = "Postulado ayer",
                status = "Programada",
                logoResId = R.drawable.ic_profile
            ),
            Application(
                jobTitle = "Backend Developer",
                companyName = "GlobalSoft",
                applicationDate = "Postulado ayer",
                status = "Programada",
                logoResId = R.drawable.ic_profile
            ),
            Application(
                jobTitle = "Backend Developer",
                companyName = "GlobalSoft",
                applicationDate = "Postulado ayer",
                status = "Programada",
                logoResId = R.drawable.ic_profile
            )
        )

        adapter = ApplicationAdapter(
            apps = sampleList,
            onCancelClicked = { item ->
                Toast.makeText(requireContext(), "Cancelaste: ${item.jobTitle}", Toast.LENGTH_SHORT).show()
            },
            onItemClicked = { item ->
                // ✅ NUEVA FUNCIONALIDAD
                navigateToApplicationDetail(item)
            }
        )

        binding.applicationsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.applicationsRecyclerView.adapter = adapter
    }

    private fun navigateToApplicationDetail(application: Application) {
        val bundle = Bundle().apply {
            putString("job_title", application.jobTitle)
            putString("company_name", application.companyName)
            putString("application_date", application.applicationDate)
            putString("status", application.status)
            putInt("company_logo", application.logoResId)
            putString("job_description", "Descripción detallada del trabajo...")
            putString("job_requirements", "Kotlin, Android, MVVM")
            putString("employment_type", "Tiempo completo")
            putString("modality", "Remoto")
        }

        findNavController().navigate(
            R.id.action_myApplicationsFragment_to_applicationDetailFragment,
            bundle
        )
    }
}