package com.example.parkingenable;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ParkingActivity extends AppCompatActivity {

    private static final String TAG = "ParkingActivity";

    TextView streetName, textLike, textDislike;
    LinearLayout cardLike, cardDislike, cardNavigation, cardParking;
    String idPlazaParking;
    Boolean voted=false;
    Button buttonErrorParking;
    PlazaParking plazaDB;
    //plaza que está ocupando el usuario registrado en este momento
    private String plazaOcupadaUsuarioID;

    //popup parking not exist
    Dialog epicDialog;
    Button buttonSendLocation, buttonBackPopUp;
    ImageView buttonClosePopupImage;

    //Database
    private CollectionReference mDocRefPlazas = FirebaseFirestore.getInstance().collection("plazas");
    private CollectionReference mDocRefErrorParking = FirebaseFirestore.getInstance().collection("parkingNotExist");
    private CollectionReference mDocRefUsuarios = FirebaseFirestore.getInstance().collection("usuarios");

    //User's preferences
    private SharedPreferences settings;
    private String userId;
    public static final String PREFS_NAME = "MyPrefsFile";
    public static final String USER_ID = "userID";
    public static final String SIN_LOGIN = "sinLogin";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking);

        //Recover the extra
        Intent intent = getIntent();
        idPlazaParking = intent.getStringExtra("IDMarker");
        plazaOcupadaUsuarioID = intent.getStringExtra("plazaOcupadaUsuarioID");

        //User's preferences
        settings = getSharedPreferences(PREFS_NAME, 0);
        userId = settings.getString(USER_ID, SIN_LOGIN);

        //Initialization

        //popup
        epicDialog = new Dialog(this);

        //Cards
        cardLike = findViewById(R.id.card_like);
        cardDislike = findViewById(R.id.card_dislike);
        cardNavigation = findViewById(R.id.card_navigation);
        cardParking = findViewById(R.id.card_parking);

        //Textview
        textLike = findViewById(R.id.textview_like);
        textDislike = findViewById(R.id.textview_dislike);

        //Call to DB
        streetName = findViewById(R.id.streetName_textGrid);
        updateScreen();

        //LIKE!
        cardLike.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                if(idPlazaParking != null){
                    if(!voted){
                        voted=true;
                        cardDislike.setClickable(false);
                        cardLike.setBackground(getDrawable(R.drawable.bg_item_selected));

                        DocumentReference docRef = mDocRefPlazas.document(idPlazaParking);
                        // Atomically increment the population of the city by 1.
                        docRef.update("like", FieldValue.increment(1));
                        updateScreen();
                    }else{
                        voted=false;
                        cardDislike.setClickable(true);
                        cardLike.setBackground(getDrawable(R.drawable.bg_item));

                        DocumentReference docRef = mDocRefPlazas.document(idPlazaParking);
                        docRef.update("like", FieldValue.increment(-1));
                        updateScreen();
                    }
                }
            }
        });

        //DISLIKE!!
        cardDislike.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                if(idPlazaParking != null){

                    if(!voted){
                        voted=true;
                        cardLike.setClickable(false);
                        cardDislike.setBackground(getDrawable(R.drawable.bg_item_selected));

                        DocumentReference docRef = mDocRefPlazas.document(idPlazaParking);
                        // Atomically increment the population of the city by 1.
                        docRef.update("dislike", FieldValue.increment(1));
                        updateScreen();
                    }else{
                        voted=false;
                        cardLike.setClickable(true);
                        cardDislike.setBackground(getDrawable(R.drawable.bg_item));

                        DocumentReference docRef = mDocRefPlazas.document(idPlazaParking);
                        docRef.update("dislike", FieldValue.increment(-1));
                        updateScreen();
                    }
                }
            }
        });

        //Google maps Navigation!!
        cardNavigation.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {

                //carNavigation.setBackground(getDrawable(R.drawable.bg_item_selected));
                if(plazaDB!=null){
                    Uri gmmIntentUri = Uri.parse("geo:"+plazaDB.getLatitude()+","+plazaDB.getLongitude() +"7?q="+plazaDB.getLatitude()+","+ plazaDB.getLongitude()+"("+streetName.getText()+")");
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                }
            }
        });

        //Registrar aparcamiento
        cardParking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mostrarAlertaRegistroAparcamiento();
            }
        });

        //buttons
        Button backButton = findViewById(R.id.button_back);
        buttonErrorParking = findViewById(R.id.button_errorParking);

        //Close Activity
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //Send No Exist parking!
        buttonErrorParking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupNewParking();
            }
        });
    }

    private void updateScreen() {
        if(idPlazaParking != null){
            DocumentReference docRef = mDocRefPlazas.document(idPlazaParking);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){

                        DocumentSnapshot document = task.getResult();
                        plazaDB = document.toObject(PlazaParking.class);

                        streetName.setText(plazaDB.getCalle());
                        textLike.setText("Me gusta: "+ plazaDB.getLike());
                        textDislike.setText("No me gusta: "+ plazaDB.getDislike());
                        //solo se muestra el boton de registro si la plaza está ocupada pero sin estar registrada por ningún usuario
                        if(!userId.equals(SIN_LOGIN) && plazaDB.getUsuarioOcupando() == null && !plazaDB.isLibre())
                            cardParking.setVisibility(View.VISIBLE);
                        else
                            cardParking.setVisibility(View.GONE);
                    }else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });
        }
    }

    /**
     * Method to show the custom pop up which help the user to send the location
     * for a new parking that we dont have it
     */
    private void showPopupNewParking() {
        epicDialog.setContentView(R.layout.popup_parkingnotexist);
        buttonClosePopupImage = epicDialog.findViewById(R.id.closepopup_crossimage);
        buttonSendLocation = epicDialog.findViewById(R.id.button_send_error);
        buttonBackPopUp = epicDialog.findViewById(R.id.button_close_popup);

        //With this button the user close the popup and doesn't send us the location
        buttonBackPopUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                epicDialog.dismiss();
            }
        });

        //With this button the user close the popup and doesn't send us the location
        buttonClosePopupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                epicDialog.dismiss();
            }
        });

        epicDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        epicDialog.show();

        //When the user push to agree they send us to our DB his location
        // and we save these data for create a new Parking marker later
        buttonSendLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //LatLng coordNewParking =  new LatLng(mLastKnowLocation.getLatitude(), mLastKnowLocation.getLongitude());
                Map<String, Object> parkingNotExist = new HashMap<>();
                parkingNotExist.put("idParking", idPlazaParking);
                int id = 1;
                if(idPlazaParking != null){
                    mDocRefErrorParking.add(parkingNotExist).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            epicDialog.dismiss();
                            Log.d(TAG, "Document added with ID: " + documentReference.getId());
                            Toast.makeText(ParkingActivity.this, "Gracias por avisar", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ParkingActivity.this, "Error en el envío", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Error en el envío");
                        }
                    });

                }else{
                    Toast.makeText(ParkingActivity.this, "Sin coordenadas que recoger", Toast.LENGTH_SHORT).show();
                    return;

                }
            }
        });
    }

    private void mostrarAlertaRegistroAparcamiento(){
        if(plazaDB != null){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            // Get the layout inflater
            LayoutInflater inflater = getLayoutInflater();
            builder.setTitle("Registrar aparcamiento");
            builder.setMessage("¿Fijar esta plaza como tu aparcamiento?");
            builder.setIcon(R.drawable.parking_register);
            builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    HashMap<String, Object> usuario = new HashMap<>();
                    usuario.put("plazaOcupada", Objects.requireNonNull(idPlazaParking));

                    HashMap<String, Object> plaza = new HashMap<>();
                    plaza.put("usuarioOcupando", userId);

                    HashMap<String, Object> plazaOcupadaAntigua = new HashMap<>();
                    plazaOcupadaAntigua.put("usuarioOcupando", null);
                    // Get a new write batch
                    WriteBatch batch = FirebaseFirestore.getInstance().batch();
                    batch.update(mDocRefUsuarios.document(userId),usuario);
                    if(plazaOcupadaUsuarioID != null)
                        batch.update(mDocRefPlazas.document(plazaOcupadaUsuarioID), plazaOcupadaAntigua);
                    batch.update(mDocRefPlazas.document(idPlazaParking), plaza);
                    batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getBaseContext(),"Aparcamiento registrado", Toast.LENGTH_SHORT).show();
                            updateScreen();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getBaseContext(),"Error al registrar aparcamiento", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            builder.show();
        }else
            Toast.makeText(this, "Error al registrar aparcamiento", Toast.LENGTH_SHORT).show();

    }

}
