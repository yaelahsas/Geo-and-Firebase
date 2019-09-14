package sastra.panji.dhimas.firebase;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MapsActivity2 extends FragmentActivity implements OnMapReadyCallback, GeoQueryEventListener, IOnLocationListener {

    private GoogleMap mMap;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Marker currentUser;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private DatabaseReference myRef;
    private GeoFire geoFire;
    private List<LatLng> list;
    private IOnLocationListener listener;
    private  GeoQuery geoQuery;

    DatabaseReference kotaKu;
    Location lastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);

        Dexter.withActivity(this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {


                Toast.makeText(MapsActivity2.this, "Permission Berhasil Diberikan !", Toast.LENGTH_LONG).show();

                buildLocatonRequest();
                builLocationCallback();

                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapsActivity2.this);

                initArea();

                settingGeoFire();


            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {

                Toast.makeText(MapsActivity2.this, "Permission Harus Diberikan !", Toast.LENGTH_LONG).show();

            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

            }
        }).check();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

    }

    private void initArea() {

        kotaKu = FirebaseDatabase.getInstance().getReference("Area Bahaya").child("Kotanya");

        listener = this;


        //Mengambil dari Firebase

        kotaKu.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                try {
                    List<MyLatLang> latLngList;
                    latLngList = new ArrayList<>();

                    for (DataSnapshot Locationsnapshot : dataSnapshot.getChildren()) {

                        MyLatLang latLng = Locationsnapshot.getValue(MyLatLang.class);
                        latLngList.add(latLng);

                    }
                    listener.onLoadLocationSucces(latLngList);
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                listener.onLoadLocationFailed(databaseError.getMessage());

            }
        });
        kotaKu.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //update tempat berbahaya ketika di ganti dari firestor

                List<MyLatLang> latLngList;
                latLngList = new ArrayList<>();

                for (DataSnapshot Locationsnapshot : dataSnapshot.getChildren()) {

                    MyLatLang latLng = Locationsnapshot.getValue(MyLatLang.class);
                    latLngList.add(latLng);

                }

                listener.onLoadLocationSucces(latLngList);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

//
//        list = new ArrayList<>();
//        list.add(new LatLng(-6.2940701, 106.8538575));
//        list.add(new LatLng(-6.2940701, 107.8538575));
//        list.add(new LatLng(-6.2940701, 108.8538575));
//
//
//        FirebaseDatabase.getInstance().getReference("Area Bahaya").child("Kotanya")
//               .setValue(list).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//
//                Toast.makeText(MapsActivity2.this, "Data Dimasukkan FireStore", Toast.LENGTH_SHORT).show();
//
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//
//                Toast.makeText(MapsActivity2.this, "Data Gagal FireStore", Toast.LENGTH_SHORT).show();
//
//
//            }
//        });

    }


    private void settingGeoFire() {


        myRef = FirebaseDatabase.getInstance().getReference("Lokasi Saya");

        geoFire = new GeoFire(myRef);
    }

    private void builLocationCallback() {

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(final LocationResult locationResult) {
                if (mMap != null) {
                    lastLocation = locationResult.getLastLocation();
                    //Tambah Lokasi dan arahkan map
                    tambahLokasiUser();

                }
            }
        };

    }

    private void tambahLokasiUser() {
        geoFire.setLocation("Anda", new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude()
        ), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                if (currentUser != null) currentUser.remove();
      currentUser =  mMap.addMarker(new MarkerOptions()
              .position(new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude())).title("Anda"));
                LatLng latlang = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlang, 12.0f));


            }
        });
    }

    private void buildLocatonRequest() {

        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10f);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        if (fusedLocationProviderClient != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }
            }
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

            //Menambahkan Daerah yang berbahaya
            tambahDaerahBahaya();
        }
    }

    private void tambahDaerahBahaya() {
       if (geoQuery != null)
       {
           geoQuery.removeGeoQueryEventListener(this);
           geoQuery.removeAllListeners();
       }
        for (LatLng latlang : list) {
            mMap.addCircle(new CircleOptions()
                    .center(latlang)
                    .radius(500)
                    .strokeColor(Color.BLUE)
                    .fillColor(0x220000FF)
                    .strokeWidth(5.0f)
            );
            //Geo Query
            geoQuery = geoFire.queryAtLocation(new GeoLocation(latlang.latitude, latlang.longitude), 0.5f);
            geoQuery.addGeoQueryEventListener(MapsActivity2.this);
        }
    }

    @Override
    protected void onStop() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        super.onStop();
    }


    @Override
    public void onKeyEntered(String key, GeoLocation location) {
        sendNotification("Anda", String.format("%s Memasuki daerah bahaya", key));
        Toast.makeText(this, "Anda Memasuki Area Berbahaya !", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onKeyExited(String key) {
        sendNotification("Anda", String.format("%s Leaved daerah bahaya", key));
    }

    @Override
    public void onKeyMoved(String key, GeoLocation location) {
        sendNotification("Anda", String.format("%s Move Within daerah bahaya", key));
    }

    private void sendNotification(String title, String content) {


        String NOTIFICATION_CHANNEL_ID = "multi area";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Notif Saya",
                    NotificationManager.IMPORTANCE_DEFAULT);

            notificationChannel.setDescription("Channel Description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.WHITE);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher);

        Notification notification = builder.build();
        notificationManager.notify(new Random().nextInt(), notification);

    }

    @Override
    public void onGeoQueryReady() {

    }

    @Override
    public void onGeoQueryError(DatabaseError error) {

        Toast.makeText(this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoadLocationSucces(List<MyLatLang> latLngs) {

        list = new ArrayList<>();
        for (MyLatLang myLatLang : latLngs) {

            LatLng convert = new LatLng(myLatLang.getLatitude(), myLatLang.getLongitude());
            list.add(convert);
        }

        //Setelah mengambil data dari firestore , Kemudian tampilkan di Map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity2.this);

        // Menghapus map dan menambahkan ulang
        if (mMap != null) {

            mMap.clear();

            //Menambahkan Penanda User
            tambahLokasiUser();

            //add Bahaya
            tambahDaerahBahaya();

        }
    }

    @Override
    public void onLoadLocationFailed(String message) {

        Toast.makeText(this, "" + message, Toast.LENGTH_SHORT).show();

    }
}
