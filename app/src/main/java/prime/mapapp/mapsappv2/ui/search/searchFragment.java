package prime.mapapp.mapsappv2.ui.search;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import androidx.navigation.Navigation;

import java.util.ArrayList;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.imperiumlabs.geofirestore.GeoFirestore;
import org.imperiumlabs.geofirestore.GeoQuery;
import org.imperiumlabs.geofirestore.listeners.GeoQueryEventListener;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import prime.mapapp.mapsappv2.R;
import prime.mapapp.mapsappv2.ui.map.mapFragment;

import static android.graphics.Typeface.DEFAULT;
import static android.graphics.Typeface.DEFAULT_BOLD;

public class searchFragment extends Fragment{


    private View view;
    private FusedLocationProviderClient mFusedLocationClient;
    private int radius=0;
    private FirebaseFirestore db;
    private SearchView searchView;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private Button filteropenclose,amwasserFilter,aussichtsplattformFilter,lostplaceFilter,überdachtFilter, mittenindernaturFilter,urbanFilter,alleFilter,nachentfernungFilter,nachbewertungFilter;
    private LinearLayout filterTabsLayout, filterTabsLayout2;
    private Boolean amwasser, aussichtsplattform, lostplace, überdacht, mittenindernatur,urban,alle;

    CollectionReference collectionRef = FirebaseFirestore.getInstance().collection("location");
    GeoFirestore geoFirestore = new GeoFirestore(collectionRef);
    GeoQuery geoQuery;


    public static ArrayList<SuchObjekt> SuchObjektList = new ArrayList<SuchObjekt>();
    public ListView listViewNew;


    private RatingBar popupRatingbar,popupRatingbar2;
    private Button popupButtonClose,popupButtonMelden,popupButtonBewerten,popupButtonBewertenScreen,popupButtonback;
    private TextView popupTitle, popupInfo, popupbewertunganzahltext;
    private ProgressBar progressBarloading, imageloading;
    private ImageView imageView;
    private int nummer = 0;
    private AlertDialog.Builder dialogBuilder,dialogBuilder2;
    private AlertDialog dialog,dialog2;
    private Button aufkarteanzeigen;


    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        view = inflater.inflate(R.layout.fragment_search, container, false);

