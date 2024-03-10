package com.example.instagramapp.ui.profile

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.example.instagramapp.R
import com.example.instagramapp.databinding.BottomSheetSettingsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class SettingFragment : BottomSheetDialogFragment(R.layout.bottom_sheet_settings) {

    lateinit var binding: BottomSheetSettingsBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = BottomSheetSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth

        btnLogout()
    }

    private fun btnLogout() {
        binding.txtLogOut.setOnClickListener {
            val dialog = Dialog(requireContext())
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.logout_dialog)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val btnLogout: TextView = dialog.findViewById(R.id.btnLogOut)
            val btnCancel: TextView = dialog.findViewById(R.id.btnCancel)

            btnLogout.setOnClickListener {
                auth.signOut()
                findNavController().navigate(R.id.action_settingFragment_to_loginFragment)
                dialog.dismiss() // Dismiss the dialog after logging out
            }
            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show() // Ensure the dialog is shown
        }
    }
}
