package com.example.parkingenable.Usuario;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class Usuario {
    private String IDusuario;
    private String email;
    private String password;
    private String name;
    private String cardNumber;
    private String cardURL;

    public Usuario(){}

    public Usuario(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }

    public Usuario(String email, String password, String name, String cardNumber) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.cardNumber = cardNumber;
    }

    public Usuario(String email, String password, String name, String cardNumber, String cardURL) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.cardNumber = cardNumber;
        this.cardURL = cardURL;
    }

    public String getIDusuario() {
        return IDusuario;
    }

    public void setIDusuario(String IDusuario) {
        this.IDusuario = IDusuario;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardURL() {
        return cardURL;
    }

    public void setCardURL(String cardURL) {
        this.cardURL = cardURL;
    }

    //Con este mapeo podemos filtrar qué datos de la clase devolver
    // [START post_to_map]
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("email", email);
        result.put("name", name);
        result.put("password", password);
        result.put("cardNumber", cardNumber);
        result.put("cardURL", cardURL);

        return result;
    }
    // [END post_to_map]
}
