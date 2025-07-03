package com.example.hirelink_2025.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hirelink_2025.R
import com.example.hirelink_2025.databinding.ItemMyAdCardBinding
import com.example.hirelink_2025.models.Job
import com.example.hirelink_2025.models.JobStatus

class MyAdAdapter(
    private val onJobClick: (Job) -> Unit,
    private val onEditClick: (Job) -> Unit,
    private val onApplicantsClick: (Job) -> Unit,
    private val onDeleteClick: (Job) -> Unit // nuevo callback
)  : ListAdapter<Job, MyAdAdapter.JobViewHolder>(JobDiffCallback())
{

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding = ItemMyAdCardBinding.inflate(
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
        private val binding: ItemMyAdCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(job: Job) = with(binding) {
            jobTitle.text = job.title
            postedDate.text = job.postedDate
            statusText.text = when (job.status) {
                JobStatus.ACTIVE -> "Estado: Activo"
                JobStatus.CLOSED -> "Estado: Cerrado"
                JobStatus.DRAFT -> "Estado: Borrador"
            }
            viewsCount.text = "${job.vacancies * 40} vistas" // Simulación
            applicantsCount.text = "${job.vacancies * 2} postulantes" // Simulación

            // Clicks
            adCard.setOnClickListener { onJobClick(job) }
            editButton.setOnClickListener { onEditClick(job) }
            applicantsButton.setOnClickListener { onApplicantsClick(job) }
            moreButton.setOnClickListener { view ->
                val popup = PopupMenu(view.context, view)
                popup.menuInflater.inflate(R.menu.menu_ad_options, popup.menu)
                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_delete -> {
                            // Aquí llamas a una función para eliminar el anuncio
                            onDeleteClick(job)
                            true
                        }
                        else -> false
                    }
                }
                popup.show()
            }

            // Animación (opcional)
            adCard.alpha = 0f
            adCard.translationY = 50f
            adCard.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300)
                .setStartDelay((adapterPosition * 50).toLong())
                .start()
        }
    }

    class JobDiffCallback : DiffUtil.ItemCallback<Job>() {
        override fun areItemsTheSame(oldItem: Job, newItem: Job): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Job, newItem: Job): Boolean =
            oldItem == newItem
    }
}
