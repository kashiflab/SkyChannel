package com.sidhow.skychannel.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.sidhow.skychannel.room.model.Videos;

import java.util.List;

@Dao
public interface VideosDao {

    @Query("SELECT * FROM videos WHERE type = :sub AND channelId = :channelId")
    List<Videos> getAllSubscriptions(String sub, String channelId);

    @Query("SELECT * FROM videos WHERE type = :sub AND videoId = :videoId")
    List<Videos> getAllLikes(String sub, String videoId);

    @Query("SELECT * FROM videos WHERE type = :sub AND videoId = :videoId")
    List<Videos> getAllViews(String sub, String videoId);

    @Query("SELECT * FROM videos WHERE type = :sub")
    List<Videos> getSubs(String sub);

    @Insert
    void insert(Videos videos);
}
