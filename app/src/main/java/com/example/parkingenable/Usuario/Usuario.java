package com.example.parkingenable.Usuario;

import com.google.firebase.Timestamp;

import java.util.HashMap;
import java.util.Map;

public class Usuario {
    //private String IDusuario;
    private String correo;
    private String password;
    private String nombre;
    private String apellidos;
    private String numeroTarjeta;
    private String fotoURL;
    private Timestamp fechaCaducidadTarjeta;
    private boolean perfilRevisado;
    private boolean perfilSuspendido;
    private boolean correoVerificado;
    private Timestamp fechaCreacion;
    private Timestamp fechaUltimaModificacion;
    private Timestamp fechaRevision;
    private String codigoCambioPassword;
    private Timestamp fechaCaducidadCodigoCambioPassword;
    private String plazaOcupada;


    /*public Usuario(String correo, String password, String nombre) {
        this.correo = correo;
        this.password = password;
        this.nombre = nombre;
    }

    public Usuario(String correo, String password, String nombre, String numeroTarjeta) {
        this.correo = correo;
        this.password = password;
        this.nombre = nombre;
        this.numeroTarjeta = numeroTarjeta;
    }

    public Usuario(String correo, String password, String nombre, String numeroTarjeta, String fotoURL) {
        this.correo = correo;
        this.password = password;
        this.nombre = nombre;
        this.numeroTarjeta = numeroTarjeta;
        this.fotoURL = fotoURL;
    }*/

    public Usuario(){}

    public Usuario(String correo, String password, String nombre, String apellidos, String numeroTarjeta, Timestamp fechaCaducidadTarjeta) {
        this.correo = correo;
        this.password = password;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.numeroTarjeta = numeroTarjeta;
        this.fechaCaducidadTarjeta = fechaCaducidadTarjeta;
    }

    /*public String getIDusuario() {
        return IDusuario;
    }

    public void setIDusuario(String IDusuario) {
        this.IDusuario = IDusuario;
    }*/

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNumeroTarjeta() {
        return numeroTarjeta;
    }

    public void setNumeroTarjeta(String numeroTarjeta) {
        this.numeroTarjeta = numeroTarjeta;
    }

    public String getFotoURL() {
        return fotoURL;
    }

    public void setFotoURL(String fotoURL) {
        this.fotoURL = fotoURL;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public Timestamp getFechaCaducidadTarjeta() {
        return fechaCaducidadTarjeta;
    }

    public void setFechaCaducidadTarjeta(Timestamp fechaCaducidadTarjeta) {
        this.fechaCaducidadTarjeta = fechaCaducidadTarjeta;
    }

    public boolean isPerfilRevisado() {
        return perfilRevisado;
    }

    public void setPerfilRevisado(boolean perfilRevisado) {
        this.perfilRevisado = perfilRevisado;
    }

    public boolean isPerfilSuspendido() {
        return perfilSuspendido;
    }

    public void setPerfilSuspendido(boolean perfilSuspendido) {
        this.perfilSuspendido = perfilSuspendido;
    }

    public boolean isCorreoVerificado() {
        return correoVerificado;
    }

    public void setCorreoVerificado(boolean correoVerificado) {
        this.correoVerificado = correoVerificado;
    }

    public Timestamp getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Timestamp fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Timestamp getFechaUltimaModificacion() {
        return fechaUltimaModificacion;
    }

    public void setFechaUltimaModificacion(Timestamp fechaUltimaModificacion) {
        this.fechaUltimaModificacion = fechaUltimaModificacion;
    }

    public Timestamp getFechaRevision() {
        return fechaRevision;
    }

    public void setFechaRevision(Timestamp fechaRevision) {
        this.fechaRevision = fechaRevision;
    }

    public String getCodigoCambioPassword() {
        return codigoCambioPassword;
    }

    public void setCodigoCambioPassword(String codigoCambioPassword) {
        this.codigoCambioPassword = codigoCambioPassword;
    }

    public Timestamp getFechaCaducidadCodigoCambioPassword() {
        return fechaCaducidadCodigoCambioPassword;
    }

    public void setFechaCaducidadCodigoCambioPassword(Timestamp fechaCaducidadCodigoCambioPassword) {
        this.fechaCaducidadCodigoCambioPassword = fechaCaducidadCodigoCambioPassword;
    }

    public String getPlazaOcupada() {
        return plazaOcupada;
    }

    public void setPlazaOcupada(String plazaOcupada) {
        this.plazaOcupada = plazaOcupada;
    }

    //Con este mapeo podemos filtrar qué datos de la clase devolver
    // [START post_to_map]
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("correo", correo);
        result.put("nombre", nombre);
        result.put("apellidos", apellidos);
        result.put("password", password);
        result.put("numeroTarjeta", numeroTarjeta);
        result.put("fechaCaducidadTarjeta", fechaCaducidadTarjeta);
        result.put("fotoURL", fotoURL);
        result.put("perfilRevisado", perfilRevisado);
        result.put("perfilSuspendido", perfilSuspendido);
        result.put("correoVerificado", correoVerificado);
        result.put("fechaCreacion", fechaCreacion);
        result.put("fechaUltimaModificacion", fechaUltimaModificacion);
        result.put("fechaRevision", fechaRevision);
        result.put("codigoCambioPassword", codigoCambioPassword);
        result.put("fechaCaducidadCodigoCambioPassword", fechaCaducidadCodigoCambioPassword);
        result.put("plazaOcupada", plazaOcupada);

        return result;
    }
    // [END post_to_map]
}
