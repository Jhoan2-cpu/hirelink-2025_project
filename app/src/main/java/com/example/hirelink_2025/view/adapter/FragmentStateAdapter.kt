package com.example.hirelink_2025.view.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.hirelink_2025.view.ui.fragments.applications.FinishedApplicationsFragment
import com.example.hirelink_2025.view.ui.fragments.applications.ReviewApplicationsFragment

class ApplicationsPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ReviewApplicationsFragment()
            1 -> FinishedApplicationsFragment()
            else -> throw IllegalArgumentException("Invalid position")
        }
    }

}
