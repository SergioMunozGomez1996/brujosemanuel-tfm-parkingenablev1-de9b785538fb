package com.example.parkingenable;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private static final String TAG = "CustomInfoWindowAdapter";
    private LayoutInflater inflater;
    private Boolean status;
    private String street;
    private final View mWindow;
    private Context mContext;

    /*public CustomInfoWindowAdapter(LayoutInflater inflater, Boolean status, String street){
        this.inflater = inflater;
        this.status = status;
        this.street = street;
    }*/
    public CustomInfoWindowAdapter(Context context){
        mContext = context;
        mWindow = LayoutInflater.from(context).inflate(R.layout.infowindow_layout, null);
    }

    private void rendowWindowTest(Marker marker, View view){

        ((TextView)view.findViewById(R.id.info_window_calle)).setText(marker.getTitle()/*this.street*/);
        ((TextView)view.findViewById(R.id.info_window_estado)).setText("Estado: "+ marker.getSnippet()/*estado*/);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        rendowWindowTest(marker, mWindow);
        return mWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        rendowWindowTest(marker, mWindow);
        return mWindow;
        //Carga layout personalizado.
       /* String estado="";
        if(this.status){
            estado="Libre";
        }else{
            estado="Ocupado";
        }
        View v = inflater.inflate(R.layout.infowindow_layout, null);
        String[] info = marker.getTitle().split("&");
        String url = marker.getSnippet();
        ((TextView)v.findViewById(R.id.info_window_calle)).setText(this.street);
        ((TextView)v.findViewById(R.id.info_window_estado)).setText("Estado: "+ estado);
        return v;*/
    }
}
