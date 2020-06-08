package com.example.parkingenable.Usuario;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.parkingenable.MapsActivity;
import com.example.parkingenable.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Objects;

public class UserProfileActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "MyPrefsFile";
    public static final String USER_ID = "userID";
    public static final String SIN_LOGIN = "sinLogin";
    public static final String AUTO_PARKING = "autoParking";


    //Database
    private CollectionReference mDocRefUsuarios = FirebaseFirestore.getInstance().collection("usuarios");
    private CollectionReference mDocRefHistoricoUsuarios = FirebaseFirestore.getInstance().collection("historicoUsuarios");

    //Creamos una instancia de FirebaseStorage
    StorageReference storageRef = FirebaseStorage.getInstance().getReference();

    private TextView userName;
    private TextView userSurname;
    private TextView userEmail;
    private TextView userNumerCard;
    private TextView userDateCard;
    private ImageView userCardImage;
    private CheckBox autoParking;

    private String userId;

    private String userPassword;
    private Usuario usuario;

    private SharedPreferences settings;

    private ProgressBar progressBar;
    private View titleLayuout;
    private View buttonsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        //Inicializar variables de usuario
        userName = findViewById(R.id.nombre_detail);
        userSurname = findViewById(R.id.surname_detail);
        userEmail = findViewById(R.id.correo_detail);
        userNumerCard = findViewById(R.id.numero_tarjeta_detail);
        userDateCard = findViewById(R.id.date_tarjeta_detail);
        userCardImage = findViewById(R.id.foto_tarjeta_imagen);
        titleLayuout = findViewById(R.id.titleLayout);
        buttonsLayout = findViewById(R.id.buttonsLayout);
        autoParking = findViewById(R.id.autoParking_checkBox);

        progressBar = findViewById(R.id.progressBar);

        Button editarButton = findViewById(R.id.editar_perfil);
        Button singoutButton = findViewById(R.id.cerrar_sesion);
        Button borrarButton = findViewById(R.id.borrar_perfil);

        singoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cerrarSesion();
            }
        });

        editarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent  = new Intent(UserProfileActivity.this, UserEditActivity.class);
                startActivity(intent);
            }
        });
        borrarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmarBorrarPerfil();
            }
        });

        settings = getSharedPreferences(PREFS_NAME, 0);
        autoParking.setChecked(settings.getBoolean(AUTO_PARKING, false));

    }

    private void confirmarBorrarPerfil() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();
        builder.setTitle("Confirmación");
        builder.setMessage("¿Seguro que quieres borrar tu perfil?");
        builder.setIcon(R.drawable.ic_delete);
        View dialogView = inflater.inflate(R.layout.delete_profile_dialog, null);
        builder.setView(dialogView);
        final EditText alertPassword = dialogView.findViewById(R.id.alert_password);
        builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(userPassword.equals(md5(alertPassword.getText().toString()))){
                    progressBar.setActivated(true);
                    // Get a new write batch
                    WriteBatch batch = FirebaseFirestore.getInstance().batch();
                    batch.delete(mDocRefUsuarios.document(userId));
                    //batch.update(mDocRefHistoricoUsuarios.document(userId),usuario.toMap());
                    batch.set(mDocRefHistoricoUsuarios.document(userId),usuario.toMap());
                    batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            settings = getSharedPreferences(PREFS_NAME, 0);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.clear();
                            editor.apply();

                            //Volver a la pantalla principal
                            Intent intent = new Intent(UserProfileActivity.this, MapsActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getBaseContext(),"Error en el borrado", Toast.LENGTH_SHORT).show();
                        }
                    });
                    /*mDocRefUsuarios.document(userId)
                            .delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    settings = getSharedPreferences(PREFS_NAME, 0);
                                    SharedPreferences.Editor editor = settings.edit();
                                    editor.clear();
                                    editor.apply();

                                    //Volver a la pantalla principal
                                    Intent intent = new Intent(UserProfileActivity.this, MapsActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getBaseContext(),"Error en el borrado", Toast.LENGTH_SHORT).show();
                                }
                            });*/
                }else
                    Toast.makeText(getBaseContext(),"Contraseña no válida", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();

    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarPerfil();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(AUTO_PARKING, autoParking.isChecked());
        // Commit the edits!
        editor.apply();
    }

    private void cargarPerfil(){
        if(!Objects.equals(settings.getString(USER_ID, SIN_LOGIN), SIN_LOGIN)){
            userId = settings.getString(USER_ID, SIN_LOGIN);

            mDocRefUsuarios.document(userId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    usuario = documentSnapshot.toObject(Usuario.class);
                    if(usuario != null) {
                        if (usuario.getNombre() != null) {
                            userName.setText(usuario.getNombre());
                        }
                        if (usuario.getApellidos() != null) {
                            userSurname.setText(usuario.getApellidos());
                        }
                        if (usuario.getCorreo() != null) {
                            userEmail.setText(usuario.getCorreo());
                        }
                        if (usuario.getNumeroTarjeta() != null) {
                            userNumerCard.setText(usuario.getNumeroTarjeta());
                        }
                        if (usuario.getFechaCaducidadTarjeta() != null) {
                            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                            userDateCard.setText(formatter.format(usuario.getFechaCaducidadTarjeta().toDate()));
                        }
                        if (usuario.getFotoURL() != null) {
                            storageRef.child(usuario.getFotoURL()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Glide.with(getBaseContext())
                                            .load(uri)
                                            .into(userCardImage);
                                }
                            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    titleLayuout.setVisibility(View.VISIBLE);
                                    buttonsLayout.setVisibility(View.VISIBLE);
                                }
                            });
                        }

                        if(usuario.getPassword() != null)
                            userPassword = usuario.getPassword();

                    }else{
                        cerrarSesion();
                        Toast.makeText(getBaseContext(), "Usuario no encontrado", Toast.LENGTH_SHORT).show();
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

    private void cerrarSesion(){
        settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.apply();

        //Volver a la pantalla principal
        Intent intent = new Intent(UserProfileActivity.this, MapsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
