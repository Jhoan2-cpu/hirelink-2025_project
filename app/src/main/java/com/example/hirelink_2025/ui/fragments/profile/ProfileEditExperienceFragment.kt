// ProfileEditExperienceFragment.kt
package com.example.hirelink_2025.ui.fragments.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hirelink_2025.databinding.FragmentProfileEditExperienceBinding
import com.example.hirelink_2025.ui.adapters.ExperienceEditAdapter

class ProfileEditExperienceFragment : Fragment() {

    private lateinit var binding: FragmentProfileEditExperienceBinding
    private lateinit var experienceAdapter: ExperienceEditAdapter
    private val experiences = mutableListOf<ProfileEditFragment.Experience>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileEditExperienceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        loadExistingExperiences()
    }

    private fun setupRecyclerView() {
        experienceAdapter = ExperienceEditAdapter(
            experiences = experiences,
            onEditClick = { experience -> showEditExperienceDialog(experience) },
            onDeleteClick = { experience -> deleteExperience(experience) }
        )

        binding.experienceRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = experienceAdapter
        }
    }

    private fun setupClickListeners() {
        binding.addExperienceButton.setOnClickListener {
            showAddExperienceDialog()
        }
    }

    private fun loadExistingExperiences() {
        // Cargar experiencias existentes
        experiences.add(
            ProfileEditFragment.Experience(
                position = "Analista",
                company = "Universidad Nacional Del Santo",
                description = "Desarrollador técnico en la empresa x trabajando 2 años.",
                years = 2
            )
        )
        experienceAdapter.notifyDataSetChanged()
    }

    private fun showAddExperienceDialog() {
        showExperienceDialog(null)
    }

    private fun showEditExperienceDialog(experience: ProfileEditFragment.Experience) {
        showExperienceDialog(experience)
    }

    private fun showExperienceDialog(experience: ProfileEditFragment.Experience?) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(
            com.example.hirelink_2025.R.layout.dialog_add_experience, null
        )

        val positionInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(
            com.example.hirelink_2025.R.id.positionInput
        )
        val companyInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(
            com.example.hirelink_2025.R.id.companyInput
        )
        val descriptionInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(
            com.example.hirelink_2025.R.id.descriptionInput
        )
        val yearsInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(
            com.example.hirelink_2025.R.id.yearsInput
        )

        val saveButton = dialogView.findViewById<com.google.android.material.button.MaterialButton>(
            com.example.hirelink_2025.R.id.saveExperienceButton
        )
        val cancelButton = dialogView.findViewById<com.google.android.material.button.MaterialButton>(
            com.example.hirelink_2025.R.id.cancelExperienceButton
        )

        // Si estamos editando, llenar los campos
        experience?.let {
            positionInput.setText(it.position)
            companyInput.setText(it.company)
            descriptionInput.setText(it.description)
            yearsInput.setText(it.years.toString())
        }

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        saveButton.setOnClickListener {
            val position = positionInput.text.toString().trim()
            val company = companyInput.text.toString().trim()
            val description = descriptionInput.text.toString().trim()
            val years = yearsInput.text.toString().toIntOrNull() ?: 0

            if (position.isNotEmpty() && company.isNotEmpty()) {
                val newExperience = ProfileEditFragment.Experience(
                    position = position,
                    company = company,
                    description = description,
                    years = years
                )

                if (experience != null) {
                    // Editar existente
                    val index = experiences.indexOf(experience)
                    if (index != -1) {
                        experiences[index] = newExperience
                    }
                } else {
                    // Agregar nuevo
                    experiences.add(newExperience)
                }

                experienceAdapter.notifyDataSetChanged()
                dialog.dismiss()
            }
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun deleteExperience(experience: ProfileEditFragment.Experience) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Eliminar Experiencia")
        builder.setMessage("¿Estás seguro de que quieres eliminar esta experiencia?")

        builder.setPositiveButton("Eliminar") { _, _ ->
            experiences.remove(experience)
            experienceAdapter.notifyDataSetChanged()
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    companion object {
        fun newInstance() = ProfileEditExperienceFragment()
    }
}
