package com.example.hirelink_2025.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hirelink_2025.R
import com.example.hirelink_2025.databinding.ItemApplicantBinding
import com.example.hirelink_2025.models.Applicant
import com.example.hirelink_2025.models.ApplicationStatus

/**
 * Adapter para mostrar lista de aplicantes
 * MVVM: Solo maneja presentación, callbacks delegados al Fragment/ViewModel
 */
class ApplicantAdapter(
    private val onViewProfileClick: (Applicant) -> Unit,
    private val onAcceptClick: ((Applicant) -> Unit)? = null,
    private val onRejectClick: ((Applicant) -> Unit)? = null
) : ListAdapter<Applicant, ApplicantAdapter.ApplicantViewHolder>(ApplicantDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicantViewHolder {
        val binding = ItemApplicantBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ApplicantViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ApplicantViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ApplicantViewHolder(
        private val binding: ItemApplicantBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(applicant: Applicant) {
            // Datos básicos
            binding.applicantName.text = applicant.name
            binding.applicantProfession.text = applicant.profession
            binding.applicantExperience.text = applicant.experience
            binding.applicantEmail.text = applicant.email
            binding.applicantPhone.text = applicant.phone
            binding.applicationDate.text = "Aplicó el ${applicant.applicationDate}"

            // Habilidades
            binding.applicantSkills.text = applicant.skills.joinToString(", ")

            // Estado visual según status
            updateStatusAppearance(applicant.status)

            // Configurar botones según callbacks disponibles
            setupActionButtons(applicant)

            // Click en perfil
            binding.root.setOnClickListener {
                onViewProfileClick(applicant)
            }
        }

        /**
         * Actualizar apariencia según estado
         */
        private fun updateStatusAppearance(status: ApplicationStatus) {
            binding.statusIndicator.apply {
                when (status) {
                    ApplicationStatus.PENDING -> {
                        setBackgroundColor(ContextCompat.getColor(context, R.color.orange_pending))
                        text = "Pendiente"
                    }
                    ApplicationStatus.ACCEPTED -> {
                        setBackgroundColor(ContextCompat.getColor(context, R.color.green_accept))
                        text = "Aceptado"
                    }
                    ApplicationStatus.REJECTED -> {
                        setBackgroundColor(ContextCompat.getColor(context, R.color.red_reject))
                        text = "Rechazado"
                    }
                }
            }
        }

        /**
         * Configurar botones de acción
         */
        private fun setupActionButtons(applicant: Applicant) {
            // Botón aceptar
            binding.acceptButton.apply {
                if (onAcceptClick != null && applicant.status == ApplicationStatus.PENDING) {
                    visibility = View.VISIBLE
                    setOnClickListener { onAcceptClick.invoke(applicant) }
                } else {
                    visibility = View.GONE
                }
            }

            // Botón rechazar
            binding.rejectButton.apply {
                if (onRejectClick != null && applicant.status != ApplicationStatus.REJECTED) {
                    visibility = View.VISIBLE
                    setOnClickListener { onRejectClick.invoke(applicant) }
                } else {
                    visibility = View.GONE
                }
            }
        }
    }

    /**
     * DiffUtil para optimizar actualizaciones
     */
    class ApplicantDiffCallback : DiffUtil.ItemCallback<Applicant>() {
        override fun areItemsTheSame(oldItem: Applicant, newItem: Applicant): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Applicant, newItem: Applicant): Boolean {
            return oldItem == newItem
        }
    }
}