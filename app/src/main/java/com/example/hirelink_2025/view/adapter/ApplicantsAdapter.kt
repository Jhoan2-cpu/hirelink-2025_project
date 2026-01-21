package com.example.hirelink_2025.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hirelink_2025.R
import com.example.hirelink_2025.models.Applicant
import com.example.hirelink_2025.models.ApplicationStatus
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class ApplicantsAdapter(
    private val onItemClicked: (Applicant) -> Unit,
    private val onAcceptClicked: (Applicant) -> Unit,
    private val onRejectClicked: (Applicant) -> Unit
) : ListAdapter<Applicant, ApplicantsAdapter.ApplicantViewHolder>(ApplicantDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicantViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_applicant, parent, false)
        return ApplicantViewHolder(view)
    }

    override fun onBindViewHolder(holder: ApplicantViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ApplicantViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        
        private val applicantCard: MaterialCardView = itemView.findViewById(R.id.applicantCard)
        private val applicantAvatar: ImageView = itemView.findViewById(R.id.applicantAvatar)
        private val applicantName: TextView = itemView.findViewById(R.id.applicantName)
        private val applicantProfession: TextView = itemView.findViewById(R.id.applicantProfession)
        private val experienceYears: TextView = itemView.findViewById(R.id.experienceYears)
        private val statusChip: Chip = itemView.findViewById(R.id.statusChip)
        private val skillsChipGroup: ChipGroup = itemView.findViewById(R.id.skillsChipGroup)
        private val applicantEmail: TextView = itemView.findViewById(R.id.applicantEmail)
        private val applicantPhone: TextView = itemView.findViewById(R.id.applicantPhone)
        private val applicationDate: TextView = itemView.findViewById(R.id.applicationDate)
        private val acceptButton: MaterialButton = itemView.findViewById(R.id.acceptButton)
        private val rejectButton: MaterialButton = itemView.findViewById(R.id.rejectButton)

        fun bind(applicant: Applicant) {
            // Basic data
            applicantName.text = applicant.name
            applicantProfession.text = applicant.profession
            experienceYears.text = applicant.experience
            applicantEmail.text = applicant.email
            applicantPhone.text = applicant.phone
            applicationDate.text = "Aplicó el ${applicant.applicationDate}"

            // Status chip
            updateStatusAppearance(applicant.status)
            
            // Skills chips
            setupSkillsChips(applicant.skills)
            
            // Set default avatar
            applicantAvatar.setImageResource(R.drawable.ic_person)

            // Configure action buttons
            setupActionButtons(applicant.status)

            // Click listeners
            applicantCard.setOnClickListener {
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
        
        private fun setupSkillsChips(skills: List<String>) {
            skillsChipGroup.removeAllViews()
            
            // Limit to first 3 skills to avoid overcrowding
            val displaySkills = skills.take(3)
            
            displaySkills.forEach { skill ->
                val chip = Chip(itemView.context)
                chip.text = skill
                chip.isClickable = false
                chip.setChipBackgroundColorResource(R.color.surface_variant)
                chip.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_secondary))
                chip.textSize = 12f
                skillsChipGroup.addView(chip)
            }
            
            // Add "+X more" chip if there are more skills
            if (skills.size > 3) {
                val moreChip = Chip(itemView.context)
                moreChip.text = "+${skills.size - 3} más"
                moreChip.isClickable = false
                moreChip.setChipBackgroundColorResource(R.color.primary)
                moreChip.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
                moreChip.textSize = 12f
                skillsChipGroup.addView(moreChip)
            }
        }
        
        private fun updateStatusAppearance(status: ApplicationStatus) {
            statusChip.apply {
                when (status) {
                    ApplicationStatus.PENDING -> {
                        text = "Pendiente"
                        setChipBackgroundColorResource(R.color.secondary)
                        setChipIconResource(R.drawable.ic_pending)
                    }
                    ApplicationStatus.ACCEPTED -> {
                        text = "Aceptado"
                        setChipBackgroundColorResource(R.color.success)
                        setChipIconResource(R.drawable.ic_check_circle)
                    }
                    ApplicationStatus.REJECTED -> {
                        text = "Rechazado"
                        setChipBackgroundColorResource(R.color.error)
                        setChipIconResource(R.drawable.ic_rejected)
                    }
                }
                setTextColor(ContextCompat.getColor(context, R.color.white))
            }
        }
        
        private fun setupActionButtons(status: ApplicationStatus) {
            when (status) {
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