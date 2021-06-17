package com.sidhow.skychannel.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.sidhow.skychannel.model.Campaign;
import com.sidhow.skychannel.room.dao.CampaignDAO;
import com.sidhow.skychannel.room.dao.VideosDao;
import com.sidhow.skychannel.room.model.Videos;


//Room database to define version and db name
@Database(entities = {Videos.class, Campaign.class}, version = 2)
public abstract class VideosDatabase extends RoomDatabase {

    private static VideosDatabase instance;

    public abstract CampaignDAO campaignDAO();
    public abstract VideosDao videosDao();

    public static synchronized VideosDatabase getInstance(Context context){
        if(instance ==null){
            //initialize ProductDatabase instance
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    VideosDatabase.class,"videos_db")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

}