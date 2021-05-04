package com.inventerit.skychannel.ApiService;


import com.inventerit.skychannel.model.Api;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface APIService {

    @GET("/youtube/v3/videos")
    Call<Api> getVideoDetails(@Query("part") String part,
                              @Query("id") String video_id,
                              @Query("key") String key);
}
