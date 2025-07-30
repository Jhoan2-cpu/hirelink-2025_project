package com.example.hirelink_2025.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hirelink_2025.R
import com.example.hirelink_2025.models.JobResult
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.button.MaterialButton

class JobResultsAdapter(
    private val onItemClick: (JobResult) -> Unit,
    private val onBookmarkClick: (JobResult) -> Unit = {},
    private val onApplyClick: (JobResult) -> Unit = {}
) : ListAdapter<JobResult, JobResultsAdapter.JobViewHolder>(JobDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_job_card, parent, false)
        return JobViewHolder(view)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val job = getItem(position)
        holder.bind(job)
    }

    inner class JobViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val jobCard: MaterialCardView = itemView.findViewById(R.id.jobCard)
        private val companyLogo: ImageView = itemView.findViewById(R.id.companyLogo)
        private val jobTitle: TextView = itemView.findViewById(R.id.jobTitle)
        private val companyName: TextView = itemView.findViewById(R.id.companyName)
        private val bookmarkButton: MaterialButton = itemView.findViewById(R.id.bookmarkButton)
        private val locationChip: Chip = itemView.findViewById(R.id.locationChip)
        private val modalityChip: Chip = itemView.findViewById(R.id.modalityChip)
        private val salaryChip: Chip = itemView.findViewById(R.id.salaryChip)
        private val postedDate: TextView = itemView.findViewById(R.id.postedDate)
        private val applyButton: MaterialButton = itemView.findViewById(R.id.applyButton)

        fun bind(job: JobResult) {
            // Información básica
            jobTitle.text = job.title
            companyName.text = job.company
            postedDate.text = job.publishedDate

            // Chips con información
            locationChip.text = job.location
            modalityChip.text = job.type
            salaryChip.text = job.salary

            // Logo de la empresa (por ahora usar icono por defecto)
            companyLogo.setImageResource(R.drawable.ic_launcher_foreground)

            // Estado del bookmark
            updateBookmarkButton(job.isBookmarked)

            // Click listeners
            jobCard.setOnClickListener {
                onItemClick(job)
            }

            bookmarkButton.setOnClickListener {
                job.isBookmarked = !job.isBookmarked
                updateBookmarkButton(job.isBookmarked)
                onBookmarkClick(job)
            }

            applyButton.setOnClickListener {
                onApplyClick(job)
            }
        }

        private fun updateBookmarkButton(isBookmarked: Boolean) {
            val iconRes = if (isBookmarked) {
                R.drawable.ic_bookmark_filled
            } else {
                R.drawable.ic_bookmark_border
            }
            bookmarkButton.setIconResource(iconRes)
        }
    }

    class JobDiffCallback : DiffUtil.ItemCallback<JobResult>() {
        override fun areItemsTheSame(oldItem: JobResult, newItem: JobResult): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: JobResult, newItem: JobResult): Boolean {
            return oldItem == newItem
        }
    }
}