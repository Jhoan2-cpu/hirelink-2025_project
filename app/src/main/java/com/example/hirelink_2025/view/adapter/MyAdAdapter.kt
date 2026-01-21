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
import com.example.hirelink_2025.models.Company
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter para MyAds adaptado al modelo Job simplificado
 */
class MyAdAdapter(
    private val onJobClick: (Job) -> Unit,
    private val onEditClick: (Job) -> Unit,
    private val onDeleteClick: (Job) -> Unit,
    private val onApplicantsClick: (Job) -> Unit,
    private val getCompanyInfo: (String) -> Company?, // Función para obtener info de empresa
    private val getApplicationsCount: (String) -> Int  // Función para obtener número de postulaciones
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
            // Información básica del trabajo
            jobTitle.text = job.title
            salary.text = if (job.salary.isNotEmpty()) "S/. " + job.salary else job.offerSalary.ifEmpty { "Salario no especificado" }
            
            // Obtener información de la empresa
            val company = getCompanyInfo(job.companyId)
            companyName.text = company?.name ?: "Empresa no encontrada"
            location.text = if (company != null) "${company.city}, ${company.country}" else "Ubicación no disponible"
            
            // Formatear fecha de publicación
            postedDate.text = formatDate(job.createdAt)

            // Estado del trabajo
            statusText.text = getStatusText(job.status)
            statusText.setTextColor(getStatusColor(job.status))
            statusText.setBackgroundResource(getStatusBackground(job.status))

            // Número de postulaciones
            val applicationsCount = getApplicationsCount(job.id)
            applicantsCount.text = "$applicationsCount postulantes"

            // Configurar visibilidad de botones basada en el estado
            setupButtonsVisibility(job)

            // Click listeners
            adCard.setOnClickListener { onJobClick(job) }
            editButton.setOnClickListener { onEditClick(job) }
            applicantsButton.setOnClickListener { onApplicantsClick(job) }
            deleteButton.setOnClickListener { onDeleteClick(job) }
        }
        
        private fun formatDate(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp
            
            return when {
                diff < 24 * 60 * 60 * 1000 -> "Hoy"
                diff < 2 * 24 * 60 * 60 * 1000 -> "Ayer"
                diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)} días"
                else -> {
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    sdf.format(Date(timestamp))
                }
            }
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
                    deleteButton.isEnabled = true
                    editButton.alpha = 1.0f
                    applicantsButton.alpha = 1.0f
                    deleteButton.alpha = 1.0f
                }
                JobStatus.DRAFT -> {
                    editButton.isEnabled = true
                    applicantsButton.isEnabled = false
                    deleteButton.isEnabled = true
                    editButton.alpha = 1.0f
                    applicantsButton.alpha = 0.5f
                    deleteButton.alpha = 1.0f
                }
                JobStatus.CLOSED -> {
                    editButton.isEnabled = false
                    applicantsButton.isEnabled = true
                    deleteButton.isEnabled = true
                    editButton.alpha = 0.5f
                    applicantsButton.alpha = 1.0f
                    deleteButton.alpha = 1.0f
                }
                else -> {
                    editButton.isEnabled = false
                    applicantsButton.isEnabled = false
                    deleteButton.isEnabled = true
                    editButton.alpha = 0.5f
                    applicantsButton.alpha = 0.5f
                    deleteButton.alpha = 1.0f
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