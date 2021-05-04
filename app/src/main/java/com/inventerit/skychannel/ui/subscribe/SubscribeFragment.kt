package com.inventerit.skychannel.ui.subscribe

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.youtube.YouTubeScopes
import com.inventerit.skychannel.Utils
import com.inventerit.skychannel.YouTubeActivityPresenter
import com.inventerit.skychannel.constant.PrefKeys
import com.inventerit.skychannel.databinding.FragmentSubscribeBinding
import com.inventerit.skychannel.interfaces.OnGetCampaign
import com.inventerit.skychannel.interfaces.YouTubeActivityView
import com.inventerit.skychannel.model.Campaign
import com.inventerit.skychannel.viewModel.MainViewModel
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.tramsun.libs.prefcompat.Pref
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks


class SubscribeFragment : Fragment(), PermissionCallbacks, YouTubeActivityView {

    private lateinit var binding: FragmentSubscribeBinding

    private val TAG: String = "SubscribeFragment"
    private var player: YouTubePlayer? = null

    private var counter = 0

    private lateinit var mainViewModel: MainViewModel
    private var currentNumber = 0;

    private var campaign: List<Campaign>? = null

    private var mCredential: GoogleAccountCredential? = null
    private var presenter: YouTubeActivityPresenter? = null

    private val PREF_ACCOUNT_NAME = "accountName"

    // if you are using YouTubePlayerView in xml then activity must extend YouTubeBaseActivity
    private val RECOVERY_DIALOG_REQUEST = 1
    val REQUEST_ACCOUNT_PICKER = 1000
    val REQUEST_AUTHORIZATION = 1001
    val REQUEST_GOOGLE_PLAY_SERVICES = 1002
    val REQUEST_PERMISSION_GET_ACCOUNTS = 1003
    private val RC_SIGN_IN = 12311

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSubscribeBinding.inflate(layoutInflater)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        // initialize presenter
        presenter = YouTubeActivityPresenter(this, requireActivity())
        mCredential = GoogleAccountCredential.usingOAuth2(
                requireActivity(), listOf(YouTubeScopes.YOUTUBE))
                .setBackOff(ExponentialBackOff())

        mainViewModel.getAllCampaigns(object : OnGetCampaign<List<Campaign>> {
            override fun onGetCampaign(status: Boolean, result: List<Campaign>) {
                campaign = ArrayList()
                if (status) {
                    campaign = result
                    binding.videoTitle.text = campaign?.get(0)?.video_title
                    startVideo(campaign?.get(0)?.video_id.toString())
                } else {
                    Toast.makeText(context, "No video found", Toast.LENGTH_LONG).show()
                }
            }
        })

        binding.subscribe.setOnClickListener {

            getResultsFromApi()
        }

        return binding.root
    }

    fun startVideo(videoId: String){
        lifecycle.addObserver(binding.video)
        binding.video.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {

                youTubePlayer.loadVideo(videoId, 0f)
                player = youTubePlayer

                binding.nextBtn.setOnClickListener {
                    if (currentNumber < campaign?.size!! - 1) {
                        currentNumber++
                        binding.videoTitle.text = campaign?.get(currentNumber)?.video_title
                        youTubePlayer.loadVideo(campaign?.get(currentNumber)?.video_id!!, 0f)
                        player = youTubePlayer
                    } else {
                        Toast.makeText(context, "No more video found", Toast.LENGTH_LONG).show()
                    }
                }

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
            initpDialog(context, "Please wait...")
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
                Toast.makeText(requireActivity(), "This app requires Google Play Services. Please " +
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
                    Toast.makeText(requireActivity(), "Permission Required if granted then check internet connection", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    override fun onPermissionsGranted(requestCode: Int, perms: List<String?>?) {
        getResultsFromApi() // user have granted permission so continue
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String?>?) {
        Toast.makeText(requireContext(), "This app needs to access your Google account for YouTube channel subscription.", Toast.LENGTH_SHORT).show()
    }


    override fun onPause() {
        super.onPause()
        if(player!=null) {
            player?.pause()
        }
    }

    override fun onSubscribetionSuccess(title: String?) {
        hidepDialog()
        Toast.makeText(requireActivity(), "Successfully subscribe to $title", Toast.LENGTH_SHORT).show()
    }

    override fun onSubscribetionFail() {

        hidepDialog()

        // user don't have youtube channel subscribe permission so grant it form him
        // as we have not taken at the time of sign in
        if (counter < 3) {
            counter++ // attempt three times on failure
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestScopes(Scope("https://www.googleapis.com/auth/youtube")) // require this scope for youtube channel subscribe
                    .build()

//            GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
//                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
//                    .build();
            val mGoogleApiClient = GoogleSignIn.getClient(requireActivity(), gso)
            //            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
            val signInIntent = mGoogleApiClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        } else {
            Toast.makeText(requireActivity(), "Already Subscribed",
                    Toast.LENGTH_LONG).show()
        }
    }

    private var pDialog: ProgressDialog? = null

    fun initpDialog(context: Context?, msg: String?) {
        pDialog = ProgressDialog(context)
        pDialog!!.setMessage(msg)
        pDialog!!.setCancelable(false)
    }

    fun showpDialog() {
        if (!pDialog!!.isShowing) pDialog!!.show()
    }

    fun hidepDialog() {
        if (pDialog!!.isShowing) pDialog!!.dismiss()
    }

}