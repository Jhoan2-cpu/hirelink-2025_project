package com.example.hirelink_2025.view.ui.fragments.auth

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
import com.example.hirelink_2025.view.ui.activities.AuthActivity
import com.example.hirelink_2025.view.ui.utils.AuthUtils
import com.example.hirelink_2025.viewmodels.LoginUiState
import com.example.hirelink_2025.viewmodels.LoginViewModel
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null //NOTA: usamos guión bajo "_" para indicar que la variable es mutable y nullable.
    private val binding get() = _binding!! //Accede al binding con el get() con el !! forzamos a que no sea nulo
    //Si intentamos

    private val viewModel: LoginViewModel by viewModels()//Obtiene una instancia de LoginViewModel delegando su creación a viewModels(), este estará asociado a este fragmento
    //El viewModel permite que el fragmento observe variables como uiState.

    override fun onCreateView(//binding
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root//DEVUELVE LA RAÍZ DE LA VISTA INFLADA PARA QUE ANDROID LO MUESTRE EN PANTALLA.
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeViewModel()
        
        // Verificar si ya hay usuario autenticado
        viewModel.checkAuthState()
    }

    private fun setupClickListeners() {
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            
            // Limpiar errores previos
            viewModel.clearFieldErrors()
            
            viewModel.login(email, password)
        }

        binding.registerButton.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
        
        // Agregar funcionalidad de "Olvidé mi contraseña"
        binding.forgotPasswordButton?.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            if (email.isBlank()) {
                AuthUtils.showErrorMessage(requireContext(), "Ingresa tu email para recuperar la contraseña")
                return@setOnClickListener
            }
            
            AuthUtils.showPasswordResetDialog(requireContext(), email) {
                viewModel.resetPassword(email)
            }
        }
        
        // Limpiar errores cuando el usuario empiece a escribir
        binding.emailEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) viewModel.clearFieldErrors()
        }
        
        binding.passwordEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) viewModel.clearFieldErrors()
        }
    }

    private fun observeViewModel() {//AQUÍ ES DONDE OCURRE LA MAGIA:
        //EL viewLifeCycleOwner, un propietario del ciclo de vida del fragmento
        //... .lifecycleScope.launch lanza una corrutina asociada al ciclo de vida del fragmento
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {//Este método se asegura de que el código dentro de su bloque se ejecute solo cuando el fragmento esté en el ciclo de vida adecuado.
                //Lifecycle.State.STARTED: Esto significa que la UI del fragmento está visible y activa.
                viewModel.uiState.collect { state ->//StateFlow en el ViewModel que contiene el estado de la UI
                    //recoge los valores emitidos por el StateFlow(uiState) state contendrá el estado actual del login, un objeto de tipo LoginUiState.
                    updateUI(state)
                }
            }
        }
    }

    private fun updateUI(state: LoginUiState) {
        // Loading state
        binding.loginButton.isEnabled = !state.isLoading
        binding.loginButton.text = if (state.isLoading) "Iniciando sesión..." else "Iniciar Sesión"
        
        // Deshabilitar botón de registro mientras se carga
        binding.registerButton.isEnabled = !state.isLoading
        
        // Deshabilitar botón de recuperar contraseña mientras se carga
        binding.forgotPasswordButton?.isEnabled = !state.isLoading

        // Field errors
        binding.emailInputLayout.error = state.emailError
        binding.passwordInputLayout.error = state.passwordError

        // General error
        state.error?.let { error ->
            AuthUtils.showErrorMessage(requireContext(), error)
            viewModel.clearError()
        }

        // Reset password success
        if (state.isResetPasswordSent) {
            state.resetPasswordMessage?.let { message ->
                AuthUtils.showSuccessMessage(requireContext(), message)
            }
            viewModel.clearResetPasswordState()
        }

        // Login success
        if (state.isLoginSuccess) {
            val userName = state.user?.fullName ?: "Usuario"
            AuthUtils.showSuccessMessage(requireContext(), "¡Bienvenido $userName!")
            
            // Navegar a MainActivity a través de AuthActivity
            (requireActivity() as AuthActivity).navigateToMain()
            viewModel.resetLoginSuccess()
        }
    }

    override fun onDestroyView() {//binding
        super.onDestroyView()
        _binding = null
    }
}