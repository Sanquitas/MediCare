package com.example.medicare.presentation.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController

class RegisterFragment : Fragment() {

    private val viewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                RegisterScreen(
                    viewModel = viewModel,
                    onBackToLogin = {
                        viewModel.clearError()
                        // Usamos el Navigator para regresar (destruyendo este fragment)
                        findNavController().popBackStack()
                    },
                    onRegisterSuccess = {
                        Toast.makeText(context, "Registro exitoso. Ahora inicia sesión.", Toast.LENGTH_LONG).show()
                        viewModel.clearError()
                        // Regresamos al Login
                        findNavController().popBackStack()
                    }
                )
            }
        }
    }
}