// ProfileExperienceFragment.kt
package com.example.hirelink_2025.ui.fragments.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.hirelink_2025.databinding.FragmentProfileExperienceBinding

class ProfileExperienceFragment : Fragment() {

    private lateinit var binding: FragmentProfileExperienceBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileExperienceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup experience data
        binding.experiencePosition.text = "Analista"
        binding.experienceDescription.text = "Desarrollador técnico en la empresa x trabajando 2 años."

        // Setup add experience button
        binding.addExperienceButton.setOnClickListener {
            Toast.makeText(requireContext(), "Función para agregar experiencia", Toast.LENGTH_SHORT).show()
            // TODO: Implement add experience functionality
        }
    }

    companion object {
        fun newInstance() = ProfileExperienceFragment()
    }
}
