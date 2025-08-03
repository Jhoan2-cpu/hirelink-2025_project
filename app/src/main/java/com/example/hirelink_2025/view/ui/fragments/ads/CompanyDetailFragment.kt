package com.example.hirelink_2025.view.ui.fragments.ads

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.hirelink_2025.R
import com.example.hirelink_2025.models.Company
import com.example.hirelink_2025.viewmodels.CompanyViewModel
import com.example.hirelink_2025.viewmodels.ViewModelFactory
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch

class CompanyDetailFragment : Fragment() {
    
    private lateinit var toolbar: MaterialToolbar
    private lateinit var companyLogo: ImageView
    private lateinit var companyName: TextView
    private lateinit var companyType: TextView
    private lateinit var companyDescription: TextView
    private lateinit var companyAddress: TextView
    private lateinit var companyCityCountry: TextView
    private lateinit var companyPhone: TextView
    private lateinit var companyEmail: TextView
    private lateinit var companyWebsite: TextView
    private lateinit var employeeCountChip: Chip
    // private lateinit var activeJobsChip: Chip // TODO: Implement or remove this feature
    private lateinit var openMapButton: MaterialButton
    private lateinit var foundedYearChip: Chip
    private lateinit var phoneLayout: LinearLayout
    private lateinit var emailLayout: LinearLayout
    private lateinit var websiteLayout: LinearLayout
    private lateinit var editCompanyButton: MaterialButton
    private lateinit var deleteCompanyButton: MaterialButton
    private lateinit var progressIndicator: CircularProgressIndicator
    
    // ViewModel
    private val viewModel: CompanyViewModel by viewModels { ViewModelFactory() }
    
    private var currentCompany: Company? = null
    private var currentUserId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_company_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        Log.d("CompanyDetailFragment", "Fragment created")
        
