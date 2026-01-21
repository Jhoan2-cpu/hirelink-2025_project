package com.example.hirelink_2025.view.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.hirelink_2025.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.button.MaterialButton

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MapPickerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MapPickerFragment : Fragment(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private var selectedLatLng: LatLng? = null
    private lateinit var mapView: MapView
    private lateinit var saveButton: MaterialButton
    private lateinit var cancelButton: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_map_picker, container, false)
        mapView = view.findViewById(R.id.mapView)
        saveButton = view.findViewById(R.id.saveButton)
        cancelButton = view.findViewById(R.id.cancelButton)

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        saveButton.setOnClickListener {
            selectedLatLng?.let { latLng ->
                findNavController().previousBackStackEntry?.savedStateHandle?.set("selected_location", latLng)
                findNavController().navigateUp()
            }
        }

        cancelButton.setOnClickListener {
            findNavController().navigateUp()
        }

        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        map.setOnMapClickListener { latLng ->
            map.clear()
            map.addMarker(MarkerOptions().position(latLng).title("Ubicación seleccionada"))
            selectedLatLng = latLng
        }

        val peru = LatLng(-9.19, -75.02)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(peru, 5f))
    }

    // Asegúrate de añadir los lifecycle methods de MapView
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}
