package com.example.parkingenable.Usuario;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.parkingenable.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class UserEditActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;

    //Database
    private CollectionReference mDocRefUsuarios = FirebaseFirestore.getInstance().collection("usuarios");
    public static final String EMAIL_KEY="email";

    //Creamos una instancia de FirebaseStorage
    StorageReference storageRef = FirebaseStorage.getInstance().getReference();

    //User's preferences
    public static final String PREFS_NAME = "MyPrefsFile";
    public static final String USER_ID = "userID";
    public static final String AUTO_PARKING = "autoParking";
    public static final String SIN_LOGIN = "sinLogin";

    private SharedPreferences settings;
    private String userId;
    private String currentSavedPassword;


    private ProgressBar progressBar;
    private EditText name;
    private EditText currentPassword;
    private EditText newPassword;
    private EditText confirmNewPassword;
    private EditText cardNumber;
    private Button cameraButton;
    private ImageView cardImage;
    private Button editButton;
    private View editLayout;

    private boolean isPhoto = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_edit);

        progressBar = findViewById(R.id.progressBar);
        name = findViewById(R.id.nombre_edit);
        currentPassword = findViewById(R.id.contraseña_actual_edit);
        newPassword = findViewById(R.id.reEnterPassword);
        confirmNewPassword = findViewById(R.id.confirmar_contraseña_edit);
        cardNumber = findViewById(R.id.n_tarjeta);
        cameraButton = findViewById(R.id.photo_button);
        cardImage = findViewById(R.id.tarjeta_ImageView);
        editButton = findViewById(R.id.editar_button);
        editLayout = findViewById(R.id.editLayout);

        userId = settings.getString(USER_ID, SIN_LOGIN);

        cargarPerfil();

        cardNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().equals("")){
                    cameraButton.setEnabled(false);
                    cameraButton.setTextColor(getResources().getColor(R.color.grayGoogle));
                    cameraButton.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                }else{
                    cameraButton.setEnabled(true);
                    cameraButton.setTextColor(getResources().getColor(R.color.whiteGoogle));
                    cameraButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                }
            }
        });

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = null;
            if (data != null) {
                extras = data.getExtras();
            }
            Bitmap imageBitmap = null;
            if (extras != null) {
                imageBitmap = (Bitmap) extras.get("data");
            }
            this.cardImage.setImageBitmap(imageBitmap);
            isPhoto = true;
        }
    }

    private void cargarPerfil(){
        if(!Objects.equals(settings.getString(USER_ID, SIN_LOGIN), SIN_LOGIN)){
            userId = settings.getString(USER_ID, SIN_LOGIN);

            mDocRefUsuarios.document(userId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    progressBar.setVisibility(View.INVISIBLE);
                    Usuario usuario = documentSnapshot.toObject(Usuario.class);
                    if(usuario != null) {
                        if (usuario.getNombre() != null) {
                            name.setText(usuario.getNombre());
                        }

                        if(usuario.getPassword() != null){
                            currentSavedPassword = usuario.getPassword();
                        }

                        if (usuario.getNumeroTarjeta() != null) {
                            cardNumber.setText(usuario.getNumeroTarjeta());
                        }
                        if (usuario.getFotoURL() != null) {
                            storageRef.child(usuario.getFotoURL()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Glide.with(getBaseContext())
                                            .load(uri)
                                            .into(cardImage);
                                }
                            });
                        }
                    }

                    editLayout.setVisibility(View.VISIBLE);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getBaseContext(), "Error al cargar el perfil", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private boolean verifyEditForm(){
        boolean valid = true;
        String name = this.name.getText().toString();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Nombre requerido", Toast.LENGTH_LONG).show();
            valid = false;
        } else {
            this.name.setError(null);
        }


        if (!newPassword.equals(confirmNewPassword)) {
            Toast.makeText(this, "La nueva contraseña no coincide con la confirmación", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        if (this.confirmNewPassword.getText().toString().isEmpty() || this.newPassword.getText().toString().isEmpty() || this.currentPassword.getText().toString().isEmpty()) {
            Toast.makeText(this, "La contraseña es un campo requerido", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        if (currentPassword.equals(currentSavedPassword)) {
            Toast.makeText(this, "La contraseña actual no coincide con la original", Toast.LENGTH_SHORT).show();

            valid = false;
        }

        return  valid;

    }

    public String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

}
