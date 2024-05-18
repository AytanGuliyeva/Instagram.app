package com.example.instagramapp.ui.main.story

import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.instagramapp.base.util.ConstValues
import com.example.instagramapp.R
import com.example.instagramapp.databinding.FragmentAddStoryBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class AddStoryFragment : Fragment() {
    private lateinit var binding: FragmentAddStoryBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionResultLauncher: ActivityResultLauncher<String>

    @Inject
    lateinit var auth: FirebaseAuth

    @Inject
    lateinit var firestore: FirebaseFirestore

    @Inject
    lateinit var storage: FirebaseStorage
    private var selectPicture: Uri? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddStoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        firestore = Firebase.firestore
        storage = Firebase.storage
        registerLauncher()
        binding.imageView.setOnClickListener {
            selectImage(view)
        }
        binding.buttonSend.setOnClickListener {
            upload()
        }
        binding.buttonCancel.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun upload() {
        val progress = ProgressDialog(requireActivity())
        progress.setMessage(getString(R.string.please_wait_adding_the_post))
        progress.show()

        val uuid = UUID.randomUUID()
        val imageName = "$uuid.jpg"

        val reference = storage.reference
        val imageReference = reference.child("story/$imageName")

        if (selectPicture != null) {
            imageReference.putFile(selectPicture!!).addOnSuccessListener {
                val uploadPictureReference =
                    storage.reference.child(ConstValues.STORY_STORAGE).child(imageName)
                uploadPictureReference.downloadUrl.addOnSuccessListener { imgUrl ->
                    val downloadUrl = imgUrl.toString()

                    val ramdonkey = UUID.randomUUID().toString()
                    val myId = Firebase.auth.currentUser!!.uid
                    val ref = firestore.collection(ConstValues.STORY).document(myId)
                    val hmapkey = hashMapOf<String, Any>()
                    val hmap = hashMapOf<String, Any>()
                    val timeEnd = System.currentTimeMillis() + 86400000

                    hmap[ConstValues.IMAGE_URL] = downloadUrl
                    hmap[ConstValues.TIME_START] = System.currentTimeMillis()
                    hmap[ConstValues.TIME_END] = timeEnd
                    hmap[ConstValues.STORY_ID] = ramdonkey
                    hmap[ConstValues.USER_ID] = myId

                    hmapkey[ramdonkey] = hmap
                    ref.set(hmapkey, SetOptions.merge()).addOnSuccessListener {
                        progress.dismiss()
                        Toast.makeText(
                            requireActivity(),
                            getString(R.string.story_successfully_shared),
                            Toast.LENGTH_SHORT
                        ).show()
                        findNavController().popBackStack()
                    }.addOnFailureListener { error ->
                        progress.dismiss()
                        Toast.makeText(
                            requireActivity(),
                            "Story not shared ${error.localizedMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(requireActivity(), it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        } else {
            progress.dismiss()
            Toast.makeText(
                requireActivity(),
                getString(R.string.please_select_photo), Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun selectImage(view: View) {
        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                Snackbar.make(
                    view,
                    getString(R.string.permission_needed_for_gallery), Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(getString(R.string.give_permission)) {
                        permissionResultLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    }.show()
            } else {
                permissionResultLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        } else {
            val intentToGallery =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activityResultLauncher.launch(intentToGallery)
        }
    }

    private fun registerLauncher() {
        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == AppCompatActivity.RESULT_OK) {
                    val intentFromResult = result.data
                    if (intentFromResult != null) {
                        selectPicture = intentFromResult.data
                        selectPicture?.let {
                            binding.imageView.setImageURI(it)
                            binding.imageView.visibility = View.VISIBLE
                        }
                    }
                }

            }

        permissionResultLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                val intentToGallery =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
    }
}