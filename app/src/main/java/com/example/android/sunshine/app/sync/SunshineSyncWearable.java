package com.example.android.sunshine.app.sync;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.sunshine.app.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by ashish-novelroots on 8/12/16.
 */

public class SunshineSyncWearable extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {



    GoogleApiClient mGoogleApiClient;

    private static final String COUNT_PATH = "/count";

    private static final String MIN_TEMP_KEY = "com.example.key.min";
    private static final String MAX_TEMP_KEY = "com.example.key.max";
    private static final String WEATHER_KEY = "com.example.key.weather";


    static  int count;
    public SunshineSyncWearable(){

        this(SunshineSyncWearable.class.getSimpleName());
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */

    public SunshineSyncWearable(String name) {
        super();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        Log.d("SunshineWearable", "onStartCommand ");

        mGoogleApiClient.connect();

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();


    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onConnected(Bundle bundle) {

        Log.d("SunshineWearable", "Connected ");
        Context context= getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        final String previousMin=prefs.getString(context.getString(R.string.pref_previous_min_temperature),"");
        final String previousMax= prefs.getString(context.getString(R.string.pref_previous_max_temperature),"");
        final int previousWeather= prefs.getInt(context.getString(R.string.pref_previous_weather_temperature),-1);


        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(COUNT_PATH);


        putDataMapRequest.getDataMap().putString(MIN_TEMP_KEY, previousMin);
        putDataMapRequest.getDataMap().putString(MAX_TEMP_KEY, previousMax);
        putDataMapRequest.getDataMap().putInt(WEATHER_KEY, previousWeather);
        putDataMapRequest.getDataMap().putInt("COUNT_UPDATE",count++);

        PutDataRequest request = putDataMapRequest.asPutDataRequest();

        Log.d("Utility", "Generating DataItem: " + request);
        if (!mGoogleApiClient.isConnected()) {
            return;
        }
        Wearable.DataApi.putDataItem(mGoogleApiClient, request)
                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(DataApi.DataItemResult dataItemResult) {
                        if (!dataItemResult.getStatus().isSuccess()) {
                            Log.e("Utility", "ERROR: failed to putDataItem, status code: "
                                    + dataItemResult.getStatus().getStatusCode());
                        }
                        else{
                            Log.e("Utility","Data put successfuly " +previousMax+" , "+previousMin+" "+previousWeather);
                        }
                    }
                });
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
