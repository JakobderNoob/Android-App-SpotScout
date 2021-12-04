package prime.mapapp.mapsappv2.ui.add;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileUtils;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.io.ByteArrayInputStream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseError;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import org.imperiumlabs.geofirestore.GeoFirestore;
import org.imperiumlabs.geofirestore.GeoQuery;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import prime.mapapp.mapsappv2.R;
import prime.mapapp.mapsappv2.ui.map.mapViewModel;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

public class addFragment extends Fragment implements OnMapReadyCallback {
    private mapViewModel mapViewModel;
    private View view;
    private GoogleMap mMap;
    private Marker nMap;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private Button weiter, senden, addpicture;
    private CheckBox checkbox;
    private double x,y;
    private boolean allowmovemarker = true;
    private String infotext,titletext;
    private Bitmap bitmap;
    private ProgressBar uploadloading;
    private Spinner spinner;
    private TextView info7,info8,info9,info10;
    private static final int CAMERA_REQUEST_CODE = 1001;
    private EditText titeltext, infotext2;
    private FloatingActionButton orten;

    CollectionReference collectionRef = FirebaseFirestore.getInstance().collection("location");
    GeoFirestore geoFirestore = new GeoFirestore(collectionRef);


    GeoQuery geoQuery;

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        allowmovemarker = true;
        view = inflater.inflate(R.layout.fragment_addplace, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.mapfragment);
        mapFragment.getMapAsync(this);
        senden = view.findViewById(R.id.senden);
        weiter = view.findViewById(R.id.weiter);
        addpicture = view.findViewById(R.id.addpicture);
        spinner = view.findViewById(R.id.spinner2);
        info7 = view.findViewById(R.id.info7);
        info8 = view.findViewById(R.id.info8);
        info9 = view.findViewById(R.id.info9);
        info10 = view.findViewById(R.id.info10);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.typ, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        db= FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        weiter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.findViewById(R.id.weiter).setVisibility(View.GONE);
                view.findViewById(R.id.mapfragment).setVisibility(View.VISIBLE);
                //view.findViewById(R.id.mapfragment3).setVisibility(View.VISIBLE);
                View view1 = view.findViewById(R.id.mapfragment);
                ViewGroup.LayoutParams layoutParams = view1.getLayoutParams();
                layoutParams.width = dpToPx(0);
                layoutParams.height = dpToPx(135);
                view1.setLayoutParams(layoutParams);
                allowmovemarker = false;
                sichtbarkeit(view);
                addMarker("title",x,y, "info");


            }
        });
        EditText infotextelement = (EditText) view.findViewById(R.id.titeltext);
        EditText titletextelement = (EditText) view.findViewById(R.id.infotext);
        CheckBox checkBoxelement = (CheckBox) view.findViewById(R.id.checkbox);
        senden.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                infotext = infotextelement.getText().toString();
                titletext = titletextelement.getText().toString();
                checkbox = (CheckBox) checkBoxelement;
                if (infotext.equals("") == false) {
                    if (titletext.equals("") == false){
                        if (checkbox.isChecked()== true){
                            if(spinner.getSelectedItem().equals("Bitte auswählen")){say("Bitte eine Kategorie auswählen");}else {
                                System.out.println(spinner.getSelectedItem());
                            if (bitmap == null) {
                                senden.setEnabled(false);
                                addpicture.setEnabled(false);
                                say("Sende Daten zu Server");
                                CountDocumentsandSendData(titletext, infotext, new GeoPoint(x, y), null, spinner.getSelectedItem().toString());
                           } else {
                                senden.setEnabled(false);
                                addpicture.setEnabled(false);
                                uploadloading = view.findViewById(R.id.progressBar);
                                uploadloading.setVisibility(View.VISIBLE);
                                uploadpicture(spinner.getSelectedItem().toString());
                           }}
                        } else {
                            say("Du musst die Nutzungsbedinungen akzeptieren");
                        }
                    } else {
                        say("Bitte alle Felder ausfüllen");
                    }
                } else {
                    say("Bitte alle Felder ausfüllen");
                }
            }
        });

        addpicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, CAMERA_REQUEST_CODE);
            }
        });




        return view;
    }

    private void sichtbarkeit(View view){

        info7.setVisibility(View.VISIBLE);
        spinner.setVisibility(View.VISIBLE);
        view.findViewById(R.id.imageView2).setVisibility(View.GONE);
        infotext2 = view.findViewById(R.id.infotext);
        titeltext = view.findViewById(R.id.titeltext);
        checkbox = view.findViewById(R.id.checkbox);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0){
                    info8.setVisibility(View.VISIBLE);
                    titeltext.setVisibility(View.VISIBLE);
                    titeltext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                        @Override
                        public void onFocusChange(View v, boolean hasFocus) {
                            if (titeltext.getText().equals("")) {}else{
                                info9.setVisibility(View.VISIBLE);
                                infotext2.setVisibility(View.VISIBLE);

                                infotext2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                                    @Override
                                    public void onFocusChange(View v, boolean hasFocus) {
                                        if (infotext2.getText().equals("")){}else{
                                            info10.setVisibility(View.VISIBLE);
                                            addpicture.setVisibility(View.VISIBLE);
                                            checkbox.setVisibility(View.VISIBLE);
                                            senden.setVisibility(View.VISIBLE);
                                        }
                                    }
                                });
                            }
                        }
                    });


                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private void uploadpicture(String Kategorie) {


            final String randomKey = UUID.randomUUID().toString();
            StorageReference riversRef = storageReference.child("images/" + randomKey);
            riversRef.putFile(getImageUri(getContext(), bitmap))
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            uploadloading.setVisibility(View.GONE);
                            CountDocumentsandSendData(titletext, infotext, new GeoPoint(x, y), randomKey, Kategorie);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            uploadloading.setVisibility(View.GONE);
                            say("Datei konnte nicht hochgeladen werden. (Fehler)");
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            int progressPercent = (int) (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                            uploadloading.setProgress(progressPercent);
                        }
                    });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode== CAMERA_REQUEST_CODE && resultCode==RESULT_OK){
                bitmap = (Bitmap) data.getExtras().get("data");
                System.out.println("YES");
                addpicture.setText("Anderes Bild machen");
        } else {
            System.out.println("requestCode: '"+requestCode+"'  resultCode: '"+resultCode+"'");
            System.out.println("NO!");
        }
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        addMarker2("Neuer Ort", 4.4,4.9, "Ausgewählter Ort");
        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                if (allowmovemarker == true) {
                    mMap = googleMap;
                    nMap.setPosition(mMap.getCameraPosition().target);
                    x = nMap.getPosition().latitude;
                    y = nMap.getPosition().longitude;
                }
            }
        });


    }

    private void addMarker2(String title, double x,double y, String info){
        nMap = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(x,y))
                .title(title)
                .draggable(true)
                .visible(false)
                .snippet(info));



    }
    private void addMarker(String title, double x,double y, String info){
        nMap = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(x,y))
                .title(title)
                .draggable(true)
                .visible(true)
                .snippet(info));



    }
    private static int dpToPx(int dp)
    {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    private static int pxToDp(int px)
    {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    private void sendData(String DokumentNummer, String info, String title, GeoPoint geoPoint, String ImageLink, String Kategorie){
                    Map<String, Object> data = new HashMap<>();
                    data.put("info", info);
                    data.put("title", title);
                    data.put("banned", "notbanned");
                    data.put("Kategorie", Kategorie);
                    if (ImageLink != null) {
                        data.put("imagelink", ImageLink);
                    }
                    db = FirebaseFirestore.getInstance();
                    db.collection("location").document(DokumentNummer)
                            .set(data)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    geoFirestore.setLocation(DokumentNummer, geoPoint, new GeoFirestore.CompletionCallback() {
                                        @Override
                                        public void onComplete(@org.jetbrains.annotations.Nullable Exception e) {
                                            if (e != null) {
                                                System.out.println("HERE ERROR: "+e);
                                            } else {
                                                System.out.println("HERE KEIN: " + e);
                                                System.out.println("HERE Location saved on server successfully!");
                                                say("Erfolgreich gesendet!");
                                                Navigation.findNavController(view).navigate(R.id.nav_gallery);
                                            }}});
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    say("Konnte den Ort nicht Senden!");
                                    senden.setEnabled(true);
                                }
                            });
    }

    private void CountDocumentsandSendData(String info, String title, GeoPoint geoPoint, String ImageLink, String Kategorie){
        db = FirebaseFirestore.getInstance();
        db.collection("location")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        int nummer = 1;
                        if (task.isSuccessful()) {
                            nummer = 1;
                            for (DocumentSnapshot document : task.getResult()) {
                                nummer++;
                            }
                        } else {
                        }
                        sendData(Integer.toString(nummer), info, title, geoPoint, ImageLink, Kategorie);
                    }
                });
    }
    private void say(String text2){

        Context context = getActivity().getApplicationContext();
        CharSequence text = text2;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();


    }
}
