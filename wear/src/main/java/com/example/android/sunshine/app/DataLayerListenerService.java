package com.example.android.sunshine.app;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.concurrent.TimeUnit;

import static com.example.android.sunshine.app.Utility.MAX_TEMP_KEY;
import static com.example.android.sunshine.app.Utility.MIN_TEMP_KEY;
import static com.example.android.sunshine.app.Utility.WEATHER_KEY;

public class DataLayerListenerService extends WearableListenerService {

    private static final String TAG = "DataLayerService";

    public static final String COUNT_PATH = "/count";
    public static final String NOTIFY_WEATHER_CHANGE ="notify_weather_change" ;
    GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {

        if (!mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting()) {
            ConnectionResult connectionResult = mGoogleApiClient
                    .blockingConnect(30, TimeUnit.SECONDS);
            if (!connectionResult.isSuccess()) {
                Log.e(TAG, "DataLayerListenerService failed to connect to GoogleApiClient, "
                        + "error code: " + connectionResult.getErrorCode());
                return;
            }
        }

        // Loop through the events and send a message back to the node that created the data item.
        for (DataEvent event : dataEvents) {
            Uri uri = event.getDataItem().getUri();
            String path = uri.getPath();
            if (COUNT_PATH.equals(path)) {
                // Get the node id of the node that created the data item from the host portion of
                // the uri.
                String nodeId = uri.getHost();
                // Set the data of the message to be the bytes of the Uri.
                byte[] payload = uri.toString().getBytes();

                // Send the rpc
                DataMap dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();

                String min= dataMap.getString(MIN_TEMP_KEY);
                String max= dataMap.getString(MAX_TEMP_KEY);
                int weather = dataMap.getInt(WEATHER_KEY);

                Log.e(TAG,"min "+min+" max "+max+ "  weather "+weather );
                Utility.saveInformation(this,weather,min,max);

                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(NOTIFY_WEATHER_CHANGE));
            }
        }
    }

    public static void LOGD(final String tag, String message) {
        if (Log.isLoggable(tag, Log.DEBUG)) {
            Log.d(tag, message);
        }
    }
}