package com.example.yohanesdwilistio.androidsql;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.kosalgeek.genasync12.AsyncResponse;
import com.kosalgeek.genasync12.PostResponseAsyncTask;

import java.util.HashMap;
import java.util.regex.Pattern;

public class FormActivity extends AppCompatActivity {
    /* Activity that display form to fill contact's data to be inserted/updated */
    String mode;
    private RadioGroup salutation;
    private EditText firstname;
    private EditText lastname;
    private EditText email;
    private EditText phone;
    private Button insert;
    private Activity activity = this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        init();
        /* First we need to fetch form mode, insert or update */
        mode = getIntent().getExtras().getString("mode");
        if (mode.equals("update")){
            /* If the form mode is update, we'll change the title and button.
             * We're also fill the form with the current data selected */
            getSupportActionBar().setTitle("Update Data");
            insert.setText("Update");
            switch (getIntent().getExtras().getString("prefix")){
                case "Mr.":
                    salutation.check(R.id.radioButton);
                    break;
                case "Mrs.":
                    salutation.check(R.id.radioButton2);
                    break;
                case "Ms.":
                    salutation.check(R.id.radioButton3);
                    break;
            }
            firstname.setText(getIntent().getExtras().getString("firstname"));
            lastname.setText(getIntent().getExtras().getString("lastname"));
            email.setText(getIntent().getExtras().getString("email"));
            phone.setText(getIntent().getExtras().getString("phone"));
        }
    }
    @Override
    public boolean onSupportNavigateUp(){
        super.onBackPressed();
        return true;
    }
    private void init(){
        salutation = findViewById(R.id.rgSalutation);
        firstname = findViewById(R.id.txtFname);
        lastname = findViewById(R.id.txtLname);
        email = findViewById(R.id.txtEmail);
        phone = findViewById(R.id.txtPhone);
        insert = findViewById(R.id.btnInsert);
        insert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* Action when insert/update button clicked */
                String fname = firstname.getText().toString();
                String lname = lastname.getText().toString();
                String mail = email.getText().toString();
                String phne = phone.getText().toString();
                /* First we'll check are the data filled completely */
                if (validateDataInput(fname, lname, mail, phne)){
                    Toast.makeText(FormActivity.this, "Please enter data correctly", Toast.LENGTH_LONG).show();
                } else {
                    /* The data to sent with POST method into server, stored in a HashMap. */
                    HashMap<String, String> postData = new HashMap<>();
                    postData.put("salutation", getSalutation());
                    postData.put("firstname", fname);
                    postData.put("lastname", lname);
                    postData.put("email", mail);
                    postData.put("phone", phne);
                    if (mode.equals("update")){
                        /* If the form is in update mode, we also need to get the contact ID to sent into server */
                        postData.put("id", String.valueOf(getIntent().getExtras().getInt("id")));
                        PostResponseAsyncTask taskUpdate = new PostResponseAsyncTask(FormActivity.this, postData, "Updating contacts...", new AsyncResponse() {
                            @Override
                            public void processFinish(String s) {
                                if (s.equals("success")){
                                    Toast.makeText(FormActivity.this, "Data updated successfully", Toast.LENGTH_LONG).show();
                                    Intent i = new Intent(activity, ListActivity.class);
                                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(i);
                                    finish();
                                }
                            }
                        });
                        taskUpdate.execute("http://192.168.43.138/androidsql/contact/update/" + String.valueOf(getIntent().getExtras().getInt("id")));
                    } else {
                        PostResponseAsyncTask taskInsert = new PostResponseAsyncTask(FormActivity.this, postData, "Adding contacts...", new AsyncResponse() {
                            @Override
                            public void processFinish(String s) {
                                if (s.equals("success")){
                                    Toast.makeText(FormActivity.this, "Data added successfully", Toast.LENGTH_LONG).show();
                                    Intent i = new Intent(activity, ListActivity.class);
                                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(i);
                                    finish();
                                }
                            }
                        });
                        /* Once again, change the IP address here (192.168.43.138) with your computer IP Address or native emulator IP Address
                         * (10.0.2.2 if you are using AVD from Android Studio) */
                        taskInsert.execute("http://192.168.43.138/androidsql/contact/insert");
                    }
                }
            }
        });
    }

    private String getSalutation(){
        switch (salutation.getCheckedRadioButtonId()){
            case R.id.radioButton:
                return "Mr.";
            case R.id.radioButton2:
                return "Mrs.";
            case R.id.radioButton3:
                return "Ms.";
        }
        return null;
    }

    private boolean validateDataInput(String fname, String lname, String mail, String phone){
        return fname.isEmpty() || lname.isEmpty() || (mail.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(mail).matches()) || phone.isEmpty();
    }
}
