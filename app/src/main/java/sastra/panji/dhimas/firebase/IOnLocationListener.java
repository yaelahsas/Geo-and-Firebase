package sastra.panji.dhimas.firebase;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public interface IOnLocationListener {
    void onLoadLocationSucces(List<MyLatLang> latLngs);
    void onLoadLocationFailed(String message);
}
