package com.example.instagramapp.ui.profile

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.instagramapp.R
import com.example.instagramapp.databinding.FragmentEditProfileBinding
import com.example.instagramapp.ui.search.UserDetailViewModel
import com.example.instagramapp.ui.search.model.Users
import com.example.instagramapp.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream


class EditProfileFragment : Fragment() {
    private lateinit var binding: FragmentEditProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private val viewModel: EditProfileViewModel by viewModels()
    private var selectedImageBitmap: Bitmap? = null
    private val PICK_IMAGE_REQUEST = 71

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditProfileBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        viewModel.getUserInfo()
        viewModel.userInformation.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    val user = resource.data
                    updateUI(user)
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                }

                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
            }
        })


        btnBack()
        selectedImage()
        addImage()
    }

    private fun btnBack() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun updateUI(user: Users) {
        binding.txtEditUsurname.setText(user.username)
        binding.txtEditBio.setText(user.bio)
        Glide.with(requireContext()).load(user.imageUrl).into(binding.imgProfile)
    }

    private fun selectedImage() {
        binding.imgProfile.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val selectedImageUri = data.data
            selectedImageBitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, selectedImageUri)
            binding.imgProfile.setImageBitmap(selectedImageBitmap)
        } else {
            Toast.makeText(requireContext(), "Something gone wrong", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addImage() {
        binding.txtDone.setOnClickListener {
            val username = binding.txtEditUsurname.text.toString()
            val bio = binding.txtEditBio.text.toString()

            if (selectedImageBitmap != null) {
                uploadImage(username, bio)
            } else {
                updateUserProfile(username, bio, "https://firebasestorage.googleapis.com/v0/b/instagramclone-d83f1.appspot.com/o/photo_5393077931670099315_m.jpg?alt=media&token=929e8f56-74c9-4247-b1f6-173f858d9f04")
            }

//            val user = Users(username,"","")
//            updateUI(user)


            val action = EditProfileFragmentDirections.actionEditProfileFragmentToProfileFragment()
            findNavController().navigate(action)
        }
    }



    private fun uploadImage(username: String, bio: String) {
        selectedImageBitmap?.let { bitmap ->
            val boas = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, boas)
            val imageData = boas.toByteArray()

            val storageRef = storage.reference.child("images").child("${auth.currentUser!!.uid}.jpg")
            storageRef.putBytes(imageData)
                .addOnSuccessListener { taskSnapshot ->
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        val downloadUrl = uri.toString()
                        updateUserProfile(username, bio, downloadUrl)
                    }
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to upload image!", Toast.LENGTH_SHORT).show()
                   // binding.progressBar.visibility = View.GONE
                }
        }
    }

    private fun updateUserProfile(username: String, bio: String, imageUrl: String) {
        viewModel.updateUserInfo(username, bio, imageUrl)
    }
}