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
import com.google.android.material.snackbar.Snackbar;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    /**
     * TODO: Extracts constants like these (IP_ADDRESS & PORT) to a separate class like Constants.java
     * This helps make it easier to access all the constants throughout the project, change them and stuff.
     */
    public static final String IP_ADDRESS = ""; //TODO: Add valid server IP Address
    public static final int PORT = 8000; //TODO: Add valid server port number

    ImageView policeBtn, hospitalBtn, breakdownBtn, generalBtn, fireBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        policeBtn = findViewById(R.id.policeBtn);
        hospitalBtn = findViewById(R.id.hospitalBtn);
        breakdownBtn = findViewById(R.id.breakDownBtn);
        generalBtn = findViewById(R.id.generalBtn);
        fireBtn = findViewById(R.id.fireBtn);

        policeBtn.setOnClickListener(v -> {

            new AlertConfirmationDialog(MainActivity.this, MainActivity.this, 0).show(
                    getSupportFragmentManager(),
                    "MY FRAGMENT"
            );
        });

        hospitalBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LocationsActivity.class);
            startActivity(intent);
        });

        breakdownBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LocationsActivity.class);
            startActivity(intent);
        });

        generalBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LocationsActivity.class);
            startActivity(intent);
        });

        fireBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LocationsActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        new AlertConfirmationDialog(MainActivity.this, MainActivity.this, 0).show(
                getSupportFragmentManager(),
                "MY FRAGMENT"
        );
    }
}




