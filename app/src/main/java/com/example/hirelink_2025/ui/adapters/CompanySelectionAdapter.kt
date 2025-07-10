package com.example.hirelink_2025.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.hirelink_2025.R
import com.example.hirelink_2025.models.Company

class CompanySelectionAdapter(
    context: Context,
    private val companies: List<Company>
) : ArrayAdapter<Company>(context, 0, companies) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_company_selection, parent, false)
        
        val company = companies[position]
        
        val companyName = view.findViewById<TextView>(R.id.companyName)
        val companyType = view.findViewById<TextView>(R.id.companyType)
        
        companyName.text = company.name
        companyType.text = "${company.type} • ${company.city}"
        
        return view
    }

    override fun getCount(): Int = companies.size

    override fun getItem(position: Int): Company = companies[position]

    override fun getItemId(position: Int): Long = companies[position].id.hashCode().toLong()
}