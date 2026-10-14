package com.example.medicare.presentation.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels // IMPORTANTE CAMBIO AQUÍ
import androidx.navigation.fragment.findNavController
import com.example.medicare.R

class LoginFragment : Fragment() {

    private val viewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                LoginScreen(
                    viewModel = viewModel,
                    onNavigateToRegister = {
                        viewModel.clearError()
                        findNavController().navigate(R.id.action_login_to_register)
                    },
                    onLoginSuccess = { rol ->
                        if (rol == "admin") {
                            findNavController().navigate(R.id.action_login_to_admin)
                        } else {
                            findNavController().navigate(R.id.action_login_to_patient)
                        }
                    }
                )
            }
        }
    }
}