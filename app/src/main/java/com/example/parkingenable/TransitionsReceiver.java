package com.example.parkingenable;

import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionResult;
import com.google.android.gms.location.DetectedActivity;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

/**
 * Handles intents from from the Transitions API.
 */
public class TransitionsReceiver extends BroadcastReceiver {

    protected static final String TAG = "DetectedActivitiesIS";
    public static final String PREFS_NAME = "MyPrefsFile";
    public static final String USER_ID = "userID";
    public static final String SIN_LOGIN = "sinLogin";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.w(TAG, "actividad recogida");
        //DetectedActivitiesIntentService.enqueueWork(context,intent);
        if (ActivityTransitionResult.hasResult(intent)) {
            ActivityTransitionResult result = ActivityTransitionResult.extractResult(intent);
            if (result != null) {
                for (ActivityTransitionEvent event : result.getTransitionEvents()) {
                    // chronological sequence of events....
                    String theActivity = toActivityString(event.getActivityType());
                    String transType = toTransitionType(event.getTransitionType());
                    if(event.getActivityType() == DetectedActivity.STILL && event.getTransitionType() == ActivityTransition.ACTIVITY_TRANSITION_EXIT){
                        startTransmittingBeacons(context);
                    }

                }
            }
        }
    }

    private static String toActivityString(int activity) {
        switch (activity) {
            case DetectedActivity.STILL:
                return "STILL";
            case DetectedActivity.WALKING:
                return "WALKING";
            case DetectedActivity.IN_VEHICLE:
                return "IN_VEHICLE";
            default:
                return "UNKNOWN";
        }
    }

    private static String toTransitionType(int transitionType) {
        switch (transitionType) {
            case ActivityTransition.ACTIVITY_TRANSITION_ENTER:
                return "ENTER";
            case ActivityTransition.ACTIVITY_TRANSITION_EXIT:
                return "EXIT";
            default:
                return "UNKNOWN";
        }
    }

    private void startTransmittingBeacons(Context context){
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        String userID = settings.getString(USER_ID, SIN_LOGIN);
        Beacon beacon = new Beacon.Builder()
                .setId1("2f234454-cf6d-4a0f-adf2-f4911ba9ffa6")
                .setId2("1")
                .setId3("2")
                .setManufacturer(0x004C) // Radius Networks.  Change this for other beacon layouts
                .setTxPower(-59)
                .setDataFields(Arrays.asList(new Long[]{0l})) // Remove this for beacon layouts without d: fields
                .build();
        // Change the layout below for other beacon types
        BeaconParser beaconParser = new BeaconParser()
                .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24");
        BeaconTransmitter beaconTransmitter = new BeaconTransmitter(context, beaconParser);
        int result = BeaconTransmitter.checkTransmissionSupported(context);
        Log.i(TAG, "CHECKOUT:."+result);
        beaconTransmitter.startAdvertising(beacon, new AdvertiseCallback() {

            @Override
            public void onStartFailure(int errorCode) {
                Log.e(TAG, "Advertisement start failed with code: " + errorCode);
            }

            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                Log.i(TAG, "Advertisement start succeeded.");
            }
        });


        /*Leyenda de el código que te devuelve el método BeaconTransmitter.checkTransmissionSupported(getApplicationContext())
        public static final int	NOT_SUPPORTED_BLE	2
        public static final int	NOT_SUPPORTED_CANNOT_GET_ADVERTISER	4
        public static final int	NOT_SUPPORTED_CANNOT_GET_ADVERTISER_MULTIPLE_ADVERTISEMENTS	5
        public static final int	NOT_SUPPORTED_MIN_SDK	1
        public static final int	NOT_SUPPORTED_MULTIPLE_ADVERTISEMENTS	3
        public static final int	SUPPORTED	0*/

        /*
        Dependiendo del tipo de beacon que queramos transmitir, tenemos
        ALTBEACON   "m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"

        EDDYSTONE  TLM  "x,s:0-1=feaa,m:2-2=20,d:3-3,d:4-5,d:6-7,d:8-11,d:12-15"

        EDDYSTONE  UID  "s:0-1=feaa,m:2-2=00,p:3-3:-41,i:4-13,i:14-19"

        EDDYSTONE  URL  "s:0-1=feaa,m:2-2=10,p:3-3:-41,i:4-20v"

        IBEACON  "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"*/

        // simply constructing this class and holding a reference to it in your custom Application
        // class will automatically cause the BeaconLibrary to save battery whenever the application
        // is not visible.  This reduces bluetooth power usage by about 60%
        //backgroundPowerSaver = new BackgroundPowerSaver(this);
    }
}