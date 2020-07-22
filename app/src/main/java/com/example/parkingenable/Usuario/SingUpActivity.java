package com.example.parkingenable.Usuario;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
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
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

public class SingUpActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;

    //Database
    private CollectionReference mDocRefUsuarios = FirebaseFirestore.getInstance().collection("usuarios");
    public static final String CARD_KEY ="numeroTarjeta";

    //User's preferences
    public static final String PREFS_NAME = "MyPrefsFile";
    public static final String USER_ID = "userID";
    public static final String AUTO_PARKING = "autoParking";

    private ProgressBar progressBar;
    private EditText email;
    private EditText name;
    private EditText surname;
    private EditText password;
    private EditText repPassword;
    private EditText cardNumber;
    private EditText dateCard;
    private Button cameraButton;
    private ImageView cardImage;
    private CheckBox checkBox;
    private Button singUpButton;

    private boolean isPhoto = false;
    private Timestamp fechaExpiracionTarjeta;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing_up);

        progressBar = findViewById(R.id.singUp_progressBar);
        email = findViewById(R.id.email);
        name = findViewById(R.id.name);
        surname = findViewById(R.id.surname);
        password = findViewById(R.id.password);
        repPassword = findViewById(R.id.reEnterPassword);
        cardNumber = findViewById(R.id.n_tarjeta);
        dateCard = findViewById(R.id.date_tarjeta);
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
                checkExistCard();
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

        dateCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
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

        saveUser();
    }

    //Validación de los parámetros de entrada en el formulario de sing up
    private boolean validateForm() {
        boolean valid = true;

        String email = this.email.getText().toString();
        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            this.email.setError("Introduce un email válido");
            valid = false;
        } else {
            this.email.setError(null);
        }

        String name = this.name.getText().toString();
        if (TextUtils.isEmpty(name)) {
            this.name.setError("Nombre requerido");
            valid = false;
        } else {
            this.name.setError(null);
        }

        String surname = this.surname.getText().toString();
        if (TextUtils.isEmpty(surname)) {
            this.surname.setError("Apellidos requeridos");
            valid = false;
        } else {
            this.surname.setError(null);
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

        String cardNumber = this.cardNumber.getText().toString();
        if (TextUtils.isEmpty(cardNumber)) {
            this.cardNumber.setError("Número de tarjeta requerido");
            valid = false;
        } else {
            this.cardNumber.setError(null);
        }

        String dateCard = this.dateCard.getText().toString();
        if (TextUtils.isEmpty(dateCard)) {
            this.dateCard.setError("Fecha de caducidad requerida");
            valid = false;
        } else {
            this.dateCard.setError(null);
        }

        if(!isPhoto){
            Toast.makeText(this, "Foto de tarjeta requerida", Toast.LENGTH_SHORT).show();
            valid = false;
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

    private void saveUser(){
        final String email = this.email.getText().toString().trim();
        final String password = this.password.getText().toString().trim();
        final String name = this.name.getText().toString().trim();
        final String surname = this.surname.getText().toString().trim();
        final String cNumber = this.cardNumber.getText().toString().trim();
        final Timestamp cardDate = fechaExpiracionTarjeta;

        String hassed = md5(password);
        final Usuario usuario;

        usuario = new Usuario(email, hassed, name, surname, cNumber, cardDate);
        usuario.setFechaCreacion(Timestamp.now());
        //Map<String, Object> usuariotValues = usuario.toMap();
        mDocRefUsuarios.add(usuario).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                userID = documentReference.getId();
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(USER_ID, userID);
                editor.putBoolean(AUTO_PARKING, checkBox.isChecked());
                // Commit the edits!
                editor.apply();

                saveStoragePhoto();

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

    private void saveStoragePhoto(){
        //Creamos una instancia de FirebaseStorage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        final String cardNumber = this.cardNumber.getText().toString().trim();
        // Create a storage reference from our app
        StorageReference storageRef = storage.getReference();

        // Creamos una referencia a la carpeta y el nombre de la imagen donde se guardara
        StorageReference mountainImagesRef = storageRef.child("tarjetas/"+ userID+"/"+cardNumber+".jpg");
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
                Toast.makeText(getBaseContext(), "ERROR al guardar foto", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                savePhotoURL(taskSnapshot.getMetadata().getPath());
            }
        });
    }

    private void savePhotoURL(String path){
        WriteBatch batch = FirebaseFirestore.getInstance().batch();
        HashMap<String, Object> usuario = new HashMap<>();
        usuario.put("fotoURL", path);
        batch.update(mDocRefUsuarios.document(userID), usuario);
        batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                sendRegistrationTokenToServer();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getBaseContext(),"Error al fijar url de la foto", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkExistCard(){
        mDocRefUsuarios.whereEqualTo(CARD_KEY, cardNumber.getText().toString()).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if(task.getResult().size() >= 1){
                                Toast.makeText(SingUpActivity.this, "Ya existe un usuario con esta tarjeta.",
                                        Toast.LENGTH_SHORT).show();
                            }else{
                                createAccount();
                            }

                        } else {
                            Toast.makeText(SingUpActivity.this, "Card check failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                });
    }


    private void showDatePickerDialog() {
        DatePickerFragment newFragment = DatePickerFragment.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                // +1 because january is zero
                final String selectedDate = twoDigits(day) + "/" + twoDigits(month + 1) + "/" + twoDigits(year);
                dateCard.setText(selectedDate);
                Calendar calendar = new GregorianCalendar(year, month, day);
                calendar.getTimeInMillis();
                fechaExpiracionTarjeta = new Timestamp(calendar.getTime());
            }
        });
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    //Es para que los días o meses se muestren a 2 dígitos.
    private String twoDigits(int n) {
        return (n <= 9) ? ("0" + n) : String.valueOf(n);
    }

    //recuperamos el token para recibir notificaciones de FCM al movil
    private void sendRegistrationTokenToServer(){
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(SingUpActivity.this, "Error al obtener el token de la app",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        saveAppToken(task.getResult().getToken());
                    }
                });
    }

    private void saveAppToken(String token){
        WriteBatch batch = FirebaseFirestore.getInstance().batch();
        HashMap<String, Object> usuario = new HashMap<>();
        usuario.put("tokenFCM", token);
        batch.update(mDocRefUsuarios.document(userID), usuario);
        batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //Volver a la pantalla principal
                Intent intent = new Intent(SingUpActivity.this, MapsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getBaseContext(),"Error al guardar el tokenFCM", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SingUpActivity.this, MapsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }
}
