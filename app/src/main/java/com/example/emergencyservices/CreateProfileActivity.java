package com.example.emergencyservices;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.LinkedList;

public class CreateProfileActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    private String bloodType = null;
    private EditText firstNameEdt;
    private EditText lastNameEdt;
    private EditText emailEdt;
    private EditText mobileEdt;
    private static LinearLayout allergiesContainer;
    private static LinearLayout conditionsContainer;
    private static LinearLayout contactsContainer;
    private static final LinkedList<String> allergies = new LinkedList<>();
    private static final LinkedList<String> conditions = new LinkedList<>();
    private static final LinkedList<Contact> contacts = new LinkedList<>();
    private boolean validToSave;
    private boolean inActivity;
    private int mode; //1 for create, 2 for view;
    private Button addAllergyBtn;
    private Button addConditionBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);
        inActivity = true;
        mode = getIntent().getIntExtra("mode", 1);
        firstNameEdt = findViewById(R.id.firstName);
        lastNameEdt = findViewById(R.id.lastName);
        emailEdt = findViewById(R.id.email);
        mobileEdt = findViewById(R.id.mobileNumber);
        addAllergyBtn = findViewById(R.id.addAllergiesBtn);
        addAllergyBtn.setOnClickListener(e ->{
            if(allergies.size() < 2){
                new AddMedicalInformationDialog(getApplicationContext(), 1).show(getSupportFragmentManager(), null);
            }
        });
        addConditionBtn = findViewById(R.id.addConditionsBtn);
        addConditionBtn.setOnClickListener(e ->{
            if(conditions.size() < 2){
                new AddMedicalInformationDialog(getApplicationContext(), 2).show(getSupportFragmentManager(), null);
            }
        });
        allergiesContainer = findViewById(R.id.allergiesContainer);
        conditionsContainer = findViewById(R.id.conditionsContainer);
        contactsContainer = findViewById(R.id.contactsContainer);
        Button saveBtn = findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(view -> {
            if(validToSave){
                save();
            }
        });
        Spinner spinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.bloodTypesArray, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        if(mode == 1) {
            startValidator();
        }else {
            showAttributes();
        }
    }

    private void showAttributes(){
        addAllergyBtn.setVisibility(View.INVISIBLE);
        addAllergyBtn.setClickable(false);
        addConditionBtn.setVisibility(View.INVISIBLE);
        addConditionBtn.setClickable(false);

        //retrieve elements from the database
        new Thread(()->{
            SQLiteDatabase dbRead = new DBHelper(getApplicationContext()).getReadableDatabase();
            final Cursor c = dbRead.query("profile", new String[]{"email", "firstname", "lastname", "mobile", "bloodtype"}, null, null, null, null, null);
            c.moveToNext();
            runOnUiThread(()->{
                emailEdt.setText(c.getString(0));
                firstNameEdt.setText(c.getString(1));
                lastNameEdt.setText(c.getString(2));
                mobileEdt.setText(c.getString(3));
                //set spinner selection here
            });
            c.close();
            final Cursor c1 = dbRead.query("allergies", new String[]{"name"}, null, null, null, null, null);
            while (c1.moveToNext()){
                View v = getLayoutInflater().inflate(R.layout.medical_information_layout, null);
                TextView nameTv = v.findViewById(R.id.condition);
                nameTv.setText(c1.getString(0));
                runOnUiThread(()-> allergiesContainer.addView(v));
            }
            c1.close();
            final Cursor c2 = dbRead.query("conditions", new String[]{"name"}, null, null, null, null, null);
            while(c2.moveToNext()){
                View v = getLayoutInflater().inflate(R.layout.medical_information_layout, null);
                TextView nameTv = v.findViewById(R.id.condition);
                ImageView cancelBtn = v.findViewById(R.id.cancelBtn);
                cancelBtn.setClickable(false);
                cancelBtn.setVisibility(View.INVISIBLE);
                nameTv.setText(c2.getString(0));
                runOnUiThread(()-> conditionsContainer.addView(v));
            }
            c2.close();
            final Cursor c3 = dbRead.query("contacts", new String[]{"fullname", "mobile"}, null, null, null, null, null);
            while (c3.moveToNext()){
                View v = getLayoutInflater().inflate(R.layout.contact_layout, null);
                TextView nameTv = v.findViewById(R.id.fullname);
                TextView mobileTv = v.findViewById(R.id.mobile);
                ImageView cancelBtn = v.findViewById(R.id.cancelBtn);
                cancelBtn.setClickable(false);
                cancelBtn.setVisibility(View.INVISIBLE);
                nameTv.setText(c3.getString(0));
                mobileTv.setText(c3.getString(1));
                runOnUiThread(()-> contactsContainer.addView(v));
            }
            c3.close();
            dbRead.close();
        }).start();
    }

    private void save(){
        new Thread(()->{
            Socket s = null;
            try{
                s = new Socket(MainActivity.IP_ADDRESS, MainActivity.PORT);
                DataOutputStream dos = new DataOutputStream(s.getOutputStream());
                dos.writeInt(2); //user connect
                dos.writeInt(1); //create user profile
                dos.writeUTF(firstNameEdt.getText().toString().trim());
                dos.writeUTF(lastNameEdt.getText().toString().trim());
                dos.writeUTF(emailEdt.getText().toString().trim());
                dos.writeUTF(mobileEdt.getText().toString().trim());
                dos.writeUTF(bloodType);
                dos.writeInt(allergies.size());
                int i = 0;
                while(i < allergies.size()){
                    dos.writeUTF(allergies.get(i++));
                }
                dos.writeInt(conditions.size());
                i = 0;
                while(i < conditions.size()){
                    dos.writeUTF(conditions.get(i++));
                }
                dos.writeInt(contacts.size());
                i = 0;
                while(i < contacts.size()){
                    dos.writeUTF(contacts.get(i).fullName);
                    dos.writeUTF(contacts.get(i++).mobile);
                }
                DataInputStream dis = new DataInputStream(s.getInputStream());
                boolean success = dis.readBoolean();
                dis.close();
                dos.close();
                s.close();
                if(success){
                    //update in the database
                    ContentValues cV = new ContentValues();
                    cV.put("firstname", firstNameEdt.getText().toString().trim());
                    cV.put("lastname", lastNameEdt.getText().toString().trim());
                    cV.put("email", emailEdt.getText().toString().trim());
                    cV.put("mobile", mobileEdt.getText().toString().trim());
                    cV.put("bloodtype", bloodType);
                    ContentValues cV1 = new ContentValues();
                    for(String al : allergies){
                        cV1.put("name", al);
                    }
                    ContentValues cV2 = new ContentValues();
                    for(String cond : conditions){
                        cV2.put("name", cond);
                    }
                    ContentValues cV3 = new ContentValues();
                    for(Contact cont : contacts){
                        cV3.put("fullname", cont.fullName);
                        cV3.put("mobile", cont.mobile);
                    }
                    //write the db
                    SQLiteDatabase dbWrite = new DBHelper(getApplicationContext()).getWritableDatabase();
                    dbWrite.insert("profile", null, cV);
                    dbWrite.insert("allergies", null, cV1);
                    dbWrite.insert("conditions", null, cV2);
                    dbWrite.insert("contacts", null, cV3);
                    dbWrite.close();
                    runOnUiThread(()->{
                        if(mode == 1){
                            Intent intent = new Intent(this, MainActivity.class);
                            startActivity(intent);
                        }
                        CreateProfileActivity.this.finish();
                    });
                }else {
                    runOnUiThread(() -> Toast.makeText(this, "Something happened, try again", Toast.LENGTH_SHORT).show());
                }
            }catch(Exception e){
                try{
                    s.close();
                }catch (Exception ex){

                }
            }
        }).start();
    }

    private void startValidator(){
        new Thread(()->{
            while(inActivity){
                validateFields();
                try{
                    Thread.sleep(500);
                }catch(Exception e){
                    //interrupt ex
                }
            }
        }).start();
    }

    void validateFields(){
        if(mode == 1){
            validToSave = (validateField(firstNameEdt, 2) && validateField(lastNameEdt, 2) && validateField(emailEdt, -1) && validateField(mobileEdt, 10) && contacts.size() > 0);
        }
    }

    boolean validateField(EditText edt, int count){
        if(count == -1){
            return emailValid(edt.getText().toString().trim());
        }else if(edt.getText().toString().trim().length() >= count){
            return true;
        }
        return false;
    }

    boolean emailValid(String email){

        return true;
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        bloodType = (String)parent.getItemAtPosition(pos);
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    public static class AddContactDialog extends DialogFragment {
        private final Context context;

        AddContactDialog(Context c){
            context = c;
        }

        @Override
        public void onCreate(Bundle savedInstanceBundle) {
            super.onCreate(savedInstanceBundle);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceBundle) {
            View v = inflater.inflate(R.layout.add_contact_fragment_layout, null);
            EditText fullNameEdt = v.findViewById(R.id.fullName);
            EditText mobileNoEdt = v.findViewById(R.id.phoneNumber);
            Button saveBtn = v.findViewById(R.id.saveBtn);
            saveBtn.setOnClickListener(view -> {
                String mobile, fullname;
                if((fullname = fullNameEdt.getText().toString()).length() > 0 && (mobile = mobileNoEdt.getText().toString()).length() > 0){
                    Contact c = new Contact(fullname, mobile);
                    contacts.add(c);
                    View v1 = inflater.inflate(R.layout.contact_layout, null);
                    TextView fullnameTxv = v1.findViewById(R.id.fullname);
                    TextView mobileTxv = v1.findViewById(R.id.mobile);
                    ImageView canceBtn = v1.findViewById(R.id.cancelBtn);
                    canceBtn.setOnClickListener(e ->{
                        contacts.remove(c);
                        contactsContainer.removeView(v1);
                    });
                    fullnameTxv.setText(c.fullName);
                    mobileTxv.setText(c.mobile);
                    contactsContainer.addView(v1);
                }
            });
            return v;
        }

        @Override
        public void onResume(){
            super.onResume();
            Window window = getDialog().getWindow();
            if(window == null) return;
            window.setLayout(-2, -2);
            window.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.white_background_rounded_corners));
        }
    }

    public static class AddMedicalInformationDialog extends DialogFragment {
        private final int type;
        private final Context context;

        AddMedicalInformationDialog(Context c, int type){
            this.type = type;
            this.context = c;
        }

        @Override
        public void onCreate(Bundle savedInstanceBundle) {
            super.onCreate(savedInstanceBundle);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceBundle) {
            View v = inflater.inflate(R.layout.add_medical_information_fragment, null);
            TextView titleTxv = v.findViewById(R.id.titleTxv);
            titleTxv.setHint((type == 1) ? "Add allergy" : "Add condition");
            EditText conditionEdt = v.findViewById(R.id.conditionEdt);
            conditionEdt.setHint((type == 1) ? "Add allergy" : "Add condition");
            Button saveBtn = v.findViewById(R.id.saveBtn);
            saveBtn.setOnClickListener(view -> {
                String cond;
                if((cond = conditionEdt.getText().toString()).trim().length() > 0){
                    if(type == 1){
                        allergies.add(cond);
                    }else {
                        conditions.add(cond);
                    }
                    View v1 = inflater.inflate(R.layout.medical_information_layout, null);
                    TextView nameTv = v1.findViewById(R.id.condition);
                    nameTv.setText(cond);
                    ImageView cancelBtn = v1.findViewById(R.id.cancelBtn);
                    cancelBtn.setOnClickListener(e ->{
                        if(type == 1){
                            allergies.remove(cond);
                            allergiesContainer.removeView(v1);
                        }else {
                            conditions.remove(cond);
                            conditionsContainer.removeView(v1);
                        }
                    });
                    if(type == 1){
                        allergiesContainer.addView(v1);
                    }else {
                        conditionsContainer.addView(v1);
                    }
                    AddMedicalInformationDialog.this.dismiss();
                }
            });
            return v;
        }

        @Override
        public void onResume(){
            super.onResume();
            Window window = getDialog().getWindow();
            if(window == null) return;
            window.setLayout(-2, -2);
            window.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.white_background_rounded_corners));
        }
    }

    private static class Contact {
        private String fullName;
        private String mobile;

        Contact(String fullName, String mobile){
            this.fullName = fullName;
            this.mobile = mobile;
        }
    }
}