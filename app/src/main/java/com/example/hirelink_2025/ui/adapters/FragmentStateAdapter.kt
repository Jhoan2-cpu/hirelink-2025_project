package com.example.hirelink_2025.ui.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.hirelink_2025.ui.fragments.applications.ActiveApplicationsFragment
import com.example.hirelink_2025.ui.fragments.applications.FinishedApplicationsFragment
import com.example.hirelink_2025.ui.fragments.applications.ReviewApplicationsFragment

class ApplicationsPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ActiveApplicationsFragment()
            1 -> ReviewApplicationsFragment()
            2 -> FinishedApplicationsFragment()
            else -> throw IllegalArgumentException("Invalid position")
        }
    }

}
