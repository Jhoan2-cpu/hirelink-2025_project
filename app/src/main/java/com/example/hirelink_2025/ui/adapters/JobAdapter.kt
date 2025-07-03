package com.example.hirelink_2025.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hirelink_2025.databinding.ItemJobCardBinding
import com.example.hirelink_2025.models.Job

class JobAdapter(
    private val onJobClick: (Job) -> Unit,
    private val onApplyClick: (Job) -> Unit,
    private val onBookmarkClick: (Job) -> Unit
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
            binding.apply {
                jobTitle.text = job.title
                companyName.text = job.companyName
                locationChip.text = job.location
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