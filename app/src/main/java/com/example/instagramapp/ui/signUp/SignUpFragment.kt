package com.example.instagramapp.ui.signUp

import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.instagramapp.R
import com.example.instagramapp.databinding.FragmentSignUpBinding
import com.example.instagramapp.base.util.Resource
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpFragment : Fragment() {
    private lateinit var binding: FragmentSignUpBinding
    val viewModel: SignUpViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        binding.btnSignUp.setOnClickListener {
            val username = binding.edtUsername.text.toString()
            val password = binding.edtPassword.text.toString()
            val email = binding.edtEmail.text.toString().trim()

            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(
                    email
                )
            ) {
                Toast.makeText(requireContext(),
                    getString(R.string.all_fields_are_required), Toast.LENGTH_SHORT)
                    .show()
            } else if (password.length < 6) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.password_must_have_at_least_6_characters),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                viewModel.signUp(username, email, password)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.userCreated.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }

                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)
                }

                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    handleSignUpFailure(resource.exception)
                }
            }
        }
    }

    private fun handleSignUpFailure(exception: Throwable) {
        when (exception) {
            is FirebaseAuthUserCollisionException -> {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.user_with_this_email_already_exists),
                    Toast.LENGTH_SHORT
                ).show()
            }

            else -> {
                Toast.makeText(
                    requireContext(),
                    "Sign up failed: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

