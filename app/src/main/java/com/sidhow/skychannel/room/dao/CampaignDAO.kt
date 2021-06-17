package com.sidhow.skychannel.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.sidhow.skychannel.model.Campaign

@Dao
interface CampaignDAO {

    @Query("SELECT * FROM campaign WHERE type = :sub")
    fun getCampaignsByType(sub: String): List<Campaign>

    @Query("DELETE FROM campaign")
    fun deleteAllCampaigns()

    @Insert
    fun insertCampaign(campaign: Campaign)
}