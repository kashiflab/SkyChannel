package com.sidhow.skychannel.activities

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.youtube.YouTubeScopes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.sidhow.skychannel.TimeHelper
import com.sidhow.skychannel.Utils
import com.sidhow.skychannel.YouTubeActivityPresenter
import com.sidhow.skychannel.constant.Constants
import com.sidhow.skychannel.constant.PrefKeys
import com.sidhow.skychannel.databinding.ActivitySubscribeBinding
import com.sidhow.skychannel.interfaces.*
import com.sidhow.skychannel.model.Campaign
import com.sidhow.skychannel.room.VideosDatabase
import com.sidhow.skychannel.room.model.Videos
import com.sidhow.skychannel.viewModel.MainViewModel
import com.tramsun.libs.prefcompat.Pref
import pub.devrel.easypermissions.EasyPermissions
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class SubscribeActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks,
    YouTubeActivityView {

    private lateinit var binding: ActivitySubscribeBinding
    private val TAG: String = "SubscribeActivity"
    private var player: YouTubePlayer? = null

    private lateinit var videosDatabase: VideosDatabase

    private lateinit var mainViewModel: MainViewModel
    private var currentNumber = 0

    private var campaign: List<Campaign>? = null
    private lateinit var videos: List<Videos>

    private var mCredential: GoogleAccountCredential? = null
    private var presenter: YouTubeActivityPresenter? = null

    private var countDownTimer: CountDownTimer? = null

    // if you are using YouTubePlayerView in xml then activity must extend YouTubeBaseActivity
    val REQUEST_ACCOUNT_PICKER = 1000
    val REQUEST_AUTHORIZATION = 1001
    val REQUEST_GOOGLE_PLAY_SERVICES = 1002
    val REQUEST_PERMISSION_GET_ACCOUNTS = 1003
    private val RC_SIGN_IN = 12311

    private var mUser = FirebaseAuth.getInstance().currentUser

    private var coins: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubscribeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Subscribe"
        actionBar?.setDisplayHomeAsUpEnabled(true)

        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        // initialize presenter
        presenter = YouTubeActivityPresenter(this, this)
        mCredential = GoogleAccountCredential.usingOAuth2(
            this, listOf(YouTubeScopes.YOUTUBE))
            .setBackOff(ExponentialBackOff())

//        mainViewModel.saveSubsCampaigns(object : OnGetCampaign<List<Campaign>> {
//            override fun onGetCampaign(status: Boolean, result: List<Campaign>) {
//                try {
//                    Utils.hidepDialog()
//                    campaign = ArrayList()
//                    if (status && result.isNotEmpty()) {
//                        campaign = result
//                        binding.videoTitle.text = campaign?.get(0)?.video_title
//                        startVideo(campaign?.get(0)?.video_id.toString())
//                    } else {
//                        Toast.makeText(this@SubscribeActivity, "No video found", Toast.LENGTH_LONG).show()
//                    }
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//            }
//        })

        videosDatabase = VideosDatabase.getInstance(this)
        coins = Pref.getString(PrefKeys.coins, "0")



        mainViewModel.getSubsCampaigns(object : OnGetCampaign<List<Campaign>> {
            override fun onGetCampaign(status: Boolean, result: List<Campaign>) {
                Thread {
                    if(result.isNotEmpty()){
                        Utils.hidepDialog()
                        try {
                            campaign = result
                            binding.videoTitle.text = campaign?.get(0)?.video_title
                            startVideo(campaign?.get(0)?.video_id.toString())
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }else {
                        runOnUiThread {
                            Toast.makeText(this@SubscribeActivity, "No video found", Toast.LENGTH_LONG)
                                .show()
                        }
                    }
                }.start()
            }
        })

        binding.subscribe.setOnClickListener {
            getResultsFromApi()
            try{
                player?.pause()
            }catch (e: java.lang.Exception){
                e.printStackTrace()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private var isAlreadySubscribed: Boolean = false
    private var isPaused: Boolean = false

    fun startVideo(videoId: String){
        lifecycle.addObserver(binding.video)

        // using library
        binding.video.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {

                binding.subscribe.isEnabled = false
                binding.subscribe.text = "Subscribe"
                Log.i(TAG,"Subscribe")

                youTubePlayer.loadVideo(videoId, 0f)
                player = youTubePlayer

                binding.nextBtn.setOnClickListener {
                    startNextVideo(youTubePlayer)
                }

            }
            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                super.onCurrentSecond(youTubePlayer, second)
                val time = campaign?.get(currentNumber)?.total_time?.toInt()!! - second.toInt()
                if(time>=0){
                    if(time==0){
                        binding.subscribe.isEnabled = true
                        binding.timer.text = time.toString()
                    }else{
                        binding.coins.visibility = View.VISIBLE
                        binding.coins.text = campaign!![currentNumber].coins
                        binding.subscribe.isEnabled = false
                        binding.timer.text = time.toString()
                    }
                }
            }

            override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
                super.onError(youTubePlayer, error)
                Toast.makeText(this@SubscribeActivity,"Some error occurred",Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun startNextVideo(youTubePlayer: YouTubePlayer){
        if (currentNumber < campaign?.size!! - 1) {
            currentNumber += 1
            Log.i(TAG,"Current Number: $currentNumber")

            binding.subscribe.isEnabled = false
            binding.subscribe.text = "Subscribe"

            binding.videoTitle.text = campaign?.get(currentNumber)?.video_title
            youTubePlayer.loadVideo(campaign?.get(currentNumber)?.video_id!!, 0f)
            player = youTubePlayer

        }else{
            Toast.makeText(this, "No more video found", Toast.LENGTH_LONG).show()
        }
    }

    // ================================= Subscribe ====================================

    private fun getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices()
        } else if (mCredential?.selectedAccountName == null) {
            chooseAccount()
        } else {
            initpDialog("Subscribing the channel")
            showpDialog()
            // handing subscribe task by presenter
            presenter?.subscribeToYouTubeChannel(mCredential, campaign?.get(currentNumber)?.channel_id.toString()) // pass youtube channelId as second parameter
        }
    }

    // checking google play service is available on phone or not
    private fun isGooglePlayServicesAvailable(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
        return connectionStatusCode == ConnectionResult.SUCCESS
    }


    private fun acquireGooglePlayServices() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            val dialog = apiAvailability.getErrorDialog(
                this,  // showing dialog to user for getting google play service
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES)
            dialog.show()
        }
    }

    private fun chooseAccount() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.GET_ACCOUNTS)) {
            val accountName: String = Pref.getString(PrefKeys.email)
            if (accountName != null) {
                mCredential?.selectedAccountName = accountName
                getResultsFromApi()
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(mCredential?.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER)
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                this,
                "This app needs to access your Google account for YouTube channel subscription.",
                REQUEST_PERMISSION_GET_ACCOUNTS, Manifest.permission.GET_ACCOUNTS)
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_GOOGLE_PLAY_SERVICES -> if (resultCode != Activity.RESULT_OK) {
                Toast.makeText(this, "This app requires Google Play Services. Please " +
                        "install Google Play Services on your device and relaunch this app.", Toast.LENGTH_SHORT).show()
            } else {
                getResultsFromApi()
            }
            REQUEST_ACCOUNT_PICKER -> if (resultCode == Activity.RESULT_OK && data != null && data.extras != null) {
                val accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                if (accountName != null) {
                    mCredential!!.selectedAccountName = accountName
                    getResultsFromApi()
                }
            }
            REQUEST_AUTHORIZATION -> if (resultCode == Activity.RESULT_OK) {
                getResultsFromApi()
            }
            RC_SIGN_IN -> {
                val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
                if (result!!.isSuccess) {
                    getResultsFromApi()
                } else {
                    Toast.makeText(this, "Permission Required if granted then check internet connection", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    override fun onPermissionsGranted(requestCode: Int, perms: List<String?>?) {
        getResultsFromApi() // user have granted permission so continue
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String?>?) {
        Toast.makeText(this, "This app needs to access your Google account for YouTube channel subscription.", Toast.LENGTH_SHORT).show()
    }


    override fun onPause() {
        super.onPause()
        if(countDownTimer!=null){
            countDownTimer?.cancel()
        }
        if(player!=null) {
            player?.pause()
        }
    }

    override fun onSubscribetionSuccess(title: String?) {
        hidepDialog()
        Toast.makeText(this, "Successfully subscribe to $title", Toast.LENGTH_SHORT).show()

        val videos = Videos()
        videos.videoId = campaign?.get(currentNumber)?.video_id.toString()
        videos.created_at = TimeHelper.getDate()
        videos.type = "0"
        videos.userId = mUser.uid
        videos.channelId = campaign?.get(currentNumber)?.channel_id.toString()

        saveIntoDB(videos)
        saveIntoFirebase(videos)

        binding.subscribe.text = "Subscribed"
        updateUserCoins()
        updateCampaignStatus()
    }

    private fun saveIntoFirebase(videos: Videos) {
        val database: DatabaseReference = FirebaseDatabase.getInstance().reference
        val map: HashMap<String,Any> = HashMap()
        val id = UUID.randomUUID().toString()
        map[Constants.id] = id
        map[Constants.video_id] = videos.videoId
        map[Constants.created_at] = videos.created_at
        map[Constants.type] = videos.type
        map[Constants.user_id] = videos.userId
        map[Constants.channel_id] = videos.channelId

        database.child(Constants.users).child(mUser.uid).child(Constants.history).child(id).setValue(map)
    }

    private fun saveIntoDB(video: Videos){
        Thread {
            videosDatabase.videosDao().insert(video)
            Log.i(TAG, "${video.videoId}, ${campaign?.get(currentNumber)?.video_id}")
        }.start()
    }

    private fun updateCampaignStatus() {
        val campaignId = campaign?.get(currentNumber)?.id.toString()
        val campaignNumber = (campaign?.get(currentNumber)?.current_number?.toInt()?.plus(1)).toString()

        mainViewModel.updateCampaignStatus(campaignId, campaignNumber, object : OnCampaignStatus {
            override fun onCampaignStatus(status: Boolean) {
                if (status) {
                    Log.i(TAG, "Campaign status increased")
                } else {
                    Log.i(TAG, "Campaign Status not increased")
                }
            }
        })

        addUser(campaignId,mUser.uid)

    }

    private fun addUser(campaignId: String, uid: String) {
        mainViewModel.addUser(campaignId,uid)
    }

    private fun updateUserCoins(){

        val updatedCoins = (coins.toInt() + campaign?.get(currentNumber)?.coins?.toInt()!!).toString()
        mainViewModel.updateCoins(updatedCoins, object : LikeListener {
            override fun onLikeStarted() {
                Log.i(TAG, "Updating Coins")
            }

            override fun onLikedSuccess() {
                if(Pref.getString(PrefKeys.isVIP)=="false") {
                    startTimer(5000, true)
                }else{
                    startTimer(1000,true)
                }
                Toast.makeText(this@SubscribeActivity, "Coins added", Toast.LENGTH_LONG).show()
                Log.i(TAG, "Coins are updated")
            }

            override fun onLikedFailed() {

                Log.i(TAG, "Coins are not updated")
            }

        })
    }

    private fun startTimer(millis: Long, isSubscribed: Boolean) {

        countDownTimer = object : CountDownTimer(millis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if(!isSubscribed) {
                    binding.coins.visibility = View.VISIBLE
                    binding.coins.text = "220"
                    binding.subscribe.isEnabled = false
                }else{
                    binding.nextBtn.isEnabled = false
                    binding.subscribe.isEnabled = false
                    binding.coins.visibility = View.GONE
                }
                if(!isPaused) {
                    binding.timer.text = "Seconds remaining: ${millisUntilFinished / 1000}"
                }
            }

            override fun onFinish() {
                if(!isSubscribed && !isAlreadySubscribed) {
                    binding.subscribe.isEnabled = true

                }else {
                    binding.nextBtn.isEnabled = true
                    if (currentNumber < campaign?.size!!-1) {
                        currentNumber +=1
                        startVideo(campaign?.get(currentNumber)?.video_id.toString())
                    }
                }
            }
        }
        countDownTimer?.start()
    }

    override fun onSubscribetionFail() {

        hidepDialog()
        Toast.makeText(this, "Already Subscribed",
            Toast.LENGTH_LONG).show()

//        val videos = Videos()
//        videos.videoId = campaign?.get(currentNumber)?.video_id.toString()
//        videos.created_at = TimeHelper.getDate()
//        videos.type = "0"
//        videos.userId = mUser.uid
//        videos.channelId = campaign?.get(currentNumber)?.channel_id.toString()
//
//        saveIntoDB(videos)

    }

    private var pDialog: ProgressDialog? = null

    private fun initpDialog(msg: String?) {
        pDialog = ProgressDialog(this)
        pDialog!!.setTitle("Please wait")
        pDialog!!.setMessage(msg)
        pDialog!!.setCancelable(false)
    }

    private fun showpDialog() {
        if (!pDialog!!.isShowing) pDialog!!.show()
    }

    private fun hidepDialog() {
        if (pDialog!!.isShowing) pDialog!!.dismiss()
    }

}