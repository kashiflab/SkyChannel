package com.sidhow.skychannel.activities

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.youtube.YouTubeScopes
import com.google.api.services.youtube.model.Subscription
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.sidhow.skychannel.R
import com.sidhow.skychannel.YouTubeActivityPresenter
import com.sidhow.skychannel.constant.Constants
import com.sidhow.skychannel.constant.PrefKeys
import com.sidhow.skychannel.databinding.ActivityLoginBinding
import com.sidhow.skychannel.interfaces.OnGetSubscribedChannelList
import com.sidhow.skychannel.interfaces.YouTubeActivityView
import com.sidhow.skychannel.room.VideosDatabase
import com.sidhow.skychannel.room.model.Videos
import com.sidhow.skychannel.viewModel.MainViewModel
import com.tbruyelle.rxpermissions3.RxPermissions
import com.tramsun.libs.prefcompat.Pref
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class LoginActivity : AppCompatActivity(), YouTubeActivityView {
    companion object {
        private const val RC_SIGN_IN = 120
    }

    private lateinit var mainViewModel: MainViewModel
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var mAuth: FirebaseAuth

    private lateinit var binding: ActivityLoginBinding
    private lateinit var rx: RxPermissions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Firebase Auth instance
        mAuth = FirebaseAuth.getInstance()

        getUsers()

        rx = RxPermissions(this)
        askPermission()
        binding.signInBtn.setOnClickListener {
            signIn()
        }

        val scopes = Scope("https://www.googleapis.com/auth/youtube")
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestScopes(scopes)
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

    }
    private fun askPermission(){
        rx.request(android.Manifest.permission.GET_ACCOUNTS, android.Manifest.permission.INTERNET)
                .subscribe{granted ->
                    if (!granted) {
                        askPermission()
                    }
                }
    }
    private fun signIn() {
        initpDialog(this,"Please wait")
        showpDialog()
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val exception = task.exception
            if (task.isSuccessful) {
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    val account = task.getResult(ApiException::class.java)!!
                    Log.d("SignInActivity", "firebaseAuthWithGoogle:" + account.id)
                    firebaseAuthWithGoogle(account.idToken!!)

                } catch (e: ApiException) {
                    hidepDialog()
                    // Google Sign In failed, update UI appropriately
                    Log.w("SignInActivity", "Google sign in failed", e)
                }
            } else {
                hidepDialog()
                Log.w("SignInActivity", exception.toString())
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    try {
//                        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
                        getData(task)
                    }catch (e:Exception){
                        e.printStackTrace()
                    }
                } else {
                    hidepDialog()
                    // If sign in fails, display a message to the user.
                    Log.d("SignInActivity", "signInWithCredential:failure ${task.exception}")
                }
            }
    }

    private fun setUserDetails(username: String, email: String, photoUrl: String, id: String) {
        val database = Firebase.database.reference.child("users")

        val format = SimpleDateFormat("dd-MM-yyyy hh:mm:ss", Locale.getDefault())
        val date = format.format(Date())
        val map: HashMap<String, Any> = HashMap()

        Pref.putString(PrefKeys.username,username)
        Pref.putString(PrefKeys.coins,"450")
        Pref.putString(PrefKeys.photoUrl,photoUrl)
        Pref.putString(PrefKeys.email,email)

        map["username"] = username
        map["email"] = email
        map["coins"] = "450"
        map["photoUrl"] = photoUrl
        map["id"] = id
        map["created_at"] = date
        map["is_vip"] = "false"

        database.child(id).setValue(map).addOnCompleteListener {
            if(it.isSuccessful){
                // Sign in success, update UI with the signed-in user's information
                Log.d("SignInActivity", "signInWithCredential:success")
                goToMain()
            }else{
                Toast.makeText(this,"Some error occurred",Toast.LENGTH_LONG).show()
                hidepDialog()
            }
        }

    }

    private lateinit var videoDatabase: VideosDatabase
    private fun getData(task: Task<AuthResult>) {
        videoDatabase = VideosDatabase.getInstance(this)

        val user = FirebaseAuth.getInstance().currentUser
        if(user!=null && Pref.getBoolean(PrefKeys.isFirstTime,false)){
            Log.i("RoomDB","First")
            // Get Liked and viewed video history
            getAllHistory(user,task)

            getSubscriptionFromYoutube(task)
        }else if(!emailList?.contains(task.result?.user?.email.toString())!!) {
            setUserDetails(
                task.result?.user?.displayName.toString(),
                task.result?.user?.email.toString(),
                task.result?.user?.photoUrl.toString(),
                task.result?.user?.uid.toString()
            )
        }else{
            goToMain()
        }
    }

    private fun getSubscriptionFromYoutube(task: Task<AuthResult>) {
        //get subscriptions from youtube data api
        val email = task.result?.user?.email
        if(email!!.isNotEmpty()) {
            val mCredential = GoogleAccountCredential.usingOAuth2(
                this, listOf(YouTubeScopes.YOUTUBE)
            )
                .setBackOff(ExponentialBackOff())
            mCredential!!.selectedAccountName = email

            val presenter =
                YouTubeActivityPresenter(this, this)
            presenter.getAllSubscribedChannels(mCredential, object :
                OnGetSubscribedChannelList {
                override fun onGetSubscribedChannelList(
                    status: Boolean,
                    subscribedChannels: List<Subscription>
                ) {

                    if(status) {
                        for (subscribed in subscribedChannels) {
                            Thread {
                                val videos = Videos()
                                videos.videoId = ""
                                videos.created_at = System.currentTimeMillis().toString()
                                videos.type = "0"
                                videos.userId = task.result?.user?.uid
                                videos.channelId = subscribed.snippet.resourceId.channelId.toString()

                                Log.i("SubscribedChannel: ","${subscribed.snippet.resourceId.channelId}, ${subscribed.snippet.title}")

                                Log.i("RoomDB", "Fetching Data")

                                Log.i("RoomDB", "Subscription Inserting")
                                videoDatabase.videosDao().insert(videos)
                                Log.i("RoomDB", "Subscription Inserted ${videos.channelId}")
                            }.start()
                        }

//                        mainViewModel.saveSubsCampaigns()
                    }

//                    Handler(Looper.myLooper()!!).postDelayed({

                        if(!emailList?.contains(task.result?.user?.email.toString())!!) {
                            setUserDetails(
                                task.result?.user?.displayName.toString(),
                                task.result?.user?.email.toString(),
                                task.result?.user?.photoUrl.toString(),
                                task.result?.user?.uid.toString()
                            )
                        }else{
                            goToMain()
                        }
//                    },1000)
                }
            })
        }
    }

    private fun getAllHistory(user: FirebaseUser, task: Task<AuthResult>){

        val database: DatabaseReference = FirebaseDatabase.getInstance().reference
        database.child(Constants.users).child(user.uid).child(Constants.history).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(snapshot1 in snapshot.children){
                    if(snapshot1.child(Constants.type).value.toString()==Constants.LIKE_TYPE.toString() ||
                        snapshot1.child(Constants.type).value.toString()==Constants.VIEW_TYPE.toString()) {
                        val videos = Videos()

                        videos.videoId = snapshot1.child(Constants.video_id).value.toString()
                        videos.created_at = snapshot1.child(Constants.created_at).value.toString()
                        videos.type = snapshot1.child(Constants.type).value.toString()
                        videos.userId = snapshot1.child(Constants.user_id).value.toString()
                        videos.channelId = snapshot1.child(Constants.channel_id).value.toString()

                        Log.i("RoomDB", "Fetching Data")
                        Thread {
                            try {
                                Log.i("RoomDB", "Inserting Data")
                                videoDatabase.videosDao().insert(videos)
                                Log.i("RoomDB", "Data Inserted")
                            }catch (e: Exception){
                                e.printStackTrace()
                            }
                        }.start()
                    }
                }

//                Thread {
//                    mainViewModel.saveLikesCampaigns()
//                    mainViewModel.saveViewsCampaigns()
//                }.start()

                Pref.putBoolean(PrefKeys.isFirstTime,false)
            }

            override fun onCancelled(error: DatabaseError) {
                hidepDialog()
                Log.i("RoomDB","${error.message}")
            }
        })
    }
    fun goToMain(){
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
        finish()
    }

    private var emailList: ArrayList<String> = ArrayList()
    private fun getUsers(){
        val database = Firebase.database.reference.child("users")

        database.get().addOnSuccessListener {
            emailList.clear()
            val children = it.children
            children.forEach {
                emailList!!.add(it.child("email").value.toString())
                Log.i("SignInActivity",emailList.toString())
            }
        }.addOnFailureListener{
            Log.e("firebase", "Error getting data", it)
        }

    }

    override fun onSubscribetionSuccess(title: String?) {

    }

    override fun onSubscribetionFail() {

    }
    private var pDialog: ProgressDialog? = null

    override fun onPause() {
        super.onPause()
    }

    fun initpDialog(context: Context?, msg: String?) {
        pDialog = ProgressDialog(context)
        pDialog!!.setMessage(msg)
        pDialog!!.setCancelable(false)
    }

    fun showpDialog() {
        try{
            if (!pDialog!!.isShowing) pDialog!!.show()
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    fun hidepDialog() {
        try {
            if (pDialog!!.isShowing) pDialog!!.dismiss()
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

}