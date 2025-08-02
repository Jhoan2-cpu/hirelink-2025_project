package com.example.hirelink_2025.view.ui.fragments.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import com.example.hirelink_2025.databinding.FragmentRegisterBinding
import com.example.hirelink_2025.view.ui.activities.AuthActivity
import com.example.hirelink_2025.view.ui.utils.AuthUtils
import com.example.hirelink_2025.viewmodels.RegisterUiState
import com.example.hirelink_2025.viewmodels.RegisterViewModel
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        setupTextWatchers()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.registerButton.setOnClickListener {
            val name = binding.nameEditText.text.toString()
            val email = binding.emailEditText.text.toString()
            val phone = binding.phoneEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val confirmPassword = binding.confirmPasswordEditText.text.toString()

            Log.d("RegisterFragment", "Register button clicked with: name=$name, email=$email, phone=$phone")
            
            // Limpiar errores previos
            viewModel.clearFieldErrors()

            viewModel.register(name, email, password, confirmPassword, phone.ifBlank { null })
        }

        binding.loginButton.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
        
        // Limpiar errores cuando el usuario haga focus en los campos
        binding.nameEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) viewModel.clearFieldErrors()
        }
        
        binding.emailEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) viewModel.clearFieldErrors()
        }
        
        binding.passwordEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) viewModel.clearFieldErrors()
        }
        
        binding.confirmPasswordEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) viewModel.clearFieldErrors()
        }
    }
    
    private fun setupTextWatchers() {
        // Verificar disponibilidad de email en tiempo real
        binding.emailEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                val email = s.toString().trim()
                if (email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    // Verificar disponibilidad con un pequeño delay
                    binding.emailEditText.postDelayed({
                        viewModel.checkEmailAvailability(email)
                    }, 1000) // 1 segundo de delay
                }
            }
        })
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

    private fun updateUI(state: RegisterUiState) {
        // Loading state
        binding.registerButton.isEnabled = !state.isLoading
        binding.registerButton.text = if (state.isLoading) "Registrando..." else "Registrarse"
        
        // Deshabilitar botón de login mientras se carga
        binding.loginButton.isEnabled = !state.isLoading

        // Field errors
        binding.nameInputLayout.error = state.nameError
        binding.emailInputLayout.error = state.emailError
        binding.passwordInputLayout.error = state.passwordError
        binding.confirmPasswordInputLayout.error = state.confirmPasswordError

        // General error
        state.error?.let { error ->
            AuthUtils.showErrorMessage(requireContext(), error)
            viewModel.clearError()
        }

        // Register success
        if (state.isRegisterSuccess) {
            val userName = state.user?.fullName ?: "Usuario"
            AuthUtils.showSuccessMessage(requireContext(), "¡Bienvenido $userName! Cuenta creada exitosamente")
            
            // Navegar a MainActivity a través de AuthActivity (similar al login)
            (requireActivity() as AuthActivity).navigateToMain()
            viewModel.resetRegisterSuccess()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}