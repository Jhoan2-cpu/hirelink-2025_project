// ProfileEditAboutFragment.kt
package com.example.hirelink_2025.view.ui.fragments.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.hirelink_2025.databinding.FragmentProfileEditAboutBinding

class ProfileEditAboutFragment : Fragment() {

    private lateinit var binding: FragmentProfileEditAboutBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileEditAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Cargar texto actual
        binding.aboutMeInput.setText("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.")
    }

    fun getAboutMeText(): String {
        return binding.aboutMeInput.text.toString()
    }

    companion object {
        fun newInstance() = ProfileEditAboutFragment()
    }
}
