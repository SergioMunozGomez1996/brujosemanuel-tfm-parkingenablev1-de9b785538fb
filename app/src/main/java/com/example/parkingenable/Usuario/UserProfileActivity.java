package com.example.parkingenable.Usuario;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.example.parkingenable.MapsActivity;
import com.example.parkingenable.R;
import com.example.parkingenable.TransitionsReceiver;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.DetectedActivity;
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

import org.altbeacon.beacon.BeaconManager;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class UserProfileActivity extends AppCompatActivity {

    //check for devices with Android 10 (29+).
    private boolean runningQOrLater =
            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q;
    /* Id to identify Activity Recognition permission request. */
    private static final int PERMISSION_REQUEST_ACTIVITY_RECOGNITION = 45;
    /* Id to identify Bluetooth enable request. */
    private int REQUEST_ENABLE_BT = 0;


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

        //listener para controlar el estado del checkbox y así controlar los permisos de actividad, estado bluetooth y reconocimiento de actividad
        autoParking.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                    checkEnableOrDisableActivityRecognition();
                else
                    removeActivityUpdates();
            }
        });

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
        if(autoParking.isChecked())
            checkEnableOrDisableActivityRecognition();
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
        deleteAppToken();
    }

    private void deleteAppToken(){
        WriteBatch batch = FirebaseFirestore.getInstance().batch();
        HashMap<String, Object> usuario = new HashMap<>();
        usuario.put("tokenFCM", null);
        batch.update(mDocRefUsuarios.document(userId), usuario);
        batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //Volver a la pantalla principal
                Intent intent = new Intent(UserProfileActivity.this, MapsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getBaseContext(),"Error al borrar el tokenFCM", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(UserProfileActivity.this, MapsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

            }
        });
    }

    /**
     * On devices Android 10 and beyond (29+), you need to ask for the ACTIVITY_RECOGNITION via the
     * run-time permissions.
     */
    private boolean activityRecognitionPermissionApproved() {

        //Review permission check for 29+.
        if (runningQOrLater) {

            return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACTIVITY_RECOGNITION
            );
        } else {
            return true;
        }
    }
    public void checkEnableOrDisableActivityRecognition() {

        //Enable/Disable activity tracking and ask for permissions if needed.
        if (activityRecognitionPermissionApproved()) {
            verifyBluetooth();

        } else {
            /*Intent startIntent = new Intent(this, PermissionRationalActivity.class);
            startActivity(startIntent);*/
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                        PERMISSION_REQUEST_ACTIVITY_RECOGNITION);
            }
        }
    }

    private void verifyBluetooth() {

        try {
            if (!BeaconManager.getInstanceForApplication(this).checkAvailability()) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Bluetooth no activado");
                builder.setMessage("Por favor, habilita el bluetooth para acceder a esta funcionalidad");
                builder.setPositiveButton("Activar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    }
                });
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        Toast.makeText(getBaseContext(), "Bluetooth necesario", Toast.LENGTH_SHORT).show();
                        autoParking.setChecked(false);
                    }
                });
                builder.show();
            }else
                requestActivityUpdates();
        } catch (RuntimeException e) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Bluetooth LE no disponible");
            builder.setMessage("Tu dispositivo no admite bluetooth");
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    autoParking.setChecked(false);
                }
            });
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    autoParking.setChecked(false);
                }

            });
            builder.show();

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            requestActivityUpdates();
        }else{
            Toast.makeText(getBaseContext(), "Bluetooth necesario", Toast.LENGTH_SHORT).show();
            autoParking.setChecked(false);
        }
    }

    public void requestActivityUpdates() {

        //si ya hay una peticion para la actualización de actividad no se lanza otra
        if(!settings.getBoolean("ActivityRecognition", false)){
            // myPendingIntent is the instance of PendingIntent where the app receives callbacks.
            Task<Void> task = ActivityRecognition.getClient(this)
                    .requestActivityTransitionUpdates(getActivityTransitionRequest(), getActivityDetectionPendingIntent());

            task.addOnSuccessListener(
                    new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            // Handle success
                            Toast.makeText(getBaseContext(),
                                    "Recogiendo actividad",
                                    Toast.LENGTH_SHORT)
                                    .show();
                            settings.edit().putBoolean("ActivityRecognition", true).apply();
                        }
                    }
            );

            task.addOnFailureListener(
                    new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            // Handle error
                            Toast.makeText(getBaseContext(),
                                    "Error al recoger actividad",
                                    Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
            );
        }
    }

    public void removeActivityUpdates() {
        Task<Void> task = ActivityRecognition.getClient(this)
                .removeActivityTransitionUpdates(getActivityDetectionPendingIntent());

        task.addOnSuccessListener(
                new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        // Handle success
                        Toast.makeText(getBaseContext(),
                                "Deshabilitando actividad",
                                Toast.LENGTH_SHORT)
                                .show();
                        settings.edit().putBoolean("ActivityRecognition", false).apply();
                    }
                }
        );

        task.addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        // Handle error
                        Toast.makeText(getBaseContext(),
                                "Error al dehabilitar reconocimiento de actividad",
                                Toast.LENGTH_SHORT)
                                .show();
                        settings.edit().putBoolean("ActivityRecognition", false).apply();
                    }
                }
        );
    }

    private ActivityTransitionRequest getActivityTransitionRequest(){
        List<ActivityTransition> transitions = new ArrayList<>();

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.IN_VEHICLE)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build());
        //El resto de transiciones están solo para hacer pruebas, las cuales se deberán quitar cuando la app esté en producción
        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.STILL)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build());
        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.STILL)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build());

        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.ON_FOOT)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build());
        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.ON_FOOT)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build());
        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.WALKING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.WALKING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build());

        return new ActivityTransitionRequest(transitions);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_ACTIVITY_RECOGNITION) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted. Continue the action or workflow
                // in your app.
                verifyBluetooth();
            }  else {
                // Explain to the user that the feature is unavailable because
                // the features requires a permission that the user has denied.
                // At the same time, respect the user's decision. Don't link to
                // system settings in an effort to convince the user to change
                // their decision.
                Toast.makeText(getBaseContext(), "Reconocimiento de actividad necesaria", Toast.LENGTH_SHORT).show();
                autoParking.setChecked(false);
                removeActivityUpdates();
            }
        }
    }

    /**
     * Gets a PendingIntent to be sent for each activity detection.
     */
    private PendingIntent getActivityDetectionPendingIntent() {
        Intent intent = new Intent(getApplicationContext(), TransitionsReceiver.class);
        return PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
