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
import com.example.hirelink_2025.models.UserProfile
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter para mostrar la lista de postulaciones usando los modelos actualizados:
 * - Application: datos de la postulación (jobId, applicantId, status, etc.)
 * - User: información básica del usuario (fullName, email, etc.)
 * - UserProfile: perfil completo (profession, skills, experience, etc.)
 */
class ApplicationsAdapter(
    private val onAcceptClick: (Application) -> Unit,
    private val onRejectClick: (Application) -> Unit,
    private val onApplicantClick: (Application) -> Unit,
    private val getUserInfo: (String) -> User?,
    private val getUserProfile: (String) -> UserProfile?
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
            val userProfile = getUserProfile(application.applicantId)

            // Verificar si los datos están completos (opcional: para debugging)
            if (!isUserDataComplete(user, userProfile)) {
                android.util.Log.w("ApplicationsAdapter", 
                    "Incomplete user data for applicant: ${application.applicantId}")
            }

            // Información básica del postulante
            applicantName.text = user?.fullName ?: "Nombre no disponible"
            applicantProfession.text = userProfile?.profession ?: "Profesión no especificada"
            
            // Experiencia (mostrar años de experiencia total)
            val experienceText = if (userProfile?.experience?.isNotEmpty() == true) {
                val totalYears = calculateExperienceYears(userProfile.experience)
                if (totalYears > 0) "$totalYears años de experiencia" else "Experiencia no especificada"
            } else {
                "Sin experiencia registrada"
            }
            applicantExperience.text = experienceText
            
            // Habilidades (usar método helper para obtener resumen)
            applicantSkills.text = getRelevantSkills(userProfile?.skills ?: emptyList())

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

        /**
         * Calcula los años totales de experiencia basado en la lista de trabajos
         */
        private fun calculateExperienceYears(experiences: List<com.example.hirelink_2025.models.WorkExperience>): Int {
            var totalMonths = 0
            
            experiences.forEach { experience ->
                try {
                    val startYear = experience.startDate.substringBefore("-").toIntOrNull() ?: 0
                    val endYear = if (experience.isCurrent || experience.endDate.isEmpty()) {
                        java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                    } else {
                        experience.endDate.substringBefore("-").toIntOrNull() ?: startYear
                    }
                    
                    if (startYear > 0 && endYear >= startYear) {
                        totalMonths += (endYear - startYear) * 12
                    }
                } catch (e: Exception) {
                    // Si hay error parseando las fechas, ignorar esta experiencia
                }
            }
            
            return totalMonths / 12
        }
        
        /**
         * Verifica si el usuario tiene información completa
         */
        private fun isUserDataComplete(user: User?, userProfile: UserProfile?): Boolean {
            return user != null && userProfile != null && 
                   user.fullName.isNotEmpty() && userProfile.profession?.isNotEmpty() == true
        }
        
        /**
         * Obtiene un resumen de habilidades relevantes (máximo 3 habilidades)
         */
        private fun getRelevantSkills(skills: List<String>): String {
            return when {
                skills.isEmpty() -> "Sin habilidades registradas"
                skills.size <= 3 -> skills.joinToString(", ")
                else -> "${skills.take(3).joinToString(", ")} y ${skills.size - 3} más"
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