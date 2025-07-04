
// ProfileEditSkillsFragment.kt
package com.example.hirelink_2025.ui.fragments.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hirelink_2025.databinding.FragmentProfileEditSkillsBinding
import com.example.hirelink_2025.ui.adapters.SkillEditAdapter

class ProfileEditSkillsFragment : Fragment() {

    private lateinit var binding: FragmentProfileEditSkillsBinding
    private lateinit var skillAdapter: SkillEditAdapter
    private val skills = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileEditSkillsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        loadExistingSkills()
    }

    private fun setupRecyclerView() {
        skillAdapter = SkillEditAdapter(
            skills = skills,
            onEditClick = { skill -> showEditSkillDialog(skill) },
            onDeleteClick = { skill -> deleteSkill(skill) },
            onInfoClick = { skill -> showSkillInfo(skill) }
        )

        binding.skillsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = skillAdapter
        }
    }

    private fun setupClickListeners() {
        binding.addSkillButton.setOnClickListener {
            showAddSkillDialog()
        }
    }

    private fun loadExistingSkills() {
        // Cargar habilidades existentes
        skills.addAll(listOf(
            "Programador en Python",
            "Manejo de Power BI"
        ))
        skillAdapter.notifyDataSetChanged()
    }

    private fun showAddSkillDialog() {
        showSkillDialog(null)
    }

    private fun showEditSkillDialog(skill: String) {
        showSkillDialog(skill)
    }

    private fun showSkillDialog(existingSkill: String?) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(
            com.example.hirelink_2025.R.layout.dialog_add_skill, null
        )

        val skillInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(
            com.example.hirelink_2025.R.id.skillNameInput
        )
        val saveButton = dialogView.findViewById<com.google.android.material.button.MaterialButton>(
            com.example.hirelink_2025.R.id.saveSkillButton
        )
        val cancelButton = dialogView.findViewById<com.google.android.material.button.MaterialButton>(
            com.example.hirelink_2025.R.id.cancelSkillButton
        )

        // Si estamos editando, llenar el campo
        existingSkill?.let {
            skillInput.setText(it)
        }

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        saveButton.setOnClickListener {
            val skillName = skillInput.text.toString().trim()

            if (skillName.isNotEmpty()) {
                if (existingSkill != null) {
                    // Editar existente
                    val index = skills.indexOf(existingSkill)
                    if (index != -1) {
                        skills[index] = skillName
                    }
                } else {
                    // Agregar nuevo
                    skills.add(skillName)
                }

                skillAdapter.notifyDataSetChanged()
                dialog.dismiss()
            }
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun deleteSkill(skill: String) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Eliminar Habilidad")
        builder.setMessage("¿Estás seguro de que quieres eliminar esta habilidad?")

        builder.setPositiveButton("Eliminar") { _, _ ->
            skills.remove(skill)
            skillAdapter.notifyDataSetChanged()
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun showSkillInfo(skill: String) {
        android.widget.Toast.makeText(requireContext(), "Información sobre: $skill", android.widget.Toast.LENGTH_SHORT).show()
    }

    companion object {
        fun newInstance() = ProfileEditSkillsFragment()
    }
}