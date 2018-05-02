package com.example.yohanesdwilistio.androidsql;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.kosalgeek.android.json.JsonConverter;
import com.kosalgeek.genasync12.AsyncResponse;
import com.kosalgeek.genasync12.PostResponseAsyncTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListActivity extends AppCompatActivity {
    /* Activity for display list of contacts */
    private Button btnAdd;
    private ArrayList<Contact> userContact = new ArrayList<>();
    private ListView listContact;
    private Activity activity = this;
    private SimpleAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        btnAdd = findViewById(R.id.btnAddContact);
        listContact = findViewById(R.id.listContact);
        loadAllContacts();
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(activity, FormActivity.class);
                i.putExtra("mode", "insert");
                startActivity(i);
            }
        });
    }

    public void loadAllContacts(){
        /* Load all contacts data from database
         * The JSON Text will be converted into ArrayList of Contact */
        PostResponseAsyncTask taskRead = new PostResponseAsyncTask(ListActivity.this, "Fetching all contacts...", new AsyncResponse() {
            @Override
            public void processFinish(String s) {
                userContact = new JsonConverter<Contact>().toArrayList(s, Contact.class);
                /* The arraylist converted into List to displayed in ListView */
                List<Map<String, String>> contact = insertContactIntoList(userContact);
                /* We using simple list item 2 layout that display Item and Sub-Item.
                * In item we'll display full name, and in sub-item we'll display email address */
                adapter = new SimpleAdapter(activity, contact, android.R.layout.simple_list_item_2,
                        new String[]{"name", "email"}, new int[]{android.R.id.text1, android.R.id.text2});
                /* We adjust ListView height so all data can be displayed on screen */
                new ListViewAdjustment().adjustListViewwithSimple(listContact, adapter);
                listContact.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        /* Event when a contact selected from list, we'll displaying contact details */
                        registerForContextMenu(listContact);
                        listContact.showContextMenu();
                        TextView tv = view.findViewById(android.R.id.text1);
                        String name = tv.getText().toString();
                        ViewGroup viewGroup = view.findViewById(R.id.layoutList);
                        View v = getLayoutInflater().inflate(R.layout.detail_contact, viewGroup);
                        viewDetailContact(v, name);
                    }
                });
            }
        });
        /* Change the IP address here (192.168.43.138) with your computer IP Address or native emulator IP Address
         * (10.0.2.2 if you are using AVD from Android Studio) */
        taskRead.execute("http://192.168.43.138/androidsql/contact");
    }

    public List<Map<String, String>> insertContactIntoList(ArrayList<Contact> userContact){
        /* Converting arraylist into List of Map which keys are full name and values are email address */
        List<Map<String, String>> contact = new ArrayList<>();
        if (userContact.size() > 0){
            for (int i = 0; i < userContact.size(); i++){
                Map<String, String> contact_detail = new HashMap<>();
                contact_detail.put("name", userContact.get(i).first_name + " " + userContact.get(i).last_name);
                contact_detail.put("email", userContact.get(i).email);
                contact.add(contact_detail);
            }
        } else {
            Map<String, String> contact_detail = new HashMap<>();
            contact_detail.put("name", "No contact saved here");
            contact_detail.put("email", "Start adding new contact!");
            contact.add(contact_detail);
        }
        return contact;
    }

    public void viewDetailContact(View v, String name){
        /* To display detail from selected contact. Here we can do update or delete the contact. */
        TextView txtDetailEmail = v.findViewById(R.id.txtDetailEmail);
        TextView txtDetailPhone = v.findViewById(R.id.txtDetailPhone);
        String[] names = name.split(" ");
        for (int i = 0; i < userContact.size(); i++){
            if (userContact.get(i).first_name.contains(names[0]) && userContact.get(i).last_name.contains(names[names.length - 1])){
                txtDetailEmail.setText(userContact.get(i).email);
                txtDetailPhone.setText(userContact.get(i).phone);
                final int finalI = i;
                new AlertDialog.Builder(this).setTitle(userContact.get(i).prefix + " " + name).setView(v).
                        setPositiveButton("Update", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                /* When Update button clicked, we'll move into FormActivity, but with update mode
                                 * and the form is filled with current data selected */
                                Intent j = new Intent(activity, FormActivity.class);
                                j.putExtra("mode", "update");
                                j.putExtra("id", userContact.get(finalI).id);
                                j.putExtra("prefix", userContact.get(finalI).prefix);
                                j.putExtra("firstname", userContact.get(finalI).first_name);
                                j.putExtra("lastname", userContact.get(finalI).last_name);
                                j.putExtra("email", userContact.get(finalI).email);
                                j.putExtra("phone", userContact.get(finalI).phone);
                                startActivity(j);
                            }
                        }).setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /* When Delete button clicked, a confirmation dialog is appear. If user are really sure
                        * to delete, the contact will deleted from database and refresh the list */
                        new AlertDialog.Builder(ListActivity.this).setTitle("Confirm Delete")
                                .setMessage("Are you sure you want to delete selected contact?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        HashMap<String, String> postData = new HashMap<>();
                                        postData.put("id", String.valueOf(userContact.get(finalI).id));
                                        PostResponseAsyncTask taskUpdate = new PostResponseAsyncTask(ListActivity.this, postData, "Deleting contacts...", new AsyncResponse() {
                                            @Override
                                            public void processFinish(String s) {
                                                if (s.equals("success")){
                                                    Toast.makeText(ListActivity.this, "Data deleted successfully", Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                                        taskUpdate.execute("http://192.168.43.138/androidsql/contact/delete/" + String.valueOf(userContact.get(finalI).id));
                                        loadAllContacts();
                                    }
                                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
                    }
                }).show();
            }
        }
    }
}
