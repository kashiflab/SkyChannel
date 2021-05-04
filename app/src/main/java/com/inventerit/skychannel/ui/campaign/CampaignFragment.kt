package com.inventerit.skychannel.ui.campaign

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.inventerit.skychannel.Adapter.CampaignAdapter
import com.inventerit.skychannel.R
import com.inventerit.skychannel.interfaces.OnGetCampaign
import com.inventerit.skychannel.activities.AddCampaignActivity
import com.inventerit.skychannel.databinding.FragmentCampaignBinding
import com.inventerit.skychannel.model.Campaign
import com.inventerit.skychannel.viewModel.MainViewModel

class CampaignFragment : Fragment() {


    private lateinit var binding: FragmentCampaignBinding
    private var adapter: CampaignAdapter? = null

    private var dialoge: AlertDialog? = null

    private lateinit var mainViewModel: MainViewModel

    private val rotateOpen: Animation by lazy { AnimationUtils.loadAnimation(
        context,
        R.anim.rotate_open_anim
    ) }
    private val rotateClose: Animation by lazy { AnimationUtils.loadAnimation(
        context,
        R.anim.rotate_close_anim
    ) }
    private val fromBottom: Animation by lazy { AnimationUtils.loadAnimation(
        context,
        R.anim.from_bottom_anim
    ) }
    private val toBottom: Animation by lazy { AnimationUtils.loadAnimation(
        context,
        R.anim.to_bottom_anim
    ) }

    private var isClicked = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentCampaignBinding.inflate(layoutInflater)

        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        binding.campaignRV.layoutManager = LinearLayoutManager(context)
        binding.campaignRV.setHasFixedSize(true)

        mainViewModel.getUserCampaigns(object : OnGetCampaign<List<Campaign>> {
            override fun onGetCampaign(status: Boolean, result: List<Campaign>) {
                if (status) {
                    adapter = CampaignAdapter(requireActivity(), result)
                    binding.campaignRV.adapter = adapter
                    binding.notFound.visibility = View.GONE
                } else {
                    binding.notFound.visibility = View.VISIBLE
                }
            }

        })

        binding.like.setOnClickListener {
            showDialog("1")
        }

        binding.subscribe.setOnClickListener {
            showDialog("0")
        }

        binding.view.setOnClickListener {
            showDialog("2")
        }

        binding.mainFab.setOnClickListener {
            onMainFabClicked()
        }
        return binding.root
    }

    fun showDialog(type: String){

        // custom dialog
        val view: View = LayoutInflater.from(context)
            .inflate(R.layout.create_campaign_dialog, null)
        dialoge = AlertDialog.Builder(context)
            .setView(view)
            .create()

        val etLink = view.findViewById<EditText>(R.id.link)
        val addBtn = view.findViewById<MaterialButton>(R.id.addVideo)

        addBtn.setOnClickListener{
            val link = etLink.text.toString()
            if(link.isNotEmpty()) {
                startActivity(
                    Intent(context, AddCampaignActivity::class.java)
                        .putExtra("link", link).putExtra("type", type)
                )
                dialoge?.hide()
            }else{
                Toast.makeText(context,"Video link is required",Toast.LENGTH_LONG).show()
            }
        }

        dialoge?.show()
    }


    private fun onMainFabClicked() {
        setVisibility(isClicked)
        setAnimation(isClicked)
        setClickable(isClicked)
        isClicked = !isClicked
    }

    private fun setAnimation(clicked: Boolean) {
        if(!clicked){
            binding.like.startAnimation(fromBottom)
            binding.subscribe.startAnimation(fromBottom)
            binding.view.startAnimation(fromBottom)
            binding.mainFab.startAnimation(rotateOpen)
        }else{

            binding.like.startAnimation(toBottom)
            binding.subscribe.startAnimation(toBottom)
            binding.view.startAnimation(toBottom)
            binding.mainFab.startAnimation(rotateClose)
        }
    }

    private fun setVisibility(clicked: Boolean) {
        if(!clicked){
            binding.like.visibility = View.VISIBLE
            binding.subscribe.visibility = View.VISIBLE
            binding.view.visibility = View.VISIBLE
        }else{

            binding.like.visibility = View.INVISIBLE
            binding.subscribe.visibility = View.INVISIBLE
            binding.view.visibility = View.INVISIBLE
        }
    }

    private fun setClickable(clicked: Boolean){
        if(!clicked){
            binding.like.isClickable = true
            binding.subscribe.isClickable = true
            binding.view.isClickable = true
        }else{

            binding.like.isClickable = false
            binding.subscribe.isClickable = false
            binding.view.isClickable = false
        }
    }
}