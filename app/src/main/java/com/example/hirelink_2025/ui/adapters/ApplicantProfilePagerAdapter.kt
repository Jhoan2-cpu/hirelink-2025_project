package com.example.hirelink_2025.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.hirelink_2025.models.Applicant
import com.example.hirelink_2025.ui.fragments.profile.ApplicantAboutFragment
import com.example.hirelink_2025.ui.fragments.profile.ApplicantExperienceFragment
import com.example.hirelink_2025.ui.fragments.profile.ApplicantSkillsFragment

class ApplicantProfilePagerAdapter(
    fragmentActivity: FragmentActivity,
    private val applicant: Applicant
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ApplicantAboutFragment.newInstance(applicant)
            1 -> ApplicantExperienceFragment.newInstance(applicant)
            2 -> ApplicantSkillsFragment.newInstance(applicant)
            else -> ApplicantAboutFragment.newInstance(applicant)
        }
    }
}