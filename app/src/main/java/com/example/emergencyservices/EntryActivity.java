package com.example.emergencyservices;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;

public class EntryActivity extends AppCompatActivity {
    private boolean profileAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entry_layout);
        Button createProfileBtn = findViewById(R.id.createProfileBtn);
        createProfileBtn.setOnClickListener(view -> {
            if(profileAvailable){
                Intent i = new Intent(getApplicationContext(), CreateProfileActivity.class);
                startActivity(i);
                EntryActivity.this.finish();
            }
        });
        new Thread(()->{
            if(checkProfile()){
               profileAvailable = true;
               runOnUiThread(()-> createProfileBtn.setAlpha(1f));
            }
        }).start();
    }

    private boolean checkProfile(){
        SQLiteDatabase dbRead = new DBHelper(getApplicationContext()).getReadableDatabase();
        Cursor c = dbRead.query("profile", new String[]{"email"}, null, null, null, null, null);
        if(c.moveToNext()){
            c.close();
            return true;
        }
        c.close();
        return false;
    }
}