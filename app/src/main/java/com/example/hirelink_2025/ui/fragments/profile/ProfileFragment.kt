package com.example.hirelink_2025.ui.fragments.profile

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.hirelink_2025.R
import com.example.hirelink_2025.databinding.FragmentProfileBinding
import com.example.hirelink_2025.ui.adapters.ProfilePagerAdapter
import com.google.android.material.tabs.TabLayoutMediator

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var pagerAdapter: ProfilePagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUserProfile()
        setupViewPager()
        setupClickListeners()
    }

    private fun setupUserProfile() {
        with(binding) {
            // User basic info
            userName.text = "Nombre del usuario"
            userProfession.text = "Ingeniero de Sistemas"
            userLocation.text = "Chimbote, Perú"

            // Contact info
            userPhone.text = "+51 980440594"
            userEmail.text = "usuario@gmail.com"
        }
    }

    private fun setupViewPager() {
        // Setup ViewPager2 with TabLayout
        pagerAdapter = ProfilePagerAdapter(requireActivity())
        binding.profileViewPager.adapter = pagerAdapter

        // Connect TabLayout with ViewPager2
        TabLayoutMediator(binding.profileTabLayout, binding.profileViewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.about_me)
                1 -> getString(R.string.experience)
                2 -> getString(R.string.skills)
                else -> ""
            }
        }.attach()
    }

    private fun setupClickListeners() {
        // Settings button
        binding.settingsButton.setOnClickListener {
            // Navigate to settings or show menu
            showSettingsMenu()
        }

        // Edit FAB
        binding.editFab.setOnClickListener {
            // Navigate to edit profile
            Toast.makeText(requireContext(), "Editar perfil", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to edit profile screen
        }

        // Profile image click
        binding.profileImage.setOnClickListener {
            // Change profile image
            Toast.makeText(requireContext(), "Cambiar foto de perfil", Toast.LENGTH_SHORT).show()
            // TODO: Implement image picker
        }

        // Contact info clicks
        binding.userPhone.setOnClickListener {
            dialPhoneNumber(binding.userPhone.text.toString())
        }

        binding.userEmail.setOnClickListener {
            sendEmail(binding.userEmail.text.toString())
        }
    }

    private fun showSettingsMenu() {
        // Show settings options
        val options = arrayOf(
            "Configuración de cuenta",
            "Privacidad",
            "Notificaciones",
            "Ayuda",
            "Cerrar sesión"
        )

        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Configuración")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> Toast.makeText(requireContext(), "Configuración de cuenta", Toast.LENGTH_SHORT).show()
                1 -> Toast.makeText(requireContext(), "Privacidad", Toast.LENGTH_SHORT).show()
                2 -> Toast.makeText(requireContext(), "Notificaciones", Toast.LENGTH_SHORT).show()
                3 -> Toast.makeText(requireContext(), "Ayuda", Toast.LENGTH_SHORT).show()
                4 -> showLogoutConfirmation()
            }
        }
        builder.show()
    }

    private fun showLogoutConfirmation() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Cerrar sesión")
        builder.setMessage("¿Estás seguro de que quieres cerrar sesión?")
        builder.setPositiveButton("Sí") { _, _ ->
            // TODO: Implement logout logic
            Toast.makeText(requireContext(), "Sesión cerrada", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun dialPhoneNumber(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = android.net.Uri.parse("tel:$phoneNumber")
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "No se pudo abrir el marcador", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendEmail(email: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = android.net.Uri.parse("mailto:$email")
            putExtra(Intent.EXTRA_SUBJECT, "Contacto desde HireLink")
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "No se pudo abrir el cliente de email", Toast.LENGTH_SHORT).show()
        }
    }
}