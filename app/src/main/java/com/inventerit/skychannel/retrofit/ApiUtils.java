package com.inventerit.skychannel.retrofit;

import com.inventerit.skychannel.ApiService.APIService;

public class ApiUtils {

    private ApiUtils() {}

    public static final String BASE_URL = "https://www.googleapis.com";

    public static APIService getAPIService() {

        return RetrofitClient.getClient(BASE_URL).create(APIService.class);
    }

}
