package com.example.parkingenable;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.parkingenable.Usuario.LoginActivity;
import com.example.parkingenable.Usuario.UserProfileActivity;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.android.clustering.ClusterManager;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private PlacesClient placesClient;
    private List<AutocompletePrediction> predictionList;

    private Location mLastKnowLocation;
    private LocationCallback locationCallback;

    //constants

    //popup new parking
    Dialog epicDialog;
    Button buttonSendLocation;
    ImageView buttonClosePopupImage;

    //environment
    private static final String TAG = "MainActivity";

    //Strings
    private static String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;

    //Another variables
    private final int REQUEST_PERMISSION_LOCATION = 1;
    private boolean mLocationPermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final float DEFAULT_ZOOM = 17f;

    //Widgets
    private MaterialSearchBar materialSearchBar;
    BottomNavigationView bottomNavigationView;
    LinearLayout bannerQuestonLocation;
    FloatingActionButton ubicationButton;
    FloatingActionButton newParkingButton;
    FloatingActionButton loginButton;

    //private WeakHashMap mMarkers = new WeakHashMap<Integer, Marker>();

    //Database
    private CollectionReference mDocRefPlazas = FirebaseFirestore.getInstance().collection("plazas");
    private CollectionReference mDocRefNewParking = FirebaseFirestore.getInstance().collection("ParkingSuggestions");
    public static final String CITY_KEY="ciudad";

    //User's preferences
    public static final String PREFS_NAME = "MyPrefsFile";
    public static final String USER_ID = "userID";
    public static final String SIN_LOGIN = "sinLogin";

    //cluster
    private ClusterManager<MyItem>clusterManager;
    private List<MyItem>items =  new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Request Permission -- habría que espera a cargar el mapa hasta que sepamos si tenemos los permisos o no
        //showPhoneLocationPermission();
        setContentView(R.layout.activity_maps);

        //SearchBar
        materialSearchBar = findViewById(R.id.searchBar);
        bannerQuestonLocation = findViewById(R.id.linearLayout_bannerQuestionsLoca);
        //bannerQuestonLocation.setVisibility(View.INVISIBLE);

        //Navigation Menu
        ubicationButton = findViewById(R.id.fab_ubication);
        newParkingButton = findViewById(R.id.fab_newParking);
        loginButton = findViewById(R.id.fab_login);
        //bottomNavigationView = findViewById(R.id.bottom_navigation_menu);
        //bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

        //popup
        epicDialog = new Dialog(this);

        //Check the GPS permission
        getLocationPermission();

        //clickListeners

        bannerQuestonLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocationPermission();
            }
        });

        ubicationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDeviceLocation();
            }
        });
        newParkingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupNewParking();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        if(!Objects.equals(settings.getString(USER_ID, SIN_LOGIN), SIN_LOGIN)){
            loginButton.setIcon(R.drawable.ic_person);
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent  = new Intent(MapsActivity.this, UserProfileActivity.class);
                    startActivity(intent);
                }
            });
        }else {
            loginButton.setIcon(R.drawable.ic_person_add);
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent  = new Intent(MapsActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    /**
     * Listener about the bottom navigation menu
     */
    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

            switch (menuItem.getItemId()){
                case R.id.nav_search:
                    MaterialSearchBar searchBar = findViewById(R.id.searchBar);
                    if(searchBar.isSuggestionsVisible()){
                        searchBar.hideSuggestionsList();
                    }

                    if(searchBar.getVisibility() == View.INVISIBLE){
                        searchBar.setVisibility(View.VISIBLE);
                    }else{
                        searchBar.setVisibility(View.INVISIBLE);
                    }
                    break;
                case R.id.nav_ubication:
                    getDeviceLocation();
                    break;
                case R.id.nav_newParking:
                    showPopupNewParking();
                    break;
            }
            return true;
        }
    };


    /**
     * Method to show the custom pop up which help the user to send the location
     * for a new parking that we dont have it
     */
    private void showPopupNewParking() {
        epicDialog.setContentView(R.layout.popup_newparking);
        buttonClosePopupImage = epicDialog.findViewById(R.id.closepopup_crossimage);
        buttonSendLocation = epicDialog.findViewById(R.id.button_popup);

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
                LatLng coordNewParking =  new LatLng(mLastKnowLocation.getLatitude(), mLastKnowLocation.getLongitude());

                if(coordNewParking != null){
                    mDocRefNewParking.add(coordNewParking).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            epicDialog.dismiss();
                            Log.d(TAG, "Document added with ID: " + documentReference.getId());
                            Toast.makeText(MapsActivity.this, "Nueva sugerencia almacenada", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "Document not added");
                        }
                    });

                }else{
                    Toast.makeText(MapsActivity.this, "Sin coordenadas que recoger", Toast.LENGTH_SHORT).show();
                    return;

                }
            }
        });
    }

    /**
     * Function to get from the BD the markers and paint at map
     */
    protected void paintMarkers(){
        super.onStart();
        mDocRefPlazas.whereEqualTo(CITY_KEY, "Elche").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                if(e!=null){
                    Log.w(TAG, "Listen failed, " , e);
                }

                mMap.clear();
                for(QueryDocumentSnapshot plaza: value){
                    if(plaza.get("calle")!=null){
                        Integer plazaID = Integer.parseInt(plaza.getId());
                        PlazaParkingDB plazaDB = plaza.toObject(PlazaParkingDB.class);

                        String estado;
                        if(plazaDB.isLibre()){
                            estado= "Libre";
                        }else{
                            estado= "Ocupada";
                        }

                        LatLng coordenadas = new LatLng((float)plazaDB.getLatitude(), (float)plazaDB.getLongitude());
                        MarkerOptions options = new MarkerOptions().position(coordenadas).title(plazaDB.getCalle()).snippet(estado);
                        Marker markerParking;

                        //Para el clustering de marcadores 09/07/2019
                        //items.add(new MyItem(coordenadas, plazaDB.getCalle(), null));
                        //clusterManager.addItems(items);
                        //clusterManager.cluster();
                        if(plazaDB.isLibre()){
                            markerParking = mMap.addMarker(options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                            markerParking.setTag(plazaID);
                        }else{
                            markerParking = mMap.addMarker(options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                            markerParking.setTag(plazaID);
                        }
                    }
                }
            }
        });



        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                return false;
            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                String id = marker.getTag().toString();
                Intent intent  = new Intent(MapsActivity.this, ParkingActivity.class);
                intent.putExtra("IDMarker", id);
                startActivity(intent);
                Toast.makeText(MapsActivity.this, "has pulsado un marcador con ID: " + id, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void init(){
        Log.d(TAG, "Init:initializing");

        ///////////*****************************************PRUEBA DE MATERIAL BAR***************************************//////////////
        Places.initialize(MapsActivity.this, "AIzaSyAXs_YDooeUnp0IJ305YGqGKii5BiXdS0s");
        placesClient = Places.createClient(this);
        final AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {

            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                startSearch(text.toString(), true, null, true);
            }

            @Override
            public void onButtonClicked(int buttonCode) {
                if(buttonCode == MaterialSearchBar.BUTTON_NAVIGATION){
                    //opening or closing a navigation drawe
                }else if( buttonCode == MaterialSearchBar.BUTTON_BACK){
                    materialSearchBar.disableSearch();
                }
                //else if(buttonCode == Keyboard.)
            }
        });

        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence newText, int start, int before, int count) {
                FindAutocompletePredictionsRequest predictionsRequest = FindAutocompletePredictionsRequest.builder()
                        .setCountry("es")
                        .setLocationBias(RectangularBounds.newInstance( new LatLng(38.22890839082041, -0.783578619027594), new LatLng(38.32271799021481, -0.5893144843914797)))
                        .setTypeFilter(TypeFilter.ESTABLISHMENT)
                        .setTypeFilter(TypeFilter.ADDRESS)
                        .setSessionToken(token)
                        .setQuery(newText.toString())
                        .build();

                placesClient.findAutocompletePredictions(predictionsRequest).addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {
                        if(task.isSuccessful()){
                            FindAutocompletePredictionsResponse predictionsResponse = task.getResult();
                            if(predictionsResponse != null){
                                predictionList = predictionsResponse.getAutocompletePredictions();
                                List <String> suggestionList = new ArrayList<>();
                                for(int i = 0; i<predictionList.size(); i++){
                                    AutocompletePrediction prediction = predictionList.get(i);
                                    suggestionList.add(prediction.getFullText(null).toString());
                                }
                                materialSearchBar.updateLastSuggestions(suggestionList);
                                if(!materialSearchBar.isSuggestionsVisible()){
                                    materialSearchBar.showSuggestionsList();
                                }
                            }
                        }else{
                            Log.i(TAG, "prediction fetching unsuccessful"+ task.getResult());
                        }
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().equals("")  || s.toString()==null ){
                    materialSearchBar.clearSuggestions();
                    materialSearchBar.hideSuggestionsList();
                    if(materialSearchBar.isSuggestionsVisible()){materialSearchBar.hideSuggestionsList();}
                }
                Log.i(TAG, "After text changed: " + s.toString());
            }
        });

        materialSearchBar.setSuggestionsClickListener(new SuggestionsAdapter.OnItemViewClickListener() {
            @Override
            public void OnItemClickListener(int position, View v) {
                if(position>= predictionList.size()){
                    return;
                }
                AutocompletePrediction selectedPrediction = predictionList.get(position);
                String suggestion = materialSearchBar.getLastSuggestions().get(position).toString();
                materialSearchBar.setText(suggestion);

                List<Address> addressList = null;

                if(suggestion != null || !suggestion.equals("")){
                    Geocoder geocoder = new Geocoder(MapsActivity.this);
                    try{
                        addressList = geocoder.getFromLocationName(suggestion,1);
                    }catch(IOException e){
                        Log.e(TAG, "Geolocate: IOException: " + e.getMessage());
                    }
                    assert addressList != null;
                    Address address = addressList.get(0);
                    Log.d(TAG, "Geolocate: found a location: " + address.toString());

                    //hideSoftKeyboard(getApplicationContext(), mSearchText);
                    materialSearchBar.clearSuggestions();
                    moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM, null);

                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        materialSearchBar.clearSuggestions();
                    }
                }, 1000);

                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if(imm != null){
                    imm.hideSoftInputFromWindow(materialSearchBar.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                }
                String placeID = selectedPrediction.getPlaceId();
                List<Place.Field> placeFields = Arrays.asList(Place.Field.LAT_LNG);
                final FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeID, placeFields).build();
               placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                    @Override
                    public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                        Place place = fetchPlaceResponse.getPlace();
                        Log.i(TAG, "Place found: " + place.getName());
                        LatLng latLngOfPlace = place.getLatLng();
                        if(latLngOfPlace != null){
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngOfPlace, DEFAULT_ZOOM ));
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if(e instanceof ApiException){
                            ApiException apiException = (ApiException) e;
                            apiException.printStackTrace();
                            int statusCode = apiException.getStatusCode();
                            Log.i(TAG, "Place not found: " + e.getMessage());
                            Log.i(TAG, "Status code: " + statusCode);
                        }
                    }
                });
            }

            @Override
            public void OnItemDeleteListener(int position, View v) {

            }
        });
    }

    private void OptionFloatingSatellite(FloatingActionButton fab2) {
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeTypeMap("satellite");
            }
        });
    }

    private void OptionFloatingNormal(FloatingActionButton fab1) {
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeTypeMap("normal");
            }
        });
    }


    private void changeTypeMap(String e) {

        switch (e){
            case "normal":
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case "satellite":
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
        }
    }

    private void initMap(){

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Para el clustering de marcadores 09/07/2019
        //clusterManager = new ClusterManager<MyItem>(this, mMap);
        //mMap.setOnCameraIdleListener(clusterManager);
        //mMap.setOnMarkerClickListener(clusterManager);


        //esto no hace falta para el clustering
        /*mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                items.add(new MyItem(latLng));
                clusterManager.addItems(items);
                clusterManager.cluster();
            }
        });
*/
        paintMarkers();
        //insertar mensajer de debg  ************

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
            mMap.setMyLocationEnabled(true);
        }

        if (mLocationPermissionGranted) {
            getDeviceLocation();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }

        //Initialize the floating menu
        FloatingActionButton fab1 = findViewById(R.id.fab1);
        FloatingActionButton fab2 =  findViewById(R.id.fab2);
        OptionFloatingNormal(fab1);
        OptionFloatingSatellite(fab2);

        init();
    }

    /**
     * Method to get the location permission that we need to use at map
     */
    private void getLocationPermission(){
        //the permissions we are going to demand to the user
        String[] permisos = {FINE_LOCATION};

        //If we already have the permissions
        if(ContextCompat.checkSelfPermission(this, FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            mLocationPermissionGranted = true;
            bannerQuestonLocation.setVisibility(View.INVISIBLE); //Banner on
            initMap();
        }else{
           // bottomNavigationView.setVisibility(View.INVISIBLE);
            bannerQuestonLocation.setVisibility(View.VISIBLE); //Banner off
            ActivityCompat.requestPermissions(this, permisos, REQUEST_PERMISSION_LOCATION);
        }
    }

    private void getDeviceLocation(){
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try{
            if(mLocationPermissionGranted){

                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            //insertar mensajer de debg  ************
                            mLastKnowLocation = (Location) task.getResult();
                            Location currentLocation = (Location) task.getResult();
                            if(mLastKnowLocation!= null){
                                moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM, null);
                            }else{
                                LocationRequest locationRequest = LocationRequest.create();
                                locationRequest.setInterval(10000);
                                locationRequest.setFastestInterval(5000);
                                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                                locationCallback = new LocationCallback(){
                                    @Override
                                    public void onLocationResult(LocationResult locationResult) {
                                        super.onLocationResult(locationResult);
                                        if(locationResult == null){
                                            return;
                                        }
                                        mLastKnowLocation = locationResult.getLastLocation();
                                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnowLocation.getLatitude(), mLastKnowLocation.getLongitude()), DEFAULT_ZOOM));

                                    }
                                };
                                mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
                            }
                        }
                        else{
                            //insertar mensajer de debg  ************
                            Toast.makeText(MapsActivity.this, "Imposible obtener la locacilación actual", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }catch (SecurityException e){
            //insertar mensajer de debg  ************
        }
    }

    private void moveCamera(LatLng latLng, float zoom, String dir){
        //insertar mensajer de debg  ************

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(MapsActivity.this));
        if (dir!=null) {
            dir = "Destino";
            MarkerOptions options = new MarkerOptions().position(latLng).title(dir);

            mMap.addMarker(options);
        }
    }
}
