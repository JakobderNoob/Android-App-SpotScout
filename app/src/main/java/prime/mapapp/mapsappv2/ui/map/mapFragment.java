package prime.mapapp.mapsappv2.ui.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.icu.text.SymbolTable;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telephony.CarrierConfigManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.lang.*;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

import io.grpc.internal.AbstractReadableBuffer;
import prime.mapapp.mapsappv2.MainActivity;
import prime.mapapp.mapsappv2.R;
import prime.mapapp.mapsappv2.ui.search.SuchObjekt;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.ContentValues.TAG;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static java.lang.String.valueOf;

public class mapFragment extends Fragment implements OnMapReadyCallback {

    private mapViewModel mapViewModel;
    private GoogleMap mMap;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private int nummer = 0;
    private AlertDialog.Builder dialogBuilder, dialogBuilder2;
    private AlertDialog dialog, dialog2;
    private RatingBar popupRatingbar, popupRatingbar2;
    private Button popupButtonClose, popupButtonMelden, popupButtonBewerten, popupButtonBewertenScreen, popupButtonback;
    private TextView popupTitle, popupInfo, popupbewertunganzahltext;
    private static String liste[];
    private ProgressBar progressBarloading, imageloading;
    private ImageView imageView;
    private Button aufkarteanzeigen;
    public static SuchObjekt suchObjekt;
    private FusedLocationProviderClient mFusedLocationClient,fusedLocationClient;
    private Marker lastpostionmarkerid;
    private LocationRequest locationRequest;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mapViewModel =
                new ViewModelProvider(this).get(mapViewModel.class);
        View root = inflater.inflate(R.layout.activity_maps, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        return root;
    }


    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                System.out.println("KLICK");
                if (marker.equals(lastpostionmarkerid)) {
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(marker.getPosition())
                            .zoom(10).build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                } else {
                    popupwindow(marker);
                    System.out.println("HEREINFO: "+lastpostionmarkerid);
                    System.out.println("HEREINFO: "+marker);
                }
            }
        });
        loadCoords();
        checkzoom();
        updateGPS();
    }

    private void updateGPS(){
        locationRequest = new LocationRequest();
        locationRequest.setInterval(30000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (lastpostionmarkerid != null) {
                        Marker markerremove = lastpostionmarkerid;
                        markerremove.remove();
                        int height = (int) (1920 * 0.065);
                        int width = (int) (1287 * 0.065);
                        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.user_gps);
                        Bitmap b = bitmapdraw.getBitmap();
                        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(location.getLatitude(), location.getLongitude()))
                                .title("Deine Position")
                                .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                                .snippet("Du befindest dich hier"));
                        marker.showInfoWindow();
                        lastpostionmarkerid = marker;
                    } else {
                        int height = (int) (1920 * 0.065);
                        int width = (int) (1287 * 0.065);
                        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.user_gps);
                        Bitmap b = bitmapdraw.getBitmap();
                        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(location.getLatitude(), location.getLongitude()))
                                .title("Deine Position")
                                .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                                .snippet("Du befindest dich hier"));
                        marker.showInfoWindow();
                        lastpostionmarkerid = marker;
                    }

                }
            });
        }else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, 1);
            say("Es ist wichtig, dass wir auf das GPS zugreifen können :D");
        }
    }




    private void checkzoom(){
        if (suchObjekt != null){
            zoomOnMarker();
        } else {
            cameragotoPlayerPostion();
        }
    }

    private void cameragotoPlayerPostion(){
        getuserlocation();
    }

    private void getuserlocation() {
        final LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            say("Bitte aktiviere das GPS");
            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        } else {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    Location location = task.getResult();
                    if (location == null) {
                        requestNewLocationData();
                    } else {
                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(new LatLng(location.getLatitude(),location.getLongitude()))
                                .zoom(10).build();
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        int height = (int)(1920*0.065);
                        int width = (int)(1287*0.065);
                        BitmapDrawable bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.user_gps);
                        Bitmap b = bitmapdraw.getBitmap();
                        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(location.getLatitude(),location.getLongitude()))
                                .title("Deine Position")
                                .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                                .snippet("Du befindest dich hier"));
                        marker.showInfoWindow();
                        lastpostionmarkerid = marker;
                    }
                }
            });
        }


    }



    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {

        // Initializing LocationRequest
        // object with appropriate methods
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        // setting LocationRequest
        // on FusedLocationClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }


    private LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()))
                    .zoom(10).build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            int height = (int)(1920*0.065);
            int width = (int)(1287*0.065);
            BitmapDrawable bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.user_gps);
            Bitmap b = bitmapdraw.getBitmap();
            Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()))
                    .title("Deine Position")
                    .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                    .snippet("Du befindest dich hier"));
            marker.showInfoWindow();
            lastpostionmarkerid = marker;
        }
    };

    private void loadCoords(){
        db = FirebaseFirestore.getInstance();
        db.collection("location")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            nummer = 0;
                            for (DocumentSnapshot document : task.getResult()) {
                                nummer++;
                            }
                        } else {
                            System.out.println("Error getting documents: "+ task.getException());
                        }
                        System.out.println("TEST" + nummer);
                        int i;
                        double x;
                        double y;
                        liste = new String[nummer+1];
                        for (i=1; i<nummer+1; i++){
                            GetDataAndCreateMarker(i);
                        }
                    }
                });
    }

    private void GetDataAndCreateMarker(int localnummer){

        db = FirebaseFirestore.getInstance();

        DocumentReference document = db.collection("location").document(String.valueOf(localnummer));

        document.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    GeoPoint geoPoint = documentSnapshot.getGeoPoint("l");
                    String title = documentSnapshot.getString("title");
                    String info = documentSnapshot.getString("info");
                    switch(documentSnapshot.getString("Kategorie")) {
                        case "Lost-Place":
                            if (documentSnapshot.getString("banned").equals("notbanned")) {
                                addMarker(title, geoPoint.getLatitude(), geoPoint.getLongitude(), info, String.valueOf(localnummer), 0);
                                System.out.println("Lost-Place");
                            }
                            break;
                        case "Aussichtsplattform":
                            if (documentSnapshot.getString("banned").equals("notbanned")) {
                                addMarker(title, geoPoint.getLatitude(), geoPoint.getLongitude(), info, String.valueOf(localnummer),60);
                                System.out.println("Aussichtsplattform");
                            }
                            break;
                        case "Überdacht":
                            if (documentSnapshot.getString("banned").equals("notbanned")) {
                                addMarker(title, geoPoint.getLatitude(), geoPoint.getLongitude(), info, String.valueOf(localnummer),120);
                                System.out.println("Überdacht");
                            }
                            break;
                        case "Mitten in der Natur":
                            if (documentSnapshot.getString("banned").equals("notbanned")) {
                                addMarker(title, geoPoint.getLatitude(), geoPoint.getLongitude(), info, String.valueOf(localnummer),180);
                                System.out.println("Mitten in der Natur");
                            }
                            break;
                        case "Urban":
                            if (documentSnapshot.getString("banned").equals("notbanned")) {
                                addMarker(title, geoPoint.getLatitude(), geoPoint.getLongitude(), info, String.valueOf(localnummer),240);
                                System.out.println("Urban");
                            }
                            break;
                        case "Am Wasser":
                            if (documentSnapshot.getString("banned").equals("notbanned")) {
                                addMarker(title, geoPoint.getLatitude(), geoPoint.getLongitude(), info, String.valueOf(localnummer),300);
                                System.out.println("Am Wasser");
                            }
                            break;
                        default:
                            if (documentSnapshot.getString("banned").equals("notbanned")) {
                                addMarker(title, geoPoint.getLatitude(), geoPoint.getLongitude(), info, String.valueOf(localnummer),360);
                                System.out.println("Fehler");
                            }
                    }
                } else System.out.println("Fehler 1, konnte Ort nicht finden");

            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("Fehler 2: " + e);
                    }
                });
    }

    private void addMarker(String title, double x,double y, String info,String id, int farbe){
        Marker nMap = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(x,y))
                .title(title)
                .icon(BitmapDescriptorFactory.defaultMarker(farbe))
                .snippet(info));
        liste[Integer.parseInt(id)] = nMap.getId();
        System.out.println("LISTE: (nMap.getId()" +liste[Integer.parseInt(id)]+"  ID:  "+id);
        System.out.println("DATENBANK: "+Integer.parseInt(id)+"  nMap ID: "+nMap.getId());


    }

    private void popupwindow(Marker marker){
        dialogBuilder = new AlertDialog.Builder(getContext());
        final View contactPopupView = getLayoutInflater().inflate(R.layout.popupmarker, null);
        popupTitle = (TextView) contactPopupView.findViewById(R.id.popupTitle);
        popupInfo = (TextView) contactPopupView.findViewById(R.id.popupInfo);
        popupRatingbar = (RatingBar) contactPopupView.findViewById(R.id.popupratingBar);
        popupButtonClose = (Button) contactPopupView.findViewById(R.id.popupButtonClose);
        popupButtonMelden = (Button) contactPopupView.findViewById(R.id.popupButtonReport);
        popupButtonBewerten = (Button) contactPopupView.findViewById(R.id.buttonbewerten);
        progressBarloading = (ProgressBar) contactPopupView.findViewById(R.id.progressBarloading);
        popupbewertunganzahltext = (TextView) contactPopupView.findViewById(R.id.popupabstimmungen);
        imageloading = (ProgressBar) contactPopupView.findViewById(R.id.loadingpictureprogressBar);
        imageView = (ImageView) contactPopupView.findViewById(R.id.imageViewPicture);
        aufkarteanzeigen = (Button) contactPopupView.findViewById(R.id.karteanzeigen);

        popupTitle.setText(marker.getTitle());
        popupInfo.setText(marker.getSnippet());

        dialogBuilder.setView(contactPopupView);
        dialog = dialogBuilder.create();
        dialog.show();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int displayWidth = displayMetrics.widthPixels;
        int displayHeight = displayMetrics.heightPixels;
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        int dialogWindowWidth = (int) (displayWidth * 0.98f);
        int dialogWindowHeight = (int) (displayHeight * 0.98f);
        layoutParams.width = dialogWindowWidth;
        layoutParams.height = dialogWindowHeight;
        dialog.getWindow().setAttributes(layoutParams);
        loadBewertung(marker);
        loadBild(marker);

        popupButtonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        popupButtonMelden.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = 0;
                for (i=1; i<(nummer+1); i++ ){
                    if(liste[i].equals(marker.getId())){
                        getreportsnummber(i);
                        popupButtonMelden.setEnabled(false);
                        popupButtonMelden.setText("GEMELDET");
                        break;
                    }
                }

            }
        });

        popupButtonBewerten.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = 0;
                for (i=1; i<(nummer+1); i++ ){
                    if(liste[i].equals(marker.getId())){
                        bewerten(i,marker);
                        break;
                    }
                }


            }
        });

        aufkarteanzeigen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(marker.getPosition())
                        .zoom(14).build();
                //Zoom in and animate the camera.
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });




    }

    private void loadBild(Marker marker){
        int i = 0;
        System.out.println("Info 0 ");
        System.out.println(nummer);
        for (i=1; i<(nummer+1); i++ ){

            if(liste[i].equals(marker.getId())){
                System.out.println("INFO: 1");
                db = FirebaseFirestore.getInstance();

                DocumentReference document = db.collection("location").document(String.valueOf(i));

                document.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        System.out.println("INFO: 2");
                        if (documentSnapshot.exists()) {
                            System.out.println("INFO: 3");
                            if (documentSnapshot.getString("imagelink") !=null) {
                                System.out.println("INFO: 4");
                                storage = FirebaseStorage.getInstance();
                                storageRef = storage.getReference();
                                StorageReference pathReference = storageRef.child("images/" + documentSnapshot.getString("imagelink"));
                                try {
                                    System.out.println("INFO: 5");
                                    final File localFile = File.createTempFile(documentSnapshot.getString("imagelink"),"jpg");
                                    pathReference.getFile(localFile)
                                            .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                    System.out.println("INFO: 6");
                                                    Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                                                    imageView.setVisibility(View.VISIBLE);
                                                    imageloading.setVisibility(View.GONE);
                                                    imageView.setImageBitmap(bitmap);
                                                    //popupInfo.setText(popupInfo.getText() + "\n\n(Nutzer hat folgendes Bild hochgeladen)");
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    System.out.println("INFO: 6.1");
                                                    imageloading.setVisibility(View.GONE);
                                                    popupInfo.setText(popupInfo.getText() + "\n\n(Nutzer hat kein Bild hochgeladen)");
                                                }
                                            });
                                } catch (IOException e) {
                                    System.out.println(e);
                                }
                            } else {
                                System.out.println("INFO: 4.1");
                                imageloading.setVisibility(View.GONE);
                                popupInfo.setText(popupInfo.getText() + "\n\n(Nutzer hat kein Bild hochgeladen)");
                            }
                        } else {
                            say("Ein Fehler ist aufgetreten. Es kann sein, dass dieser Ort nicht mehr existiert.");
                        };

                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                System.out.println("Fehler: " + e);
                            }
                        });


            }
            }


    }

    private void bewerten(int markernummer,Marker marker){
        dialogBuilder2 = new AlertDialog.Builder(getContext());
        final View contactPopupView2 = getLayoutInflater().inflate(R.layout.popupbewerten, null);
        popupButtonBewertenScreen = (Button) contactPopupView2.findViewById(R.id.popupBewerten2);
        popupRatingbar2 = (RatingBar) contactPopupView2.findViewById(R.id.ratingBar2);
        popupButtonback = (Button) contactPopupView2.findViewById(R.id.back);
        dialogBuilder2.setView(contactPopupView2);
        dialog2 = dialogBuilder2.create();
        /*DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int displayWidth = displayMetrics.widthPixels;
        int displayHeight = displayMetrics.heightPixels;
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog2.getWindow().getAttributes());
        int dialogWindowWidth = (int) (displayWidth * 0.8f);
        int dialogWindowHeight = (int) (displayHeight * 0.4f);
        layoutParams.width = dialogWindowWidth;
        layoutParams.height = dialogWindowHeight;
        dialog2.getWindow().setAttributes(layoutParams); */
        dialog2.show();
        popupButtonback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog2.dismiss();
            }
        });

        popupButtonBewertenScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //say("Danke für deine Bewertung");
                popupButtonBewertenScreen.setEnabled(false);
                popupRatingbar2.setEnabled(false);
                sendeholeBewertung(markernummer,marker);
            }
        });


    }

    private void loadBewertung(Marker marker){
        int i = 0;
        for (i=1; i<(nummer+1); i++ ){
            if(liste[i].equals(marker.getId())){
                db = FirebaseFirestore.getInstance();

                DocumentReference document = db.collection("bewertung").document(String.valueOf(i));

                document.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            popupRatingbar.setRating(documentSnapshot.getDouble("bewertung").floatValue());
                            popupbewertunganzahltext.setText("Abstimmungen: "+(int) Math. round(documentSnapshot.getDouble("abstimmungen")));
                            popupRatingbar.setVisibility(View.VISIBLE);
                            popupbewertunganzahltext.setVisibility(View.VISIBLE);
                            progressBarloading.setVisibility(View.GONE);
                        } else {
                            System.out.println("Noch nie");
                            popupRatingbar.setRating(0);
                            popupbewertunganzahltext.setText("Wurde noch nie bewertet");
                            popupbewertunganzahltext.setVisibility(View.VISIBLE);
                            popupRatingbar.setVisibility(View.VISIBLE);
                            progressBarloading.setVisibility(View.GONE);

                        };

                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                System.out.println("Fehler: " + e);
                            }
                        });

                break;
            }
        }
    }

    private void sendeholeBewertung(int markernummer, Marker marker){
        //hole bewertung


        db = FirebaseFirestore.getInstance();

        DocumentReference document = db.collection("bewertung").document(String.valueOf(markernummer));

        document.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Double bewertung = documentSnapshot.getDouble("bewertung");
                    Double abstimmungen = documentSnapshot.getDouble("abstimmungen");
                    System.out.println("Bewertungen: "+bewertung+"   Abstimmungen: "+abstimmungen);
                    berechnesendeBewertung(bewertung,abstimmungen,markernummer,marker);
                } else {
                    System.out.println("Noch nie");
                    berechnesendeBewertung(0,0,markernummer,marker);

                };

            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("Fehler: " + e);
                    }
                });




    }

    private void berechnesendeBewertung(double bewertung, double abstimmungen, int markernummer, Marker marker){
        double neueBewertung;
        if (bewertung == 0 && abstimmungen == 0){
            sendBewertung(popupRatingbar2.getRating(), 1, markernummer, marker);
        } else {
            neueBewertung = (((bewertung * abstimmungen) + (popupRatingbar2.getRating()) )/ (abstimmungen + 1));
            System.out.println(neueBewertung);
            sendBewertung(neueBewertung, (abstimmungen+1), markernummer, marker);
        }
    }

    private void sendBewertung(double bewertung, double abstimmungen, int markernummer, Marker marker){
        db = FirebaseFirestore.getInstance();
        Map<String, Object> data = new HashMap<>();
        data.put("bewertung", bewertung);
        data.put("abstimmungen", abstimmungen);

        db.collection("bewertung").document(String.valueOf(markernummer))
                .set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        dialog2.dismiss();
                        loadBewertung(marker);
                        popupButtonBewerten.setEnabled(false);
                        say("Erfolgreich gesendet!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        say("Bewertung konnte nicht gesendet werden");
                        popupButtonBewertenScreen.setEnabled(true);
                        popupRatingbar2.setEnabled(true);
                    }
                });

    }
    private void say(String text2) {

        Context context = getContext();
        CharSequence text = text2;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();


    }

    private void report(int datenbanknummer, double anzahl){

        Map<String, Object> data = new HashMap<>();
        System.out.println("anzahl: "+anzahl+"  Anzahl+1: "+anzahl+1);
        data.put("anzahl", anzahl+1);

        db.collection("reports").document(String.valueOf(datenbanknummer))
                  .set(data)
                  .addOnSuccessListener(new OnSuccessListener<Void>() {
                      @Override
                      public void onSuccess(Void aVoid) {
                          say("Meldung gesendet!");
                      }
                  })
                  .addOnFailureListener(new OnFailureListener() {
                      @Override
                      public void onFailure(@NonNull Exception e) {
                          say("Konnte den Meldung nicht Senden!");
                      }
                  });
    }

    private void getreportsnummber(int datenbanknummer){
        db = FirebaseFirestore.getInstance();
        System.out.println("Datenbanknummer: "+datenbanknummer);
        DocumentReference document = db.collection("reports").document(String.valueOf(datenbanknummer));

        document.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                System.out.println("Here1");
                if (documentSnapshot.exists()) {
                    System.out.println("Datenbank sagt, dass die Anzahl folgendes ist: "+Double.toString(documentSnapshot.getDouble("anzahl")));
                    report(datenbanknummer,documentSnapshot.getDouble("anzahl"));
                } else {
                    System.out.println("Returndata wurde 0 gesetzt");
                    report(datenbanknummer,0);
                }

            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        say("Fehler: "+e);
                    }
                });
    }

    public void zoomOnMarker(){

        db = FirebaseFirestore.getInstance();
        DocumentReference document = db.collection("location").document(suchObjekt.getId());

        document.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                System.out.println("Here1");
                if (documentSnapshot.exists()) {
                    GeoPoint geoPoint = documentSnapshot.getGeoPoint("l");
                    double lat = geoPoint.getLatitude();
                    double lng = geoPoint.getLongitude ();
                    LatLng latLng = new LatLng(lat, lng);
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(latLng)
                            .zoom(14).build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    suchObjekt = null;
                } else {
                    suchObjekt = null;
                }

            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        say("Fehler: "+e);
                        suchObjekt = null;
                    }
                });
    }








}