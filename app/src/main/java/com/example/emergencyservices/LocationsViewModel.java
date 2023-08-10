package com.example.emergencyservices;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

class LocationsViewModel extends ViewModel {
    private MutableLiveData<String> profileIdLiveData;

    public LocationsViewModel() {
    }

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


}
