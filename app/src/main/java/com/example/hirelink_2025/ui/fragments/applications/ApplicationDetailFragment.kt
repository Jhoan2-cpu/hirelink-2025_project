package com.example.hirelink_2025.ui.fragments.applications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.hirelink_2025.databinding.FragmentApplicationDetailBinding

class ApplicationDetailFragment : Fragment() {

    private var _binding: FragmentApplicationDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentApplicationDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        loadApplicationData()
    }

    private fun setupUI() {
        binding.cancelApplicationButton.setOnClickListener {
            showCancelConfirmation()
        }
    }

    private fun loadApplicationData() {
        arguments?.let { args ->
            binding.apply {
                jobTitleText.text = args.getString("job_title", "")
                companyNameText.text = args.getString("company_name", "")
                applicationDateText.text = args.getString("application_date", "")

                jobDescriptionText.text = args.getString("job_description", "Sin descripción")
                jobRequirementsText.text = args.getString("job_requirements", "Sin requisitos específicos")
                employmentTypeText.text = args.getString("employment_type", "No especificado")
                modalityText.text = args.getString("modality", "No especificado")

                val logoResId = args.getInt("company_logo", 0)
                if (logoResId != 0) {
                    companyLogoImage.setImageResource(logoResId)
                }

                setStatusColor(args.getString("status", ""))
            }
        }
    }

    private fun setStatusColor(status: String) {
        val (backgroundColor, textColor) = when (status) {
            "En revisión" -> Pair(android.R.color.holo_orange_light, android.R.color.white)
            "Aceptado" -> Pair(android.R.color.holo_green_light, android.R.color.white)
            "Rechazado" -> Pair(android.R.color.holo_red_light, android.R.color.white)
            else -> Pair(android.R.color.darker_gray, android.R.color.white)
        }

        binding.statusChip.setChipBackgroundColorResource(backgroundColor)
        binding.statusChip.setTextColor(resources.getColor(textColor, null))
    }

    private fun showCancelConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Cancelar postulación")
            .setMessage("¿Estás seguro de que quieres cancelar esta postulación?")
            .setPositiveButton("Sí, cancelar") { _, _ ->
                cancelApplication()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun cancelApplication() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Postulación cancelada")
            .setMessage("Tu postulación ha sido cancelada exitosamente")
            .setPositiveButton("OK") { _, _ ->
                findNavController().popBackStack()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}