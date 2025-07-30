package com.example.hirelink_2025.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hirelink_2025.R
import com.example.hirelink_2025.databinding.ItemMyAdCardBinding
import com.example.hirelink_2025.models.Job
import com.example.hirelink_2025.models.JobStatus

/**
 * Adapter para MyAds corregido
 * Versión simplificada que funciona con el JobStatus actual
 */
class MyAdAdapter(
    private val onJobClick: (Job) -> Unit,
    private val onEditClick: (Job) -> Unit,
    private val onDeleteClick: (Job) -> Unit,
    private val onApplicantsClick: (Job) -> Unit
) : ListAdapter<Job, MyAdAdapter.JobViewHolder>(JobDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding = ItemMyAdCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return JobViewHolder(binding)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class JobViewHolder(
        private val binding: ItemMyAdCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(job: Job) = with(binding) {
            // Información básica
            jobTitle.text = job.title
            companyName.text = job.companyName
            location.text = job.location
            postedDate.text = job.postedDate
            salary.text = job.salary

            // Estado del trabajo - Versión simplificada
            statusText.text = getStatusText(job.status)
            statusText.setTextColor(getStatusColor(job.status))
            statusText.setBackgroundResource(getStatusBackground(job.status))

            // Estadísticas
            //viewsCount.text = "${job.vacancies * 40} vistas"
            applicantsCount.text = "${job.vacancies * 2} postulantes"

            // Configurar visibilidad de botones basada en el estado
            setupButtonsVisibility(job)

            // Click listeners
            adCard.setOnClickListener { onJobClick(job) }
            editButton.setOnClickListener { onEditClick(job) }
            applicantsButton.setOnClickListener { onApplicantsClick(job) }
        }

        private fun getStatusText(status: JobStatus): String {
            return when (status) {
                JobStatus.ACTIVE -> "Activo"
                JobStatus.CLOSED -> "Cerrado"
                JobStatus.DRAFT -> "Borrador"
                // Si tienes más estados, agrégalos aquí
                else -> status.name.lowercase().replaceFirstChar { it.uppercase() }
            }
        }

        private fun getStatusColor(status: JobStatus): Int {
            return when (status) {
                JobStatus.ACTIVE -> itemView.context.getColor(android.R.color.holo_green_dark)
                JobStatus.CLOSED -> itemView.context.getColor(android.R.color.darker_gray)
                JobStatus.DRAFT -> itemView.context.getColor(android.R.color.holo_orange_dark)
                else -> itemView.context.getColor(android.R.color.black)
            }
        }

        private fun getStatusBackground(status: JobStatus): Int {
            return when (status) {
                JobStatus.ACTIVE -> R.drawable.status_active_bg
                JobStatus.CLOSED -> R.drawable.status_closed_bg
                JobStatus.DRAFT -> R.drawable.status_draft_bg
                else -> R.drawable.status_default_bg
            }
        }

        private fun setupButtonsVisibility(job: Job) = with(binding) {
            when (job.status) {
                JobStatus.ACTIVE -> {
                    editButton.isEnabled = true
                    applicantsButton.isEnabled = true
                    editButton.alpha = 1.0f
                    applicantsButton.alpha = 1.0f
                }
                JobStatus.DRAFT -> {
                    editButton.isEnabled = true
                    applicantsButton.isEnabled = false
                    editButton.alpha = 1.0f
                    applicantsButton.alpha = 0.5f
                }
                JobStatus.CLOSED -> {
                    editButton.isEnabled = false
                    applicantsButton.isEnabled = true
                    editButton.alpha = 0.5f
                    applicantsButton.alpha = 1.0f
                }
                else -> {
                    editButton.isEnabled = false
                    applicantsButton.isEnabled = false
                    editButton.alpha = 0.5f
                    applicantsButton.alpha = 0.5f
                }
            }
        }

    }
}

/**
 * DiffCallback para comparar Jobs
 */
class JobDiffCallback : DiffUtil.ItemCallback<Job>() {
    override fun areItemsTheSame(oldItem: Job, newItem: Job): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Job, newItem: Job): Boolean {
        return oldItem == newItem
    }
}