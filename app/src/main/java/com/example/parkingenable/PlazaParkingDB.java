package com.example.parkingenable;

import com.google.android.gms.maps.model.LatLng;

public class PlazaParkingDB {

    private String comunidad;
    private String provincia;
    private String ciudad;
    private String calle;
    private double latitude;
    private double longitude;
    //private LatLng coordenadas;
    private boolean libre;
    private int like;
    private int dislike;
    private String usuarioOcupando;

    public PlazaParkingDB(){}

    public int getDislike() { return dislike; }

    public void setDislike(int dislike) { this.dislike = dislike; }

    public int getLike() { return like; }

    public void setLike(int like) { this.like = like; }

    public String getComunidad() {
        return comunidad;
    }

    public void setComunidad(String comunidad) {
        this.comunidad = comunidad;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
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

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public boolean isLibre() {
        return libre;
    }

    public void setLibre(boolean libre) {
        this.libre = libre;
    }

    public String getUsuarioOcupando() {
        return usuarioOcupando;
    }

    public void setUsuarioOcupando(String usuarioOcupando) {
        this.usuarioOcupando = usuarioOcupando;
    }

    /*
    public LatLng getCoordenadas() {
        return coordenadas;
    }

    public void setCoordenadas(LatLng coordenadas) {
        this.coordenadas = coordenadas;
    }
    */
}
