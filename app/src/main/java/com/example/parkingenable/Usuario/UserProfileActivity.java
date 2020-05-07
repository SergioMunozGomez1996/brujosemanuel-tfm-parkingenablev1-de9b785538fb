package com.example.parkingenable.Usuario;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.parkingenable.MapsActivity;
import com.example.parkingenable.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

public class UserProfileActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "MyPrefsFile";
    public static final String USER_ID = "userID";
    public static final String SIN_LOGIN = "sinLogin";
    public static final String AUTO_PARKING = "autoParking";


    //Database
    private CollectionReference mDocRefUsuarios = FirebaseFirestore.getInstance().collection("usuarios");

    //Creamos una instancia de FirebaseStorage
    StorageReference storageRef = FirebaseStorage.getInstance().getReference();

    private TextView userName;
    private TextView userEmail;
    private TextView userNumerCard;
    private ImageView userCardImage;
    private CheckBox autoParking;

    private String userId;

    private Context mContext;

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
        userEmail = findViewById(R.id.correo_detail);
        userNumerCard = findViewById(R.id.numero_tarjeta_detail);
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
                settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.clear();
                editor.apply();

                //Volver a la pantalla principal
                Intent intent = new Intent(UserProfileActivity.this, MapsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        /*editarButton.setOnClickListener(v -> {
            MyProfileEditFragment myProfileEditFragment = new MyProfileEditFragment();
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.content_main, myProfileEditFragment).addToBackStack(null).commit();
        });

        borrarButton.setOnClickListener(v -> {
            confirmarBorrarPerfil();
        });*/

        settings = getSharedPreferences(PREFS_NAME, 0);
        autoParking.setChecked(settings.getBoolean(AUTO_PARKING, false));
        if(!Objects.equals(settings.getString(USER_ID, SIN_LOGIN), SIN_LOGIN)){
            userId = settings.getString(USER_ID, SIN_LOGIN);

            mDocRefUsuarios.document(userId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    progressBar.setVisibility(View.INVISIBLE);
                    Usuario usuario = documentSnapshot.toObject(Usuario.class);
                    if(usuario != null) {
                        if (usuario.getName() != null) {
                            userName.setText(usuario.getName());
                        }
                        if (usuario.getEmail() != null) {
                            userEmail.setText(usuario.getEmail());
                        }
                        if (usuario.getCardNumber() != null) {
                            userNumerCard.setText(usuario.getCardNumber());
                        }
                        if (usuario.getCardURL() != null) {
                            storageRef.child(usuario.getCardURL()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Glide.with(getBaseContext())
                                            .load(uri)
                                            .into(userCardImage);
                                }
                            });
                        }
                    }

                    titleLayuout.setVisibility(View.VISIBLE);
                    buttonsLayout.setVisibility(View.VISIBLE);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getBaseContext(), "Error al cargar el perfil", Toast.LENGTH_SHORT).show();
                }
            });
        }
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
}
