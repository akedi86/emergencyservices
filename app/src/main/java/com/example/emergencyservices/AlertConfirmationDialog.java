package com.example.emergencyservices;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
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

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

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

public class AlertConfirmationDialog extends DialogFragment {
    private static final int PERMISSION_REQUEST_CODE = 498; //Just a random code
    /**
     * If the profile ID varies from one user to another, then you'll need to change it from a static final constant
     * Make it a variable, you can pass it to the dialog fragment using the constructor when launching the Dialog.
     */
    private static final String PROFILE_ID = "profile id"; //TODO: Get valid profile ID
    private final Context context;
    private final int i;
    private TextView locationTxv;
    private Location loc = null; // Changed to Locations from CallLog.Locations
    private boolean sendingInfo = false;

    public FusedLocationProviderClient mFusedLocationClient;

    AlertConfirmationDialog(Activity a, Context c, int i){
        this.context = c;
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
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
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
                        Socket s = new Socket(MainActivity.IP_ADDRESS, MainActivity.PORT);
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
//        getLastLocation();
//        Commenting this so that we can test the alert dialog layout
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

        if (checkPermissions(context)) {
            mFusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
                Location location = task.getResult();
                if (location != null) {
                    String latitude = String.valueOf(location.getLatitude());
                    String longitude = String.valueOf(location.getLongitude());
//                    loc = new Location(latitude, longitude);
                    loc = location;


                    // Use Geocoder to retrieve the address
                    Geocoder geocoder = new Geocoder(context, Locale.getDefault());
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
            String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION};
            ActivityCompat.requestPermissions(
                    requireActivity(),
                    permissions,
                    PERMISSION_REQUEST_CODE
            );
        }
    }

    private void requestNewLocationData() {
        //TODO: Type stuff :)
    }

    public static boolean checkPermissions(Context context) {
        return ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location location = locationResult.getLastLocation();
            if (location != null) {
                String latitude = String.valueOf(location.getLatitude());
                String longitude = String.valueOf(location.getLongitude());
//                loc = new Location(latitude, longitude);
                loc = location;

                // Use Geocoder to retrieve the address
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
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


}
