package com.inventerit.skychannel.ui.more

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat.recreate
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.inventerit.skychannel.LoginActivity
import com.inventerit.skychannel.`interface`.OnComplete
import com.inventerit.skychannel.constant.PrefKeys
import com.inventerit.skychannel.databinding.FragmentMoreBinding
import com.inventerit.skychannel.model.User
import com.inventerit.skychannel.viewModel.MainViewModel
import com.tramsun.libs.prefcompat.Pref

class MoreFragment : Fragment() {

    private lateinit var mAtuh: FirebaseAuth

    private lateinit var binding: FragmentMoreBinding

    private lateinit var mainViewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentMoreBinding.inflate(layoutInflater)

        if (Pref.getBoolean(PrefKeys.isDarkMode,false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        mainViewModel =
            ViewModelProvider(this).get(MainViewModel::class.java)

        setDarkMode()

        mAtuh = FirebaseAuth.getInstance()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val username = Pref.getString(PrefKeys.username)
        val coins = Pref.getString(PrefKeys.coins)
        val photoUrl = Pref.getString(PrefKeys.photoUrl)

        Glide.with(requireActivity()).load(photoUrl).into(binding.profileCircleImageView)
        binding.username.text = username
        binding.coins.text = coins + " Coins"

        binding.logout.setOnClickListener {
            mAtuh.signOut()
            startActivity(Intent(context,LoginActivity::class.java))
            requireActivity().finish()
        }
    }

    private fun setDarkMode(){
        binding.darkMode.isChecked = Pref.getBoolean(PrefKeys.isDarkMode, false)

        binding.darkMode.setOnCheckedChangeListener { compoundButton, b ->
//            if(Pref.getBoolean(PrefKeys.isDarkMode)) {
//                Pref.putBoolean(PrefKeys.isDarkMode, false)
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//                recreate(requireActivity())
//            }else{
//                Pref.putBoolean(PrefKeys.isDarkMode, true)
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//                recreate(requireActivity())
//            }
            Pref.putBoolean(PrefKeys.isDarkMode, b)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            recreate(requireActivity())
        }
    }
}