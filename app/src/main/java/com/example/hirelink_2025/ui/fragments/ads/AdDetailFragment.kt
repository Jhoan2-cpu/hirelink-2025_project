package com.example.hirelink_2025.ui.fragments.ads

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hirelink_2025.R

class AdDetailFragment : Fragment() {

    private lateinit var btnBack: ImageButton
    private lateinit var tvTitulo: TextView
    private lateinit var tvDescripcionEmpresa: TextView
    private lateinit var tvHabilidades: TextView
    private lateinit var tvFecha: TextView
    private lateinit var tvTipoEmpleo: TextView
    private lateinit var tvCargo: TextView
    private lateinit var tvModalidad: TextView
    private lateinit var tvEstado: TextView
    private lateinit var btnTelefono: Button
    private lateinit var btnEmail: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ad_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        loadData()
        setupClickListeners()
    }

    private fun initViews(view: View) {
        btnBack = view.findViewById(R.id.btnBack)
        tvTitulo = view.findViewById(R.id.tvTitulo)
        tvDescripcionEmpresa = view.findViewById(R.id.tvDescripcionEmpresa)
        tvHabilidades = view.findViewById(R.id.tvHabilidades)
        tvFecha = view.findViewById(R.id.tvFecha)
        tvTipoEmpleo = view.findViewById(R.id.tvTipoEmpleo)
        tvCargo = view.findViewById(R.id.tvCargo)
        tvModalidad = view.findViewById(R.id.tvModalidad)
        tvEstado = view.findViewById(R.id.tvEstado)
        btnTelefono = view.findViewById(R.id.btnTelefono)
        btnEmail = view.findViewById(R.id.btnEmail)
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

            // Asignar datos a las vistas
            tvTitulo.text = titulo
            tvDescripcionEmpresa.text = descripcion
            tvHabilidades.text = habilidades
            tvFecha.text = fecha
            tvTipoEmpleo.text = tipoEmpleo
            tvCargo.text = cargo
            tvModalidad.text = modalidad
            tvEstado.text = estado

            // Cambiar color del estado según el tipo
            when (estado.lowercase()) {
                "disponible" -> tvEstado.setTextColor(resources.getColor(android.R.color.holo_green_dark))
                "pausado" -> tvEstado.setTextColor(resources.getColor(android.R.color.holo_orange_dark))
                "cerrado" -> tvEstado.setTextColor(resources.getColor(android.R.color.holo_red_dark))
                else -> tvEstado.setTextColor(resources.getColor(android.R.color.darker_gray))
            }
        }
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        btnTelefono.setOnClickListener {
            // Obtener número de teléfono desde argumentos o usar uno por defecto
            val telefono = arguments?.getString("telefono", "+51 999 999 999") ?: "+51 999 999 999"
            llamarTelefono(telefono)
        }

        btnEmail.setOnClickListener {
            // Obtener email desde argumentos o usar uno por defecto
            val email = arguments?.getString("email", "contacto@empresa.com") ?: "contacto@empresa.com"
            val tituloAnuncio = arguments?.getString("titulo", "Anuncio")
            enviarEmail(email, tituloAnuncio ?: "Anuncio")
        }
    }

    private fun llamarTelefono(numero: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$numero")
        }
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(intent)
        }
    }

    private fun enviarEmail(email: String, tituloAnuncio: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
            putExtra(Intent.EXTRA_SUBJECT, "Consulta sobre: $tituloAnuncio")
            putExtra(Intent.EXTRA_TEXT, "Hola,\n\nMe interesa conocer más sobre la oferta de trabajo: $tituloAnuncio\n\nSaludos")
        }
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(intent)
        }
    }

    companion object {
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
            email: String? = null
        ): AdDetailFragment{
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
            }
            fragment.arguments = args
            return fragment
        }
    }
}