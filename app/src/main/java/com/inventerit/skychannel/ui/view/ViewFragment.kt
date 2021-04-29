package com.inventerit.skychannel.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.inventerit.skychannel.R
import com.inventerit.skychannel.databinding.FragmentViewBinding

class ViewFragment : Fragment() {

    private lateinit var binding: FragmentViewBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentViewBinding.inflate(layoutInflater)



        return binding.root
    }
}