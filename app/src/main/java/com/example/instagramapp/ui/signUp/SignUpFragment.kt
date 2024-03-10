package com.example.instagramapp.ui.signUp

import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.instagramapp.R
import com.example.instagramapp.databinding.BottomSheetSettingsBinding
import com.example.instagramapp.databinding.FragmentSignUpBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class SignUpFragment : Fragment() {
    private lateinit var binding: FragmentSignUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db:FirebaseFirestore


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        db = Firebase.firestore
        btnSignUp()
    }

    fun btnSignUp() {
        binding.btnSignUp.setOnClickListener {
            val username = binding.edtUsername.text.toString()
            val password = binding.edtPassword.text.toString()
            val email = binding.edtEmail.text.toString().trim()
            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(
                    email
                )
            ) {
                Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT)
                    .show()

            } else if (password.length < 6) {
                Toast.makeText(
                    requireContext(),
                    "Password not have 6 characters",
                    Toast.LENGTH_SHORT
                ).show()

            } else {
                auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener {
                    Toast.makeText(
                        requireContext(),
                        "User created successfully.",
                        Toast.LENGTH_SHORT
                    ).show()
                    userAccount(username,password, email)
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
     fun userAccount(username: String,password:String,email:String){
        val userId= auth.currentUser!!.uid
        val userMap = hashMapOf(
            "username" to username,
            "email" to email,
            "password" to password,
            "bio" to "",
            "imageUrl" to "https://firebasestorage.googleapis.com/v0/b/instagramclone-d83f1.appspot.com/o/photo_5393077931670099315_m.jpg?alt=media&token=929e8f56-74c9-4247-b1f6-173f858d9f04"
        )

        val refDb=db.collection("Users").document(userId)
        refDb.set(userMap)
            .addOnSuccessListener {
                findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(),it.message,Toast.LENGTH_SHORT).show()
            }
    }

}