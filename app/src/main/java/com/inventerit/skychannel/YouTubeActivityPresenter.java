package com.inventerit.skychannel;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeRequestInitializer;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.Subscription;
import com.google.api.services.youtube.model.SubscriptionSnippet;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.inventerit.skychannel.interfaces.YouTubeActivityView;

import java.io.IOException;
import java.util.HashMap;


public class YouTubeActivityPresenter {
    private final YouTubeActivityView view;
    private String youtubeKey = "AIzaSyDUqZdrWnKoGlKVF3cMllPqybWi2TO9NIg";// paste your youtube key here
    private Context context;

    public YouTubeActivityPresenter(YouTubeActivityView view, Context context) {
        this.view = view;
        this.context = context;
    }

    public void likeToYouTubeChannel(GoogleAccountCredential credential, String videoId){
        new MakeLikeTask(credential,videoId).execute(); // AsyncTask for like a video
    }

    public void subscribeToYouTubeChannel(GoogleAccountCredential mCredential, String channelId) {

        new MakeRequestTask(mCredential,channelId).execute(); // creating AsyncTask for channel subscribe

    }

    private class MakeLikeTask extends AsyncTask<Object, Object, YouTube.Videos.Rate> {
        private YouTube mService = null;
        private String videoId;

        MakeLikeTask(GoogleAccountCredential credential, String videoId) {
            this.videoId = videoId;
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new YouTube.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName(context.getResources().getString(R.string.app_name))
                    .build();

            getTokenC(credential);
        }

        @Override
        protected YouTube.Videos.Rate doInBackground(Object... objects) {
            YouTube.Videos.Rate response = null;

            HashMap<String, String> parameters = new HashMap<>();
            parameters.put("part", "snippet");

            // if you could not able to import the classes then check the dependency in build.gradle
            Video video = new Video();
            VideoSnippet snippet = new VideoSnippet();
//            ResourceId resourceId = new ResourceId();
//            resourceId.set("id", videoId);
//            resourceId.set("rate", "like");

            try {
                response = mService.videos().rate(videoId,"like").setKey("");
                response.setKey(youtubeKey).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

//            snippet.set("id",videoId);
//            snippet.set("rate","like");
//            snippet.setResourceId(resourceId);
//            video.setSnippet(snippet);

//            YouTube.Subscriptions.Insert subscriptionsInsertRequest = null;
//            try {
//                subscriptionsInsertRequest = mService.subscriptions().insert(parameters.get("part").toString(), subscription);
//                response = subscriptionsInsertRequest.execute();
//            } catch (IOException e) {
//               /* if you got error message below
//                "message" : "Access Not Configured. YouTube Data API has not been used in project YOUR_PROJECT_ID before or it is disabled.
//                then goto following link and enable the access
//                https://console.developers.google.com/apis/api/youtube.googleapis.com/overview?project=YOUR_PROJECT_ID*/
//
//                e.printStackTrace();
//            }

            return response;
        }

        @Override
        protected void onPostExecute(YouTube.Videos.Rate rate) {
            super.onPostExecute(rate);
            if(rate!=null){
                view.onSubscribetionSuccess("Liked");
            }else{
                view.onSubscribetionFail();
            }
        }
    }

    private class MakeRequestTask  extends AsyncTask<Object, Object, Subscription> {
        private YouTube mService = null;
        private String channelId;

        MakeRequestTask(GoogleAccountCredential credential, String channelId) {
            this.channelId = channelId;
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new YouTube.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName(context.getResources().getString(R.string.app_name))
                    .setYouTubeRequestInitializer(new YouTubeRequestInitializer(youtubeKey))
                    .build();

            getTokenC(credential);
        }

        @Override
        protected Subscription doInBackground(Object... params) {
            // code for channel subscribe
            Subscription response = null;

            HashMap<String, String> parameters = new HashMap<>();
            parameters.put("part", "snippet");

            // if you could not able to import the classes then check the dependency in build.gradle
            Subscription subscription = new Subscription();
            SubscriptionSnippet snippet = new SubscriptionSnippet();
            ResourceId resourceId = new ResourceId();
            resourceId.set("channelId", channelId);
            resourceId.set("kind", "youtube#channel");

            snippet.setResourceId(resourceId);
            subscription.setSnippet(snippet);

            YouTube.Subscriptions.Insert subscriptionsInsertRequest = null;
            try {
                subscriptionsInsertRequest = mService.subscriptions().insert(parameters.get("part").toString(), subscription);
                response = subscriptionsInsertRequest.execute();
            } catch (IOException e) {
               /* if you got error message below
                "message" : "Access Not Configured. YouTube Data API has not been used in project YOUR_PROJECT_ID before or it is disabled.
                then goto following link and enable the access
                https://console.developers.google.com/apis/api/youtube.googleapis.com/overview?project=YOUR_PROJECT_ID*/

                e.printStackTrace();
            }

            return response;
        }

        @Override
        protected void onPostExecute(Subscription subscription) {
            super.onPostExecute(subscription);
            if (subscription != null) {
                view.onSubscribetionSuccess(subscription.getSnippet().getTitle());
            } else {
                view.onSubscribetionFail();
            }
        }
    }

    private void getTokenC(final GoogleAccountCredential credential) {

        new Thread(){
            @Override
            public void run() {
                try {
                    Log.i("TOKEN11","Token: "+credential.getToken().toString());
                }catch (Exception e){
                    Log.e("TOKEN11",e.getMessage().toString());
                    e.printStackTrace();
                }
            }
        }.start();

    }
}
