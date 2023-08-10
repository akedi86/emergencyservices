package com.example.emergencyservices;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        ImageView[] alertsBtn = {
                findViewById(R.id.policeBtn),
                findViewById(R.id.hospitalBtn),
                findViewById(R.id.breakDownBtn),
                findViewById(R.id.generalBtn),
                findViewById(R.id.fireBtn)
        };

        for (int i = 0; i < alertsBtn.length; i++) {
            final int k = i;
            alertsBtn[i].setOnClickListener(view -> {
                Intent intent = new Intent(MainActivity.this, Locations.class);
                startActivity(intent);
            });
        }
    }
}

public class Locations extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locations);

        LocationsViewModel locationsViewModel = new ViewModelProvider(this).get(LocationsViewModel.class);

        // Observe the LiveData to get the profile ID
        locationsViewModel.getProfileIdLiveData(getApplicationContext()).observe(this, (Observer<String>) profileId -> {
            // Use the profile ID as needed
        });

        String latitude = getIntent().getStringExtra("latitude");
        String longitude = getIntent().getStringExtra("longitude");
    }
}

class LocationsViewModel extends ViewModel {
    private MutableLiveData<String> profileIdLiveData;

    public LiveData<String> getProfileIdLiveData(Context context) {
        if (profileIdLiveData == null) {
            profileIdLiveData = new MutableLiveData<>();
            loadProfileId(context);
        }
        return profileIdLiveData;
    }

    private void loadProfileId(Context context) {
        SQLiteDatabase dbRead = new DBHelper(context).getReadableDatabase();
        Cursor c = dbRead.query("profile", new String[]{"email"}, null, null, null, null, null);

        if (c.moveToFirst()) {
            profileIdLiveData.setValue(c.getString(0));
        }

        c.close();
        dbRead.close();
    }



    public static boolean checkPermissions(Context context) {
                return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            }
        }

        public class AlertConfirmationDialog extends DialogFragment {
        private final Context context;
        private final int i;
        private TextView locationTxv;
        private Locations loc = null; // Changed to Locations from CallLog.Locations
        private boolean sendingInfo = false;

        AlertConfirmationDialog(Activity a, Context c, int i){
            context = c;
            this.i = i;
        }
        @Override
        public void onStart() {
            super.onStart();
            configureWindow();
        }

        private void configureWindow() {
            Dialog dialog = getDialog();
            if (dialog != null) {
                Window window = dialog.getWindow();
                if (window != null) {
                    window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    window.setBackgroundDrawableResource(R.drawable.white_background_rounded_corners);
                }
            }
        }

        @Override
        public void onCreate(Bundle savedInstanceBundle) {
            super.onCreate(savedInstanceBundle);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.alert_confirmation_dialog_layout, container, false); // Use 'container' as the parent
            ImageView iconIv = v.findViewById(R.id.icon);
            locationTxv = v.findViewById(R.id.locationTxt);
            Button confirmBtn = v.findViewById(R.id.confirmBtn);
            confirmBtn.setOnClickListener(view -> {
                if (loc != null && !sendingInfo) {
                    sendingInfo = true;
                    new Thread(()->{
                        try{
                            Socket s = new Socket(IP_ADDRESS, PORT);
                            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
                            dos.writeInt(2); //user connected
                            dos.writeInt(2); //sending alert
                            dos.writeUTF(PROFILE_ID); //profile id
                            dos.writeInt(i); //alert type
                            dos.writeUTF(loc.getLatitude() + "/" + loc.getLongitude());

                            s.close();
                        }catch (Exception e){
                            Handler h = new Handler(Looper.getMainLooper());
                            h.post(()-> Toast.makeText(context, "Check your network connection", Toast.LENGTH_SHORT).show());
                        }finally {
                            sendingInfo = false;
                        }
                    }).start();
                }
            });
            switch (i){
                case 0 : iconIv.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.police)); break;
                case 1 : iconIv.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.hospital)); break;
                case 2 : iconIv.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.breakdown)); break;
                case 3 : iconIv.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.generalemergency)); break;
                case 4 : iconIv.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.fire)); break;
            }
            locationTxv.setText(R.string.retrieving_location);
            return v;
        }

        @Override
        public void onResume() {
            super.onResume();
            getLastLocation();

            Window window = Objects.requireNonNull(getDialog()).getWindow();
            if (window != null) {
                window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                window.setBackgroundDrawableResource(R.drawable.white_background_rounded_corners);
            }
        }


        @SuppressLint("MissingPermission")
    private void getLastLocation() {
        locationTxv.setText(R.string.retrieving_location);
        loc = null;

            if (checkPermissions(this)) {
                mFusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
                    Location location = task.getResult();
                    if (location != null) {
                        String latitude = String.valueOf(location.getLatitude());
                        String longitude = String.valueOf(location.getLongitude());
                        loc = new Locations(latitude, longitude);


                        // Use Geocoder to retrieve the address
                        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                        try {
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            if (addresses != null && !addresses.isEmpty()) {
                                Address address = addresses.get(0);
                                String addressLine = address.getAddressLine(0);
                                locationTxv.setText(addressLine);
                                locationTxv.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
                            } else {
                                locationTxv.setText(R.string.location_not_found);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            locationTxv.setText(R.string.location_not_found);
                        }
                    } else {
                        requestNewLocationData();
                    }
                });
            } else {
                requestPermissions();
            }
        }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location location = locationResult.getLastLocation();
            if (location != null) {
                String latitude = String.valueOf(location.getLatitude());
                String longitude = String.valueOf(location.getLongitude());
                loc = new Locations(latitude, longitude);

                // Use Geocoder to retrieve the address
                Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        String addressLine = address.getAddressLine(0);
                        locationTxv.setText(addressLine);
                        locationTxv.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
                    } else {
                        locationTxv.setText(R.string.location_not_found);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    locationTxv.setText(R.string.location_not_found);
                }
            }
        }
    };
