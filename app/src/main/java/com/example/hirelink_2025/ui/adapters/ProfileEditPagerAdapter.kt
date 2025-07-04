package com.example.hirelink_2025.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.hirelink_2025.ui.fragments.profile.ProfileEditAboutFragment
import com.example.hirelink_2025.ui.fragments.profile.ProfileEditExperienceFragment
import com.example.hirelink_2025.ui.fragments.profile.ProfileEditSkillsFragment

class ProfileEditPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ProfileEditAboutFragment.newInstance()
            1 -> ProfileEditExperienceFragment.newInstance()
            2 -> ProfileEditSkillsFragment.newInstance()
            else -> ProfileEditAboutFragment.newInstance()
        }
    }
}