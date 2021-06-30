package com.sidhow.skychannel.repo

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.sidhow.skychannel.ApiService.APIService
import com.sidhow.skychannel.constant.Constants
import com.sidhow.skychannel.constant.PrefKeys
import com.sidhow.skychannel.interfaces.*
import com.sidhow.skychannel.model.Api
import com.sidhow.skychannel.model.Campaign
import com.sidhow.skychannel.model.SkuProducts
import com.sidhow.skychannel.model.User
import com.sidhow.skychannel.retrofit.ApiUtils
import com.tramsun.libs.prefcompat.Pref
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

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

    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var mUser: FirebaseUser
    private var database = Firebase.database.reference
    private var apiService: APIService? = null

    init {
        mUser = mAuth?.currentUser
    }

    fun fetchSkuDetails(onFetchSkuDetails: OnFetchSkuDetails){
        val list: MutableList<SkuProducts> = ArrayList()
        database.child(Constants.credits).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                list.clear()
                for(snapshot1 in snapshot.children){
                    list.add(SkuProducts(snapshot1.key.toString(),snapshot1.child(Constants.price).value.toString(),
                    snapshot1.child(Constants.coins).value.toString(),snapshot1.child(Constants.sku).value.toString(),
                    snapshot1.child(Constants.title).value.toString(),snapshot1.child(Constants.desc).value.toString(),
                    snapshot1.child("type").value.toString()))
                }

                if(list.isEmpty()){
                    onFetchSkuDetails.onFetchSkuDetails(false, emptyList())
                }else{
                    onFetchSkuDetails.onFetchSkuDetails(true,list)
                }

            }

            override fun onCancelled(error: DatabaseError) {
                onFetchSkuDetails.onFetchSkuDetails(false, emptyList())
            }
        })
    }

    fun updateCampaignStatus(campaignId: String, campaignNumber: String, onCampaignStatus: OnCampaignStatus){
        val map: HashMap<String, Any> = HashMap()

        map[Constants.current_number] = campaignNumber

        database.child(Constants.campaigns).child(campaignId).updateChildren(map).addOnCompleteListener {
            if(it.isSuccessful){
                onCampaignStatus.onCampaignStatus(true)
            }else{
                onCampaignStatus.onCampaignStatus(false)
            }
        }
    }

    fun updateCoins(coins: String, onLike: LikeListener){
        onLike.onLikeStarted()
        val map: HashMap<String,Any> = HashMap()

        map[Constants.coins] = coins

        database.child(Constants.users).child(mUser.uid).updateChildren(map).addOnCompleteListener {
            if(it.isSuccessful){
                Pref.putString(PrefKeys.coins,coins)
                onLike.onLikedSuccess()
            }else{
                onLike.onLikedFailed()
            }
        }
    }

    fun getVideoDetails(video_id: String, onVideoDetails: OnVideoDetails<Api>){
        apiService = ApiUtils.getAPIService()
        apiService?.getVideoDetails("snippet,contentDetails", video_id, Constants.CAMPAIGN_KEY)
            ?.enqueue(object : Callback<Api> {
                override fun onResponse(call: Call<Api>, response: Response<Api>) {
                    if (response.isSuccessful) {
                        if (response.code() == 200) {
                            onVideoDetails.onVideoDetails(true, response.body())
                        } else {
                            onVideoDetails.onVideoDetails(false, response.body())
                        }
                    }else{
                        onVideoDetails.onVideoDetails(false,response.body())
                    }
                }

                override fun onFailure(call: Call<Api>, t: Throwable) {
                    onVideoDetails.onVideoDetails(false, null)
                }

            })
    }

    fun getUserData(onComplete: OnComplete<User>){
        database.child(Constants.users).child(mUser.uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = User()
                user.id = snapshot.child(Constants.id).value.toString()
                user.username = snapshot.child(Constants.username).value.toString()
                user.email = snapshot.child(Constants.email).value.toString()
                user.photoUrl = snapshot.child(Constants.photoUrl).value.toString()
                user.coins = snapshot.child(Constants.coins).value.toString()
                user.created_at = snapshot.child(Constants.created_at).value.toString()
                user.is_vip = snapshot.child(Constants.is_vip).value.toString()

                Pref.putString(PrefKeys.coins,user.coins)
                Pref.putString(PrefKeys.isVIP,user.is_vip)
                onComplete.onCompleteTask(true, user)
            }

            override fun onCancelled(error: DatabaseError) {
                onComplete.onCompleteTask(false, User())
            }

        })
    }

    fun saveCampaign(campaign: Campaign, previousCoins: String, onCompleteAdded: OnCampaignAdded){
        val map: HashMap<String, Any> = HashMap()

        val id = UUID.randomUUID().toString()

        campaign.id = id
        map[Constants.id] = id
        map[Constants.user_id] = campaign.user_id
        map[Constants.created_at] = campaign.created_at
        map[Constants.current_number] = campaign.current_number
        map[Constants.total_number] = campaign.total_number
        map[Constants.total_coins] = campaign.total_coins
        map[Constants.total_time] = campaign.total_time
        map[Constants.status] = campaign.status
        map[Constants.type] = campaign.type
        map[Constants.video_id] = campaign.video_id
        map[Constants.video_title] = campaign.video_title
        map[Constants.channel_id] = campaign.channel_id
        map[Constants.coins] = campaign.coins
        map[Constants.is_paused] = campaign.paused
        map[Constants.channel_title] = campaign.channelTitle
        map[Constants.channel_image] = campaign.channelImage

        val campaignList: List<Campaign> = listOf(campaign)


        database.child(Constants.campaigns).child(id).setValue(map).addOnCompleteListener {
            if(it.isSuccessful){
                saveUserCampaign(campaign, onCompleteAdded)
                deductCoins(campaign.user_id,previousCoins,campaign.total_coins);
            }else{
                onCompleteAdded.onCampaignAdded(false)
            }
        }
    }

    private fun deductCoins(userId: String,previousCoins: String, campaignCoins: String) {
        val totalCoins = previousCoins.toInt() - campaignCoins.toInt()
        Pref.putString(PrefKeys.coins,totalCoins.toString())
        database.child(Constants.users).child(userId).child(Constants.coins).setValue(totalCoins.toString())
    }

    private fun saveUserCampaign(campaign: Campaign, onCompleteAdded: OnCampaignAdded) {
        val map: HashMap<String, Any> = HashMap()

        map[Constants.id] = campaign.id
        map[Constants.video_id] = campaign.video_id

        database.child(Constants.users).child(mUser.uid).child(Constants.campaigns)
            .child(campaign.id).setValue(map).addOnCompleteListener {
                if(it.isSuccessful){
                    onCompleteAdded.onCampaignAdded(true)
                }else{
                    onCompleteAdded.onCampaignAdded(false)
                }
            }

    }

    fun getUserCampaigns(onGetCampaign: OnGetCampaign<List<Campaign>>){
        val videoList: MutableList<String> = ArrayList()
        database.child(Constants.users).child(mUser.uid).child(Constants.campaigns)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(videoList.isNullOrEmpty()){
                        videoList.clear()
                    }
                    for (snapshot1 in snapshot.children){
                        videoList.add(snapshot1.child(Constants.id).value.toString())
                    }

                    if (videoList.isNotEmpty()) {
                        getCampaignById(videoList, onGetCampaign)
                    }else{
                        val campaign: MutableList<Campaign> = ArrayList()
                        onGetCampaign.onGetCampaign(false,campaign)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    val campaign: MutableList<Campaign> = ArrayList()
                    onGetCampaign.onGetCampaign(false,campaign)
                }
            })

    }

    private fun getCampaignById(videoList: MutableList<String>, onGetCampaign: OnGetCampaign<List<Campaign>>) {
        val campaignList: MutableList<Campaign> = ArrayList()
        database.child(Constants.campaigns)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(!campaignList.isNullOrEmpty()){
                            campaignList.clear()
                        }
                        for (snapshot1 in snapshot.children) {
                            if (videoList.contains(snapshot1.child(Constants.id).value.toString())) {
                                val campaign = Campaign()
                                campaign.id = snapshot1.child(Constants.id).value.toString()
                                campaign.video_id = snapshot1.child(Constants.video_id).value.toString()
                                campaign.video_title = snapshot1.child(Constants.video_title).value.toString()
                                campaign.user_id = snapshot1.child(Constants.user_id).value.toString()
                                campaign.current_number = snapshot1.child(Constants.current_number).value.toString()
                                campaign.total_number = snapshot1.child(Constants.total_number).value.toString()
                                campaign.total_coins = snapshot1.child(Constants.total_coins).value.toString()
                                campaign.total_time = snapshot1.child(Constants.total_time).value.toString()
                                campaign.status = snapshot1.child(Constants.status).value.toString()
                                campaign.created_at = snapshot1.child(Constants.created_at).value.toString()
                                campaign.type = snapshot1.child(Constants.type).value.toString()
                                campaign.channel_id = snapshot1.child(Constants.channel_id).value.toString()
                                campaign.coins = snapshot1.child(Constants.coins).value.toString()
                                campaign.paused = snapshot1.child(Constants.is_paused).value.toString()
                                campaign.channelTitle = snapshot1.child(Constants.channel_title).value.toString()
                                campaign.channelImage = snapshot1.child(Constants.channel_image).value.toString()
                                campaignList.add(campaign)
                            }
                        }
                        if(campaignList.isNotEmpty()) {
                            onGetCampaign.onGetCampaign(true, campaignList)
                        }else{
                            onGetCampaign.onGetCampaign(false,campaignList)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        onGetCampaign.onGetCampaign(false, campaignList)
                    }
                })
    }

    fun getViewsCampaigns(onGetCampaign: OnGetCampaign<List<Campaign>>){
        val campaignList: MutableList<Campaign> = ArrayList()

        database.child(Constants.campaigns).get().addOnCompleteListener {
            if(it.isSuccessful){
                if(!campaignList.isNullOrEmpty()){
                    campaignList.clear()
                }
                for(snapshot1 in it.result?.children!!){
                    if(snapshot1.child(Constants.user_id).value.toString() != mUser.uid &&
                            snapshot1.child(Constants.type).value.toString().toInt() == Constants.VIEW_TYPE &&
                        snapshot1.child(Constants.status).value!! == "0" &&
                        snapshot1.child(Constants.is_paused).value!! == "0") {
                        val campaign = Campaign()
                        campaign.id = snapshot1.child(Constants.id).value.toString()
                        campaign.video_id = snapshot1.child(Constants.video_id).value.toString()
                        campaign.video_title =
                                snapshot1.child(Constants.video_title).value.toString()
                        campaign.user_id = snapshot1.child(Constants.user_id).value.toString()
                        campaign.current_number =
                                snapshot1.child(Constants.current_number).value.toString()
                        campaign.total_number =
                                snapshot1.child(Constants.total_number).value.toString()
                        campaign.total_coins =
                                snapshot1.child(Constants.total_coins).value.toString()
                        campaign.total_time = snapshot1.child(Constants.total_time).value.toString()
                        campaign.status = snapshot1.child(Constants.status).value.toString()
                        campaign.created_at = snapshot1.child(Constants.created_at).value.toString()
                        campaign.type = snapshot1.child(Constants.type).value.toString()
                        campaign.channel_id = snapshot1.child(Constants.channel_id).value.toString()
                        campaign.coins = snapshot1.child(Constants.coins).value.toString()
                        campaign.paused = snapshot1.child(Constants.is_paused).value.toString()

                        campaign.channelTitle = snapshot1.child(Constants.channel_title).value.toString()
                        campaign.channelImage = snapshot1.child(Constants.channel_image).value.toString()
                        campaignList.add(campaign)
                    }
                }
                if(campaignList.isNotEmpty()) {
                    onGetCampaign.onGetCampaign(true, campaignList)
                }else{
                    onGetCampaign.onGetCampaign(false, emptyList())
                }
            }else{
                onGetCampaign.onGetCampaign(false, emptyList())
            }
        }

    }

    fun getLikesCampaigns(onGetCampaign: OnGetCampaign<List<Campaign>>){
        val campaignList: MutableList<Campaign> = ArrayList()

        database.child(Constants.campaigns).get().addOnCompleteListener {
            if(it.isSuccessful){
                if(!campaignList.isNullOrEmpty()){
                    campaignList.clear()
                }
                for(snapshot1 in it.result?.children!!){
                    if(snapshot1.child(Constants.user_id).value.toString() != mUser.uid &&
                            snapshot1.child(Constants.type).value.toString().toInt() == Constants.LIKE_TYPE &&
                        snapshot1.child(Constants.status).value!! == "0" &&
                        snapshot1.child(Constants.is_paused).value!! == "0") {
                        val campaign = Campaign()
                        campaign.id = snapshot1.child(Constants.id).value.toString()
                        campaign.video_id = snapshot1.child(Constants.video_id).value.toString()
                        campaign.video_title =
                                snapshot1.child(Constants.video_title).value.toString()
                        campaign.user_id = snapshot1.child(Constants.user_id).value.toString()
                        campaign.current_number =
                                snapshot1.child(Constants.current_number).value.toString()
                        campaign.total_number =
                                snapshot1.child(Constants.total_number).value.toString()
                        campaign.total_coins =
                                snapshot1.child(Constants.total_coins).value.toString()
                        campaign.total_time = snapshot1.child(Constants.total_time).value.toString()
                        campaign.status = snapshot1.child(Constants.status).value.toString()
                        campaign.created_at = snapshot1.child(Constants.created_at).value.toString()
                        campaign.type = snapshot1.child(Constants.type).value.toString()
                        campaign.channel_id = snapshot1.child(Constants.channel_id).value.toString()
                        campaign.coins = snapshot1.child(Constants.coins).value.toString()
                        campaign.paused = snapshot1.child(Constants.is_paused).value.toString()
                        campaign.channelTitle = snapshot1.child(Constants.channel_title).value.toString()
                        campaign.channelImage = snapshot1.child(Constants.channel_image).value.toString()
                        campaignList.add(campaign)
                    }
                }
                if(campaignList.isNotEmpty()) {
                    onGetCampaign.onGetCampaign(true, campaignList)
                }else{
                    onGetCampaign.onGetCampaign(false, campaignList)
                }
            }else{
                onGetCampaign.onGetCampaign(false, campaignList)
            }
        }

    }

    fun getSubsCampaigns(onGetCampaign: OnGetCampaign<List<Campaign>>){
        val campaignList: MutableList<Campaign> = ArrayList()

        database.child(Constants.campaigns).get().addOnCompleteListener {
            if(it.isSuccessful){
                if(!campaignList.isNullOrEmpty()){
                    campaignList.clear()
                }
                for(snapshot1 in it.result?.children!!){
                    if(snapshot1.child(Constants.user_id).value.toString() != mUser.uid &&
                            snapshot1.child(Constants.type).value.toString().toInt() == Constants.SUBSCRIBE_TYPE &&
                        snapshot1.child(Constants.status).value!! == "0" &&
                        snapshot1.child(Constants.is_paused).value!! == "0") {
                        val campaign = Campaign()
                        campaign.id = snapshot1.child(Constants.id).value.toString()
                        campaign.video_id = snapshot1.child(Constants.video_id).value.toString()
                        campaign.video_title =
                                snapshot1.child(Constants.video_title).value.toString()
                        campaign.user_id = snapshot1.child(Constants.user_id).value.toString()
                        campaign.current_number =
                                snapshot1.child(Constants.current_number).value.toString()
                        campaign.total_number =
                                snapshot1.child(Constants.total_number).value.toString()
                        campaign.total_coins =
                                snapshot1.child(Constants.total_coins).value.toString()
                        campaign.total_time = snapshot1.child(Constants.total_time).value.toString()
                        campaign.status = snapshot1.child(Constants.status).value.toString()
                        campaign.created_at = snapshot1.child(Constants.created_at).value.toString()
                        campaign.type = snapshot1.child(Constants.type).value.toString()
                        campaign.channel_id = snapshot1.child(Constants.channel_id).value.toString()
                        campaign.coins = snapshot1.child(Constants.coins).value.toString()
                        campaign.paused = snapshot1.child(Constants.is_paused).value.toString()
                        campaign.channelTitle = snapshot1.child(Constants.channel_title).value.toString()
                        campaign.channelImage = snapshot1.child(Constants.channel_image).value.toString()
                        campaignList.add(campaign)
                    }
                }
                if(campaignList.isNotEmpty()) {
                    onGetCampaign.onGetCampaign(true, campaignList)
                }else{
                    onGetCampaign.onGetCampaign(false, campaignList)
                }
            }else{
                onGetCampaign.onGetCampaign(false, campaignList)
            }
        }

    }
    fun endAllCompletedCampaigns(onCampaignCompleted: OnCampaignCompleted){
        database.child(Constants.campaigns).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(snapshot1 in snapshot.children){
                    if(snapshot1.child(Constants.current_number).value.toString() >=
                        snapshot1.child(Constants.total_number).value.toString()){
                        endCampaigns(snapshot1.child(Constants.id).value.toString(), onCampaignCompleted)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onCampaignCompleted.onCampaignCompleted(false,error.message.toString())
            }
        })
    }

    fun getAllCampaigns(onGetCampaign: OnGetCampaign<List<Campaign>>){
        val campaignList: MutableList<Campaign> = ArrayList()
        database.child(Constants.campaigns).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(!campaignList.isNullOrEmpty()){
                    campaignList.clear()
                }
                for(snapshot1 in snapshot.children){

                    if(snapshot1.child(Constants.user_id).value.toString() != mUser.uid) {
                        val campaign = Campaign()
                        campaign.id = snapshot1.child(Constants.id).value.toString()
                        campaign.video_id = snapshot1.child(Constants.video_id).value.toString()
                        campaign.video_title =
                            snapshot1.child(Constants.video_title).value.toString()
                        campaign.user_id = snapshot1.child(Constants.user_id).value.toString()
                        campaign.current_number =
                            snapshot1.child(Constants.current_number).value.toString()
                        campaign.total_number =
                            snapshot1.child(Constants.total_number).value.toString()
                        campaign.total_coins =
                            snapshot1.child(Constants.total_coins).value.toString()
                        campaign.total_time = snapshot1.child(Constants.total_time).value.toString()
                        campaign.status = snapshot1.child(Constants.status).value.toString()
                        campaign.created_at = snapshot1.child(Constants.created_at).value.toString()
                        campaign.type = snapshot1.child(Constants.type).value.toString()
                        campaign.channel_id = snapshot1.child(Constants.channel_id).value.toString()
                        campaign.coins = snapshot1.child(Constants.coins).value.toString()
                        campaign.paused = snapshot1.child(Constants.is_paused).value.toString()
                        campaign.channelTitle = snapshot1.child(Constants.channel_title).value.toString()
                        campaign.channelImage = snapshot1.child(Constants.channel_image).value.toString()
                        campaignList.add(campaign)
                    }
                }
                if(campaignList.isNotEmpty()) {
                    onGetCampaign.onGetCampaign(true, campaignList)
                }else{
                    onGetCampaign.onGetCampaign(false, campaignList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onGetCampaign.onGetCampaign(false, campaignList)
            }
        })
    }

    private fun endCampaigns(id: String, onCampaignCompleted: OnCampaignCompleted){
        database.child(Constants.campaigns).child(id).child(Constants.status).setValue("1")
            .addOnCompleteListener {
                if(it.isSuccessful){
                    onCampaignCompleted.onCampaignCompleted(true,"Success");
                }else{
                    onCampaignCompleted.onCampaignCompleted(false,it.exception.toString())
                }
            }
    }

    fun addUser(campaignId: String, uid: String) {
        val map: HashMap<String,Any> = HashMap()
        map[Constants.user_id] = uid
        database.child(Constants.campaigns).child(campaignId).child(Constants.users).child(uid)
            .setValue(map)
    }

    fun getCampaignUsers(onGetUsers: OnGetUsers,campaignId: String){
        database.child(Constants.campaigns).child(campaignId).child(Constants.users)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(userList.isNotEmpty()){
                        userList.clear()
                    }
                    for(snapshot1 in snapshot.children){
                        Log.i("CampaignUser",snapshot.childrenCount.toString())
                        getUser(onGetUsers,snapshot1.child(Constants.user_id).value.toString())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    onGetUsers.onGetUsers(false, emptyList())
                }
            })
    }

    var userList: MutableList<User> = ArrayList()
    private fun getUser(onGetUsers: OnGetUsers, id: String) {
        database.child(Constants.users).child(id)
            .addValueEventListener(object :ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    onGetUsers.onGetUsers(false, emptyList())
                }

                override fun onDataChange(snapshot: DataSnapshot) {

                    Log.i("CampaignUser",snapshot.childrenCount.toString())
                    val user = User()
                    user.id = snapshot.child(Constants.id).value.toString()
                    user.username = snapshot.child(Constants.username).value.toString()
                    user.email = snapshot.child(Constants.email).value.toString()
                    user.photoUrl = snapshot.child(Constants.photoUrl).value.toString()
                    user.coins = snapshot.child(Constants.coins).value.toString()
                    user.created_at = snapshot.child(Constants.created_at).value.toString()
                    user.is_vip = snapshot.child(Constants.is_vip).value.toString()
                    userList.add(user)

                    if(userList.isEmpty()){
                        onGetUsers.onGetUsers(false,userList)
                    }else{
                        onGetUsers.onGetUsers(true,userList)
                    }
                }
            })
    }
}