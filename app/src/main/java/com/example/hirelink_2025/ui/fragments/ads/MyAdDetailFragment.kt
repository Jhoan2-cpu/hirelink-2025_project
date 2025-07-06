package com.example.hirelink_2025.ui.fragments.ads

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.hirelink_2025.R
import androidx.navigation.fragment.findNavController
import com.example.hirelink_2025.databinding.FragmentMyAdDetailBinding

/**
 * Fragment específico para mostrar detalles de anuncios desde MyAdsFragment
 */
class MyAdDetailFragment : Fragment() {

    private var _binding: FragmentMyAdDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyAdDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        loadJobData()
    }

    private fun setupUI() {

        // Configurar otros botones específicos para "mis anuncios"
        binding.editJobButton?.setOnClickListener {
            navigateToEdit()
        }

        binding.viewApplicantsButton?.setOnClickListener {
            navigateToApplicants()
        }

        binding.deleteJobButton?.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun loadJobData() {
        arguments?.let { args ->
            binding.apply {
                jobTitleText.text = args.getString("job_title", "")
                jobDescriptionText.text = args.getString("job_description", "")
                jobRequirementsText.text = args.getString("job_requirements", "")
                jobPostedDateText.text = args.getString("job_posted_date", "")
                jobEmploymentTypeText.text = args.getString("job_employment_type", "")
                jobModalityText.text = args.getString("job_modality", "")
                jobStatusText.text = args.getString("job_status", "")
                jobPhoneText.text = args.getString("job_phone", "")
                jobEmailText.text = args.getString("job_email", "")
            }
        }
    }

    private fun navigateToEdit() {
        val jobId = arguments?.getString("job_id") ?: return
        val jobTitle = arguments?.getString("job_title") ?: return

        findNavController().navigate(
            R.id.action_myAdDetailFragment_to_myAdsEditFragment,
            Bundle().apply {
                putString("job_id", jobId)
                putString("job_title", jobTitle)
            }
        )
    }

    private fun navigateToApplicants() {
        val jobId = arguments?.getString("job_id") ?: return
        val jobTitle = arguments?.getString("job_title") ?: return

        findNavController().navigate(
            R.id.action_myAdDetailFragment_to_myAdsApplicantsFragment,
            Bundle().apply {
                putString("job_id", jobId)
                putString("job_title", jobTitle)
            }
        )
    }

    private fun showDeleteConfirmation() {
        // Implementar confirmación de eliminación si es necesario
    }

    companion object {
        fun newInstance(
            jobId: String,
            titulo: String,
            descripcion: String,
            habilidades: String,
            fecha: String,
            tipoEmpleo: String,
            modalidad: String,
            estado: String,
            telefono: String,
            email: String
        ): MyAdDetailFragment {
            val fragment = MyAdDetailFragment()
            val args = Bundle().apply {
                putString("job_id", jobId)
                putString("job_title", titulo)
                putString("job_description", descripcion)
                putString("job_requirements", habilidades)
                putString("job_posted_date", fecha)
                putString("job_employment_type", tipoEmpleo)
                putString("job_modality", modalidad)
                putString("job_status", estado)
                putString("job_phone", telefono)
                putString("job_email", email)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}