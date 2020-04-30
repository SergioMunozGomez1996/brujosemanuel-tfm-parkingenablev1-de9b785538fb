package com.example.parkingenable.Usuario;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.parkingenable.MapsActivity;
import com.example.parkingenable.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class SingUpActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;

    //Database
    private CollectionReference mDocRefUsuarios = FirebaseFirestore.getInstance().collection("usuarios");
    public static final String EMAIL_KEY="email";

    //User's preferences
    public static final String PREFS_NAME = "MyPrefsFile";
    public static final String USER_ID = "userID";
    public static final String AUTO_PARKING = "autoParking";

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

        singUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkExistEmail();
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

    private void createAccount() {


        if (!validateForm()) {
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        singUpButton.setEnabled(false);

        if(isPhoto){
            //Creamos una instancia de FirebaseStorage
            FirebaseStorage storage = FirebaseStorage.getInstance();
            final String email = this.email.getText().toString().trim();
            // Create a storage reference from our app
            StorageReference storageRef = storage.getReference();

            // Creamos una referencia a la carpeta y el nombre de la imagen donde se guardara
            StorageReference mountainImagesRef = storageRef.child("tarjetas/"+email+".jpg");
            Bitmap bitmap = ((BitmapDrawable) this.cardImage.getDrawable()).getBitmap();
            //Pasamos la imagen a un array de byte
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] datas = baos.toByteArray();

            // Empezamos con la subida a Firebase
            UploadTask uploadTask = mountainImagesRef.putBytes(datas);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    progressBar.setVisibility(View.INVISIBLE);
                    singUpButton.setEnabled(true);
                    Toast.makeText(getBaseContext(), "ERROR en el registro", Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    saveUserWithPhoto(taskSnapshot.getMetadata().getPath());
                }
            });
        }else {
            saveUser();
        }



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


        return valid;
    }

    public String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte[] messageDigest = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) hexString.append(Integer.toHexString(0xFF & b));
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void saveUser() {
        final String email = this.email.getText().toString().trim();
        final String password = this.password.getText().toString().trim();
        final String name = this.name.getText().toString().trim();
        final String cardNumber = this.cardNumber.getText().toString();

        String hassed = md5(password);
        final Usuario usuario;
        if(!cardNumber.equals("")){
            usuario = new Usuario(email, hassed, name, cardNumber);
        }else {
            usuario = new Usuario(email, hassed, name);
        }
        Map<String, Object> usuariotValues = usuario.toMap();
        mDocRefUsuarios.add(usuariotValues).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                progressBar.setVisibility(View.INVISIBLE);
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(USER_ID, documentReference.getId());
                editor.putBoolean(AUTO_PARKING, checkBox.isChecked());
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
                singUpButton.setEnabled(true);
                Toast.makeText(getBaseContext(), "ERROR en el registro", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserWithPhoto(String photoURL){
        final String email = this.email.getText().toString().trim();
        final String password = this.password.getText().toString().trim();
        final String name = this.name.getText().toString().trim();
        final String nCard = this.cardNumber.getText().toString().trim();

        String hassed = md5(password);
        final Usuario usuario;

        usuario = new Usuario(email, hassed, name, nCard, photoURL);

        Map<String, Object> usuariotValues = usuario.toMap();
        mDocRefUsuarios.add(usuariotValues).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                progressBar.setVisibility(View.INVISIBLE);
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(USER_ID, documentReference.getId());
                editor.putBoolean(AUTO_PARKING, checkBox.isChecked());
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
                singUpButton.setEnabled(true);
                Toast.makeText(getBaseContext(), "ERROR en el registro", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkExistEmail(){
        mDocRefUsuarios.whereEqualTo(EMAIL_KEY, email.getText().toString()).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if(task.getResult().size() >= 1){
                                Toast.makeText(SingUpActivity.this, "Ya existe un usuario con ese email.",
                                        Toast.LENGTH_SHORT).show();
                            }else{
                                createAccount();
                            }

                        } else {
                            Toast.makeText(SingUpActivity.this, "Email check failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                });
    }
}
