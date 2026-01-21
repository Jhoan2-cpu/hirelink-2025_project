package com.example.hirelink_2025.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hirelink_2025.R
import com.example.hirelink_2025.models.JobAd

class JobsAdapter(
    private val onJobClick: (JobAd) -> Unit
) : RecyclerView.Adapter<JobsAdapter.JobViewHolder>() {

    private var jobs = listOf<JobAd>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_job, parent, false)
        return JobViewHolder(view)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val job = jobs[position]
        holder.bind(job)
    }

    override fun getItemCount(): Int = jobs.size

    fun updateJobs(newJobs: List<JobAd>) {
        jobs = newJobs
        notifyDataSetChanged()
    }

    inner class JobViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val jobTitle: TextView = itemView.findViewById(R.id.jobTitle)
        private val companyName: TextView = itemView.findViewById(R.id.companyName)
        private val jobLocation: TextView = itemView.findViewById(R.id.jobLocation)
        private val jobSalary: TextView = itemView.findViewById(R.id.jobSalary)

        fun bind(job: JobAd) {
            jobTitle.text = job.titulo
            companyName.text = job.empresa
            jobLocation.text = job.ubicacion
            jobSalary.text = job.salario

            itemView.setOnClickListener {
                onJobClick(job)
            }
        }
    }
}