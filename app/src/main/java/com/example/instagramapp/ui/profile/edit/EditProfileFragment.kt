package com.example.instagramapp.ui.profile.edit

import android.app.Activity
import android.app.ProgressDialog
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
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.instagramapp.R
import com.example.instagramapp.databinding.FragmentEditProfileBinding
import com.example.instagramapp.data.model.Users
import com.example.instagramapp.base.util.ConstValues
import com.example.instagramapp.base.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class EditProfileFragment : Fragment() {
    private lateinit var binding: FragmentEditProfileBinding

    val viewModel: EditProfileViewModel by viewModels()
    private var selectedImageBitmap: Bitmap? = null
    private val PICK_IMAGE_REQUEST = 71
    private lateinit var progressDialoq: ProgressDialog
    private lateinit var imageUrl: String

    @Inject
    lateinit var auth: FirebaseAuth

    @Inject
    lateinit var firestore: FirebaseFirestore

    @Inject
    lateinit var storage: FirebaseStorage
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressDialoq = ProgressDialog(requireContext())
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        viewModel.getUserInfo()
        viewModel.userInformation.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    val user = resource.data
                    updateUI(user)
                    imageUrl = user.imageUrl
                }

                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                }

                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
            }
        }
        initListener()
        selectedImage()
        addImage()
    }

    private fun initListener() {
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
            val galleryIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val selectedImageUri = data.data
            selectedImageBitmap = MediaStore.Images.Media.getBitmap(
                requireContext().contentResolver,
                selectedImageUri
            )
            binding.imgProfile.setImageBitmap(selectedImageBitmap)
        } else {
            Toast.makeText(requireContext(), getString(R.string.something_gone_wrong), Toast.LENGTH_SHORT).show()
        }
    }

    private fun addImage() {
        binding.txtDone.setOnClickListener {
            progressDialoq.setTitle(getString(R.string.info))
            progressDialoq.setMessage(getString(R.string.update_user_info))
            progressDialoq.show()
            val username = binding.txtEditUsurname.text.toString()
            val bio = binding.txtEditBio.text.toString()

            if (selectedImageBitmap != null) {
                uploadImage(username, bio)
            } else {
                updateUserProfile(username, bio, imageUrl)
            }
        }
    }


    private fun uploadImage(username: String, bio: String) {
        selectedImageBitmap?.let { bitmap ->
            val boas = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, boas)
            val imageData = boas.toByteArray()
            val uuid = UUID.randomUUID()
            val imageName = "$uuid.jpg"

            val storageRef = storage.reference.child(ConstValues.IMAGES).child(imageName)
            storageRef.putBytes(imageData)
                .addOnSuccessListener { taskSnapshot ->
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        val downloadUrl = uri.toString()
                        updateUserProfile(username, bio, downloadUrl)
                    }
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), getString(R.string.failed_to_upload_image), Toast.LENGTH_SHORT)
                        .show()
                    // binding.progressBar.visibility = View.GONE
                }

        }
    }

    private fun updateUserProfile(username: String, bio: String, imageUrl: String) {
        viewModel.updateUserInfo(username, bio, imageUrl, progressDialoq)
    }
}