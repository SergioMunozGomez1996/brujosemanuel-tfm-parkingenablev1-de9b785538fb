package com.example.parkingenable.Usuario;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
    public static final String EMAIL_KEY="email";
    public static final String PASSWORD_KEY="password";
    //User's preferences
    public static final String PREFS_NAME = "MyPrefsFile";

    private EditText emailUsuario;
    private EditText passwordUsuario;
    private ProgressBar progressBar;
    private Button loginButton;
    private TextView singUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailUsuario = findViewById(R.id.email);
        passwordUsuario = findViewById(R.id.password);
        progressBar = findViewById(R.id.login_progressBar);
        loginButton = findViewById(R.id.login_button);
        singUpButton = findViewById(R.id.singUp_textView);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
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
        String email = emailUsuario.getText().toString();
        String password = passwordUsuario.getText().toString();
        String hashPassword = md5(password);

        if (!validateForm()) {
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        mDocRefUsuarios.whereEqualTo(EMAIL_KEY, email).whereEqualTo(PASSWORD_KEY, hashPassword).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult().size() == 1) {
                            for (QueryDocumentSnapshot usuario : task.getResult()) {
                                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                                SharedPreferences.Editor editor = settings.edit();
                                editor.putString("userID", usuario.getId());

                                // Commit the edits!
                                editor.apply();

                                //Volver a la pantalla principal
                                Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                });
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = emailUsuario.getText().toString();
        if (TextUtils.isEmpty(email)|| !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailUsuario.setError("Introduce un email válido");
            valid = false;
        } else {
            emailUsuario.setError(null);
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
