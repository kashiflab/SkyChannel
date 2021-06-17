package com.sidhow.skychannel.activities

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.sidhow.skychannel.Adapter.CampaignAdapter
import com.sidhow.skychannel.R
import com.sidhow.skychannel.databinding.ActivityCampaignBinding
import com.sidhow.skychannel.interfaces.OnGetCampaign
import com.sidhow.skychannel.model.Campaign
import com.sidhow.skychannel.viewModel.MainViewModel
import java.util.regex.Matcher
import java.util.regex.Pattern

class CampaignActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityCampaignBinding
    private var adapter: CampaignAdapter? = null

    private var dialoge: AlertDialog? = null

    private lateinit var mainViewModel: MainViewModel

    private val rotateOpen: Animation by lazy { AnimationUtils.loadAnimation(
        this,
        R.anim.rotate_open_anim
    ) }
    private val rotateClose: Animation by lazy { AnimationUtils.loadAnimation(
        this,
        R.anim.rotate_close_anim
    ) }
    private val fromBottom: Animation by lazy { AnimationUtils.loadAnimation(
        this,
        R.anim.from_bottom_anim
    ) }
    private val toBottom: Animation by lazy { AnimationUtils.loadAnimation(
        this,
        R.anim.to_bottom_anim
    ) }

    private var isClicked = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCampaignBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Campaigns"
        actionBar?.setDisplayHomeAsUpEnabled(true)
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        binding.campaignRV.layoutManager = LinearLayoutManager(this)
        binding.campaignRV.setHasFixedSize(true)

        mainViewModel.getUserCampaigns(object : OnGetCampaign<List<Campaign>> {
            override fun onGetCampaign(status: Boolean, result: List<Campaign>) {
                if (status) {
                    try {
                        adapter = CampaignAdapter(this@CampaignActivity, result)
                        binding.campaignRV.adapter = adapter
                        binding.notFound.visibility = View.GONE
                    }catch (e: Exception){
                        e.printStackTrace()
                        binding.notFound.visibility = View.VISIBLE
                    }
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
        
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    fun showDialog(type: String){

        // custom dialog
        val view: View = LayoutInflater.from(this)
            .inflate(R.layout.create_campaign_dialog, null)
        dialoge = AlertDialog.Builder(this)
            .setView(view)
            .create()

        val etLink = view.findViewById<EditText>(R.id.link)
        val addBtn = view.findViewById<MaterialButton>(R.id.addVideo)

        addBtn.setOnClickListener{
            val link = etLink.text.toString()
            var pattern: Pattern = Pattern.compile("^(?:https?:)?(?:\\/\\/)?(?:youtu\\.be\\/|(?:www\\.|m\\.)?youtube\\.com\\/(?:watch|v|embed)(?:\\.php)?(?:\\?.*v=|\\/))([a-zA-Z0-9\\_-]{7,15})(?:[\\?&][a-zA-Z0-9\\_-]+=[a-zA-Z0-9\\_-]+)*(?:[&\\/\\#].*)?\$")
            val m: Matcher = pattern.matcher(link)

            if(link.isNotEmpty()) {
                if(m.matches()){
                    startActivity(
                        Intent(this, AddCampaignActivity::class.java)
                            .putExtra("link", link).putExtra("type", type)
                    )
                    dialoge?.hide()
                }else{
                    Toast.makeText(this,"Please enter a valid link", Toast.LENGTH_LONG).show()
                }

            }else{
                Toast.makeText(this,"Video link is required", Toast.LENGTH_LONG).show()
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