package com.example.hirelink_2025.view.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.hirelink_2025.view.ui.fragments.profile.ProfileAboutFragment
import com.example.hirelink_2025.view.ui.fragments.profile.ProfileExperienceFragment
import com.example.hirelink_2025.view.ui.fragments.profile.ProfileSkillsFragment

class ProfilePagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ProfileAboutFragment.newInstance()
            1 -> ProfileExperienceFragment.newInstance()
            2 -> ProfileSkillsFragment.newInstance()
            else -> ProfileAboutFragment.newInstance()
        }
    }
}