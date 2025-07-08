package com.example.hirelink_2025.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hirelink_2025.R
import com.example.hirelink_2025.databinding.ItemApplicantBinding
import com.example.hirelink_2025.models.Applicant
import com.example.hirelink_2025.models.ApplicationStatus

class ApplicantsAdapter(
    private val onItemClicked: (Applicant) -> Unit,
    private val onAcceptClicked: (Applicant) -> Unit,
    private val onRejectClicked: (Applicant) -> Unit
) : ListAdapter<Applicant, ApplicantsAdapter.ApplicantViewHolder>(ApplicantDiffCallback()) {

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
            binding.apply {
                // Información básica
                applicantName.text = applicant.name
                applicantProfession.text = applicant.profession
                applicantExperience.text = applicant.experience
                applicantSkills.text = applicant.skills.joinToString(", ")
                applicantEmail.text = applicant.email
                applicantPhone.text = applicant.phone
                applicationDate.text = "Aplicó el ${applicant.applicationDate}"

                // Estado del aplicante
                statusIndicator.text = when (applicant.status) {
                    ApplicationStatus.PENDING -> "Pendiente"
                    ApplicationStatus.ACCEPTED -> "Aceptado"
                    ApplicationStatus.REJECTED -> "Rechazado"
                }

                // Color del estado
                val (backgroundColor, textColor) = when (applicant.status) {
                    ApplicationStatus.PENDING -> Pair(R.color.orange_pending, R.color.white)
                    ApplicationStatus.ACCEPTED -> Pair(R.color.green_accept, R.color.white)
                    ApplicationStatus.REJECTED -> Pair(R.color.red_reject, R.color.white)
                }

                statusIndicator.setBackgroundColor(
                    ContextCompat.getColor(binding.root.context, backgroundColor)
                )
                statusIndicator.setTextColor(
                    ContextCompat.getColor(binding.root.context, textColor)
                )

                // Visibilidad de botones según estado
                when (applicant.status) {
                    ApplicationStatus.PENDING -> {
                        acceptButton.visibility = android.view.View.VISIBLE
                        rejectButton.visibility = android.view.View.VISIBLE
                        acceptButton.isEnabled = true
                        rejectButton.isEnabled = true
                    }
                    ApplicationStatus.ACCEPTED -> {
                        acceptButton.visibility = android.view.View.VISIBLE
                        rejectButton.visibility = android.view.View.VISIBLE
                        acceptButton.isEnabled = false
                        rejectButton.isEnabled = true
                        acceptButton.alpha = 0.5f
                        rejectButton.alpha = 1.0f
                    }
                    ApplicationStatus.REJECTED -> {
                        acceptButton.visibility = android.view.View.VISIBLE
                        rejectButton.visibility = android.view.View.VISIBLE
                        acceptButton.isEnabled = true
                        rejectButton.isEnabled = false
                        acceptButton.alpha = 1.0f
                        rejectButton.alpha = 0.5f
                    }
                }

                // Click listeners
                root.setOnClickListener {
                    onItemClicked(applicant)
                }

                acceptButton.setOnClickListener {
                    if (applicant.status != ApplicationStatus.ACCEPTED) {
                        onAcceptClicked(applicant)
                    }
                }

                rejectButton.setOnClickListener {
                    if (applicant.status != ApplicationStatus.REJECTED) {
                        onRejectClicked(applicant)
                    }
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