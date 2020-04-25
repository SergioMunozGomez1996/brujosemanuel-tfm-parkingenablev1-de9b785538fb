package com.example.parkingenable;

import com.google.android.gms.maps.model.LatLng;

public class PlazaParking {

    private LatLng Coordenadas;
    private double lat;
    private double lng;
    private String ciudad;
    private String calle;

    public LatLng getCoordenadas() {
        return Coordenadas;
    }

    public void setCoordenadas(LatLng coordenadas) {
        Coordenadas = coordenadas;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getCalle() {
        return calle;
    }

    public void setCalle(String calle) {
        this.calle = calle;
    }
}
