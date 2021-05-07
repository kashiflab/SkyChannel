package com.inventerit.skychannel.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface VideosDao {

    @Query("SELECT * FROM videos WHERE type = :sub")
    List<Videos> getAllSubscriptions(String sub);

    @Query("SELECT * FROM videos WHERE type = :sub")
    List<Videos> getAllLikes(String sub);

    @Query("SELECT * FROM videos WHERE type = :sub")
    List<Videos> getAllViews(String sub);

    @Insert
    void insert(Videos videos);
}
