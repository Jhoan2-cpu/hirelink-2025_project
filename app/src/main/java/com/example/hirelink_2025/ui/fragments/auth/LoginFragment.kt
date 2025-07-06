package com.example.hirelink_2025.ui.fragments.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.hirelink_2025.R
import com.example.hirelink_2025.databinding.FragmentLoginBinding
import com.example.hirelink_2025.ui.activities.AuthActivity
import com.example.hirelink_2025.viewmodels.LoginUiState
import com.example.hirelink_2025.viewmodels.LoginViewModel
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            viewModel.login(email, password)
        }

        binding.registerButton.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateUI(state)
                }
            }
        }
    }

    private fun updateUI(state: LoginUiState) {
        // Loading state
        binding.loginButton.isEnabled = !state.isLoading
        binding.loginButton.text = if (state.isLoading) "Iniciando sesión..." else "Iniciar Sesión"

        // Field errors
        binding.emailInputLayout.error = state.emailError
        binding.passwordInputLayout.error = state.passwordError

        // General error
        state.error?.let { error ->
            Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }

        // Login success
        if (state.isLoginSuccess) {
            Toast.makeText(requireContext(), "¡Bienvenido!", Toast.LENGTH_SHORT).show()
            // Navegar a MainActivity a través de AuthActivity
            (requireActivity() as AuthActivity).navigateToMain()
            viewModel.resetLoginSuccess()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}