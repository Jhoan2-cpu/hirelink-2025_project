package com.example.hirelink_2025.ui.fragments.ads

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.hirelink_2025.R
//import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hirelink_2025.databinding.FragmentMyAdsBinding
import com.example.hirelink_2025.models.Job
import com.example.hirelink_2025.models.JobStatus
import com.example.hirelink_2025.ui.adapters.MyAdAdapter

//import com.example.hirelink_2025.viewmodels.ads.MyAdsViewModel


class MyAdsFragment : Fragment() {

    private var _binding: FragmentMyAdsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: MyAdAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyAdsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        loadFakeAds()
    }

    private fun setupUI() {
        adapter = MyAdAdapter(
            onJobClick = { jobAd ->
                val fragment = AdDetailFragment.newInstance(
                    titulo = "jobAd.titulo",
                    descripcion = "jobAd.descripcion",
                    habilidades = "jobAd.habilidades",
                    fecha = "jobAd.fechaPublicacion",
                    tipoEmpleo = "jobAd.tipoEmpleo",
                    cargo = "jobAd.cargo",
                    modalidad = "jobAd.modalidad",
                    estado = "jobAd.estado",
                    telefono = "jobAd.telefono",
                    email = "jobAd.email"
                )
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.main, fragment) // Este debe ser el ID del FrameLayout contenedor
                    .addToBackStack(null)
                    .commit()
            },
            onEditClick = { jobAd ->
                // Acción al hacer clic en editar
            },
            onDeleteClick = {jobAd ->

            },
            onApplicantsClick = { jobAd ->
                // Acción al hacer clic en postulantes
            }
        )

        binding.adsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.adsRecyclerView.adapter = adapter

        binding.createAdFab.setOnClickListener {
            findNavController().navigate(R.id.action_myAdsFragment_to_myAdsRegisterFragment)
        }
    }

    private fun loadFakeAds() {
        val dummyAds = listOf(
            Job(
                id = "1",
                title = "Desarrollador Android",
                companyName = "InnovaTech",
                companyLogo = null,
                location = "Lima, Perú",
                modality = "Remoto",
                salary = "S/ 5000 - S/ 6000",
                description = "Desarrolla apps modernas con Kotlin y Jetpack.",
                requirements = listOf("Kotlin", "MVVM", "Jetpack Compose"),
                postedDate = "Publicado hace 3 días",
                vacancies = 2,
                employmentType = "Tiempo completo",
                isBookmarked = false,
                hasApplied = false,
                status = JobStatus.ACTIVE
            ),
            Job(
                id = "2",
                title = "QA Tester",
                companyName = "SoftLab",
                companyLogo = null,
                location = "Arequipa, Perú",
                modality = "Presencial",
                salary = "S/ 3500 - S/ 4000",
                description = "Encargado de pruebas funcionales y automatizadas.",
                requirements = listOf("Selenium", "JUnit", "Postman"),
                postedDate = "Publicado hace 2 días",
                vacancies = 1,
                employmentType = "Medio tiempo",
                isBookmarked = true,
                hasApplied = true,
                status = JobStatus.CLOSED
            )
        )

        if (dummyAds.isEmpty()) {
            showEmptyState()
        } else {
            showAds(dummyAds)
        }

        // Estadísticas simuladas
        binding.activeAdsCount.text = dummyAds.count { it.status == JobStatus.ACTIVE }.toString()
        binding.totalApplicantsCount.text = dummyAds.sumOf { it.vacancies }.toString() //aquí podría ir aplicants en ves de vancancies, nNOO DEBE IR APLICANTS
    }

    private fun showEmptyState() {
        binding.emptyStateLayout.visibility = View.VISIBLE
        binding.adsRecyclerView.visibility = View.GONE
    }

    private fun showAds(ads: List<Job>) {
        binding.emptyStateLayout.visibility = View.GONE
        binding.adsRecyclerView.visibility = View.VISIBLE
        adapter.submitList(ads)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}