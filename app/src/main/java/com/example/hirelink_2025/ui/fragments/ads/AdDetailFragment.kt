package com.example.hirelink_2025.ui.fragments.ads

import android.content.Intent
import android.net.Uri
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
import com.example.hirelink_2025.databinding.FragmentAdDetailBinding
import com.example.hirelink_2025.viewmodels.ads.AdDetailViewModel
import com.example.hirelink_2025.viewmodels.ads.AdData
import kotlinx.coroutines.launch

/**
 * Fragment para mostrar detalles de un anuncio
 * Implementa arquitectura MVVM
 */
class AdDetailFragment : Fragment() {

    private var _binding: FragmentAdDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AdDetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupObservers()
        setupClickListeners()

        // Cargar datos
        viewModel.loadAdData(arguments)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                handleLoadingState(state.isLoading)
                handleErrorState(state.error)
                state.adData?.let { updateUI(it) }
            }
        }
    }

    private fun handleLoadingState(isLoading: Boolean) {
        // Cambiar opacidad solo de los botones y contenido principal
        binding.btnTelefono.isEnabled = !isLoading
        binding.btnEmail.isEnabled = !isLoading

        // Si quieres mostrar un indicador visual de carga
        binding.btnTelefono.alpha = if (isLoading) 0.5f else 1.0f
        binding.btnEmail.alpha = if (isLoading) 0.5f else 1.0f
    }

    private fun handleErrorState(error: String?) {
        error?.let {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    private fun updateUI(adData: AdData) {
        // Configurar título en toolbar
        binding.toolbar.title = adData.titulo

        // Asignar datos a las vistas
        with(binding) {
            tvDescripcionEmpresa.text = adData.descripcion
            tvDescripcionEmpleo.text = adData.descripcion
            tvHabilidades.text = viewModel.formatHabilidades(adData.habilidades)
            tvCantidadVacantes.text = adData.cantidadVacantes
            tvFecha.text = adData.fecha
            tvTipoEmpleo.text = adData.tipoEmpleo
            tvCargo.text = adData.cargo
            tvModalidad.text = adData.modalidad
            tvEstado.text = adData.estado
            tvTelefono.text = adData.telefono
            tvEmail.text = adData.email
        }

        // Configurar color del estado
        configurarColorEstado(adData.estado)
    }

    private fun configurarColorEstado(estado: String) {
        val colorResId = when (estado.lowercase()) {
            "activo", "disponible", "active" -> R.color.success
            "pausado", "paused" -> R.color.warning
            "cerrado", "closed" -> R.color.error
            else -> R.color.text_primary
        }

        binding.tvEstado.setTextColor(resources.getColor(colorResId, null))
    }

    private fun setupClickListeners() {
        binding.btnTelefono.setOnClickListener {
            val telefono = binding.tvTelefono.text.toString()
            llamarTelefono(telefono)
        }

        binding.btnEmail.setOnClickListener {
            val email = binding.tvEmail.text.toString()
            val tituloAnuncio = binding.toolbar.title.toString()
            enviarEmail(email, tituloAnuncio)
        }
    }

    private fun llamarTelefono(numero: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$numero")
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "No se pudo abrir la aplicación de teléfono", Toast.LENGTH_SHORT).show()
        }
    }

    private fun enviarEmail(email: String, tituloAnuncio: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$email")
                putExtra(Intent.EXTRA_SUBJECT, "Consulta sobre: $tituloAnuncio")
                putExtra(Intent.EXTRA_TEXT, "Hola,\n\nMe interesa conocer más sobre la oferta de trabajo: $tituloAnuncio\n\nSaludos")
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "No se pudo abrir la aplicación de email", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(
            titulo: String,
            descripcion: String,
            habilidades: String,
            fecha: String,
            tipoEmpleo: String,
            cargo: String,
            modalidad: String,
            estado: String,
            telefono: String? = null,
            email: String? = null,
            cantidadVacantes: String? = null
        ): AdDetailFragment {
            val fragment = AdDetailFragment()
            val args = Bundle().apply {
                putString("titulo", titulo)
                putString("descripcion", descripcion)
                putString("habilidades", habilidades)
                putString("fecha", fecha)
                putString("tipo_empleo", tipoEmpleo)
                putString("cargo", cargo)
                putString("modalidad", modalidad)
                putString("estado", estado)
                telefono?.let { putString("telefono", it) }
                email?.let { putString("email", it) }
                cantidadVacantes?.let { putString("cantidad_vacantes", it) }
            }
            fragment.arguments = args
            return fragment
        }
    }
}