package com.example.parkingenable.Usuario;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.parkingenable.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Objects;

public class UserEditActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;

    //Database
    private CollectionReference mDocRefUsuarios = FirebaseFirestore.getInstance().collection("usuarios");
    public static final String EMAIL_KEY="email";
    public static final String CARD_KEY ="numeroTarjeta";


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
    private Timestamp fechaExpiracionTarjeta;
    private boolean perfilRevisado;
    private Timestamp fechaEdicion;
    private String numeroTarjetaActual;



    private ProgressBar progressBar;
    private EditText name;
    private EditText apellidos;
    private EditText email;
    private EditText currentPassword;
    private EditText newPassword;
    private EditText confirmNewPassword;
    private EditText cardNumber;
    private EditText cardDate;
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
        name = findViewById(R.id.name);
        apellidos = findViewById(R.id.surname);
        email = findViewById(R.id.email);
        currentPassword = findViewById(R.id.current_password);
        newPassword = findViewById(R.id.new_password);
        confirmNewPassword = findViewById(R.id.reEnterPassword);
        cardNumber = findViewById(R.id.n_tarjeta);
        cameraButton = findViewById(R.id.photo_button);
        cardImage = findViewById(R.id.tarjeta_ImageView);
        cardDate = findViewById(R.id.date_tarjeta);
        editButton = findViewById(R.id.edit_user_button);
        editLayout = findViewById(R.id.editLayout);

        settings = getSharedPreferences(PREFS_NAME, 0);
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

        cardDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkExistCard();
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

                    Usuario usuario = documentSnapshot.toObject(Usuario.class);
                    if(usuario != null) {
                        if(usuario.isPerfilRevisado()){
                            name.setVisibility(View.GONE);
                            apellidos.setVisibility(View.GONE);
                            cardDate.setVisibility(View.GONE);
                            cardNumber.setVisibility(View.GONE);
                        }
                        if (usuario.getNombre() != null) {
                            name.setText(usuario.getNombre());
                        }
                        if (usuario.getApellidos() != null) {
                            apellidos.setText(usuario.getApellidos());
                        }
                        if (usuario.getCorreo() != null) {
                            email.setText(usuario.getCorreo());
                        }
                        if (usuario.getFechaCaducidadTarjeta() != null) {
                            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                            cardDate.setText(formatter.format(usuario.getFechaCaducidadTarjeta().toDate()));
                            fechaExpiracionTarjeta = usuario.getFechaCaducidadTarjeta();
                        }

                        if(usuario.getPassword() != null){
                            currentSavedPassword = usuario.getPassword();
                        }

                        if (usuario.getNumeroTarjeta() != null) {
                            cardNumber.setText(usuario.getNumeroTarjeta());
                            numeroTarjetaActual = usuario.getNumeroTarjeta();
                        }
                        if (usuario.getFotoURL() != null) {
                            storageRef.child(usuario.getFotoURL()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Glide.with(getBaseContext())
                                            .load(uri)
                                            .into(cardImage);
                                }
                            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    editLayout.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                        perfilRevisado = usuario.isPerfilRevisado();
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getBaseContext(), "Error al cargar el perfil", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void guardarFoto() {
        progressBar.setVisibility(View.VISIBLE);
        editButton.setEnabled(false);
        //Creamos una instancia de FirebaseStorage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        final String cardNumber = this.cardNumber.getText().toString().trim();
        // Create a storage reference from our app
        StorageReference storageRef = storage.getReference();

        // Creamos una referencia a la carpeta y el nombre de la imagen donde se guardara
        StorageReference mountainImagesRef = storageRef.child("tarjetas/" + userId + "/" + cardNumber + ".jpg");
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
                editButton.setEnabled(true);
                Toast.makeText(getBaseContext(), "ERROR en la edición", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                actualizarPerfil(taskSnapshot.getMetadata().getPath());

            }
        });
    }

    private void actualizarPerfil(String path) {
        WriteBatch batch = FirebaseFirestore.getInstance().batch();
        HashMap<String, Object> nuevoUsuario = new HashMap<>();
        if(!email.getText().toString().isEmpty())
            nuevoUsuario.put("correo", email.getText().toString());
        if(!name.getText().toString().isEmpty())
            nuevoUsuario.put("nombre", name.getText().toString());
        if(!apellidos.getText().toString().isEmpty())
            nuevoUsuario.put("apellidos", apellidos.getText().toString());
        if(!cardNumber.getText().toString().isEmpty())
            nuevoUsuario.put("numeroTarjeta", cardNumber.getText().toString());
        if(!cardDate.getText().toString().isEmpty())
            nuevoUsuario.put("fechaCaducidadTarjeta", fechaExpiracionTarjeta);
        fechaEdicion = Timestamp.now();
        nuevoUsuario.put("fechaUltimaModificacion", fechaEdicion);
        nuevoUsuario.put("fotoURL", path);
        if(verifyPassword()){
            nuevoUsuario.put("password", md5(newPassword.getText().toString()));
        }
        batch.update(mDocRefUsuarios.document(userId), nuevoUsuario);
        batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getBaseContext(),"Error en la edición", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean verifyPassword(){
        boolean valid = true;
        if (!newPassword.getText().toString().equals(confirmNewPassword.getText().toString())) {
            Toast.makeText(this, "La nueva contraseña no coincide con la confirmación", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        if (newPassword.getText().toString().length() < 6) {
            newPassword.setError("Más de 6 caracteres.");
            valid = false;
        } else {
            newPassword.setError(null);
        }

        if(currentPassword.getText().toString().isEmpty() && !newPassword.getText().toString().isEmpty()){
            currentPassword.setError("Introduce la contrasella actual");
            valid = false;
        }
        else {
            currentPassword.setError(null);
        }

        if(currentPassword.getText().toString().isEmpty() && !confirmNewPassword.getText().toString().isEmpty()){
            currentPassword.setError("Introduce la contrasella actual");
            valid = false;
        }else {
            currentPassword.setError(null);
        }
        if(!currentPassword.getText().toString().isEmpty() && newPassword.getText().toString().isEmpty()){
            currentPassword.setError("Introduce la contrasella nueva");
            valid = false;
        }else {
            currentPassword.setError(null);
        }

        if(!currentPassword.getText().toString().isEmpty() && confirmNewPassword.getText().toString().isEmpty()){
            currentPassword.setError("Introduce la confirmación de nueva contrasella");
            valid = false;
        }else {
            currentPassword.setError(null);
        }

        if (!currentPassword.getText().toString().isEmpty() && !md5(currentPassword.getText().toString()).equals(currentSavedPassword)) {
            Toast.makeText(this, "La contraseña actual no coincide con la original", Toast.LENGTH_SHORT).show();

            valid = false;
        }else {
            currentPassword.setError(null);
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

    private void showDatePickerDialog() {
        DatePickerFragment newFragment = DatePickerFragment.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                // +1 because january is zero
                final String selectedDate = twoDigits(day) + "/" + twoDigits(month + 1) + "/" + twoDigits(year);
                cardDate.setText(selectedDate);
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

    private void checkExistCard(){
        mDocRefUsuarios.whereEqualTo(CARD_KEY, cardNumber.getText().toString()).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if(task.getResult().size() >= 1){
                                for (QueryDocumentSnapshot doc : task.getResult()) {
                                    if (doc.get("numeroTarjeta").toString().equals(numeroTarjetaActual)) {
                                        guardarFoto();
                                        return;
                                    }
                                }
                                cardNumber.setError("Ya existe un usuario con esta tarjeta");
                            }else{
                                guardarFoto();
                            }

                        } else {
                            Toast.makeText(UserEditActivity.this, "Card check failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                });
    }

}
