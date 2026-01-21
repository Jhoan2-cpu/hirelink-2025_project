package com.example.hirelink_2025.view.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.hirelink_2025.R
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var locationName: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Verificar si Google Play Services está disponible
        if (!isGooglePlayServicesAvailable()) {
            Toast.makeText(requireContext(), "Google Play Services no está disponible", Toast.LENGTH_LONG).show()
            return
        }

        arguments?.let {
            latitude = it.getDouble("latitude")
            longitude = it.getDouble("longitude")
            locationName = it.getString("locationName", "")

            Log.d("MapFragment", "Coordenadas recibidas - Lat: $latitude, Lng: $longitude")
        }

        if (latitude == 0.0 && longitude == 0.0) {
            Toast.makeText(requireContext(), "Coordenadas no válidas", Toast.LENGTH_SHORT).show()
            return
        }

        setupMap()
    }

    private fun isGooglePlayServicesAvailable(): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(requireContext())
        return resultCode == ConnectionResult.SUCCESS
    }

    private fun setupMap() {
        try {
            val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
            if (mapFragment == null) {
                Log.e("MapFragment", "SupportMapFragment not found in layout")
                Toast.makeText(requireContext(), "Error al cargar el mapa", Toast.LENGTH_SHORT).show()
                return
            }
            mapFragment.getMapAsync(this)
        } catch (e: Exception) {
            Log.e("MapFragment", "Error setting up map", e)
            Toast.makeText(requireContext(), "Error al configurar el mapa: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        try {
            googleMap = map
            
            Log.d("MapFragment", "Google Map ready - Lat: $latitude, Lng: $longitude")

            if (latitude == 0.0 && longitude == 0.0) {
                Toast.makeText(requireContext(), "Coordenadas no válidas", Toast.LENGTH_SHORT).show()
                return
            }

            val location = LatLng(latitude, longitude)
            
            // Configurar el mapa
            googleMap.apply {
                // Habilitar controles del mapa
                uiSettings.isZoomControlsEnabled = true
                uiSettings.isCompassEnabled = true
                uiSettings.isMyLocationButtonEnabled = false
                
                // Agregar marcador
                addMarker(
                    MarkerOptions()
                        .position(location)
                        .title(locationName.ifEmpty { "Ubicación" })
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                )?.showInfoWindow()

                // Mover la cámara con animación
                animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
            }
            
            Log.d("MapFragment", "Map configured successfully")
            
        } catch (e: Exception) {
            Log.e("MapFragment", "Error in onMapReady", e)
            Toast.makeText(requireContext(), "Error al mostrar el mapa: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}