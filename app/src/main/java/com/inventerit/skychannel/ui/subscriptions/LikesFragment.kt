package com.inventerit.skychannel.ui.subscriptions

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.inventerit.skychannel.R
import com.inventerit.skychannel.databinding.FragmentLikesBinding


class LikesFragment : Fragment() {

    private lateinit var binding: FragmentLikesBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentLikesBinding.inflate(layoutInflater)


        return binding.root
    }
}