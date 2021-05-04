package com.inventerit.skychannel

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.inventerit.skychannel.ApiService.APIService
import com.inventerit.skychannel.interfaces.OnCampaignAdded
import com.inventerit.skychannel.interfaces.OnComplete
import com.inventerit.skychannel.interfaces.OnGetCampaign
import com.inventerit.skychannel.interfaces.OnVideoDetails
import com.inventerit.skychannel.constant.Constants
import com.inventerit.skychannel.model.Api
import com.inventerit.skychannel.model.Campaign
import com.inventerit.skychannel.model.User
import com.inventerit.skychannel.retrofit.ApiUtils
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
        mUser = mAuth.currentUser
    }

    fun getVideoDetails(video_id: String, onVideoDetails: OnVideoDetails<Api>){
        apiService = ApiUtils.getAPIService()
        apiService?.getVideoDetails("snippet", video_id, Constants.DATA_API_KEY)
            ?.enqueue(object : Callback<Api> {
                override fun onResponse(call: Call<Api>, response: Response<Api>) {
                    if (response.isSuccessful) {
                        if (response.code() == 200) {
                            onVideoDetails.onVideoDetails(true, response.body())
                        } else {
                            onVideoDetails.onVideoDetails(false, response.body())
                        }
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
                onComplete.onCompleteTask(true, user)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    fun saveCampaign(campaign: Campaign, onCompleteAdded: OnCampaignAdded){
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

        val campaignList: List<Campaign> = listOf(campaign)


        database.child(Constants.campaigns).child(id).setValue(map).addOnCompleteListener {
            if(it.isSuccessful){
                saveUserCampaign(campaign, onCompleteAdded)
            }else{
                onCompleteAdded.onCampaignAdded(false)
            }
        }
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
                            if (videoList.contains(snapshot1.child(Constants.id).value.toString()) &&
                                    snapshot1.child(Constants.status).value!! == "0") {
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

    fun getAllCampaigns(onGetCampaign: OnGetCampaign<List<Campaign>>){
        var campaignList: MutableList<Campaign> = ArrayList()
        database.child(Constants.campaigns).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(!campaignList.isNullOrEmpty()){
                    campaignList.clear()
                }
                for(snapshot1 in snapshot.children){
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
                    campaignList.add(campaign)
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
        });
    }
}