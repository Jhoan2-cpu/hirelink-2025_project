package com.example.hirelink_2025.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hirelink_2025.databinding.ItemJobCardBinding
import com.example.hirelink_2025.models.Job

/**
 * Adapter para mostrar trabajos usando modelo simplificado
 * - Job: modelo con companyId (sin companyName ni location directos)
 * - Obtiene información de Company por separado
 */
class JobAdapter(
    private val onJobClick: (Job) -> Unit,
    private val onApplyClick: (Job) -> Unit,
    private val onBookmarkClick: (Job) -> Unit,
    private val getCompanyInfo: (String) -> com.example.hirelink_2025.models.Company?
) : ListAdapter<Job, JobAdapter.JobViewHolder>(JobDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding = ItemJobCardBinding.inflate(
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
        private val binding: ItemJobCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.jobCard.setOnClickListener {
                val position = adapterPosition

                if (position != RecyclerView.NO_POSITION) {
                    onJobClick(getItem(position))
                }
            }

            binding.applyButton.setOnClickListener {
                val position = adapterPosition

                if (position != RecyclerView.NO_POSITION) {
                    onApplyClick(getItem(position))
                }
            }

            binding.bookmarkButton.setOnClickListener {
                val position = adapterPosition

                if (position != RecyclerView.NO_POSITION) {
                    onBookmarkClick(getItem(position))
                }
            }
        }

        fun bind(job: Job) {
            // Obtener información de la compañía
            val company = getCompanyInfo(job.companyId)
            
            binding.apply {
                jobTitle.text = job.title
                companyName.text = company?.name ?: "Compañía no encontrada"
                locationChip.text = company?.city ?: "Ubicación no especificada"
                modalityChip.text = job.modality
                salaryChip.text = job.salary
                postedDate.text = job.postedDate

                // Animate card appearance
                jobCard.alpha = 0f
                jobCard.translationY = 50f
                jobCard.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(300)
                    .setStartDelay((adapterPosition * 50).toLong())
                    .start()
            }
        }
    }

    class JobDiffCallback : DiffUtil.ItemCallback<Job>() {
        override fun areItemsTheSame(oldItem: Job, newItem: Job): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Job, newItem: Job): Boolean {
            return oldItem == newItem
        }
    }
}