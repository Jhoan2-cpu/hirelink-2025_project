package com.example.hirelink_2025.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.hirelink_2025.ui.fragments.applications.AcceptedApplicantsFragment
import com.example.hirelink_2025.ui.fragments.ads.PendingApplicantsFragment


class ApplicantsPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val jobId: String
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AcceptedApplicantsFragment.newInstance(jobId)
            1 -> PendingApplicantsFragment.newInstance(jobId)
            else -> AcceptedApplicantsFragment.newInstance(jobId)
        }
    }
}