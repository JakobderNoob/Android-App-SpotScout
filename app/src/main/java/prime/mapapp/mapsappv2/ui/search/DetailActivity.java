package prime.mapapp.mapsappv2.ui.search;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import prime.mapapp.mapsappv2.R;

public class DetailActivity extends AppCompatActivity
{
    SuchObjekt suchObjekt;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popupmarker);
        getSelectedShape();
        setValues();

    }

    private void getSelectedShape()
    {
        Intent previousIntent = getIntent();
        String parsedStringID = previousIntent.getStringExtra("id");
        suchObjekt = searchFragment.SuchObjektList.get(Integer.valueOf(parsedStringID));
    }

    private void setValues()
    {
        TextView title = (TextView) findViewById(R.id.popupTitle);
        //TextView entfernung = (TextView) findViewById(R.id.list_entfeung);
        ImageView vorschaubild = (ImageView) findViewById(R.id.imageViewPicture);

        title.setText(suchObjekt.getTitle());
        //entfernung.setText(suchObjekt.getEntfernung());
        vorschaubild.setImageBitmap(suchObjekt.getImage());
    }

}
