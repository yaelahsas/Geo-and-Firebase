package sastra.panji.dhimas.firebase;

public class Lokasi {
    String Langitude, Longitude;

    public Lokasi(String langitude, String longitude) {
        Langitude = langitude;
        Longitude = longitude;
    }

    public String getLangitude() {
        return Langitude;
    }

    public void setLangitude(String langitude) {
        Langitude = langitude;
    }

    public String getLongitude() {
        return Longitude;
    }

    public void setLongitude(String longitude) {
        Longitude = longitude;
    }
}
