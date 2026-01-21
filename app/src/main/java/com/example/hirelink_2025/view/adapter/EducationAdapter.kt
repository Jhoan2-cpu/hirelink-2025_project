package com.example.hirelink_2025.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hirelink_2025.R
import com.example.hirelink_2025.models.Education

class EducationAdapter(
    private var educationList: List<Education>,
    private val onEditClick: (Education) -> Unit = {},
    private val onDeleteClick: (Education) -> Unit = {}
) : RecyclerView.Adapter<EducationAdapter.EducationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EducationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_education_edit, parent, false)
        return EducationViewHolder(view)
    }

    override fun onBindViewHolder(holder: EducationViewHolder, position: Int) {
        val education = educationList[position]
        holder.bind(education)
    }

    override fun getItemCount(): Int = educationList.size

    fun updateEducation(newEducationList: List<Education>) {
        educationList = newEducationList
        notifyDataSetChanged()
    }

    inner class EducationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val degreeText: TextView = itemView.findViewById(R.id.educationDegree)
        private val institutionText: TextView = itemView.findViewById(R.id.educationInstitution)
        private val fieldText: TextView = itemView.findViewById(R.id.educationField)
        private val yearsText: TextView = itemView.findViewById(R.id.educationYears)
        private val descriptionText: TextView = itemView.findViewById(R.id.educationDescription)
        private val editButton: com.google.android.material.button.MaterialButton = itemView.findViewById(R.id.editEducationButton)
        private val deleteButton: com.google.android.material.button.MaterialButton = itemView.findViewById(R.id.deleteEducationButton)

        fun bind(education: Education) {
            degreeText.text = education.degree
            institutionText.text = education.institution
            fieldText.text = education.field
            
            val years = if (education.startYear.isNotBlank() && education.endYear.isNotBlank()) {
                "${education.startYear} - ${education.endYear}"
            } else if (education.startYear.isNotBlank()) {
                "Desde ${education.startYear}"
            } else {
                "Años no especificados"
            }
            yearsText.text = years
            
            if (education.description.isNotBlank()) {
                descriptionText.text = education.description
                descriptionText.visibility = View.VISIBLE
            } else {
                descriptionText.visibility = View.GONE
            }

            editButton.setOnClickListener {
                onEditClick(education)
            }

            deleteButton.setOnClickListener {
                onDeleteClick(education)
            }
        }
    }
}