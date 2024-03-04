package com.example.instagramapp.coreFeatures.profile

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.instagramapp.R
import com.example.instagramapp.databinding.FragmentProfileBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var dialog: BottomSheetDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
  //      btnMenu()
    }
//    private fun showBottomSheet() {
//        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_settings,null)
//        dialog = BottomSheetDialog(requireContext(), R.style.DialogAnimation)
//        dialog.setContentView(dialogView)
//
//
//        dialog.show()
//        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)
//        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
//        dialog.window?.setGravity(Gravity.BOTTOM)
//    }


//    private fun btnMenu(){
//        binding.btnMenu.setOnClickListener {
//            showBottomSheet()
//        }
//    }
}