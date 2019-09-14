package sastra.panji.dhimas.firebase;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button send;
    EditText name,age,full;
    String nama,umur,nm_pjg;
    Location location;
    Map<Location,Lokasi> lokasiMap;
    @RequiresApi(api = Build.VERSION_CODES.M)


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    send = findViewById(R.id.btn_Send);
    name = findViewById(R.id.Edit_Name);
    age =  findViewById(R.id.Edit_Umur);
    full = findViewById(R.id.Full);






    nm_pjg = "Sastra";

    // Write a message to the database
    send.setOnClickListener(this);
    }
    void CekValid(){
        if (TextUtils.isEmpty(nama)){
            Toast.makeText(this,"Masukkan Nama !",Toast.LENGTH_SHORT);
            return ;
        }else if (TextUtils.isEmpty(umur)){
            Toast.makeText(this,"Masukkan Umur !",Toast.LENGTH_SHORT);
            return;
        }else{
            Toast.makeText(this,"Isi Dengan Benar  !",Toast.LENGTH_SHORT);
            return;
        }
    }

    void checkLocation(){

    }

    void SendData(){

        String sas = full.getText().toString();
        String tra = name.getText().toString();
        String jos = age.getText().toString();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("user");
        DatabaseReference usersRef = myRef.child(sas);
        DatabaseReference namanya = usersRef.child("Name");
        DatabaseReference umurnya = usersRef.child("Umur");
        DatabaseReference lokasinya = usersRef.child("Lokasi");
      final  DatabaseReference lat = lokasinya.child("lat");
    final    DatabaseReference longitude = lokasinya.child("long");
        DatabaseReference nm_panjang = usersRef.child("Nama Panjang :");

        FusedLocationProviderClient mFusedLocation = LocationServices.getFusedLocationProviderClient(this);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MainActivity.this,"Harus Ada Acces Lokasi",Toast.LENGTH_LONG).show();

        }
        mFusedLocation.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    String Latit = Double.toString(location.getLatitude());
                    String Longit =  Double.toString(location.getLongitude());

                    lat.setValue(Latit);
                    longitude.setValue(Longit);
                    // Do it all with location
                    Log.d("My Current location", "Lat : " + location.getLatitude() + " Long : " + location.getLongitude());
                    // Display in Toast
                    Toast.makeText(MainActivity.this,
                            "Lat : " + location.getLatitude() + " Long : " + location.getLongitude(),
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        namanya.setValue(tra);
        umurnya.setValue(jos);


        nm_panjang.setValue(sas).addOnSuccessListener(this, new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                name.setText("");
                age.setText("");
                full.setText("");
                Toast.makeText(MainActivity.this,"Berhasil Menambahkan Data !!",Toast.LENGTH_LONG).show();

            }
        });


        }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_Send:
//                CekValid();
                SendData();
                break;
        }
    }

    public void Lokasinya(View view) {
        checkLocation();
    }
}
