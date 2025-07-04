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
                    titulo = jobAd.title,
                    descripcion = jobAd.description,
                    habilidades = jobAd.requirements.joinToString(", "),
                    fecha = jobAd.postedDate,
                    tipoEmpleo = jobAd.employmentType,
                    cargo = jobAd.title,
                    modalidad = jobAd.modality,
                    estado = jobAd.status.name,
                    telefono = "123456789", // Placeholder
                    email = "contact@company.com" // Placeholder
                )
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.main, fragment)
                    .addToBackStack(null)
                    .commit()
            },
            onEditClick = { jobAd ->
                // Navegar a editar anuncio
                findNavController().navigate(
                    R.id.action_myAdsFragment_to_myAdsEditFragment,
                    Bundle().apply {
                        putString("job_id", jobAd.id)
                        putString("job_title", jobAd.title)
                    }
                )
            },
            onDeleteClick = { jobAd ->
                // Eliminar anuncio (implementar lógica)
                android.widget.Toast.makeText(
                    requireContext(),
                    "Eliminar: ${jobAd.title}",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            },
            onApplicantsClick = { jobAd ->
                // NAVEGACIÓN A POSTULANTES - IMPLEMENTADA
                findNavController().navigate(
                    R.id.action_myAdsFragment_to_myAdsApplicantsFragment,
                    Bundle().apply {
                        putString("job_id", jobAd.id)
                        putString("job_title", jobAd.title)
                    }
                )
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
                description = "Desarrolla apps modernas con Kotlin y Jetpack Compose. Únete a nuestro equipo innovador.",
                requirements = listOf("Kotlin", "Android Studio", "Git", "REST APIs"),
                postedDate = "Hace 2 días",
                vacancies = 3,
                employmentType = "Tiempo completo",
                status = JobStatus.ACTIVE
            ),
            Job(
                id = "2",
                title = "Diseñador UI/UX",
                companyName = "CreativeStudio",
                companyLogo = null,
                location = "Arequipa, Perú",
                modality = "Híbrido",
                salary = "S/ 3500 - S/ 4500",
                description = "Crea experiencias digitales extraordinarias. Buscamos un diseñador apasionado por la innovación.",
                requirements = listOf("Figma", "Adobe XD", "Prototyping", "User Research"),
                postedDate = "Hace 1 semana",
                vacancies = 2,
                employmentType = "Tiempo completo",
                status = JobStatus.ACTIVE
            ),
            Job(
                id = "3",
                title = "Desarrollador Backend",
                companyName = "TechSolutions",
                companyLogo = null,
                location = "Trujillo, Perú",
                modality = "Presencial",
                salary = "S/ 4000 - S/ 5500",
                description = "Construye la infraestructura que impulsa nuestras aplicaciones. Experiencia en Node.js requerida.",
                requirements = listOf("Node.js", "MongoDB", "Express", "AWS"),
                postedDate = "Hace 3 días",
                vacancies = 1,
                employmentType = "Tiempo completo",
                status = JobStatus.ACTIVE
            ),
            Job(
                id = "4",
                title = "Analista de Datos",
                companyName = "DataCorp",
                companyLogo = null,
                location = "Lima, Perú",
                modality = "Remoto",
                salary = "S/ 4500 - S/ 6000",
                description = "Convierte datos en insights valiosos. Únete a nuestro equipo de analytics.",
                requirements = listOf("Python", "SQL", "Power BI", "Machine Learning"),
                postedDate = "Hace 5 días",
                vacancies = 2,
                employmentType = "Tiempo completo",
                status = JobStatus.CLOSED
            )
        )

        adapter.submitList(dummyAds)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}