package com.example.hirelink_2025.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hirelink_2025.R
import com.example.hirelink_2025.models.JobCategory

class CategoriesAdapter(
    private val onCategoryClick: (JobCategory?) -> Unit
) : RecyclerView.Adapter<CategoriesAdapter.CategoryViewHolder>() {

    private var categories = listOf<JobCategory>()
    private var selectedCategory: JobCategory? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.bind(category, category == selectedCategory)
    }

    override fun getItemCount(): Int = categories.size

    fun updateCategories(newCategories: List<JobCategory>) {
        categories = newCategories
        notifyDataSetChanged()
    }

    fun setSelectedCategory(category: JobCategory?) {
        selectedCategory = category
        notifyDataSetChanged()
    }

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryName: TextView = itemView.findViewById(R.id.categoryName)

        fun bind(category: JobCategory, isSelected: Boolean) {
            categoryName.text = category.name

            // Cambiar estilo visual según selección
            categoryName.isSelected = isSelected

            itemView.setOnClickListener {
                onCategoryClick(category)
            }
        }
    }
}