
package com.spreadtrum.sgps;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import android.util.Log;

public class SgpsService extends Service {

    private static final String TAG = "EM/SGPS_Service";

    @Override
    public void onCreate() {
        Log.v(TAG, "SGPSService onCreate");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "SGPSService onDestroy");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "onStartCommand " + intent + " flags " + flags);

        if (intent == null) {
            Log.w(TAG, "error: intent == null" );
            return START_STICKY;
        }

        return START_STICKY;
    }

}
