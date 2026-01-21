package com.example.hirelink_2025.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hirelink_2025.R
import com.example.hirelink_2025.models.WorkExperience

class ExperienceAdapter(
    private var experiences: List<WorkExperience>,
    private val onEditClick: (WorkExperience) -> Unit = {},
    private val onDeleteClick: (WorkExperience) -> Unit = {}
) : RecyclerView.Adapter<ExperienceAdapter.ExperienceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExperienceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_experience_edit, parent, false)
        return ExperienceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExperienceViewHolder, position: Int) {
        val experience = experiences[position]
        holder.bind(experience)
    }

    override fun getItemCount(): Int = experiences.size

    fun updateExperiences(newExperiences: List<WorkExperience>) {
        experiences = newExperiences
        notifyDataSetChanged()
    }

    inner class ExperienceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val positionText: TextView = itemView.findViewById(R.id.experiencePosition)
        private val companyText: TextView = itemView.findViewById(R.id.experienceCompany)
        private val descriptionText: TextView = itemView.findViewById(R.id.experienceDescription)
        private val yearsText: TextView = itemView.findViewById(R.id.experienceYears)
        private val editButton: com.google.android.material.button.MaterialButton = itemView.findViewById(R.id.editExperienceButton)
        private val deleteButton: com.google.android.material.button.MaterialButton = itemView.findViewById(R.id.deleteExperienceButton)

        fun bind(experience: WorkExperience) {
            positionText.text = experience.position
            companyText.text = experience.company
            
            val duration = if (experience.isCurrent) {
                "Experiencia: Desde ${experience.startDate} - Presente"
            } else {
                "Experiencia: ${experience.startDate} - ${experience.endDate ?: ""}"
            }
            yearsText.text = duration
            
            if (experience.description.isNotBlank()) {
                descriptionText.text = experience.description
                descriptionText.visibility = View.VISIBLE
            } else {
                descriptionText.visibility = View.GONE
            }

            editButton.setOnClickListener {
                onEditClick(experience)
            }

            deleteButton.setOnClickListener {
                onDeleteClick(experience)
            }
        }
    }
}
