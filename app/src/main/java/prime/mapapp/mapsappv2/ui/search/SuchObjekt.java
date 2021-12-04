package prime.mapapp.mapsappv2.ui.search;

import android.graphics.Bitmap;
import android.media.Image;

import java.util.Comparator;

public class SuchObjekt {

    private String id;
    private String title;
    private String entfernung;
    private String info;
    private Double bewertung;
    private String kategorie;
    private Bitmap image;

    public SuchObjekt(String id, String title, String entfernung, Bitmap image, Double bewertung, String kategorie, String info) {
        this.id = id;
        this.title = title;
        this.image = image;
        this.entfernung = entfernung;
        this.bewertung = bewertung;
        this.kategorie = kategorie;
        this.info = info;
    }


    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getEntfernung(){return entfernung;}

    public void setEntfernung(String entfernung){this.entfernung = entfernung;}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public void setInfo(){this.info = info;}

    public String getInfo(){return info;}

    public void setBewertung(Double bewertung){this.bewertung = bewertung;}

    public Double getBewertung(){return bewertung;}

    public void setKategorie(String Kategorie){this.kategorie = Kategorie;}

    public String getKategorie(){return kategorie;}

    public static Comparator<SuchObjekt> sortentfernung = new Comparator<SuchObjekt>()
    {
        @Override
        public int compare(SuchObjekt objekt1, SuchObjekt objekt2)
        {
            Double newObjekt1,newObjekt2;
            newObjekt1 = Double.valueOf(objekt1.getEntfernung());
            newObjekt2 = Double.valueOf(objekt2.getEntfernung());

            return Double.compare(newObjekt1, newObjekt2);

        }
    };

    public static Comparator<SuchObjekt> sortbewertung = new Comparator<SuchObjekt>()
    {
        @Override
        public int compare(SuchObjekt objekt3, SuchObjekt objekt4)
        {
            Double bewertung1 = 0.0;
            Double bewertung2 = 0.0;
            if (objekt3.getBewertung() != null){
                bewertung1 = Double.valueOf(objekt3.getBewertung());
            }
            if (objekt4.getBewertung() != null){
                bewertung2 = Double.valueOf(objekt4.getBewertung());
            }

            return Double.compare(bewertung2,bewertung1);
        }
    };


}

