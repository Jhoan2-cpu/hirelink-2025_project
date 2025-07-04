package com.example.hirelink_2025.ui.fragments.ads

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hirelink_2025.R
import com.example.hirelink_2025.databinding.FragmentAdDetailBinding

class AdDetailFragment : Fragment() {

    private var _binding: FragmentAdDetailBinding? = null
    private val binding get() = _binding!!

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
        loadData()
        setupClickListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun loadData() {
        // Obtener datos de los argumentos del fragmento
        arguments?.let { args ->
            val titulo = args.getString("titulo", "Sin título")
            val descripcion = args.getString("descripcion", "Sin descripción")
            val habilidades = args.getString("habilidades", "No especificadas")
            val fecha = args.getString("fecha", "Sin fecha")
            val tipoEmpleo = args.getString("tipo_empleo", "No especificado")
            val cargo = args.getString("cargo", "No especificado")
            val modalidad = args.getString("modalidad", "No especificada")
            val estado = args.getString("estado", "Sin estado")
            val telefono = args.getString("telefono", "+51 999 999 999")
            val email = args.getString("email", "contacto@empresa.com")
            val cantidadVacantes = args.getString("cantidad_vacantes", "1")

            // Configurar título en toolbar
            binding.toolbar.title = titulo

            // Asignar datos a las vistas
            with(binding) {
                tvDescripcionEmpresa.text = descripcion
                tvDescripcionEmpleo.text = descripcion // Podrías tener descripciones separadas
                tvHabilidades.text = formatearHabilidades(habilidades)
                tvCantidadVacantes.text = cantidadVacantes
                tvFecha.text = fecha
                tvTipoEmpleo.text = tipoEmpleo
                tvCargo.text = cargo
                tvModalidad.text = modalidad
                tvEstado.text = estado
                tvTelefono.text = telefono
                tvEmail.text = email
            }

            // Configurar color del estado
            configurarColorEstado(estado)
        }
    }

    private fun formatearHabilidades(habilidades: String): String {
        // Convertir lista separada por comas a formato de bullet points
        return habilidades.split(",").joinToString("\n") { "• ${it.trim()}" }
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