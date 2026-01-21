package com.example.hirelink_2025.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hirelink_2025.R
import com.example.hirelink_2025.databinding.ItemJobSearchResultBinding
import com.example.hirelink_2025.models.Job
import com.example.hirelink_2025.models.Company
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adapter para mostrar resultados de búsqueda de empleos
 * Siguiendo patrón MVVM con ViewBinding y DiffUtil
 */
class SearchResultsAdapter(
    private val onJobClick: (Job) -> Unit,
    private val getCompanyForJob: (Job) -> Company?
) : ListAdapter<Job, SearchResultsAdapter.JobSearchResultViewHolder>(JobDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobSearchResultViewHolder {
        val binding = ItemJobSearchResultBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return JobSearchResultViewHolder(binding)
    }

    override fun onBindViewHolder(holder: JobSearchResultViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class JobSearchResultViewHolder(
        private val binding: ItemJobSearchResultBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(job: Job) {
            binding.apply {
                // Job Title
                jobTitleTextView.text = job.title

                // Company information
                val company = getCompanyForJob(job)
                companyNameTextView.text = company?.name ?: "Empresa no disponible"


                companyLogoImageView.setImageResource(R.drawable.ic_work)
                /* Company Logo
                if (!company?.logoUrl.isNullOrEmpty()) {
                    Glide.with(companyLogoImageView.context)
                        .load(company?.logoUrl)
                        .placeholder(R.drawable.ic_work)
                        .error(R.drawable.ic_work)
                        .circleCrop()
                        .into(companyLogoImageView)
                } else {
                    companyLogoImageView.setImageResource(R.drawable.ic_work)
                }
*/
                // Location from company
                locationChip.text = when {
                    !company?.city.isNullOrBlank() && !company?.country.isNullOrBlank() -> 
                        "${company.city}, ${company.country}"
                    !company?.city.isNullOrBlank() -> company.city
                    !company?.country.isNullOrBlank() -> company.country
                    else -> "Ubicación no especificada"
                }

                // Modality
                modalityChip.text = job.modality.ifBlank { "No especificado" }

                // Employment Type
                employmentTypeChip.text = job.employmentType.ifBlank { "No especificado" }

                // Job Description
                jobDescriptionTextView.text = when {
                    job.aboutJob.isNotBlank() -> job.aboutJob
                    job.requirements.isNotEmpty() -> job.requirements.joinToString(". ")
                    else -> "Descripción no disponible"
                }

                // Salary
                salaryTextView.text = if (job.salary.isNotBlank()) {
                    "S/. " + job.salary
                } else {
                    "Salario a convenir"
                }

                // Posted Date
                postedDateTextView.text = formatPostedDate(job.createdAt)

                // Click listeners
                root.setOnClickListener {
                    onJobClick(job)
                }

            }
        }

        private fun formatPostedDate(timestamp: Long?): String {
            if (timestamp == null || timestamp == 0L) return "Fecha no disponible"
            
            val now = System.currentTimeMillis()
            val diff = now - timestamp
            
            return when {
                diff < 60_000 -> "Hace un momento" // Less than 1 minute
                diff < 3_600_000 -> "Hace ${diff / 60_000} min" // Less than 1 hour
                diff < 86_400_000 -> "Hace ${diff / 3_600_000} h" // Less than 1 day
                diff < 604_800_000 -> "Hace ${diff / 86_400_000} días" // Less than 1 week
                diff < 2_592_000_000 -> "Hace ${diff / 604_800_000} semanas" // Less than 1 month
                else -> {
                    // More than 1 month, show actual date
                    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("es", "ES"))
                    dateFormat.format(Date(timestamp))
                }
            }
        }
    }

    /**
     * DiffUtil callback para optimizar updates del RecyclerView
     */
    private class JobDiffCallback : DiffUtil.ItemCallback<Job>() {
        override fun areItemsTheSame(oldItem: Job, newItem: Job): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Job, newItem: Job): Boolean {
            return oldItem == newItem
        }
    }

    /**
     * Get job at specific position
     */
    fun getJobAt(position: Int): Job? {
        return if (position in 0 until itemCount) {
            getItem(position)
        } else null
    }

    /**
     * Clear all results
     */
    fun clearResults() {
        submitList(emptyList())
    }
}