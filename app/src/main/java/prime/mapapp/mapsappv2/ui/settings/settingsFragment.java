package prime.mapapp.mapsappv2.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import prime.mapapp.mapsappv2.MainActivity;
import prime.mapapp.mapsappv2.R;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

public class settingsFragment extends Fragment {

    private View view;
    private Context mContext;
    private Spinner spinner;
    private Button speicher;

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        view = inflater.inflate(R.layout.fragment_settings, container, false);
        speicher = view.findViewById(R.id.einstellungenspeichern);
        spinner = view.findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.darkmode, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        setVersioninfo();
        mContext = getContext();
        speicher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("HERE: '"+spinner.getSelectedItem()+"'");
                if (spinner.getSelectedItem().equals("System Einstellung")){
                    System.out.println("HERE1");
                    SharedPreferences mPrefs = mContext.getSharedPreferences("settingsdarkmode",0);
                    SharedPreferences.Editor editor = mPrefs.edit();
                    editor.putString("settingsdarkmode", "system");
                    editor.apply();
                    AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM);
                    System.out.println("HERE1");
                    say("Gespeichert!");
                }
                if (spinner.getSelectedItem().equals("Dark Mode")){
                    System.out.println("HERE2");
                    SharedPreferences mPrefs = mContext.getSharedPreferences("settingsdarkmode", 0);
                    SharedPreferences.Editor editor = mPrefs.edit();
                    editor.putString("settingsdarkmode", "dark");
                    editor.apply();
                    AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES);
                    System.out.println("HERE2");
                    say("Gespeichert!");
                }
                if (spinner.getSelectedItem().equals("Light Mode")){
                    System.out.println("HERE3");
                    SharedPreferences mPrefs = mContext.getSharedPreferences("settingsdarkmode", 0);
                    SharedPreferences.Editor editor = mPrefs.edit();
                    editor.putString("settingsdarkmode", "light");
                    editor.apply();
                    AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO);
                    System.out.println("HERE3");
                    say("Gespeichert!");
                }
            }
        });

        return view;
    }

    private void setVersioninfo(){
        TextView versionText = view.findViewById(R.id.versionview);
        versionText.setText("App Version: "+ MainActivity.version+"\nServer Version: "+MainActivity.serverversion);
    }


    private void say(String text2) {

        Context context = getContext().getApplicationContext();
        CharSequence text = text2;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();


    }

}