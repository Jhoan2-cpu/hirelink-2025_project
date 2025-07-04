package com.example.hirelink_2025.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hirelink_2025.R
import com.example.hirelink_2025.databinding.ItemApplicantCardBinding
import com.example.hirelink_2025.models.Applicant
import com.example.hirelink_2025.models.ApplicationStatus

class ApplicantAdapter(
    private val onViewProfileClick: (Applicant) -> Unit,
    private val onAcceptClick: (Applicant) -> Unit,
    private val onRejectClick: (Applicant) -> Unit
) : ListAdapter<Applicant, ApplicantAdapter.ApplicantViewHolder>(ApplicantDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicantViewHolder {
        val binding = ItemApplicantCardBinding.inflate(
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
        private val binding: ItemApplicantCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(applicant: Applicant) = with(binding) {
            // Información básica
            applicantName.text = applicant.name
            applicantProfession.text = applicant.profession
            applicantExperience.text = applicant.experience
            applicationDate.text = applicant.applicationDate

            // Mostrar habilidades (máximo 3 para evitar que se vea muy largo)
            val skillsString = applicant.skills.take(3).joinToString(", ")
            binding.skillsText.text = if (applicant.skills.size > 3) {
                "$skillsString, +${applicant.skills.size - 3} más"
            } else {
                skillsString
            }

            // Configurar chip de estado
            setupStatusChip(applicant.status)

            // Configurar botones según el estado
            setupActionButtons(applicant)

            // Click listeners
            applicantCard.setOnClickListener { onViewProfileClick(applicant) }
            viewProfileButton.setOnClickListener { onViewProfileClick(applicant) }
            acceptButton.setOnClickListener { onAcceptClick(applicant) }
            rejectButton.setOnClickListener { onRejectClick(applicant) }
        }

        private fun setupStatusChip(status: ApplicationStatus) = with(binding) {
            when (status) {
                ApplicationStatus.PENDING -> {
                    statusChip.text = "Pendiente"
                    statusChip.setChipBackgroundColorResource(R.color.warning)
                    statusChip.setTextColor(ContextCompat.getColor(itemView.context, R.color.warning_text))
                }
                ApplicationStatus.ACCEPTED -> {
                    statusChip.text = "Aceptado"
                    statusChip.setChipBackgroundColorResource(R.color.success)
                    statusChip.setTextColor(ContextCompat.getColor(itemView.context, R.color.success_text))
                }
                ApplicationStatus.REJECTED -> {
                    statusChip.text = "Rechazado"
                    statusChip.setChipBackgroundColorResource(R.color.error)
                    statusChip.setTextColor(ContextCompat.getColor(itemView.context, R.color.error_text))
                }
            }
        }

        private fun setupActionButtons(applicant: Applicant) = with(binding) {
            when (applicant.status) {
                ApplicationStatus.PENDING -> {
                    // Mostrar botones de aceptar y rechazar
                    acceptButton.visibility = android.view.View.VISIBLE
                    rejectButton.visibility = android.view.View.VISIBLE
                    acceptButton.text = "Contratar"
                    rejectButton.text = "Rechazar"
                }
                ApplicationStatus.ACCEPTED -> {
                    // Mostrar solo opción de despedir
                    acceptButton.visibility = android.view.View.GONE
                    rejectButton.visibility = android.view.View.VISIBLE
                    rejectButton.text = "Despedir"
                }
                ApplicationStatus.REJECTED -> {
                    // Ocultar botones de acción
                    acceptButton.visibility = android.view.View.GONE
                    rejectButton.visibility = android.view.View.GONE
                }
            }
        }
    }

    class ApplicantDiffCallback : DiffUtil.ItemCallback<Applicant>() {
        override fun areItemsTheSame(oldItem: Applicant, newItem: Applicant): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Applicant, newItem: Applicant): Boolean {
            return oldItem == newItem
        }
    }
}