        initViews(view)
        setupClickListeners()
        observeViewModel()
        getCurrentUserAndLoadCompany()
    }
    
    private fun initViews(view: View) {
        toolbar = view.findViewById(R.id.toolbar)
        companyLogo = view.findViewById(R.id.companyLogo)
        companyName = view.findViewById(R.id.companyName)
        companyType = view.findViewById(R.id.companyType)
        companyDescription = view.findViewById(R.id.companyDescription)
        companyAddress = view.findViewById(R.id.companyAddress)
        companyCityCountry = view.findViewById(R.id.companyCityCountry)
        companyPhone = view.findViewById(R.id.companyPhone)
        companyEmail = view.findViewById(R.id.companyEmail)
        companyWebsite = view.findViewById(R.id.companyWebsite)
        employeeCountChip = view.findViewById(R.id.employeeCountChip)
        foundedYearChip = view.findViewById(R.id.foundedYearChip)
        phoneLayout = view.findViewById(R.id.phoneLayout)
        emailLayout = view.findViewById(R.id.emailLayout)
        websiteLayout = view.findViewById(R.id.websiteLayout)
        editCompanyButton = view.findViewById(R.id.editCompanyButton)
        deleteCompanyButton = view.findViewById(R.id.deleteCompanyButton)
        progressIndicator = view.findViewById(R.id.progressIndicator)

        openMapButton = view.findViewById(R.id.openMapButton)
    }
    
    private fun setupClickListeners() {
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        
        phoneLayout.setOnClickListener {
            currentCompany?.phone?.let { phone ->
                if (phone.isNotEmpty()) {
                    dialPhone(phone)
                }
            }
        }
        
        emailLayout.setOnClickListener {
            currentCompany?.email?.let { email ->
                if (email.isNotEmpty()) {
                    sendEmail(email)
                }
            }
        }
        
        websiteLayout.setOnClickListener {
            currentCompany?.website?.let { website ->
                if (website.isNotEmpty()) {
                    openWebsite(website)
                }
            }
        }
        
        editCompanyButton.setOnClickListener {
            currentCompany?.let { company ->
                editCompany(company)
            }
        }
        
        deleteCompanyButton.setOnClickListener {
            currentCompany?.let { company ->
                confirmDeleteCompany(company)
            }
        }

        // En setupClickListeners():
        openMapButton.setOnClickListener {
            currentCompany?.let { company ->
                if (company.ubication.isNotBlank()) {
                    openLocationInMaps(company)
                } else {
                    Toast.makeText(requireContext(), "La compañía no tiene ubicación registrada", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                
                // Observar estado de carga
                launch {
                    viewModel.isLoading.collect { isLoading ->
                        Log.d("CompanyDetailFragment", "Loading state: $isLoading")
                        updateLoadingState(isLoading)
                    }
                }
                
                // Observar errores
                launch {
                    viewModel.error.collect { error ->
                        error?.let {
                            Log.e("CompanyDetailFragment", "Error: $it")
                            showError(it)
                            viewModel.clearError()
                        }
                    }
                }
                
                // Observar resultados de operaciones
                launch {
                    viewModel.operationResult.collect { result ->
                        result?.let {
                            Log.d("CompanyDetailFragment", "Operation result: $it")
                            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                            viewModel.clearOperationResult()
                        }
                    }
                }
            }
        }
    }
    
    private fun getCurrentUserAndLoadCompany() {
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        
        if (currentUserId == null) {
            Log.w("CompanyDetailFragment", "No authenticated user found")
            showError("Usuario no autenticado")
            findNavController().navigateUp()
            return
        }
        
        // Obtener ID de la compañía desde argumentos
        val companyId = arguments?.getString("companyId") ?: ""
        Log.d("CompanyDetailFragment", "Received companyId from arguments: '$companyId'")
        
        if (companyId.isNotEmpty()) {
            Log.d("CompanyDetailFragment", "Loading company: $companyId")
            loadCompanyById(companyId)
        } else {
            Log.e("CompanyDetailFragment", "No company ID provided in arguments")
            showError("ID de compañía no encontrado")
            findNavController().navigateUp()
        }
    }
    
    private fun loadCompanyById(companyId: String) {
        viewModel.getCompanyById(companyId) { company ->
            if (company != null) {
                Log.d("CompanyDetailFragment", "Company loaded: ${company.name}")
                currentCompany = company
                try {
                    displayCompanyData(company)
                } catch (e: Exception) {
                    Log.e("CompanyDetailFragment", "Error displaying company data: ${e.message}")
                    showError("Error al mostrar los datos de la compañía")
                    findNavController().navigateUp()
                }
            } else {
                Log.e("CompanyDetailFragment", "Company not found")
                showError("Compañía no encontrada")
                findNavController().navigateUp()
            }
        }
    }
    
    private fun displayCompanyData(company: Company) {
        Log.d("CompanyDetailFragment", "Displaying company data for: ${company.name}")
        
        // Actualizar título del toolbar
        toolbar.title = company.name
        
        // Llenar datos básicos
        companyName.text = company.name
        companyType.text = company.type
        companyDescription.text = company.description.ifEmpty { "Sin descripción disponible" }
        companyAddress.text = company.address.ifEmpty { "Dirección no especificada" }
        companyCityCountry.text = "${company.city}, ${company.country}"
        
        // Información de contacto
        if (company.phone.isNotEmpty()) {
            companyPhone.text = company.phone
            phoneLayout.visibility = View.VISIBLE
        } else {
            phoneLayout.visibility = View.GONE
        }
        
        if (company.email.isNotEmpty()) {
            companyEmail.text = company.email
            emailLayout.visibility = View.VISIBLE
        } else {
            emailLayout.visibility = View.GONE
        }
        // Mostrar botón de mapa si hay dirección
        if (company.address.isNotEmpty() && company.city.isNotEmpty() && company.country.isNotEmpty()) {
            openMapButton.visibility = View.VISIBLE
        } else {
            openMapButton.visibility = View.GONE
        }
        if (company.website.isNotEmpty()) {
            companyWebsite.text = company.website
            websiteLayout.visibility = View.VISIBLE
        } else {
            websiteLayout.visibility = View.GONE
        }
        
        // Chips informativos
        val employeeText = when (company.size) {
            com.example.hirelink_2025.models.CompanySize.STARTUP -> "1-10 empleados"
            com.example.hirelink_2025.models.CompanySize.SMALL -> "11-50 empleados"
            com.example.hirelink_2025.models.CompanySize.MEDIUM -> "51-200 empleados"
            com.example.hirelink_2025.models.CompanySize.LARGE -> "201-1000 empleados"
            com.example.hirelink_2025.models.CompanySize.ENTERPRISE -> "1000+ empleados"
        }
        employeeCountChip.text = employeeText
        
        // TODO: Get active jobs count from Firestore or remove this feature
        // val jobsText = "Jobs: N/A"
        // activeJobsChip.text = jobsText
        
        if (company.foundedYear > 0) {
            foundedYearChip.text = "Fundada en ${company.foundedYear}"
            foundedYearChip.visibility = View.VISIBLE
        } else {
            foundedYearChip.visibility = View.GONE
        }
        
        // Cargar logo de la compañía
        loadCompanyLogo(company.logoUrl)
    }
    
    private fun loadCompanyLogo(logoUrl: String) {
        if (logoUrl.isNotEmpty()) {
            Log.d("CompanyDetailFragment", "Loading company logo from: $logoUrl")
            
            Glide.with(this)
                .load(logoUrl)
                .fitCenter()
                .placeholder(R.drawable.ic_group)
                .error(R.drawable.ic_group)
                .into(companyLogo)
        } else {
            Log.d("CompanyDetailFragment", "No logo URL provided, using default")
            // Usar imagen por defecto sin transformaciones
            companyLogo.setImageResource(R.drawable.ic_group)
        }
    }
    
    private fun updateLoadingState(isLoading: Boolean) {
        if (isLoading) {
            progressIndicator.visibility = View.VISIBLE
        } else {
            progressIndicator.visibility = View.GONE
        }
    }
    
    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
    
    private fun editCompany(company: Company) {
        Log.d("CompanyDetailFragment", "Edit company: ${company.name}")
        viewModel.selectCompany(company)
        val bundle = Bundle().apply {
            putString("companyId", company.id)
        }
        findNavController().navigate(R.id.action_companyDetailFragment_to_companyEditFragment, bundle)
    }
    
    private fun confirmDeleteCompany(company: Company) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar Compañía")
            .setMessage("¿Estás seguro de que deseas eliminar '${company.name}'? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                deleteCompany(company)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun deleteCompany(company: Company) {
        currentUserId?.let { ownerId ->
            Log.d("CompanyDetailFragment", "Deleting company: ${company.name}")
            viewModel.deleteCompany(company.id, ownerId) { success ->
                if (success) {
                    Log.d("CompanyDetailFragment", "Company deleted successfully")
                    findNavController().navigateUp()
                } else {
                    Log.e("CompanyDetailFragment", "Failed to delete company")
                }
            }
        }
    }
    
    private fun dialPhone(phone: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phone")
            }
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("CompanyDetailFragment", "Error opening dialer", e)
            Toast.makeText(requireContext(), "No se pudo abrir el marcador", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun sendEmail(email: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$email")
            }
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("CompanyDetailFragment", "Error opening email client", e)
            Toast.makeText(requireContext(), "No se pudo abrir el cliente de email", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun openWebsite(website: String) {
        try {
            val url = if (!website.startsWith("http://") && !website.startsWith("https://")) {
                "https://$website"
            } else {
                website
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("CompanyDetailFragment", "Error opening website", e)
            Toast.makeText(requireContext(), "No se pudo abrir el sitio web", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openLocationInMaps(company: Company) {
        // Prioridad 1: Usar coordenadas exactas si están disponibles
        if (company.ubication.isNotBlank()) {
            val coordinates = company.ubication.parseLatLng()
            if (coordinates != null) {
                val (lat, lng) = coordinates
                if (lat != 0.0 || lng != 0.0) {
                    navigateToMap(lat, lng, company.name)
                    return
                }
            }
        }
        
        // Prioridad 2: Usar direccón para abrir en Google Maps externo
        if (company.address.isNotEmpty() && company.city.isNotEmpty()) {
            openInExternalMaps(company)
            return
        }
        
        // Si no hay información suficiente
        Toast.makeText(requireContext(), "La empresa no tiene ubicación registrada", Toast.LENGTH_SHORT).show()
    }
    
    private fun navigateToMap(lat: Double, lng: Double, locationName: String) {
        val args = bundleOf(
            "latitude" to lat,
            "longitude" to lng,
            "locationName" to locationName
        )

        try {
            findNavController().navigate(
                R.id.action_companyDetailFragment_to_mapFragment,
                args
            )
        } catch (e: Exception) {
            Log.e("Navigation", "Error: ${e.message}")
            Toast.makeText(requireContext(), "Error al abrir el mapa", Toast.LENGTH_SHORT).show()
        }
    }


    private fun openInExternalMaps(company: Company) {
        try {
            val address = buildString {
                append(company.address)
                if (company.city.isNotEmpty()) {
                    append(", ${company.city}")
                }
                if (company.country.isNotEmpty()) {
                    append(", ${company.country}")
                }
            }
            
            val encodedAddress = Uri.encode(address)
            val gmmIntentUri = Uri.parse("geo:0,0?q=$encodedAddress")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            
            if (mapIntent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(mapIntent)
            } else {
                // Fallback: abrir en navegador web
                val webUrl = "https://www.google.com/maps/search/?api=1&query=$encodedAddress"
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl))
                startActivity(webIntent)
            }
        } catch (e: Exception) {
            Log.e("CompanyDetailFragment", "Error opening external maps", e)
            Toast.makeText(requireContext(), "No se pudo abrir Google Maps", Toast.LENGTH_SHORT).show()
        }
    }

    //MOVERLO A VIEWMODEL.
    // Versión mejorada del parseo
    fun String.parseLatLng(): Pair<Double, Double>? {
        return try {
            val pattern = "Lat:\\s*(-?\\d+\\.\\d+),\\s*Lng:\\s*(-?\\d+\\.\\d+)".toRegex()
            val match = pattern.find(this.trim()) ?: return null

            val (latStr, lngStr) = match.destructured
            val lat = latStr.toDoubleOrNull()
            val lng = lngStr.toDoubleOrNull()

            if (lat != null && lng != null) lat to lng else null
        } catch (e: Exception) {
            Log.e("MapUtils", "Error parsing coordinates: ${e.message}")
            null
        }
    }

}