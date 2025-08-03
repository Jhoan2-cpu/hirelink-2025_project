package com.example.hirelink_2025.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hirelink_2025.R
import com.example.hirelink_2025.models.Application
import com.example.hirelink_2025.models.ApplicationStatus
import com.example.hirelink_2025.models.User
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter para mostrar la lista de postulaciones usando los modelos actualizados:
 * - Application: datos de la postulación (jobId, applicantId, status, etc.)
 * - User: información básica del usuario (fullName, email, etc.)
 * Simplificado para trabajar solo con User básico
 */
class ApplicationsAdapter(
    private val onAcceptClick: (Application) -> Unit,
    private val onRejectClick: (Application) -> Unit,
    private val onApplicantClick: (Application) -> Unit,
    private val getUserInfo: (String) -> User?
) : ListAdapter<Application, ApplicationsAdapter.ApplicationViewHolder>(ApplicationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_applicant_card, parent, false)
        return ApplicationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ApplicationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ApplicationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Card principal
        private val applicantCard: MaterialCardView = itemView as MaterialCardView
        
        // Elementos principales
        private val applicantName: TextView = itemView.findViewById(R.id.applicantName)
        private val applicantProfession: TextView = itemView.findViewById(R.id.applicantProfession)
        private val applicantExperience: TextView = itemView.findViewById(R.id.applicantExperience)
        private val applicantSkills: TextView = itemView.findViewById(R.id.applicantSkills)
        private val applicationDate: TextView = itemView.findViewById(R.id.applicationDate)
        private val statusText: TextView = itemView.findViewById(R.id.statusText)
        private val statusIndicator: View = itemView.findViewById(R.id.statusIndicator)
        
        // Botones de acción
        private val acceptButton: MaterialButton = itemView.findViewById(R.id.acceptButton)
        private val rejectButton: MaterialButton = itemView.findViewById(R.id.rejectButton)
        private val viewProfileButton: MaterialButton = itemView.findViewById(R.id.viewProfileButton)

        fun bind(application: Application) {
            // Obtener información del usuario
            val user = getUserInfo(application.applicantId)

            // Información básica del postulante
            applicantName.text = user?.name ?: "Nombre no disponible"
            applicantProfession.text = "Postulante" // Valor por defecto, ya que no tenemos profession en User
            
            // Experiencia - usar cover letter como información adicional
            val experienceText = if (application.coverLetter.isNotEmpty()) {
                "Ver carta de presentación"
            } else {
                "Sin carta de presentación"
            }
            applicantExperience.text = experienceText
            
            // Mostrar email en lugar de skills ya que no tenemos skills en User básico
            applicantSkills.text = user?.email ?: "Email no disponible"

            // Fecha de postulación
            applicationDate.text = "Aplicó el ${formatDate(application.appliedAt)}"

            // Estado de la postulación
            setupStatus(application.status)

            // Configurar botones según el estado
            setupButtons(application)

            // Click listeners
            applicantCard.setOnClickListener { onApplicantClick(application) }
            acceptButton.setOnClickListener { onAcceptClick(application) }
            rejectButton.setOnClickListener { onRejectClick(application) }
            viewProfileButton.setOnClickListener { onApplicantClick(application) }
        }

        private fun setupStatus(status: ApplicationStatus) {
            when (status) {
                ApplicationStatus.PENDING -> {
                    statusText.text = "Pendiente"
                    statusText.setTextColor(itemView.context.getColor(R.color.warning))
                    statusIndicator.setBackgroundColor(itemView.context.getColor(R.color.warning))
                }
                ApplicationStatus.ACCEPTED -> {
                    statusText.text = "Aceptado"
                    statusText.setTextColor(itemView.context.getColor(R.color.success))
                    statusIndicator.setBackgroundColor(itemView.context.getColor(R.color.success))
                }
                ApplicationStatus.REJECTED -> {
                    statusText.text = "Rechazado"
                    statusText.setTextColor(itemView.context.getColor(R.color.error))
                    statusIndicator.setBackgroundColor(itemView.context.getColor(R.color.error))
                }
            }
        }

        private fun setupButtons(application: Application) {
            when (application.status) {
                ApplicationStatus.PENDING -> {
                    acceptButton.visibility = View.VISIBLE
                    rejectButton.visibility = View.VISIBLE
                    acceptButton.isEnabled = true
                    rejectButton.isEnabled = true
                    acceptButton.alpha = 1.0f
                    rejectButton.alpha = 1.0f
                }
                ApplicationStatus.ACCEPTED -> {
                    acceptButton.visibility = View.VISIBLE
                    rejectButton.visibility = View.VISIBLE
                    acceptButton.isEnabled = false
                    rejectButton.isEnabled = true
                    acceptButton.alpha = 0.5f
                    rejectButton.alpha = 1.0f
                }
                ApplicationStatus.REJECTED -> {
                    acceptButton.visibility = View.VISIBLE
                    rejectButton.visibility = View.VISIBLE
                    acceptButton.isEnabled = true
                    rejectButton.isEnabled = false
                    acceptButton.alpha = 1.0f
                    rejectButton.alpha = 0.5f
                }
            }
        }


        private fun formatDate(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp
            
            return when {
                diff < 24 * 60 * 60 * 1000 -> "hoy"
                diff < 2 * 24 * 60 * 60 * 1000 -> "ayer"
                diff < 7 * 24 * 60 * 60 * 1000 -> "hace ${diff / (24 * 60 * 60 * 1000)} días"
                else -> {
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    sdf.format(Date(timestamp))
                }
            }
        }
    }
}

/**
 * DiffCallback para comparar Applications
 */
class ApplicationDiffCallback : DiffUtil.ItemCallback<Application>() {
    override fun areItemsTheSame(oldItem: Application, newItem: Application): Boolean {
        return oldItem.applicationId == newItem.applicationId
    }

    override fun areContentsTheSame(oldItem: Application, newItem: Application): Boolean {
        return oldItem == newItem
    }
}