        searchView = view.findViewById(R.id.suchleiste);
        SuchObjektList.clear();
        /*
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                    System.out.println("HERE START");
                    radius = 30000;
                    getuserlocation();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        */
        setupData();
        initSearchWidgets();
        onclickButtonHandler();
        return view;
    }

    private void setupData()
    {
        // Jetzt wird die Postion abgefragt etc.
        radius = 30000;
        getuserlocation();
    }

    private void setUpList()
    {
        listViewNew = (ListView) view.findViewById(R.id.search_list);
        SuchObjektAdapter adapter = new SuchObjektAdapter(getContext().getApplicationContext(), 0, SuchObjektList);
        Collections.sort(SuchObjektList, SuchObjekt.sortentfernung);
        listViewNew.setAdapter(adapter);
    }

    private void setUpOnclickListener()
    {
        listViewNew.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l)
            {
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
                aufkarteanzeigen.setVisibility(View.VISIBLE);

                SuchObjekt suchObjekt = (SuchObjekt) (listViewNew.getItemAtPosition(position));

                popupTitle.setText(suchObjekt.getTitle());
                popupInfo.setText(suchObjekt.getInfo());

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
                loadBewertung(suchObjekt);
                loadBild(suchObjekt);

                popupButtonClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                popupButtonMelden.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getreportsnummber(suchObjekt);
                        popupButtonMelden.setEnabled(false);
                        popupButtonMelden.setText("GEMELDET");
                    }
                });

                popupButtonBewerten.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bewerten(suchObjekt);
                    }
                });

                aufkarteanzeigen.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        Navigation.findNavController(view).navigate(R.id.nav_gallery);
                        mapFragment.suchObjekt = suchObjekt;
                    }
                });
            }
        });

    }

    private void loadBild(SuchObjekt suchObjekt){

                db = FirebaseFirestore.getInstance();

                DocumentReference document = db.collection("location").document(String.valueOf(suchObjekt.getId()));

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

    private void bewerten(SuchObjekt suchObjekt){
        dialogBuilder2 = new AlertDialog.Builder(getContext());
        final View contactPopupView2 = getLayoutInflater().inflate(R.layout.popupbewerten, null);
        popupButtonBewertenScreen = (Button) contactPopupView2.findViewById(R.id.popupBewerten2);
        popupRatingbar2 = (RatingBar) contactPopupView2.findViewById(R.id.ratingBar2);
        popupButtonback = (Button) contactPopupView2.findViewById(R.id.back);
        dialogBuilder2.setView(contactPopupView2);
        dialog2 = dialogBuilder2.create();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int displayWidth = displayMetrics.widthPixels;
        int displayHeight = displayMetrics.heightPixels;
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog2.getWindow().getAttributes());
        int dialogWindowWidth = (int) (displayWidth * 0.8f);
        int dialogWindowHeight = (int) (displayHeight * 0.8f);
        layoutParams.width = dialogWindowWidth;
        layoutParams.height = dialogWindowHeight;
        dialog2.getWindow().setAttributes(layoutParams);
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
                sendeholeBewertung(suchObjekt);
            }
        });


    }

    private void loadBewertung(SuchObjekt suchObjekt){
                db = FirebaseFirestore.getInstance();

                DocumentReference document = db.collection("bewertung").document(suchObjekt.getId());

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




    }

    private void sendeholeBewertung(SuchObjekt suchObjekt){
        db = FirebaseFirestore.getInstance();

        DocumentReference document = db.collection("bewertung").document(suchObjekt.getId());

        document.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Double bewertung = documentSnapshot.getDouble("bewertung");
                    Double abstimmungen = documentSnapshot.getDouble("abstimmungen");
                    System.out.println("Bewertungen: "+bewertung+"   Abstimmungen: "+abstimmungen);
                    berechnesendeBewertung(bewertung,abstimmungen,suchObjekt);
                } else {
                    System.out.println("Noch nie");
                    berechnesendeBewertung(0,0,suchObjekt);

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

    private void berechnesendeBewertung(double bewertung, double abstimmungen, SuchObjekt suchObjekt){
        double neueBewertung;
        if (bewertung == 0 && abstimmungen == 0){
            sendBewertung(popupRatingbar2.getRating(), 1, suchObjekt);
        } else {
            neueBewertung = (((bewertung * abstimmungen) + (popupRatingbar2.getRating()) )/ (abstimmungen + 1));
            System.out.println(neueBewertung);
            sendBewertung(neueBewertung, (abstimmungen+1), suchObjekt);
        }
    }

    private void sendBewertung(double bewertung, double abstimmungen, SuchObjekt suchObjekt){
        db = FirebaseFirestore.getInstance();
        Map<String, Object> data = new HashMap<>();
        data.put("bewertung", bewertung);
        data.put("abstimmungen", abstimmungen);

        db.collection("bewertung").document(suchObjekt.getId())
                .set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        dialog2.dismiss();
                        loadBewertung(suchObjekt);
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

    private void report(String datenbanknummer, double anzahl){

        Map<String, Object> data = new HashMap<>();
        System.out.println("anzahl: "+anzahl+"  Anzahl+1: "+anzahl+1);
        data.put("anzahl", anzahl+1);

        db.collection("reports").document(datenbanknummer)
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

    private void getreportsnummber(SuchObjekt suchObjekt){
        db = FirebaseFirestore.getInstance();
        DocumentReference document = db.collection("reports").document(suchObjekt.getId());

        document.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                System.out.println("Here1");
                if (documentSnapshot.exists()) {
                    System.out.println("Datenbank sagt, dass die Anzahl folgendes ist: "+Double.toString(documentSnapshot.getDouble("anzahl")));
                    report(suchObjekt.getId(),documentSnapshot.getDouble("anzahl"));
                } else {
                    System.out.println("Returndata wurde 0 gesetzt");
                    report(suchObjekt.getId(),0);
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
                        System.out.println("HERE LATITUDE: "+location.getLatitude()+"  LONGITUDE: "+location.getLongitude());
                        getNearGeopointList(new GeoPoint(location.getLatitude(),location.getLongitude()),radius);
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
            System.out.println("HERE LATITUDE: "+mLastLocation.getLatitude()+"  LONGITUDE: "+mLastLocation.getLongitude());
            getNearGeopointList(new GeoPoint(mLastLocation.getLatitude(),mLastLocation.getLongitude()),radius);
        }
    };

    private void getNearGeopointList(GeoPoint location, int radius){

        //Wird aufgerufen, wenn ein gültier Standort des Nutzers gefunden wurde, ERST DANN!

        geoQuery = geoFirestore.queryAtLocation(location,radius);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(@NotNull String s, @NotNull GeoPoint geoPoint) {
                System.out.println("HERE STRING: "+s);

                db = FirebaseFirestore.getInstance();

                DocumentReference document = db.collection("location").document(s);

                document.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        storage = FirebaseStorage.getInstance();
                        storageRef = storage.getReference();
                        StorageReference pathReference = storageRef.child("images/" + documentSnapshot.getString("imagelink"));
                        try {
                            if (documentSnapshot.getString("imagelink")!= null){
                                final File localFile = File.createTempFile(documentSnapshot.getString("imagelink"), "jpg");
                                pathReference.getFile(localFile)
                                        .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                                                String documentenNummer = s;
                                                String title = documentSnapshot.getString("title");
                                                String entfernung = Integer.toString((int)(getDistance(location.getLatitude(), location.getLongitude(), documentSnapshot.getGeoPoint("l").getLatitude(), documentSnapshot.getGeoPoint("l").getLongitude()) / 1000));
                                                String kategorie = documentSnapshot.getString("Kategorie");
                                                String info = documentSnapshot.getString("info");

                                                db = FirebaseFirestore.getInstance();

                                                DocumentReference document = db.collection("bewertung").document(s);

                                                document.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                        Double bewertung = documentSnapshot.getDouble("bewertung");
                                                        SuchObjekt place = new SuchObjekt(documentenNummer, title, entfernung , bitmap,bewertung,kategorie,info);
                                                        SuchObjektList.add(place);
                                                        setUpList();
                                                        setUpOnclickListener();
                                                    }});

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.add_location);
                                                String documentenNummer = s;
                                                String title = documentSnapshot.getString("title");
                                                //String entfernung = Float.toString(Math.round(getDistance(location.getLatitude(), location.getLongitude(), documentSnapshot.getGeoPoint("l").getLatitude(), documentSnapshot.getGeoPoint("l").getLongitude()) / 1000));
                                                String entfernung = Integer.toString((int)(getDistance(location.getLatitude(), location.getLongitude(), documentSnapshot.getGeoPoint("l").getLatitude(), documentSnapshot.getGeoPoint("l").getLongitude()) / 1000));
                                                String kategorie = documentSnapshot.getString("Kategorie");
                                                String info = documentSnapshot.getString("info");

                                                db = FirebaseFirestore.getInstance();

                                                DocumentReference document = db.collection("bewertung").document(s);

                                                document.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                        Double bewertung = documentSnapshot.getDouble("bewertung");
                                                        SuchObjekt place = new SuchObjekt(documentenNummer, title, entfernung , bitmap,bewertung,kategorie,info);
                                                        SuchObjektList.add(place);
                                                        setUpList();
                                                        setUpOnclickListener();
                                                    }});
                                            }
                                        });
                        }else{
                            Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.add_location);
                                String documentenNummer = s;
                                String title = documentSnapshot.getString("title");
                                //String entfernung = Float.toString(Math.round(getDistance(location.getLatitude(), location.getLongitude(), documentSnapshot.getGeoPoint("l").getLatitude(), documentSnapshot.getGeoPoint("l").getLongitude()) / 1000));
                                String entfernung = Integer.toString((int)(getDistance(location.getLatitude(), location.getLongitude(), documentSnapshot.getGeoPoint("l").getLatitude(), documentSnapshot.getGeoPoint("l").getLongitude()) / 1000));
                                String kategorie = documentSnapshot.getString("Kategorie");
                                String info = documentSnapshot.getString("info");

                                db = FirebaseFirestore.getInstance();

                                DocumentReference document = db.collection("bewertung").document(s);

                                document.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        Double bewertung = documentSnapshot.getDouble("bewertung");
                                        SuchObjekt place = new SuchObjekt(documentenNummer, title, entfernung , bitmap,bewertung,kategorie,info);
                                        SuchObjektList.add(place);
                                        setUpList();
                                        setUpOnclickListener();
                                    }});
                        }
                        } catch (IOException e) {
                            System.out.println(e);
                        }

                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                System.out.println("Fehler: " + e);
                            }
                        });
            }

            @Override
            public void onKeyExited(@NotNull String s) {

                System.out.println("JAKOB: "+s);

            }

            @Override
            public void onKeyMoved(@NotNull String s, @NotNull GeoPoint geoPoint) {
                System.out.println("HERE 2");

            }

            @Override
            public void onGeoQueryReady() {

                System.out.println("HERE 3");

            }

            @Override
            public void onGeoQueryError(@NotNull Exception e) {

                System.out.println("HERE 4");

            }
        });


    }


    public void say(String text2) {

        Context context = getContext();
        CharSequence text = text2;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();


    }

    private float getDistance(double lat1, double lon1, double lat2, double lon2) {
        float[] distance = new float[2];
        Location.distanceBetween(lat1, lon1, lat2, lon2, distance);
        return distance[0];
    }

    private void initSearchWidgets(){
        SearchView searchView = (SearchView) view.findViewById(R.id.suchleiste);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ArrayList<SuchObjekt> filteredObjekte = new ArrayList<SuchObjekt>();
                for(SuchObjekt suchObjekt: SuchObjektList){
                    if (suchObjekt.getTitle().toLowerCase().contains(newText.toLowerCase())){
                        if (suchObjekt.getKategorie().equals("Lost-Place") && lostplace==true){
                            filteredObjekte.add(suchObjekt);
                        }
                        if (suchObjekt.getKategorie().equals("Aussichtsplattform") && aussichtsplattform==true){
                            filteredObjekte.add(suchObjekt);
                        }
                        if (suchObjekt.getKategorie().equals("Überdacht") && überdacht==true){
                            filteredObjekte.add(suchObjekt);
                        }
                        if (suchObjekt.getKategorie().equals("Mitten in der Natur") && mittenindernatur==true){
                            filteredObjekte.add(suchObjekt);
                        }
                        if (suchObjekt.getKategorie().equals("Urban") && urban==true){
                            filteredObjekte.add(suchObjekt);
                        }
                        if (suchObjekt.getKategorie().equals("Am Wasser") && amwasser==true){
                            filteredObjekte.add(suchObjekt);
                        }
                    }
                }

                SuchObjektAdapter adapter = new SuchObjektAdapter(getContext(), 0, filteredObjekte);
                listViewNew.setAdapter(adapter);

                return false;
            }
        });
    }

    private void ReloadList(){
        SearchView searchView = (SearchView) view.findViewById(R.id.suchleiste);
        ArrayList<SuchObjekt> filteredObjekte = new ArrayList<SuchObjekt>();
        for(SuchObjekt suchObjekt: SuchObjektList){
            if (searchView.getQuery().toString().equals("") == true){
                if (suchObjekt.getKategorie().equals("Lost-Place") && lostplace==true){
                    filteredObjekte.add(suchObjekt);
                }
                if (suchObjekt.getKategorie().equals("Aussichtsplattform") && aussichtsplattform==true){
                    filteredObjekte.add(suchObjekt);
                }
                if (suchObjekt.getKategorie().equals("Überdacht") && überdacht==true){
                    filteredObjekte.add(suchObjekt);
                }
                if (suchObjekt.getKategorie().equals("Mitten in der Natur") && mittenindernatur==true){
                    filteredObjekte.add(suchObjekt);
                }
                if (suchObjekt.getKategorie().equals("Urban") && urban==true){
                    filteredObjekte.add(suchObjekt);
                }
                if (suchObjekt.getKategorie().equals("Am Wasser") && amwasser==true){
                    filteredObjekte.add(suchObjekt);
                }
            }else {
            if (suchObjekt.getTitle().toLowerCase().contains(searchView.getQuery().toString().toLowerCase())){
                if (suchObjekt.getKategorie().equals("Lost-Place") && lostplace==true){
                    filteredObjekte.add(suchObjekt);
                }
                if (suchObjekt.getKategorie().equals("Aussichtsplattform") && aussichtsplattform==true){
                    filteredObjekte.add(suchObjekt);
                }
                if (suchObjekt.getKategorie().equals("Überdacht") && überdacht==true){
                    filteredObjekte.add(suchObjekt);
                }
                if (suchObjekt.getKategorie().equals("Mitten in der Natur") && mittenindernatur==true){
                    filteredObjekte.add(suchObjekt);
                }
                if (suchObjekt.getKategorie().equals("Urban") && urban==true){
                    filteredObjekte.add(suchObjekt);
                }
                if (suchObjekt.getKategorie().equals("Am Wasser") && amwasser==true){
                    filteredObjekte.add(suchObjekt);
                }
            }
        }
        }

        SuchObjektAdapter adapter = new SuchObjektAdapter(getContext(), 0, filteredObjekte);
        listViewNew.setAdapter(adapter);
    }

    private void onclickButtonHandler(){
        filteropenclose = view.findViewById(R.id.filteropenclose);
        filteropenclose.setTypeface(DEFAULT_BOLD);
        amwasserFilter = view.findViewById(R.id.amwasserFilter);
        amwasserFilter.setTypeface(DEFAULT_BOLD);
        amwasser=true;
        aussichtsplattformFilter = view.findViewById(R.id.aussichtsplattformFilter);
        aussichtsplattformFilter.setTypeface(DEFAULT_BOLD);
        aussichtsplattform=true;
        lostplaceFilter = view.findViewById(R.id.lostplaceFilter);
        lostplaceFilter.setTypeface(DEFAULT_BOLD);
        lostplace=true;
        überdachtFilter = view.findViewById(R.id.überdachtFilter);
        überdachtFilter.setTypeface(DEFAULT_BOLD);
        überdacht=true;
        mittenindernaturFilter = view.findViewById(R.id.mittenindernachtFilter);
        mittenindernaturFilter.setTypeface(DEFAULT_BOLD);
        mittenindernatur=true;
        urbanFilter  = view.findViewById(R.id.urbanFilter);
        urbanFilter.setTypeface(DEFAULT_BOLD);
        urban=true;
        alleFilter = view.findViewById(R.id.alleFilter);
        alleFilter.setTypeface(DEFAULT_BOLD);
        nachentfernungFilter = view.findViewById(R.id.nachentfernungFilter);
        nachentfernungFilter.setTypeface(DEFAULT_BOLD);
        nachbewertungFilter = view.findViewById(R.id.nachbewertungFilter);
        nachbewertungFilter.setTypeface(DEFAULT);
        filterTabsLayout = view.findViewById(R.id.filterTabsLayout);
        filterTabsLayout2 = view.findViewById(R.id.filterTabsLayout2);


        filteropenclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (filterTabsLayout.getVisibility() == View.VISIBLE){
                    filterTabsLayout.setVisibility(View.GONE);
                    filterTabsLayout2.setVisibility(View.GONE);
                    filteropenclose.setTypeface(DEFAULT_BOLD);
                } else {
                    filterTabsLayout.setVisibility(View.VISIBLE);
                    filterTabsLayout2.setVisibility(View.VISIBLE);
                    filteropenclose.setTypeface(Typeface.DEFAULT);
                }

            }
        });

        amwasserFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (amwasserFilter.getTypeface().equals(DEFAULT_BOLD)){
                    amwasserFilter.setTypeface(Typeface.DEFAULT);
                    alleFilter.setTypeface(DEFAULT);
                    amwasser=false;
                    ReloadList();
                }else{
                    amwasserFilter.setTypeface(DEFAULT_BOLD);
                    amwasser=true;
                    if(amwasser && aussichtsplattform && lostplace && überdacht && mittenindernatur && urban){
                        alleFilter.setTypeface(DEFAULT_BOLD);
                    }
                    ReloadList();
                }

            }
        });

        aussichtsplattformFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (aussichtsplattformFilter.getTypeface().equals(DEFAULT_BOLD)){
                    aussichtsplattformFilter.setTypeface(Typeface.DEFAULT);
                    alleFilter.setTypeface(DEFAULT);
                    aussichtsplattform=false;
                    ReloadList();
                }else{
                    aussichtsplattformFilter.setTypeface(DEFAULT_BOLD);
                    aussichtsplattform=true;
                    if(amwasser && aussichtsplattform && lostplace && überdacht && mittenindernatur && urban){
                        alleFilter.setTypeface(DEFAULT_BOLD);
                    }
                    ReloadList();
                }

            }
        });

        lostplaceFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lostplaceFilter.getTypeface().equals(DEFAULT_BOLD)){
                    lostplaceFilter.setTypeface(Typeface.DEFAULT);
                    alleFilter.setTypeface(DEFAULT);
                    lostplace=false;
                    ReloadList();
                }else{
                    lostplaceFilter.setTypeface(DEFAULT_BOLD);
                    lostplace=true;
                    if(amwasser && aussichtsplattform && lostplace && überdacht && mittenindernatur && urban){
                        alleFilter.setTypeface(DEFAULT_BOLD);
                    }
                    ReloadList();
                }

            }
        });

        überdachtFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (überdachtFilter.getTypeface().equals(DEFAULT_BOLD)){
                    überdachtFilter.setTypeface(Typeface.DEFAULT);
                    alleFilter.setTypeface(DEFAULT);
                    überdacht=false;
                    ReloadList();
                }else{
                    überdachtFilter.setTypeface(DEFAULT_BOLD);
                    überdacht=true;
                    if(amwasser && aussichtsplattform && lostplace && überdacht && mittenindernatur && urban){
                        alleFilter.setTypeface(DEFAULT_BOLD);
                    }
                    ReloadList();
                }

            }
        });

        mittenindernaturFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mittenindernaturFilter.getTypeface().equals(DEFAULT_BOLD)){
                    mittenindernaturFilter.setTypeface(Typeface.DEFAULT);
                    alleFilter.setTypeface(DEFAULT);
                    mittenindernatur=false;
                    ReloadList();
                }else{
                    mittenindernaturFilter.setTypeface(DEFAULT_BOLD);
                    mittenindernatur=true;
                    if(amwasser && aussichtsplattform && lostplace && überdacht && mittenindernatur && urban){
                        alleFilter.setTypeface(DEFAULT_BOLD);
                    }
                    ReloadList();
                }

            }
        });

        urbanFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (urbanFilter.getTypeface().equals(DEFAULT_BOLD)){
                    urbanFilter.setTypeface(Typeface.DEFAULT);
                    alleFilter.setTypeface(DEFAULT);
                    urban=false;
                    ReloadList();
                }else{
                    urbanFilter.setTypeface(DEFAULT_BOLD);
                    urban=true;
                    if(amwasser && aussichtsplattform && lostplace && überdacht && mittenindernatur && urban){
                        alleFilter.setTypeface(DEFAULT_BOLD);
                    }
                    ReloadList();
                }

            }
        });

        alleFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alleFilter.setTypeface(DEFAULT_BOLD);
                amwasser=true;
                amwasserFilter.setTypeface(DEFAULT_BOLD);
                aussichtsplattformFilter.setTypeface(DEFAULT_BOLD);
                aussichtsplattform=true;
                lostplaceFilter.setTypeface(DEFAULT_BOLD);
                lostplace=true;
                überdachtFilter.setTypeface(DEFAULT_BOLD);
                überdacht=true;
                mittenindernaturFilter.setTypeface(DEFAULT_BOLD);
                mittenindernatur=true;
                urbanFilter.setTypeface(DEFAULT_BOLD);
                urban=true;
                ReloadList();
            }
        });

        nachentfernungFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nachentfernungFilter.setTypeface(DEFAULT_BOLD);
                Collections.sort(SuchObjektList, SuchObjekt.sortentfernung);
                nachbewertungFilter.setTypeface(DEFAULT);
                ReloadList();

            }
        });

        nachbewertungFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nachbewertungFilter.setTypeface(DEFAULT_BOLD);
                Collections.sort(SuchObjektList, SuchObjekt.sortbewertung);
                nachentfernungFilter.setTypeface(DEFAULT);
                ReloadList();
            }
        });
    }


}
