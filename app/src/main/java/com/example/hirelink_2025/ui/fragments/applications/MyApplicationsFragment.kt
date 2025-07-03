package com.example.hirelink_2025.ui.fragments.applications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
//Clases necesarios para inflar el layout y manejar el ciclo de vida del gragmento:
//"Inflar" significa convertir un archivo XML de layout en una jerarquía real de vistas (View Objects) que el sistema puede mostrar en pantalla.

import androidx.fragment.app.Fragment //Clase base para crear fragmentos.
import com.example.hirelink_2025.databinding.FragmentMyApplicationsBinding//Clase generada automáticamente
import com.example.hirelink_2025.ui.adapters.ApplicationsPagerAdapter//Adaptador personalizado que gestiona las páginas del viewPager.
import com.google.android.material.tabs.TabLayoutMediator//Clase de material design que sincroniza el TabLayout con el ViewPager2.
class MyApplicationsFragment : Fragment() {

    private var _binding: FragmentMyApplicationsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Infla el layout del fragmento y asigna el binding
        _binding = FragmentMyApplicationsBinding.inflate(inflater, container, false)

        // Devuelve la vista raíz del layout inflado
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ApplicationsPagerAdapter(this)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Activas"
                1 -> "En revisión"
                2 -> "Finalizadas"
                else -> ""
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
