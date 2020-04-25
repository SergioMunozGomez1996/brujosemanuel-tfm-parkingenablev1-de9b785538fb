package com.example.parkingenable;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class SplashActivity extends Activity {

    // Duration of the splash in miliseconds
    private final int DURACION_SPLASH = 3000; // 3 segundos


    //Strings
    private static String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;

    //Another variables
    private final int REQUEST_PERMISSION_LOCATION = 1;
    private boolean mLocationPermissionGranted;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getLocationPermission();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MapsActivity.class);
                startActivity(intent);
                finish();
            }
        }, DURACION_SPLASH);
    }

    /**
     * Method to get the location permission that we need to use at map
     */
    private void getLocationPermission(){
        //the permissions we are going to demand to the user
        String[] permisos = {FINE_LOCATION};

        //If we already have the permissions
        if(ContextCompat.checkSelfPermission(this, FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            mLocationPermissionGranted = true;
        }else{
            ActivityCompat.requestPermissions(this, permisos, REQUEST_PERMISSION_LOCATION);
        }
    }
}
