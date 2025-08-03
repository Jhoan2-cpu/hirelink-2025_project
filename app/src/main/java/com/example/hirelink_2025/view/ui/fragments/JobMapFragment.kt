package com.example.hirelink_2025.view.ui.fragments

import android.Manifest
import android.animation.ValueAnimator
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.hirelink_2025.R
import com.example.hirelink_2025.databinding.FragmentJobMapBinding
import com.example.hirelink_2025.models.CompanyLocation
import com.example.hirelink_2025.utils.LocationUtils
import com.example.hirelink_2025.viewmodels.JobMapViewModel
import com.example.hirelink_2025.viewmodels.ViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class JobMapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentJobMapBinding? = null
    private val binding get() = _binding!!

    private val viewModel: JobMapViewModel by viewModels {
        ViewModelFactory()
    }

    private var googleMap: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val locationUtils = LocationUtils()
    
    // Mapa de marcadores para animaciones
    private val markerAnimations = mutableMapOf<Marker, ValueAnimator>()
    private val companyMarkers = mutableMapOf<Marker, CompanyLocation>()

    // Solicitud de permisos de ubicación
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                enableMyLocation()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                enableMyLocation()
            }
            else -> {
                showMessage("Permisos de ubicación denegados")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJobMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupMap()
        observeViewModel()
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.myLocationButton.setOnClickListener {
            centerMapOnUserLocation()
        }

        binding.filterFab.setOnClickListener {
            showFilterDialog()
        }
    }

    private fun setupMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        
        // Configurar el mapa
        setupMapSettings()
        
        // Solicitar permisos de ubicación
        requestLocationPermissions()
        
        // Cargar ubicaciones de empleos
        viewModel.loadJobLocations()
        
        // Configurar click listeners del mapa
        setupMapClickListeners()
    }

    private fun setupMapSettings() {
        googleMap?.apply {
            uiSettings.isZoomControlsEnabled = true
            uiSettings.isCompassEnabled = true
            uiSettings.isMapToolbarEnabled = false
            
            // Centrar en Lima, Perú por defecto
            val lima = LatLng(-12.0464, -77.0428)
            moveCamera(CameraUpdateFactory.newLatLngZoom(lima, 10f))
        }
    }

    private fun setupMapClickListeners() {
        googleMap?.setOnMarkerClickListener { marker ->
            val companyLocation = companyMarkers[marker]
            if (companyLocation != null) {
                handleMarkerClick(companyLocation)
                true
            } else {
                false
            }
        }

        googleMap?.setOnInfoWindowClickListener { marker ->
            val companyLocation = companyMarkers[marker]
            if (companyLocation != null) {
                navigateToCompanyJobs(companyLocation)
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                android.util.Log.d("JobMapFragment", "ViewModel state updated:")
                android.util.Log.d("JobMapFragment", "- Loading: ${state.isLoading}")
                android.util.Log.d("JobMapFragment", "- Company locations: ${state.companyLocations.size}")
                android.util.Log.d("JobMapFragment", "- Total jobs: ${state.totalJobs}")
                android.util.Log.d("JobMapFragment", "- Total companies: ${state.totalCompanies}")
                android.util.Log.d("JobMapFragment", "- Nearby companies: ${state.nearbyCompanies}")
                android.util.Log.d("JobMapFragment", "- Error: ${state.error}")
                
                updateLoadingState(state.isLoading)
                
                if (state.error != null) {
                    android.util.Log.e("JobMapFragment", "Error received: ${state.error}")
                    showMessage(state.error)
                    viewModel.clearError()
                }

                updateStatistics(state.totalJobs, state.totalCompanies, state.nearbyCompanies)
                
                if (state.companyLocations.isNotEmpty()) {
                    android.util.Log.d("JobMapFragment", "Displaying ${state.companyLocations.size} company locations on map")
                    displayCompanyMarkers(state.companyLocations)
                } else {
                    android.util.Log.w("JobMapFragment", "No company locations to display")
                }
            }
        }
    }

    private fun requestLocationPermissions() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                enableMyLocation()
            }
            else -> {
                locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap?.isMyLocationEnabled = true
            getUserLocation()
        }
    }

    private fun getUserLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    viewModel.updateUserLocation(it.latitude, it.longitude)
                    centerMapOnLocation(LatLng(it.latitude, it.longitude))
                }
            }
        }
    }

    private fun centerMapOnUserLocation() {
        getUserLocation()
    }

    private fun centerMapOnLocation(latLng: LatLng) {
        googleMap?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(latLng, 12f),
            1000,
            null
        )
    }

    private fun displayCompanyMarkers(companyLocations: List<CompanyLocation>) {
        android.util.Log.d("JobMapFragment", "Displaying ${companyLocations.size} company markers")
        
        googleMap?.let { map ->
            // Limpiar marcadores anteriores
            clearMarkers()

            companyLocations.forEach { companyLocation ->
                android.util.Log.d("JobMapFragment", "Creating marker for ${companyLocation.company.name} at (${companyLocation.latitude}, ${companyLocation.longitude})")
                
                val latLng = LatLng(companyLocation.latitude, companyLocation.longitude)
                
                val markerOptions = MarkerOptions()
                    .position(latLng)
                    .title(companyLocation.getMarkerTitle())
                    .snippet(companyLocation.getMarkerSnippet())
                    .icon(getMarkerIcon(companyLocation))

                val marker = map.addMarker(markerOptions)
                if (marker != null) {
                    companyMarkers[marker] = companyLocation
                    android.util.Log.d("JobMapFragment", "Marker created successfully for ${companyLocation.company.name}")
                    
                    // Animar marcadores cercanos
                    if (companyLocation.isNearby()) {
                        android.util.Log.d("JobMapFragment", "Starting animation for nearby company: ${companyLocation.company.name}")
                        startMarkerAnimation(marker)
                    }
                } else {
                    android.util.Log.e("JobMapFragment", "Failed to create marker for ${companyLocation.company.name}")
                }
            }
            
            android.util.Log.d("JobMapFragment", "Total markers created: ${companyMarkers.size}")
        } ?: run {
            android.util.Log.e("JobMapFragment", "GoogleMap is null, cannot display markers")
        }
    }

    private fun getMarkerIcon(companyLocation: CompanyLocation): BitmapDescriptor {
        val iconResource = when {
            companyLocation.isNearby() -> R.drawable.ic_work // Icono especial para cercanos
            companyLocation.activeJobsCount > 5 -> R.drawable.ic_work // Icono para muchas ofertas
            else -> R.drawable.ic_work_small // Icono normal
        }

        val color = when {
            companyLocation.isNearby() -> ContextCompat.getColor(requireContext(), R.color.success)
            companyLocation.activeJobsCount > 5 -> ContextCompat.getColor(requireContext(), R.color.primary)
            else -> ContextCompat.getColor(requireContext(), R.color.secondary)
        }

        return createColoredMarkerIcon(iconResource, color)
    }

    private fun createColoredMarkerIcon(iconResource: Int, color: Int): BitmapDescriptor {
        val drawable: Drawable? = ContextCompat.getDrawable(requireContext(), iconResource)
        drawable?.setTint(color)
        
        val bitmap = Bitmap.createBitmap(
            drawable?.intrinsicWidth ?: 48,
            drawable?.intrinsicHeight ?: 48,
            Bitmap.Config.ARGB_8888
        )
        
        val canvas = Canvas(bitmap)
        drawable?.setBounds(0, 0, canvas.width, canvas.height)
        drawable?.draw(canvas)
        
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun startMarkerAnimation(marker: Marker) {
        val animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1500
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            
            addUpdateListener { animation ->
                val alpha = animation.animatedValue as Float
                marker.alpha = 0.3f + (alpha * 0.7f) // Parpadeo entre 0.3 y 1.0
            }
        }
        
        markerAnimations[marker] = animator
        animator.start()
    }

    private fun clearMarkers() {
        // Detener animaciones
        markerAnimations.values.forEach { it.cancel() }
        markerAnimations.clear()
        
        // Limpiar marcadores
        companyMarkers.clear()
        googleMap?.clear()
    }

    private fun handleMarkerClick(companyLocation: CompanyLocation) {
        // Navegar directamente a las ofertas de la empresa
        navigateToCompanyJobs(companyLocation)
    }

    private fun navigateToCompanyJobs(companyLocation: CompanyLocation) {
        // Navegar directamente a SearchResultsFragment con filtros de empresa
        val bundle = Bundle().apply {
            putString("companyId", companyLocation.company.id)
            putString("companyName", companyLocation.company.name)
            putString("searchType", "company_jobs")
        }
        
        findNavController().navigate(
            R.id.action_jobMapFragment_to_searchResultsFragment,
            bundle
        )
    }

    private fun showFilterDialog() {
        val distances = arrayOf("Todas", "Menos de 5 km", "Menos de 10 km", "Menos de 25 km")
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Filtrar por distancia")
        builder.setItems(distances) { _, which ->
            when (which) {
                0 -> viewModel.resetFilters()
                1 -> viewModel.filterByDistance(5.0)
                2 -> viewModel.filterByDistance(10.0)
                3 -> viewModel.filterByDistance(25.0)
            }
        }
        builder.show()
    }

    private fun updateLoadingState(isLoading: Boolean) {
        binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun updateStatistics(totalJobs: Int, totalCompanies: Int, nearbyCompanies: Int) {
        binding.totalJobsCount.text = totalJobs.toString()
        binding.companiesCount.text = totalCompanies.toString()
        binding.nearbyCount.text = nearbyCompanies.toString()
    }

    private fun showMessage(message: String, isLong: Boolean = false) {
        val duration = if (isLong) Snackbar.LENGTH_LONG else Snackbar.LENGTH_SHORT
        Snackbar.make(binding.root, message, duration).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Limpiar animaciones
        markerAnimations.values.forEach { it.cancel() }
        markerAnimations.clear()
        _binding = null
    }
}