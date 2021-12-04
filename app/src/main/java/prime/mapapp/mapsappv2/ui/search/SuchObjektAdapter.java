package prime.mapapp.mapsappv2.ui.search;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDelegate;

import java.util.List;

import prime.mapapp.mapsappv2.R;


public class SuchObjektAdapter extends ArrayAdapter<SuchObjekt>
{
    public SuchObjektAdapter(Context context, int resource, List<SuchObjekt> suchObjektList)
    {
        super(context,resource,suchObjektList);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        SuchObjekt suchObjekt = getItem(position);

        if(convertView == null)
        {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_design, parent, false);
        }
        TextView title = (TextView) convertView.findViewById(R.id.list_title);
        TextView entfernung = (TextView) convertView.findViewById(R.id.list_entfernung);
        ImageView vorschaubild = (ImageView) convertView.findViewById(R.id.vorschaubild);
        TextView kategorie = (TextView) convertView.findViewById(R.id.list_kategorie);
        RatingBar ratingBar = (RatingBar) convertView.findViewById(R.id.list_ratingbar);

        switch (AppCompatDelegate.getDefaultNightMode()) {
            case AppCompatDelegate.MODE_NIGHT_YES:
                title.setTextColor(Color.WHITE);
                entfernung.setTextColor(Color.WHITE);
                kategorie.setTextColor(Color.WHITE);
                System.out.println("HERE: WHITE");
                break;

            case AppCompatDelegate.MODE_NIGHT_NO:
                title.setTextColor(Color.BLACK);
                entfernung.setTextColor(Color.BLACK);
                kategorie.setTextColor(Color.BLACK);
                System.out.println("HERE: BLACK");
                break;

            case AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM:
                switch (getContext().getResources().getConfiguration().uiMode){
                    case 33:
                        title.setTextColor(Color.WHITE);
                        entfernung.setTextColor(Color.WHITE);
                        kategorie.setTextColor(Color.WHITE);
                        System.out.println("HERE: WHITE2");
                        break;
                    case 17:
                        title.setTextColor(Color.BLACK);
                        entfernung.setTextColor(Color.BLACK);
                        kategorie.setTextColor(Color.BLACK);
                        System.out.println("HERE: BLACK2");
                }
                System.out.println(Configuration.UI_MODE_NIGHT_MASK);
                System.out.println(getContext().getResources().getConfiguration().uiMode);
                System.out.println("HERE: BLACK UNDEFINDED");

                break;
        }

        title.setText(suchObjekt.getTitle());
        entfernung.setText(suchObjekt.getEntfernung() + " km");
        vorschaubild.setImageBitmap(suchObjekt.getImage());
        kategorie.setText(suchObjekt.getKategorie());
        ratingBar.setEnabled(false);
        if (suchObjekt.getBewertung() != null) {
            ratingBar.setRating(suchObjekt.getBewertung().floatValue());
        } else {
            ratingBar.setRating(0);
        }

        return convertView;
    }
}
