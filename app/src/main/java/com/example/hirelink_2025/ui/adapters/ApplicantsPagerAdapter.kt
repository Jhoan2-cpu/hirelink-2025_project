package com.example.hirelink_2025.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.hirelink_2025.ui.fragments.ads.MyAdsAcceptedApplicantsFragment
import com.example.hirelink_2025.ui.fragments.ads.MyAdsPendingApplicantsFragment

class ApplicantsPagerAdapter(
    activity: FragmentActivity,
    private val jobId: String
) : FragmentStateAdapter(activity) {

    private var applicantsAdapter: ApplicantsAdapter? = null
    private val fragments = mutableListOf<Fragment>()

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        val fragment = when (position) {
            0 -> MyAdsAcceptedApplicantsFragment.newInstance(jobId)
            1 -> MyAdsPendingApplicantsFragment.newInstance(jobId)
            else -> throw IllegalArgumentException("Invalid position: $position")
        }

        fragments.add(fragment)

        // Pasar el adapter al fragment hijo
        applicantsAdapter?.let { adapter ->
            when (fragment) {
                is MyAdsAcceptedApplicantsFragment -> fragment.setApplicantsAdapter(adapter)
                is MyAdsPendingApplicantsFragment -> fragment.setApplicantsAdapter(adapter)
            }
        }

        return fragment
    }

    /**
     * Método para recibir el adapter del fragment PADRE
     */
    fun setApplicantsAdapter(adapter: ApplicantsAdapter) {
        this.applicantsAdapter = adapter

        // Pasar el adapter a fragments ya creados
        fragments.forEach { fragment ->
            when (fragment) {
                is MyAdsAcceptedApplicantsFragment -> {
                    fragment.setApplicantsAdapter(adapter)
                    fragment.updateFilteredList()
                }
                is MyAdsPendingApplicantsFragment -> {
                    fragment.setApplicantsAdapter(adapter)
                    fragment.updateFilteredList()
                }
            }
        }
    }
}