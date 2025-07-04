package com.example.hirelink_2025.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hirelink_2025.R
import com.example.hirelink_2025.databinding.ItemApplicantCardBinding
import com.example.hirelink_2025.models.Applicant
import com.example.hirelink_2025.models.ApplicationStatus

/**
 * Adapter para mostrar aplicantes en RecyclerView
 * Sigue las mejores prácticas de MVVM - no contiene lógica de negocio
 */
class ApplicantAdapter(
    private val onViewProfileClick: (Applicant) -> Unit,
    private val onAcceptClick: ((Applicant) -> Unit)? = null,
    private val onRejectClick: ((Applicant) -> Unit)? = null
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
        val applicant = getItem(position)
        holder.bind(applicant)

        // Configurar clicks usando los métodos públicos del holder
        holder.setupClickListeners(applicant, onViewProfileClick, onAcceptClick, onRejectClick)
    }

    inner class ApplicantViewHolder(
        private val binding: ItemApplicantCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(applicant: Applicant) = with(binding) {
            // Información básica del aplicante
            applicantName.text = applicant.name
            applicantProfession.text = applicant.profession
            applicantExperience.text = applicant.experience
            applicationDate.text = applicant.applicationDate

            // Mostrar skills como chips o texto
            val skillsText = applicant.skills.joinToString(", ")
            applicantSkills.text = skillsText

            // Configurar estado visual según el status
            updateStatusVisual(applicant.status)

            // Configurar botones según el estado
            setupActionButtons(applicant)
        }

        /**
         * Método público para configurar los click listeners
         */
        fun setupClickListeners(
            applicant: Applicant,
            onViewProfile: (Applicant) -> Unit,
            onAccept: ((Applicant) -> Unit)?,
            onReject: ((Applicant) -> Unit)?
        ) = with(binding) {
            // Botón ver perfil siempre disponible
            viewProfileButton?.setOnClickListener { onViewProfile(applicant) }

            // Solo configurar botones si existen en el layout y hay callbacks
            if (applicant.status == ApplicationStatus.PENDING) {
                acceptButton?.setOnClickListener { onAccept?.invoke(applicant) }
                rejectButton?.setOnClickListener { onReject?.invoke(applicant) }

                // Mostrar/ocultar botones según si hay callbacks
                acceptButton?.visibility = if (onAccept != null) View.VISIBLE else View.GONE
                rejectButton?.visibility = if (onReject != null) View.VISIBLE else View.GONE
            } else {
                // Para estados no pendientes, ocultar botones de acción
                acceptButton?.visibility = View.GONE
                rejectButton?.visibility = View.GONE
            }
        }

        /**
         * Actualizar visual según el estado del aplicante
         */
        private fun updateStatusVisual(status: ApplicationStatus) = with(binding) {
            val context = root.context

            when (status) {
                ApplicationStatus.PENDING -> {
                    statusIndicator?.setBackgroundColor(
                        ContextCompat.getColor(context, R.color.warning)
                    )
                    statusText?.text = "Pendiente"
                    statusText?.setTextColor(
                        ContextCompat.getColor(context, R.color.warning)
                    )
                }
                ApplicationStatus.ACCEPTED -> {
                    statusIndicator?.setBackgroundColor(
                        ContextCompat.getColor(context, R.color.success)
                    )
                    statusText?.text = "Aceptado"
                    statusText?.setTextColor(
                        ContextCompat.getColor(context, R.color.success)
                    )
                }
                ApplicationStatus.REJECTED -> {
                    statusIndicator?.setBackgroundColor(
                        ContextCompat.getColor(context, R.color.error)
                    )
                    statusText?.text = "Rechazado"
                    statusText?.setTextColor(
                        ContextCompat.getColor(context, R.color.error)
                    )
                }
            }
        }

        /**
         * Configurar botones de acción según el estado
         */
        private fun setupActionButtons(applicant: Applicant) = with(binding) {
            when (applicant.status) {
                ApplicationStatus.PENDING -> {
                    // Mostrar botones de aceptar y rechazar
                    acceptButton?.text = "Aceptar"
                    rejectButton?.text = "Rechazar"
                    actionButtonsLayout?.visibility = View.VISIBLE
                }

                ApplicationStatus.ACCEPTED -> {
                    // Mostrar estado de contratado
                    acceptButton?.text = "Contratado"
                    acceptButton?.isEnabled = false
                    rejectButton?.text = "Despedir"
                    actionButtonsLayout?.visibility = View.VISIBLE
                }

                ApplicationStatus.REJECTED -> {
                    // Ocultar botones de acción
                    actionButtonsLayout?.visibility = View.GONE
                }
            }
        }
    }
}

/**
 * DiffCallback para optimizar las actualizaciones del RecyclerView
 */
class ApplicantDiffCallback : DiffUtil.ItemCallback<Applicant>() {
    override fun areItemsTheSame(oldItem: Applicant, newItem: Applicant): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Applicant, newItem: Applicant): Boolean {
        return oldItem == newItem
    }
}