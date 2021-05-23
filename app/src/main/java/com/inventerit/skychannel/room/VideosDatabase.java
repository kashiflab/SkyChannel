package com.inventerit.skychannel.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.inventerit.skychannel.room.dao.VideosDao;
import com.inventerit.skychannel.room.model.Videos;


//Room database to define version and db name
@Database(entities = {Videos.class}, version = 1)
public abstract class VideosDatabase extends RoomDatabase {

    private static VideosDatabase instance;

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