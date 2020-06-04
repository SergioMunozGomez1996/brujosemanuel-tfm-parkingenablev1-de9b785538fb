package com.example.parkingenable.Usuario;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.parkingenable.MapsActivity;
import com.example.parkingenable.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    //Database
    private CollectionReference mDocRefUsuarios = FirebaseFirestore.getInstance().collection("usuarios");
    public static final String CARD_KEY ="numeroTarjeta";
    public static final String PASSWORD_KEY="password";
    //User's preferences
    public static final String PREFS_NAME = "MyPrefsFile";
    public static final String USER_ID = "userID";
    public static final String AUTO_PARKING = "autoParking";

    private EditText tarjetaUsuario;
    private EditText passwordUsuario;
    private ProgressBar progressBar;
    private Button loginButton;
    private TextView singUpButton;
    private CheckBox autoParking;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tarjetaUsuario = findViewById(R.id.n_tarjeta);
        passwordUsuario = findViewById(R.id.password);
        progressBar = findViewById(R.id.login_progressBar);
        loginButton = findViewById(R.id.login_button);
        singUpButton = findViewById(R.id.singUp_textView);
        autoParking = findViewById(R.id.autoParking_checkBox);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        singUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent  = new Intent(LoginActivity.this, SingUpActivity.class);
                startActivity(intent);
            }
        });

    }

    private void signIn() {
        String tarjeta = tarjetaUsuario.getText().toString();
        String password = passwordUsuario.getText().toString();
        String hashPassword = md5(password);

        if (!validateForm()) {
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        loginButton.setEnabled(false);

        mDocRefUsuarios.whereEqualTo(CARD_KEY, tarjeta).whereEqualTo(PASSWORD_KEY, hashPassword).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult().size() == 1) {
                            for (QueryDocumentSnapshot usuario : task.getResult()) {
                                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                                SharedPreferences.Editor editor = settings.edit();
                                editor.putString(USER_ID, usuario.getId());
                                editor.putBoolean(AUTO_PARKING, autoParking.isChecked());

                                // Commit the edits!
                                editor.apply();

                                //Volver a la pantalla principal
                                Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            }
                        } else {
                            progressBar.setVisibility(View.INVISIBLE);
                            loginButton.setEnabled(true);
                            Log.d(TAG, "Error getting documents: ", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                });
    }

    private boolean validateForm() {
        boolean valid = true;

        String tarjeta = tarjetaUsuario.getText().toString();
        if (TextUtils.isEmpty(tarjeta)) {
            tarjetaUsuario.setError("Introduce una tarjeta válida");
            valid = false;
        } else {
            tarjetaUsuario.setError(null);
        }

        String password = passwordUsuario.getText().toString();
        if (TextUtils.isEmpty(password)|| password.length() < 6) {
            passwordUsuario.setError("Más de 6 caracteres.");
            valid = false;
        } else {
            passwordUsuario.setError(null);
        }

        return valid;
    }

    public String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i=0; i<messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
