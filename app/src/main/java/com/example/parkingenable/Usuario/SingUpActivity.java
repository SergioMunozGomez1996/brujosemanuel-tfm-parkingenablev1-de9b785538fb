package com.example.parkingenable.Usuario;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.parkingenable.MapsActivity;
import com.example.parkingenable.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class SingUpActivity extends AppCompatActivity {

    //Database
    private CollectionReference mDocRefUsuarios = FirebaseFirestore.getInstance().collection("usuarios");
    //User's preferences
    public static final String PREFS_NAME = "MyPrefsFile";

    private ProgressBar progressBar;
    private EditText email;
    private EditText name;
    private EditText password;
    private EditText repPassword;
    private EditText cardNumber;
    private Button cameraButton;
    private ImageView cardImage;
    private CheckBox checkBox;
    private Button singUpButton;

    private boolean isPhoto = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing_up);

        progressBar = findViewById(R.id.singUp_progressBar);
        email = findViewById(R.id.email);
        name = findViewById(R.id.name);
        password = findViewById(R.id.password);
        repPassword = findViewById(R.id.reEnterPassword);
        cardNumber = findViewById(R.id.n_tarjeta);
        cameraButton = findViewById(R.id.photo_button);
        cardImage = findViewById(R.id.tarjeta_ImageView);
        checkBox = findViewById(R.id.autoParking_checkBox);
        singUpButton = findViewById(R.id.singUp_button);

        singUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAccount();
            }
        });
    }

    private void createAccount(){
        final String email = this.email.getText().toString().trim();
        final String password = this.password.getText().toString().trim();
        final String name = this.name.getText().toString().trim();

        if (!validateForm()) {
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        String hassed = md5(password);
        final Usuario usuario = new Usuario(email, hassed, name);
        Map<String, Object> usuariotValues = usuario.toMap();
        mDocRefUsuarios.add(usuariotValues).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                progressBar.setVisibility(View.INVISIBLE);
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("userID", documentReference.getId());

                // Commit the edits!
                editor.apply();

                //Volver a la pantalla principal
                Intent intent = new Intent(SingUpActivity.this, MapsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(getBaseContext(), "ERROR en el registro", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Validación de los parámetros de entrada en el formulario de sing up
    private boolean validateForm() {
        boolean valid = true;

        String email = this.email.getText().toString();
        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Introduce un email válido", Toast.LENGTH_LONG).show();
            valid = false;
        } else {
            this.email.setError(null);
        }

        String password = this.password.getText().toString();
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            this.password.setError("Más de 6 caracteres.");
            valid = false;
        } else {
            this.password.setError(null);
        }

        String reEnterPassword = this.repPassword.getText().toString();
        if (reEnterPassword.isEmpty() || reEnterPassword.length() < 4 || reEnterPassword.length() > 10 || !(reEnterPassword.equals(password))) {
            this.repPassword.setError("Contraseñas no coinciden");
            valid = false;
        } else {
            this.repPassword.setError(null);
        }

        String name = this.name.getText().toString();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Nombre requerido", Toast.LENGTH_LONG).show();
            valid = false;
        } else {
            this.name.setError(null);
        }

        /*String numer = this.cardNumber.getText().toString();
        if(TextUtils.isEmpty(numer)){

        }*/

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
