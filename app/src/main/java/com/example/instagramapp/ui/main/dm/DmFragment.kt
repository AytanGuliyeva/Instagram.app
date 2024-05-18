package com.example.instagramapp.ui.main.dm

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.instagramapp.databinding.FragmentDmBinding
import com.example.instagramapp.ui.main.dm.adapter.DmAdapter
import com.example.instagramapp.base.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DmFragment : Fragment() {
    private lateinit var binding: FragmentDmBinding
    val viewModel: DmViewModel by viewModels()
    private lateinit var dmAdapter: DmAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        viewModel.getUsersId()
        viewModel.chatLiveData.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    val chatList = resource.data
                    dmAdapter = DmAdapter(
                        messageClick = {
                            it
                            buttonMessage(it)
                        }, chatList
                    )
                    dmAdapter.updateChatList(chatList)
                    binding.rvUsers.adapter = dmAdapter
                }

                is Resource.Loading -> {}
                is Resource.Error -> {}
            }
        }
    }

    private fun buttonMessage(userId: String) {
        val action = DmFragmentDirections.actionDmFragmentToMessagesFragment(userId)
        findNavController().navigate(action)
    }

    private fun initListeners() {
        binding.buttonBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}