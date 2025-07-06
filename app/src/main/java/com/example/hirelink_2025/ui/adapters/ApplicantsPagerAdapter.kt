package com.example.hirelink_2025.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.hirelink_2025.ui.fragments.applications.AcceptedApplicantsFragment
import com.example.hirelink_2025.ui.fragments.applications.PendingApplicantsFragment

/**
 * Adapter para ViewPager2 con tabs de aplicantes
 * MVVM: Solo maneja presentación de fragments
 */
class ApplicantsPagerAdapter(
    activity: FragmentActivity,
    private val jobId: String
) : FragmentStateAdapter(activity) {

    private var applicantsAdapter: ApplicantsAdapter? = null

    fun setApplicantsAdapter(adapter: ApplicantsAdapter) {
        this.applicantsAdapter = adapter
    }
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AcceptedApplicantsFragment.newInstance(jobId)
            1 -> PendingApplicantsFragment.newInstance(jobId)
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
}