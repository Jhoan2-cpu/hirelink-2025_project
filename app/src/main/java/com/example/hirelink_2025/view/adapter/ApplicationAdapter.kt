package com.example.hirelink_2025.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hirelink_2025.R
import com.example.hirelink_2025.models.Application
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip

/**
 * Adapter para mostrar postulaciones usando modelos simplificados
 * - Application: modelo simplificado con jobId y applicantId
 * - Obtiene información de Job y User por separado
 */
class ApplicationAdapter(
    private val apps: List<Application>,
    private val onCancelClicked: (Application) -> Unit,
    private val onItemClicked: (Application) -> Unit,
    private val getJobInfo: (String) -> com.example.hirelink_2025.models.Job?,
    private val getCompanyInfo: (String) -> com.example.hirelink_2025.models.Company?
) : RecyclerView.Adapter<ApplicationAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val logoImage: ImageView = view.findViewById(R.id.companyLogo)
        val jobTitle: TextView = view.findViewById(R.id.jobTitle)
        val companyName: TextView = view.findViewById(R.id.companyName)
        val applicationDate: TextView = view.findViewById(R.id.applicationDate)
        val statusChip: Chip = view.findViewById(R.id.statusChip)
        val cancelButton: MaterialButton = view.findViewById(R.id.cancelButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_application_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = apps[position]
        
        // Obtener información del trabajo
        val job = getJobInfo(app.jobId)
        val company = job?.let { getCompanyInfo(it.companyId) }
        
        // Poblar campos con datos del modelo simplificado
        holder.jobTitle.text = job?.title ?: "Trabajo no encontrado"
        holder.companyName.text = company?.name ?: "Compañía no encontrada"
        holder.applicationDate.text = formatDate(app.appliedAt)
        holder.statusChip.text = getStatusText(app.status)
        
        // Configurar imagen de la compañía (placeholder por ahora)
        holder.logoImage.setImageResource(R.drawable.ic_placeholder_company)
        
        // Configurar color del chip según estado
        setupStatusChip(holder.statusChip, app.status)

        // Click listeners
        holder.cancelButton.setOnClickListener {
            onCancelClicked(app)
        }

        holder.itemView.setOnClickListener {
            onItemClicked(app)
        }
    }
    
    private fun formatDate(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 24 * 60 * 60 * 1000 -> "Hoy"
            diff < 2 * 24 * 60 * 60 * 1000 -> "Ayer"
            diff < 7 * 24 * 60 * 60 * 1000 -> "Hace ${diff / (24 * 60 * 60 * 1000)} días"
            else -> {
                val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                sdf.format(java.util.Date(timestamp))
            }
        }
    }
    
    private fun getStatusText(status: com.example.hirelink_2025.models.ApplicationStatus): String {
        return when (status) {
            com.example.hirelink_2025.models.ApplicationStatus.PENDING -> "Pendiente"
            com.example.hirelink_2025.models.ApplicationStatus.ACCEPTED -> "Aceptado"
            com.example.hirelink_2025.models.ApplicationStatus.REJECTED -> "Rechazado"
        }
    }
    
    private fun setupStatusChip(chip: Chip, status: com.example.hirelink_2025.models.ApplicationStatus) {
        val context = chip.context
        when (status) {
            com.example.hirelink_2025.models.ApplicationStatus.PENDING -> {
                chip.setChipBackgroundColorResource(R.color.warning_light)
                chip.setTextColor(context.getColor(R.color.warning))
            }
            com.example.hirelink_2025.models.ApplicationStatus.ACCEPTED -> {
                chip.setChipBackgroundColorResource(R.color.success_light)
                chip.setTextColor(context.getColor(R.color.success))
            }
            com.example.hirelink_2025.models.ApplicationStatus.REJECTED -> {
                chip.setChipBackgroundColorResource(R.color.error_light)
                chip.setTextColor(context.getColor(R.color.error))
            }
        }
    }

    override fun getItemCount(): Int = apps.size
}
