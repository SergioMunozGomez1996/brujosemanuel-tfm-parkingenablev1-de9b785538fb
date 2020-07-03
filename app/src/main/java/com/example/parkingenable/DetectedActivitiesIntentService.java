/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.parkingenable;

import android.app.IntentService;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;

import java.util.ArrayList;
import java.util.Arrays;

/**
 *  IntentService for handling incoming intents that are generated as a result of requesting
 *  activity updates using
 *  {@link com.google.android.gms.location.ActivityRecognitionApi#requestActivityUpdates}.
 */
public class DetectedActivitiesIntentService extends IntentService {

    protected static final String TAG = "DetectedActivitiesIS";
    private static final String PREFS_NAME = "MyPrefsFile";

    /*int	IN_VEHICLE	The device is in a vehicle, such as a car = 0.
    int	ON_BICYCLE	The device is on a bicycle = 1.
    int	ON_FOOT	The device is on a user who is walking or running = 2.
    int	RUNNING	The device is on a user who is running = 8.
    int	STILL	The device is still (not moving) = 3.
    int	TILTING	The device angle relative to gravity changed significantly = 5.
    int	UNKNOWN	Unable to detect the current activity = 4.
    int	WALKING	The device is on a user who is walking. = 7*/

    /**
     * This constructor is required, and calls the super IntentService(String)
     * constructor with the name for a worker thread.
     */
    public DetectedActivitiesIntentService() {
        // Use the TAG to name the worker thread.
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * Handles incoming intents.
     * @param intent The Intent is provided (inside a PendingIntent) when requestActivityUpdates()
     *               is called.
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void onHandleIntent(Intent intent) {
        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
        Log.w(TAG, String.valueOf(result.getMostProbableActivity().getType()));
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        int ultimaActividad = settings.getInt("UltimaActividad", -1);
        int actividadActual = result.getMostProbableActivity().getType();

        // Get the list of the probable activities associated with the current state of the
        // device. Each activity is associated with a confidence level, which is an int between
        // 0 and 100.
        //ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();

        //si la ultima actividad guardada es conducir y la nueva es estar parado, empieza a emitir beacons
        if(ultimaActividad == 7 && actividadActual == 3){
            startTransmittingBeacons();
        }

        settings.edit()
                /*.putString(Constants.KEY_DETECTED_ACTIVITIES,
                        Utils.detectedActivitiesToJson(detectedActivities))*/
                .putInt("UltimaActividad", actividadActual)
                .apply();

        // Log each activity.
        /*Log.i(TAG, "activities detected");
        for (DetectedActivity da: detectedActivities) {
            Log.i(TAG, Utils.getActivityString(
                            getApplicationContext(),
                            da.getType()) + " " + da.getConfidence() + "%"
            );
        }*/
    }

    private void startTransmittingBeacons(){
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
        BeaconTransmitter beaconTransmitter = new BeaconTransmitter(getApplicationContext(), beaconParser);
        int result = BeaconTransmitter.checkTransmissionSupported(getApplicationContext());
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
