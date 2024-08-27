package com.example.contactsapp;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements ContactActionListener {

    private RecyclerView recyclerView;
    private ContactAdapter adapter;
    private ArrayList<Contact> contactList;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        contactList = new ArrayList<>();
        databaseHelper = new DatabaseHelper(this);

        // Load contacts from the database
        loadContacts();

        // Set up the Floating Action Button
        findViewById(R.id.fab).setOnClickListener(v -> showAddContactDialog());
    }

    @Override
    public void onContactSelected(Contact contact) {
        showContactOptions(contact);
    }

    public void showContactOptions(Contact contact) {
        String[] options = {"Update", "Delete", "Call"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(contact.getName());
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // Update
                    showUpdateContactDialog(contact);
                    break;
                case 1: // Delete
                    deleteContact(contact);
                    break;
                case 2: // Call
                    makePhoneCall(contact.getPhone());
                    break;
            }
        });
        builder.show();
    }

    public void showAddContactDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.add_contact_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        EditText nameInput = dialogView.findViewById(R.id.contact_name_input);
        EditText phoneInput = dialogView.findViewById(R.id.contact_phone_input);
        Button saveButton = dialogView.findViewById(R.id.save_button);

        saveButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString();
            String phone = phoneInput.getText().toString();

            if (!name.isEmpty() && !phone.isEmpty()) {
                long id = databaseHelper.addContact(name, phone);
                Contact newContact = new Contact((int) id, name, phone);
                contactList.add(newContact);
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void showUpdateContactDialog(Contact contact) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.add_contact_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        EditText nameInput = dialogView.findViewById(R.id.contact_name_input);
        EditText phoneInput = dialogView.findViewById(R.id.contact_phone_input);
        Button saveButton = dialogView.findViewById(R.id.save_button);

        nameInput.setText(contact.getName());
        phoneInput.setText(contact.getPhone());

        saveButton.setOnClickListener(v -> {
            String newName = nameInput.getText().toString();
            String newPhone = phoneInput.getText().toString();

            if (!newName.isEmpty() && !newPhone.isEmpty()) {
                contact.setName(newName);
                contact.setPhone(newPhone);
                databaseHelper.updateContact(contact);
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void deleteContact(Contact contact) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Contact");
        builder.setMessage("Are you sure you want to delete this contact?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            databaseHelper.deleteContact(contact.getId());
            contactList.remove(contact);
            adapter.notifyDataSetChanged();
        });
        builder.setNegativeButton("No", null);
        builder.show();
    }

    public void makePhoneCall(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(intent);
    }

    private void loadContacts() {
        contactList = databaseHelper.getAllContacts();
        adapter = new ContactAdapter(this, contactList, databaseHelper, this); // Pass the ContactActionListener
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
}
