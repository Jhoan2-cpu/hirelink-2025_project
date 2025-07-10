package com.example.hirelink_2025.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hirelink_2025.R
import com.example.hirelink_2025.models.Company
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip

class CompanyAdapter(
    private var companies: MutableList<Company> = mutableListOf(),
    private val onEditClick: (Company) -> Unit,
    private val onDeleteClick: (Company) -> Unit,
    private val onItemClick: (Company) -> Unit
) : RecyclerView.Adapter<CompanyAdapter.CompanyViewHolder>() {

    inner class CompanyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val companyCard: MaterialCardView = itemView.findViewById(R.id.companyCard)
        val companyLogo: ImageView = itemView.findViewById(R.id.companyLogo)
        val companyName: TextView = itemView.findViewById(R.id.companyName)
        val companyType: TextView = itemView.findViewById(R.id.companyType)
        val companyDescription: TextView = itemView.findViewById(R.id.companyDescription)
        val employeeCountChip: Chip = itemView.findViewById(R.id.employeeCountChip)
        val activeJobsChip: Chip = itemView.findViewById(R.id.activeJobsChip)
        val editCompanyButton: MaterialButton = itemView.findViewById(R.id.editCompanyButton)
        val deleteCompanyButton: MaterialButton = itemView.findViewById(R.id.deleteCompanyButton)
        val menuButton: MaterialButton = itemView.findViewById(R.id.menuButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompanyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_company_card, parent, false)
        return CompanyViewHolder(view)
    }

    override fun onBindViewHolder(holder: CompanyViewHolder, position: Int) {
        val company = companies[position]
        
        with(holder) {
            companyName.text = company.name
            companyType.text = "${company.type} • ${company.city}, ${company.country}"
            companyDescription.text = company.description.ifEmpty { "Sin descripción disponible" }
            
            // Set employee count chip
            val employeeText = when {
                company.employeeCount == 0 -> company.size.ifEmpty { "No especificado" }
                company.employeeCount == 1 -> "1 empleado"
                else -> "${company.employeeCount} empleados"
            }
            employeeCountChip.text = employeeText
            
            // Set active jobs chip
            val jobsText = when (company.activeJobsCount) {
                0 -> "Sin empleos activos"
                1 -> "1 empleo activo"
                else -> "${company.activeJobsCount} empleos activos"
            }
            activeJobsChip.text = jobsText
            
            // Set click listeners
            companyCard.setOnClickListener { onItemClick(company) }
            editCompanyButton.setOnClickListener { onEditClick(company) }
            deleteCompanyButton.setOnClickListener { onDeleteClick(company) }
            
            // Menu button can be used for additional options
            menuButton.setOnClickListener {
                // TODO: Show popup menu with additional options
            }
            
            // TODO: Load company logo if available
            // If logoUrl is not empty, load the image using Glide or similar
        }
    }

    override fun getItemCount(): Int = companies.size

    fun updateCompanies(newCompanies: List<Company>) {
        companies.clear()
        companies.addAll(newCompanies)
        notifyDataSetChanged()
    }

    fun addCompany(company: Company) {
        companies.add(0, company) // Add to beginning
        notifyItemInserted(0)
    }

    fun removeCompany(company: Company) {
        val position = companies.indexOf(company)
        if (position != -1) {
            companies.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun updateCompany(updatedCompany: Company) {
        val position = companies.indexOfFirst { it.id == updatedCompany.id }
        if (position != -1) {
            companies[position] = updatedCompany
            notifyItemChanged(position)
        }
    }
}