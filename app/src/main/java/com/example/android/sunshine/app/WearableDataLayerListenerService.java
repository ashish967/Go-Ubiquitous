package com.example.android.sunshine.app;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.example.android.sunshine.app.sync.SunshineSyncWearable;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.concurrent.TimeUnit;

/*
 This class is for handling wearable device sync request.
*
* */
public class WearableDataLayerListenerService extends WearableListenerService {

    private static final String TAG = "DataLayerService";

    private static final String DATA_RECEIVE = "/update";


    private GoogleApiClient mGoogleApiClient;

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
                Log.e(TAG, "WearableDataLayerListenerService failed to connect to GoogleApiClient, "
                        + "error code: " + connectionResult.getErrorCode());
                return;
            }
        }

        // Loop through the events and send a message back to the node that created the data item.
        for (DataEvent event : dataEvents) {
            Uri uri = event.getDataItem().getUri();
            String path = uri.getPath();
            if (DATA_RECEIVE.equals(path)) {
                // Get the node id of the node that created the data item from the host portion of
                // the uri.

                Intent intent= new Intent(getApplicationContext(),SunshineSyncWearable.class);
                getApplicationContext().startService(intent);
            }
        }
    }



    public static void LOGD(final String tag, String message) {
        if (Log.isLoggable(tag, Log.DEBUG)) {
            Log.d(tag, message);
        }
    }
}