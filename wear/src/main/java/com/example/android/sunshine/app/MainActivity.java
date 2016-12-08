package com.example.android.sunshine.app;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.wearable.view.WatchViewStub;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.example.android.sunshine.app.DataLayerListenerService.COUNT_PATH;
import static com.example.android.sunshine.app.DataLayerListenerService.LOGD;
import static com.example.android.sunshine.app.Utility.MAX_TEMP_KEY;
import static com.example.android.sunshine.app.Utility.MIN_TEMP_KEY;
import static com.example.android.sunshine.app.Utility.WEATHER_KEY;

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, DataApi.DataListener, GoogleApiClient.OnConnectionFailedListener, CapabilityApi.CapabilityListener, MessageApi.MessageListener {

    private TextView mTvTemperature,mTvTime,mTvDay;
    ImageView mWeatherIcon;

    public static final String TAG= MainActivity.class.getSimpleName();

    private static final String COUNT_KEY = "com.example.key.count";
    private static final String START_ACTIVITY_PATH = "/abc";



    public static boolean IS_RUNNING;

    private GoogleApiClient mGoogleApiClient;
    private int count = 0;

    BroadcastReceiver receiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            updateCount();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTvTemperature = (TextView) stub.findViewById(R.id.tv_temp_high_low);
                mTvTime= (TextView) stub.findViewById(R.id.tv_time);
                mTvDay= (TextView) stub.findViewById(R.id.tv_day);
                mWeatherIcon = (ImageView) stub.findViewById(R.id.iv_weather_icon);
                updateCount();


            }
        });


        IS_RUNNING= true;


    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onDestroy() {
        IS_RUNNING=false;
        super.onDestroy();

    }

    @Override
    public void onConnectionSuspended(int i) {

        Log.d(TAG,"onConnectionSuspended");

    }

    @Override
    protected void onPause() {
        if ((mGoogleApiClient != null) && mGoogleApiClient.isConnected()) {
            Wearable.DataApi.removeListener(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }

        super.onPause();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        LOGD(TAG, "onConnected(): Successfully connected to Google API client");
        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {

        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo(COUNT_PATH) == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();

                    String min= dataMap.getString(MIN_TEMP_KEY);
                    String max= dataMap.getString(MAX_TEMP_KEY);
                    int weather = dataMap.getInt(WEATHER_KEY);

                    Log.e(TAG,"min "+min+" max "+max+ "  weather "+weather );
                    Utility.saveInformation(this,weather,min,max);

                    updateCount();
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }
    }

    // Our method to update the count
    private void updateCount() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        String min=prefs.getString(Utility.MIN_TEMP_KEY,"-");
        String max= prefs.getString(Utility.MAX_TEMP_KEY,"-");
        int weatherId= prefs.getInt(Utility.WEATHER_KEY,201);
        


        SimpleDateFormat formatTimeHour= new SimpleDateFormat("HH:");
        SimpleDateFormat formatTimeMin= new SimpleDateFormat("mm");

        SimpleDateFormat formatDay=new SimpleDateFormat("EEE, MMM d yyyy");


        String timeH =formatTimeHour.format(new Date(System.currentTimeMillis()));
        String timeM= formatTimeMin.format(new Date(System.currentTimeMillis()));

        SpannableString timeText= new SpannableString(timeH+timeM);
        timeText.setSpan(new StyleSpan(Typeface.BOLD),0,timeH.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mTvTime.setText(timeText);


        mTvDay.setText(formatDay.format(new Date(System.currentTimeMillis())));


        SpannableString tempText= new SpannableString(max+" "+min);
        tempText.setSpan(new StyleSpan(Typeface.BOLD),0,max.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);


        mTvTemperature.setText(tempText);


        mWeatherIcon.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));


    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG,"onconnectFailed");
    }
    // Create a data map and put data in it

    private void increaseCounter() {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(START_ACTIVITY_PATH);
        putDataMapReq.getDataMap().putInt(COUNT_KEY, (int) System.currentTimeMillis());
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
        pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(final DataApi.DataItemResult result) {
                if(result.getStatus().isSuccess()) {
                    Log.d(TAG, "Data item set: " + result.getDataItem().getUri());
                }
            }
        });


    }

    @Override
    public void onCapabilityChanged(CapabilityInfo capabilityInfo) {
        LOGD(TAG, "onCapabilityChanged: " + capabilityInfo);

    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

    }
}
