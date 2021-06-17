package com.sidhow.skychannel.ApiService;


import com.sidhow.skychannel.model.Api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface APIService {

    @GET("/youtube/v3/videos")
    Call<Api> getVideoDetails(@Query("part") String part,
                              @Query("id") String video_id,
                              @Query("key") String key);
}
