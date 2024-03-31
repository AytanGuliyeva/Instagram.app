package com.example.instagramapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.instagramapp.databinding.ActivityStartBinding
import dagger.hilt.android.AndroidEntryPoint

class StartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStartBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController

        NavigationUI.setupWithNavController(binding.bottomNav, navController)

        navHostFragment.navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.loginFragment || destination.id == R.id.signUpFragment || destination.id == R.id.addImageFragment) {
                hideBottomNavigationView()
            } else {
                showBottomNavigationView()
            }
        }
    }



    fun hideBottomNavigationView() {
        binding.bottomNav.visibility = View.GONE
    }

    fun showBottomNavigationView() {
        binding.bottomNav.visibility = View.VISIBLE
    }
}
