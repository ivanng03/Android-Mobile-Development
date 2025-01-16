package com.example.cet343assignment;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.DateFormat;
import java.util.Calendar;

public class Edit extends AppCompatActivity {
    // Views
    ImageView editUpload;
    EditText editName, editDesc, editPrice, editDate;
    Button editSave;

    // Image URLs
    String imageURL, currentImage, newImage;

    // Data fields
    String name, desc, date, price, key;

    // Firebase Authentication
    private FirebaseAuth authProfile;
    private FirebaseUser firebaseUsers;

    // Uri for image selection
    Uri uri;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        // Initialize views
        editName = findViewById(R.id.editName);
        editDesc = findViewById(R.id.editDesc);
        editDate = findViewById(R.id.editDate);
        editPrice = findViewById(R.id.editPrice);
        editSave = findViewById(R.id.editSave);
        editUpload = findViewById(R.id.editUpload);

        // Firebase Authentication instance
        authProfile = FirebaseAuth.getInstance();
        firebaseUsers = authProfile.getCurrentUser();

        // Activity Result Launcher for image selection
        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            uri = data.getData();
                            // Use Glide to load the selected image
                            if (uri != null) {
                                Log.d("URI", uri.toString());
                                Glide.with(Edit.this)
                                        .load(uri)
                                        .into(editUpload);
                            }

                        } else {
                            Toast.makeText(Edit.this, "No image selected", Toast.LENGTH_SHORT).show();
                        }

                    }
                }
        );

        // Retrieve data from the intent
        Intent intent = getIntent();
        if (intent != null) {
            name = intent.getStringExtra("Name");
            desc = intent.getStringExtra("description");
            date = intent.getStringExtra("date");
            price = intent.getStringExtra("price");
            currentImage = intent.getStringExtra("uploadImage");
            key = intent.getStringExtra("Key");
            Log.d("UpdateActivity", "Key Received:" + key);

            // Display the existing data in the EditText fields
            editName.setText(name);
            editDesc.setText(desc);
            editDate.setText(date);
            editPrice.setText(price);

            // Load the existing image using Glide
            Glide.with(Edit.this)
                    .load(currentImage)
                    .into(editUpload);
        }

        // Set click listener for image selection
        editUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPicker = new Intent(Intent.ACTION_PICK);
                photoPicker.setType("image/*");
                activityResultLauncher.launch(photoPicker);
            }
        });

        // Set click listener for save button
        editSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });
    }

    // Method to save data to Firebase
    public void saveData() {
        if (uri != null) {
            // Storage reference for the uploaded image
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Images Data")
                    .child(authProfile.getCurrentUser().getUid());

            // Upload the image
            UploadTask uploadTask = storageReference.putFile(uri);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // Getting the download URL for the uploaded image
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    uriTask.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            // Store the URL
                            imageURL = uri.toString();
                            firebaseUsers = authProfile.getCurrentUser();
                            // Use Glide to load and display the image
                            if (imageURL != null) {
                                Glide.with(Edit.this)
                                        .load(imageURL)
                                        .into(editUpload);
                            }
                            // Continue with uploading other data if needed
                            uploadData();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            e.printStackTrace();
                            Toast.makeText(Edit.this, "Failed to get download URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    e.printStackTrace();
                    Toast.makeText(Edit.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(Edit.this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to upload data to Firebase Realtime Database
    public void uploadData() {
        // Get data from EditText fields
        String name = editName.getText().toString();
        String desc = editDesc.getText().toString();
        String date = editDate.getText().toString();
        String price = editPrice.getText().toString();

        String itemId = key;

        // Create Info object
        Info infoClass = new Info(name, desc, date, price, imageURL);




        // Get current date and time
        // String currentDate = DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());

        // Store the data in Firebase Realtime Database
        FirebaseDatabase.getInstance().getReference("Android Asm").child(authProfile.getCurrentUser().getUid())
                .child(itemId)
                .setValue(infoClass).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Delete the old image from storage
                            StorageReference reference = FirebaseStorage.getInstance().getReference().child(currentImage);
                            reference.delete();
                            Toast.makeText(Edit.this, "Saved", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Edit.this, e.getMessage().toString(), Toast.LENGTH_SHORT);
                    }
                });
    }
}
