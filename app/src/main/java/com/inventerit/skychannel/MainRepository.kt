package com.inventerit.skychannel

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.inventerit.skychannel.`interface`.OnComplete
import com.inventerit.skychannel.model.User

class MainRepository {
    //Singleton
    companion object {
        @Volatile
        private var INSTANCE: MainRepository? = null
        fun getRepo(): MainRepository {
            if (INSTANCE == null) {
                synchronized(this) {
                    INSTANCE = MainRepository()
                }
            }
            return INSTANCE!!
        }
    }

    var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    var mUser: FirebaseUser
    var database = Firebase.database.reference.child("users")

    init {
        mUser = mAuth.currentUser
    }

    fun getUserData(onComplete: OnComplete<User>){
        database.child(mUser.uid).addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = User()
                user.id = snapshot.child("id").value.toString()
                user.username = snapshot.child("username").value.toString()
                user.email = snapshot.child("email").value.toString()
                user.photoUrl = snapshot.child("photoUrl").value.toString()
                user.coins = snapshot.child("coins").value.toString()
                user.created_at = snapshot.child("created_at").value.toString()
                onComplete.onCompleteTask(true,user)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}