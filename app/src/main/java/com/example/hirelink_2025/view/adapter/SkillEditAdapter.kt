// SkillEditAdapter.kt
package com.example.hirelink_2025.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.hirelink_2025.databinding.ItemSkillEditBinding

class SkillEditAdapter(
    private val skills: MutableList<String>,
    private val onEditClick: (String) -> Unit,
    private val onDeleteClick: (String) -> Unit,
    private val onInfoClick: (String) -> Unit
) : RecyclerView.Adapter<SkillEditAdapter.SkillViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkillViewHolder {
        val binding = ItemSkillEditBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SkillViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SkillViewHolder, position: Int) {
        holder.bind(skills[position])
    }

    override fun getItemCount(): Int = skills.size

    inner class SkillViewHolder(
        private val binding: ItemSkillEditBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(skill: String) {
            binding.skillName.text = skill

            binding.editButton.setOnClickListener {
                onEditClick(skill)
            }

            binding.deleteButton.setOnClickListener {
                onDeleteClick(skill)
            }

            binding.infoButton.setOnClickListener {
                onInfoClick(skill)
            }
        }
    }
}