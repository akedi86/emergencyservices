package com.example.emergencyservices;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

public class LocationsActivity extends AppCompatActivity {

    private LocationsViewModel locationsViewModel;
    private String profileID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        locationsViewModel = new ViewModelProvider(this).get(LocationsViewModel.class);
//
//        // Observe the LiveData to get the profile ID
//        locationsViewModel.getProfileIdLiveData(getApplicationContext()).observe(this, (Observer<String>) profileId -> {
//            // Use the profile ID as needed
//        });
        getProfileID();

        String latitude = getIntent().getStringExtra("latitude");
        String longitude = getIntent().getStringExtra("longitude");
    }

    @Override
    protected void onResume() {
        super.onResume();

        Toast.makeText(this, "Profile ID is: "+profileID, Toast.LENGTH_SHORT).show();
    }

    public void getProfileID(){
        SQLiteDatabase dbRead = new DBHelper(this).getReadableDatabase();
        Cursor c = dbRead.query("profile", new String[]{"email"}, null, null, null, null, null);

        if (c.moveToFirst()) {
            profileID = c.getString(0);
            Toast.makeText(this, "Profile ID 2 is: "+profileID, Toast.LENGTH_SHORT).show();
        }

        c.close();
        dbRead.close();
    }
}