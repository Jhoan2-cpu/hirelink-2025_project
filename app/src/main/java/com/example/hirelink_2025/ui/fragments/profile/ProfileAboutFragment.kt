// ProfileAboutFragment.kt
package com.example.hirelink_2025.ui.fragments.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.hirelink_2025.databinding.FragmentProfileAboutBinding

class ProfileAboutFragment : Fragment() {

    private lateinit var binding: FragmentProfileAboutBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup about me text
        binding.aboutMeText.text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."
    }

    companion object {
        fun newInstance() = ProfileAboutFragment()
    }
}