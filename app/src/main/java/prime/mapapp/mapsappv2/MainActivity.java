package prime.mapapp.mapsappv2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageInstaller;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.net.Uri;

import java.io.File;


import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.FileProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import java.io.File;

import prime.mapapp.mapsappv2.ui.map.mapFragment;

import static android.Manifest.permission.ACCESS_BACKGROUND_LOCATION;
import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.Intent.ACTION_INSTALL_PACKAGE;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static android.os.Environment.getExternalStorageDirectory;
import static android.os.SystemClock.sleep;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    FirebaseFirestore db;
    public static String version = "2.1";
    public static String serverversion = "Konnte Serverversion nicht laden!";
    private Button updatebutton, offneordner;
    private ProgressBar progressBar;
    private TextView textView,offline;
    public static boolean readinstall = false;
    private boolean stopwhile = false;
    private boolean bypassoffline = false;




    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        darkmodesettings();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_search,
                R.id.nav_gallery, R.id.nav_home, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,WRITE_EXTERNAL_STORAGE,READ_EXTERNAL_STORAGE}, 1);
            return;
        }else{

        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,WRITE_EXTERNAL_STORAGE,READ_EXTERNAL_STORAGE}, 1);
            return;
        }else{

        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,WRITE_EXTERNAL_STORAGE,READ_EXTERNAL_STORAGE}, 1);
            return;
        }else{

        }

        //StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        //StrictMode.setVmPolicy(builder.build());
        checkVersion();
        if (bypassoffline != true) {
            checkServerOffline();
        }

    }

    private void checkServerOffline() {
        db = FirebaseFirestore.getInstance();

        DocumentReference document = db.collection("offline").document("offline");

        document.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    if (documentSnapshot.getString("grund").equals("")) {
                    } else {
                        setContentView(R.layout.offline);
                        offline = findViewById(R.id.grund);
                        offline.setText("Grund:\n\n"+documentSnapshot.getString("grund"));
                    }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    public void checkVersion() {

        db = FirebaseFirestore.getInstance();

        DocumentReference document = db.collection("version").document("version");

        document.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    serverversion = documentSnapshot.getString("version");
                    if (documentSnapshot.getString("version").equals(version)) {
                        System.out.println("Server Version: Correct Version!");
                    } else {
                        setUpdateScreenandmore();
                        setText(documentSnapshot.getString("version"));
                        System.out.println("Server Version : '" + documentSnapshot.getString("version") + "'");

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

    public void setText(String serverversion) {
        TextView wrongversion = (TextView) findViewById(R.id.textView2);
        wrongversion.setText("Leider ist diese App nicht auf der aktuellsten Version!\n\n\nDeine aktuelle Version: "+version+"\n\nDie Server Version: " + serverversion);
    }

    public void setUpdateScreenandmore() {

        setContentView(R.layout.old_version);
        updatebutton = findViewById(R.id.updateknopf);
        progressBar = findViewById(R.id.progressBar2);
        textView = findViewById(R.id.updateinfo);
        updatebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("YES");
                onClickedUpdateButtonConfirmed();

            }
        });

    }

    public void onClickedUpdateButtonConfirmed() {
        updatebutton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        textView.setVisibility(View.VISIBLE);
        textView = findViewById(R.id.updateinfo);
        textView.setText("Verbindung mit dem Update Server wird aufgebaut!");
        //requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
        dbgetUpdateLink();


    }

    public void dbgetUpdateLink() {

        db = FirebaseFirestore.getInstance();
        textView = findViewById(R.id.updateinfo);
        textView.setText("Verbindung mit dem Update Server wurde aufgebaut!\nDie Update Datei wird heruntergeladen!");
        DocumentReference document = db.collection("version").document("version");

        document.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    downloadapkandinstall(documentSnapshot.getString("downloadlink"));
                } else {
                    say("Fehler, Updatelink nicht finden, bitte App Neutstarten. Hat die App Internet?");
                }

            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        say("Fehler: " + e);
                    }
                });

    }


    public void downloadapkandinstall(String APKDOWNLOADLINK) {
        new UpdateAppClass().execute(APKDOWNLOADLINK);

        while (stopwhile == false) {
            if (readinstall == true) {
                System.out.println("install");
                install();
                stopwhile = true;
            }
        }
        //install();
        //UpdateAppClass update = new UpdateAppClass();
        //update.setContext(getApplicationContext(),APKDOWNLOADLINK);
        //update.execute();


    }


    public void say(String text2) {

        Context context = getApplicationContext();
        CharSequence text = text2;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();


    }

    public void install() {
        textView = findViewById(R.id.updateinfo);
        textView.setText("1.Die Update APK wurde im Download Ordner gespeichert.\n2.Die APK heißt 'OpenThisFileToUpdate.apk'.\n3.Öffne jetzt den Ordner mit dem Knopf.\n4.Dort bitte die APK anklicken und installieren.\n5.Danach einfach die App wieder starten.");
        progressBar.setVisibility(View.GONE);
        updatebutton.setText("1. Erfolgreich heruntergeladen!");
        offneordner = findViewById(R.id.öffneordner);
        offneordner.setEnabled(true);
        offneordner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFolder();

            }
        });
    }

    public void openFolder(){
        Uri selectedUri = Uri.parse(Environment.getExternalStorageDirectory().getPath() +  File.separator + "UpdateFile" + File.separator);
        Intent intent = new Intent("android.intent.action.VIEW_DOWNLOADS");
        intent.setClassName("com.android.documentsui", "com.android.documentsui.files.FilesActivity");
        intent.setDataAndType(selectedUri, "*/*");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            say("Konnte leider kein Programm finden, mit welchen du einen Ordner öffnen kannst.");
        }

    }
    private void darkmodesettings(){

        SharedPreferences mPrefs = this.getSharedPreferences("settingsdarkmode", 0);
        String mString = mPrefs.getString("settingsdarkmode", "light");
        if(mString.equals("system")){
            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM);
        }
        if(mString.equals("dark")){
            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES);
        }
        if(mString.equals("light")){
            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO);
        }

    }









}




