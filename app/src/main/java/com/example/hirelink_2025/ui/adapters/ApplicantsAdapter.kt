package com.example.hirelink_2025.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hirelink_2025.R
import com.example.hirelink_2025.databinding.ItemApplicantBinding
import com.example.hirelink_2025.models.Applicant
import com.example.hirelink_2025.models.ApplicationStatus

class ApplicantsAdapter(
    private val onApplicantClick: (Applicant) -> Unit
) : ListAdapter<Applicant, ApplicantsAdapter.ApplicantViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicantViewHolder {
        val binding = ItemApplicantBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ApplicantViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ApplicantViewHolder, position: Int) {
        val applicant = getItem(position)
        holder.bind(applicant)

        // Click en toda la tarjeta para ver perfil
        holder.itemView.setOnClickListener {
            onApplicantClick(applicant)
        }
    }

    inner class ApplicantViewHolder(
        private val binding: ItemApplicantBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(applicant: Applicant) {
            binding.apply {
                // Información básica del aplicante
                applicantName.text = applicant.name
                applicantProfession.text = applicant.profession
                applicantEmail.text = applicant.email

                // Fecha de aplicación
                applicationDate.text = "Aplicó: ${applicant.applicationDate}"

                // Estado con colores (usando textView genérico para el estado)
                val statusText = when (applicant.status) {
                    ApplicationStatus.PENDING -> "Pendiente"
                    ApplicationStatus.ACCEPTED -> "Aceptado"
                    ApplicationStatus.REJECTED -> "Rechazado"
                }

                // Asumiendo que hay un TextView para mostrar el estado
                // Si no existe, puedes agregar el estado al final del nombre o profesión
                applicantProfession.text = "${applicant.profession} • $statusText"
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Applicant>() {
        override fun areItemsTheSame(oldItem: Applicant, newItem: Applicant): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Applicant, newItem: Applicant): Boolean {
            return oldItem == newItem
        }
    }
}