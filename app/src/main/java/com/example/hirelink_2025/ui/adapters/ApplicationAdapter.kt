package com.example.hirelink_2025.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hirelink_2025.R
import com.example.hirelink_2025.models.Application
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip

class ApplicationAdapter(
    private val apps: List<Application>,
    private val onCancelClicked: (Application) -> Unit
) : RecyclerView.Adapter<ApplicationAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val logoImage: ImageView = view.findViewById(R.id.companyLogo)
        val jobTitle: TextView = view.findViewById(R.id.jobTitle)
        val companyName: TextView = view.findViewById(R.id.companyName)
        val applicationDate: TextView = view.findViewById(R.id.applicationDate)
        val statusChip: Chip = view.findViewById(R.id.statusChip)
        val cancelButton: MaterialButton = view.findViewById(R.id.cancelButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_application_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = apps[position]
        holder.jobTitle.text = app.jobTitle
        holder.companyName.text = app.companyName
        holder.applicationDate.text = app.applicationDate
        holder.statusChip.text = app.status
        holder.logoImage.setImageResource(app.logoResId)

        holder.cancelButton.setOnClickListener {
            onCancelClicked(app)
        }
    }

    override fun getItemCount(): Int = apps.size
}
