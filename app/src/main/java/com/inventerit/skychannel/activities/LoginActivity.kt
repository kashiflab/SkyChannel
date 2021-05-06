package com.inventerit.skychannel.activities

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.inventerit.skychannel.R
import com.inventerit.skychannel.Utils
import com.inventerit.skychannel.constant.PrefKeys
import com.inventerit.skychannel.databinding.ActivityLoginBinding
import com.tbruyelle.rxpermissions3.RxPermissions
import com.tramsun.libs.prefcompat.Pref
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class LoginActivity : AppCompatActivity() {
    companion object {
        private const val RC_SIGN_IN = 120
    }

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
//            startActivity(Intent(this,MainActivity::class.java))
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
        Utils.initpDialog(this, "Please wait...")
        Utils.showpDialog()
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
                    Utils.hidepDialog()
                    // Google Sign In failed, update UI appropriately
                    Log.w("SignInActivity", "Google sign in failed", e)
                }
            } else {
                Utils.hidepDialog()
                Log.w("SignInActivity", exception.toString())
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    if(email?.contains(task.result?.user?.email.toString()) == false) {
                        setUserDetails(
                            task.result?.user?.displayName.toString(),
                            task.result?.user?.email.toString(),
                            task.result?.user?.photoUrl.toString(),
                            task.result?.user?.uid.toString()
                        );
                    }else{
                        goToMain()
                    }

                } else {
                    Utils.hidepDialog()
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
        Pref.putString(PrefKeys.photoUrl,username)

        map["username"] = username
        map["email"] = email
        map["coins"] = "450"
        map["photoUrl"] = photoUrl
        map["id"] = id
        map["created_at"] = date

        database.child(id).setValue(map).addOnCompleteListener {
            if(it.isSuccessful){
                // Sign in success, update UI with the signed-in user's information
                Log.d("SignInActivity", "signInWithCredential:success")
                goToMain()
            }else{
                Toast.makeText(this,"Some error occurred",Toast.LENGTH_LONG).show()
                Utils.hidepDialog()
            }
        }

    }

    fun goToMain(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
        Utils.hidepDialog()
    }

    private var email: ArrayList<String>? = null
    private fun getUsers(){
        val database = Firebase.database.reference.child("users")

        database.get().addOnSuccessListener {
            email = ArrayList()
            val children = it.children
            children.forEach {
                email!!.add(it.child("email").value.toString())
                Log.i("SignInActivity",email.toString())
            }
        }.addOnFailureListener{
            Log.e("firebase", "Error getting data", it)
        }

    }

}