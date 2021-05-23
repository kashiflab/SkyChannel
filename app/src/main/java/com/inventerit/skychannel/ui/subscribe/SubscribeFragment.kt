package com.inventerit.skychannel.ui.subscribe

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
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
import com.inventerit.skychannel.TimeHelper
import com.inventerit.skychannel.Utils
import com.inventerit.skychannel.YouTubeActivityPresenter
import com.inventerit.skychannel.constant.Constants
import com.inventerit.skychannel.constant.PrefKeys
import com.inventerit.skychannel.databinding.FragmentSubscribeBinding
import com.inventerit.skychannel.interfaces.LikeListener
import com.inventerit.skychannel.interfaces.OnCampaignStatus
import com.inventerit.skychannel.interfaces.OnGetCampaign
import com.inventerit.skychannel.interfaces.YouTubeActivityView
import com.inventerit.skychannel.model.Campaign
import com.inventerit.skychannel.room.model.Videos
import com.inventerit.skychannel.room.VideosDatabase
import com.inventerit.skychannel.viewModel.MainViewModel
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.tramsun.libs.prefcompat.Pref
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class SubscribeFragment : Fragment(), PermissionCallbacks, YouTubeActivityView {

    private lateinit var binding: FragmentSubscribeBinding

    private val TAG: String = "SubscribeFragment"
    private var player: YouTubePlayer? = null

    private lateinit var videosDatabase: VideosDatabase

    private var counter = 0

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

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSubscribeBinding.inflate(layoutInflater)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        Utils.initpDialog(context,"Please wait...")
        Utils.showpDialog()

        // initialize presenter
        presenter = YouTubeActivityPresenter(this, requireActivity())
        mCredential = GoogleAccountCredential.usingOAuth2(
                requireActivity(), listOf(YouTubeScopes.YOUTUBE))
                .setBackOff(ExponentialBackOff())


        videosDatabase = VideosDatabase.getInstance(context)
        coins = Pref.getString(PrefKeys.coins, "0")

        mainViewModel.getSubsCampaigns(object : OnGetCampaign<List<Campaign>> {
            override fun onGetCampaign(status: Boolean, result: List<Campaign>) {
                try {
                    campaign = ArrayList()
                    if (status) {
                        campaign = result
                        binding.videoTitle.text = campaign?.get(0)?.video_title
                        startVideo(campaign?.get(0)?.video_id.toString())
                    } else if (context != null) {
                        Utils.hidepDialog()
                        Toast.makeText(context, "No video found", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })

        binding.subscribe.setOnClickListener {

            getResultsFromApi()
        }

        return binding.root
    }

    private var isAlreadySubscribed: Boolean = false
    private var isPaused: Boolean = false
    private var secondsLeft: Long = 0
    fun checkAlreadySubscribed(videoId: String){

        Thread {
            videos = videosDatabase.videosDao().getAllSubscriptions("0", videoId)
            isAlreadySubscribed = videos.isNotEmpty()

            Log.i(TAG, videos.size.toString())
        }.start()
    }

    fun startVideo(videoId: String){
        lifecycle.addObserver(binding.video)

        // using library
        binding.video.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                Utils.hidepDialog()
                checkAlreadySubscribed(campaign?.get(currentNumber)?.channel_id.toString())
                Handler(Looper.myLooper()!!).postDelayed(Runnable {
                    if (videoId.isNotEmpty()) {
                        if(isAlreadySubscribed){
                            binding.subscribe.isEnabled = false
                            binding.subscribe.text = "Subscribed"
                            Log.i(TAG,"Subscribed")
                        }else {
                            binding.subscribe.isEnabled = false
                            binding.subscribe.text = "Subscribe"
                            Log.i(TAG,"Subscribe")
                        }
                        val time = campaign?.get(currentNumber)?.total_time?.toLong()!! * 1000
//                        startTimer(time, false)
                        youTubePlayer.loadVideo(videoId, 0f)
                        player = youTubePlayer
                    }
                },500)


                binding.nextBtn.setOnClickListener {
                    if (currentNumber < campaign?.size!! - 1) {
                        currentNumber += 1
                        checkAlreadySubscribed(campaign?.get(currentNumber)?.video_id.toString())
                        Handler(Looper.myLooper()!!).postDelayed({
                            Log.i(TAG,"Current Number: $currentNumber")
                                if(isAlreadySubscribed){
                                    binding.subscribe.isEnabled = false
                                    binding.subscribe.text = "Subscribed"
                                }else {
                                    binding.subscribe.isEnabled = false
                                    binding.subscribe.text = "Subscribe"
                                }

                                val time = campaign?.get(currentNumber)?.total_time?.toLong()!! * 1000
//                                startTimer(time, false)
                                binding.videoTitle.text = campaign?.get(currentNumber)?.video_title
                                youTubePlayer.loadVideo(campaign?.get(currentNumber)?.video_id!!, 0f)
                                player = youTubePlayer

                        },500)
                    } else if (context != null) {
                        Toast.makeText(context, "No more video found", Toast.LENGTH_LONG).show()
                    }
                }

            }

            /**override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                super.onStateChange(youTubePlayer, state)
                Log.i(TAG,state.toString())
                if(state.toString() == "PAUSED"){
                    Log.i(TAG,"timer")
//                    countDownTimer?.cancel()
                    isPaused = true
                    secondsLeft = binding.timer.text.toString().split(": ")[1].toLong()
                    Log.i(TAG,secondsLeft.toString())
                }else if(isPaused){
                    startTimer(secondsLeft,false)
                    isPaused = false
                }
            }*/

            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                super.onCurrentSecond(youTubePlayer, second)
                val time = campaign?.get(currentNumber)?.total_time?.toInt()!! - second.toInt()
                if(time>=0){
                    if(time==0){
                        binding.subscribe.isEnabled = !isAlreadySubscribed
                        binding.timer.text = time.toString()
                    }else{
                        binding.coins.visibility = View.VISIBLE
                        binding.coins.text = "220"
                        binding.subscribe.isEnabled = false
                        binding.timer.text = time.toString()
                    }
                }
            }

            override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
                super.onError(youTubePlayer, error)
                Utils.hidepDialog()
                Toast.makeText(context,"Some error occurred",Toast.LENGTH_LONG).show()
            }
        })
    }

    // ================================= Subscribe ====================================

    private fun getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices()
        } else if (mCredential?.selectedAccountName == null) {
            chooseAccount()
        } else {
            initpDialog(context, "Checking all subscribed channels")
            showpDialog()
            // handing subscribe task by presenter
            presenter?.subscribeToYouTubeChannel(mCredential, campaign?.get(currentNumber)?.channel_id.toString()) // pass youtube channelId as second parameter
        }
    }

    // checking google play service is available on phone or not
    private fun isGooglePlayServicesAvailable(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(requireActivity())
        return connectionStatusCode == ConnectionResult.SUCCESS
    }


    private fun acquireGooglePlayServices() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(requireActivity())
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            val dialog = apiAvailability.getErrorDialog(
                    requireActivity(),  // showing dialog to user for getting google play service
                    connectionStatusCode,
                    REQUEST_GOOGLE_PLAY_SERVICES)
            dialog.show()
        }
    }

    private fun chooseAccount() {
        if (EasyPermissions.hasPermissions(requireContext(), Manifest.permission.GET_ACCOUNTS)) {
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
                Toast.makeText(context, "This app requires Google Play Services. Please " +
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
                } else if (context != null) {
                    Toast.makeText(context, "Permission Required if granted then check internet connection", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    override fun onPermissionsGranted(requestCode: Int, perms: List<String?>?) {
        getResultsFromApi() // user have granted permission so continue
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String?>?) {
        if (context != null) {
            Toast.makeText(context, "This app needs to access your Google account for YouTube channel subscription.", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onPause() {
        super.onPause()
        if(countDownTimer!=null){
            countDownTimer?.cancel()
        }
//        if(player!=null) {
//            player?.pause()
//        }
    }

    override fun onSubscribetionSuccess(title: String?) {
        hidepDialog()
        if(context!=null) {
            Toast.makeText(context, "Successfully subscribe to $title", Toast.LENGTH_SHORT).show()
        }
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

        val updatedCoins = (coins.toInt() + 130).toString()
        mainViewModel.updateCoins(updatedCoins, object : LikeListener {
            override fun onLikeStarted() {
                Log.i(TAG, "Updating Coins")
            }

            override fun onLikedSuccess() {
                startTimer(5000, true)
                Pref.putString(PrefKeys.coins, updatedCoins)
                if (context != null) {
                    Toast.makeText(context, "Received $updatedCoins coins", Toast.LENGTH_LONG).show()
                }
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
                        currentNumber++
                        startVideo(campaign?.get(currentNumber)?.video_id.toString())
                    }
                }
            }
        }
        countDownTimer?.start()
    }

    override fun onSubscribetionFail() {

        hidepDialog()
        Toast.makeText(context, "Already Subscribed",
                Toast.LENGTH_LONG).show()

    }

    private var pDialog: ProgressDialog? = null

    private fun initpDialog(context: Context?, msg: String?) {
        pDialog = ProgressDialog(context)
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