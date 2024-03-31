package com.example.instagramapp.ui.addImage

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
import androidx.navigation.fragment.findNavController
import com.example.instagramapp.R
import com.example.instagramapp.databinding.FragmentAddImageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream


class AddImageFragment : Fragment() {
    private lateinit var binding: FragmentAddImageBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var selectedImageBitmap: Bitmap? = null
    private val PICK_IMAGE_REQUEST = 71


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        firestore = Firebase.firestore
        storage = Firebase.storage
        addProduct()
        selectedImage()
        btnBack()
    }


    private fun selectedImage() {
        binding.imgAddPost.setOnClickListener {
            val galleryIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val selectedImageUri = data.data
            selectedImageBitmap =
                MediaStore.Images.Media.getBitmap(
                    requireContext().contentResolver,
                    selectedImageUri
                )
            binding.imgAddPost.setImageBitmap(selectedImageBitmap)
        } else {
            Toast.makeText(requireContext(), "Something gone wrong", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_addImageFragment_to_profileFragment)
        }
    }


    private fun addProduct() {
        binding.btnShare.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            val caption = binding.edtCaption.text.toString()
            val user = auth.currentUser!!.uid

            if (selectedImageBitmap != null) {
                val time = com.google.firebase.Timestamp.now()
                val ref = firestore.collection("Posts").document()
                val postId = ref.id

                val postMap = hashMapOf<String, Any>(
                    "caption" to caption,
                    "userId" to user,
                    "time" to time,
                    "postId" to postId,
                )

                uploadImage(postId, postMap, ref)
            } else {
                Toast.makeText(requireContext(), "Please select an image", Toast.LENGTH_SHORT)
                    .show()
                binding.progressBar.visibility = View.GONE

            }
        }
    }

    private fun uploadImage(
        postId: String,
        postMap: HashMap<String, Any>,
        ref: DocumentReference
    ) {
        selectedImageBitmap?.let { bitmap ->
            val boas = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, boas)
            val imageData = boas.toByteArray()

            val storageRef = storage.reference.child("images").child("$postId.jpg")
            storageRef.putBytes(imageData)
                .addOnSuccessListener { taskSnapshot ->
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        val downloadUrl = uri.toString()
                        postMap["postImageUrl"] = downloadUrl
                        postMap["time"] = com.google.firebase.Timestamp.now()
                        addProductInfoToFireStore(postMap, ref)
                    }
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to upload image!", Toast.LENGTH_SHORT)
                        .show()
                }
        }
    }

    private fun addProductInfoToFireStore(postMap: HashMap<String, Any>, ref: DocumentReference) {
        ref.set(postMap)
            .addOnSuccessListener {
                findNavController().navigate(R.id.action_addImageFragment_to_profileFragment)
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Failed!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_addImageFragment_to_profileFragment)
            }
    }


    private fun btnBack() {
        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_addImageFragment_to_profileFragment)
        }
    }
}