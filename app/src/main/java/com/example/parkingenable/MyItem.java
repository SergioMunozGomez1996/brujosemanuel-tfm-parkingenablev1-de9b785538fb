package com.example.parkingenable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class MyItem implements ClusterItem {

    private LatLng position;
    private String title;
    private String snippet;


    public MyItem(LatLng position){
        this.position = position;
    }

    public MyItem(LatLng position, String title, String snippet){
        this.position = position;
        this.title = title;
        this.snippet = snippet;
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSnippet() {
        return snippet;
    }
}
