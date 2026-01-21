
// ProfileSkillsFragment.kt
package com.example.hirelink_2025.view.ui.fragments.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hirelink_2025.databinding.FragmentProfileSkillsBinding

class ProfileSkillsFragment : Fragment() {

    private lateinit var binding: FragmentProfileSkillsBinding
    private val additionalSkills = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileSkillsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        // Setup add skill button
        binding.addSkillButton.setOnClickListener {
            Toast.makeText(requireContext(), "Función para agregar habilidad", Toast.LENGTH_SHORT).show()
            // TODO: Implement add skill functionality
            addSampleSkill()
        }
    }

    private fun setupRecyclerView() {
        binding.skillsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        // TODO: Setup adapter for additional skills
    }

    private fun addSampleSkill() {
        // Sample function to demonstrate adding skills
        additionalSkills.add("Nueva habilidad ${additionalSkills.size + 1}")
        // TODO: Update RecyclerView adapter
    }

    companion object {
        fun newInstance() = ProfileSkillsFragment()
    }
}