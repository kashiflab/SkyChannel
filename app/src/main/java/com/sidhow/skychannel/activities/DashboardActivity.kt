package com.sidhow.skychannel.activities

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.ui.AppBarConfiguration
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.sidhow.skychannel.BuildConfig
import com.sidhow.skychannel.R
import com.sidhow.skychannel.Utils
import com.sidhow.skychannel.constant.Constants
import com.sidhow.skychannel.constant.PrefKeys
import com.sidhow.skychannel.databinding.ActivityDashboardBinding
import com.sidhow.skychannel.interfaces.OnComplete
import com.sidhow.skychannel.interfaces.OnGetCampaign
import com.sidhow.skychannel.model.Campaign
import com.sidhow.skychannel.model.User
import com.sidhow.skychannel.viewModel.MainViewModel
import com.tramsun.libs.prefcompat.Pref
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.content_dashboard.view.*
import java.util.*
import kotlin.collections.HashMap

class DashboardActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityDashboardBinding

    private var toggle: ActionBarDrawerToggle? = null
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var auth: FirebaseAuth

    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarDashboard.toolbar)

        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        mainViewModel.saveLikesCampaigns()
        mainViewModel.saveViewsCampaigns()


        val isUpdateAvailable = Pref.getString(Constants.versionName,"")

        if(isUpdateAvailable != BuildConfig.VERSION_NAME) {
//            showCustomDialog()
        }

        Utils.initpDialog(this,"Please wait")
        Utils.showpDialog()
        auth = FirebaseAuth.getInstance()

        //============================= Sidebar Drawer Navigation ==========================================================
        drawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView

        val header: View = navView.getHeaderView(0)
        navView.setItemIconTintList(null);

        //set username and Email
        val tvUsername = header.findViewById<TextView>(R.id.username)
        val tvEmail = header.findViewById<TextView>(R.id.email)
        val navHeaderImage = header.findViewById<CircleImageView>(R.id.imageView)

        navView.setNavigationItemSelectedListener(this)

        mainViewModel.getData(object : OnComplete<User> {
            override fun onCompleteTask(status: Boolean, result: User) {
                if(status) {
                    runOnUiThread {

                        tvUsername.text = result.username
                        tvEmail.text = result.email
                        Glide.with(this@DashboardActivity)
                            .load(result.photoUrl)
                            .into(navHeaderImage)

                        binding.root.txtCoin.text = result.coins
                    }
                    if(result.coins==null || result.coins.isEmpty()){
                        startActivity(Intent(this@DashboardActivity,LoginActivity::class.java))
                    }
                    Pref.putString(PrefKeys.username, result.username)
                    Pref.putString(PrefKeys.coins, result.coins)
                    Pref.putString(PrefKeys.photoUrl, result.photoUrl)
                    Pref.putString(PrefKeys.email, result.email)
                    Pref.putString(PrefKeys.isVIP, result.is_vip)
                }else{
                    if(auth.currentUser!=null){
                        auth.signOut()
                    }
                    startActivity(Intent(this@DashboardActivity,LoginActivity::class.java))
                    finish()
                }
            }
        })

        toggle = ActionBarDrawerToggle(
            this, drawerLayout, binding.appBarDashboard.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle!!)
        toggle?.isDrawerIndicatorEnabled = true
        toggle?.syncState()

        clickListeners()

    }

    private fun showCustomDialog() {
        // custom dialog
        val view: View = LayoutInflater.from(this)
            .inflate(R.layout.custom_dialog, null)
        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .setCancelable(false)
            .create()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val update = view.findViewById<Button>(R.id.update)

        update.setOnClickListener {
            val feedback = "https://play.google.com/store/apps/details?id=com.sidhow.skychannel"
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(feedback))
            startActivity(browserIntent)
            dialog?.dismiss()
        }

        dialog?.show()
    }

    override fun onResume() {

        super.onResume()
        mainViewModel.saveSubsCampaigns(object : OnGetCampaign<List<Campaign>> {
            override fun onGetCampaign(status: Boolean, result: List<Campaign>) {
                Utils.hidepDialog()
            }
        })
    }

    private fun clickListeners() {

        binding.root.subscribe.setOnClickListener {
            startActivity(Intent(this@DashboardActivity,SubscribeActivity::class.java))
        }

        binding.root.views.setOnClickListener {
            startActivity(Intent(this@DashboardActivity,ViewsActivity::class.java))
        }

        binding.root.like.setOnClickListener {
            startActivity(Intent(this@DashboardActivity,LikeActivity::class.java))
        }

        binding.root.campaign.setOnClickListener {
            startActivity(Intent(this@DashboardActivity,CampaignActivity::class.java))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle!!.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item);
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.buyCoins ->{
                drawerLayout.closeDrawer(Gravity.LEFT)
                startActivity(Intent(this@DashboardActivity,BuyPointsActivity::class.java))
            }
            R.id.vipAccount ->{
                drawerLayout.closeDrawer(Gravity.LEFT)
                startActivity(Intent(this@DashboardActivity,VIPMembershipActivity::class.java))
            }
            R.id.subscriptions ->{
                drawerLayout.closeDrawer(Gravity.LEFT)
                startActivity(Intent(this@DashboardActivity,SubscriptionActivity::class.java))
            }
            R.id.feedback ->{
                drawerLayout.closeDrawer(Gravity.LEFT)
                val feedback = "https://play.google.com/store/apps/details?id=com.sidhow.skychannel"
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(feedback))
                startActivity(browserIntent)
//                Toast.makeText(this@DashboardActivity,"Coming Soon",Toast.LENGTH_LONG).show()
            }
            R.id.share ->{
                drawerLayout.closeDrawer(Gravity.LEFT)
                val sharingIntent = Intent(Intent.ACTION_SEND)
                sharingIntent.type = "text/plain"
                val shareBody = "Checkout this new amazing app from which you can get authentic YT subscribers, likes and views. Try this app to boost your YT Channel."
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "SkyChannel")
                sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
                startActivity(Intent.createChooser(sharingIntent, "Share via"))
            }
            R.id.privacy ->{
                drawerLayout.closeDrawer(Gravity.LEFT)
                val privacyPolicy = "https://sites.google.com/view/skychannel-privacypolicy/home"
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicy))
                startActivity(browserIntent)
            }
            R.id.logout ->{
                drawerLayout.closeDrawer(Gravity.LEFT)
                auth.signOut()
                startActivity(Intent(this@DashboardActivity,LoginActivity::class.java))
                finish()
                finishAffinity()
            }
        }
        return true
    }
}

