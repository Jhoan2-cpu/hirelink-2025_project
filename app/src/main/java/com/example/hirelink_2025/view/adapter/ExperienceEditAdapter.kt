
// ExperienceEditAdapter.kt
package com.example.hirelink_2025.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.hirelink_2025.databinding.ItemExperienceEditBinding
import com.example.hirelink_2025.view.ui.fragments.profile.ProfileEditFragment

class ExperienceEditAdapter(
    private val experiences: MutableList<ProfileEditFragment.Experience>,
    private val onEditClick: (ProfileEditFragment.Experience) -> Unit,
    private val onDeleteClick: (ProfileEditFragment.Experience) -> Unit
) : RecyclerView.Adapter<ExperienceEditAdapter.ExperienceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExperienceViewHolder {
        val binding = ItemExperienceEditBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ExperienceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExperienceViewHolder, position: Int) {
        holder.bind(experiences[position])
    }

    override fun getItemCount(): Int = experiences.size

    inner class ExperienceViewHolder(
        private val binding: ItemExperienceEditBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(experience: ProfileEditFragment.Experience) {
            with(binding) {
                experiencePosition.text = experience.position
                experienceCompany.text = experience.company
                experienceDescription.text = experience.description
                
                val dateRange = if (experience.isCurrent) {
                    "${experience.startDate} - Presente"
                } else {
                    "${experience.startDate} - ${experience.endDate}"
                }
                experienceYears.text = dateRange

                editExperienceButton.setOnClickListener {
                    onEditClick(experience)
                }

                deleteExperienceButton.setOnClickListener {
                    onDeleteClick(experience)
                }
            }
        }
    }
}