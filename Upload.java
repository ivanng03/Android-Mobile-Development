package com.example.cet343assignment;


import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class Upload extends AppCompatActivity {
    // Declare UI elements
    private ImageView upload;
    EditText uploadName;
    EditText uploadDesc;
    EditText uploadDate;
    EditText uploadPrice;
    Button saveItem;

    // Store image URL
    String image;

    // Firebase authentication
    private FirebaseAuth authProfile;
    private FirebaseUser firebaseUsers;

    // Store selected image URI
    Uri uri_data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        // Initialize UI elements
        uploadName = findViewById(R.id.uploadName);
        uploadDesc = findViewById(R.id.uploadDesc);
        uploadDate = findViewById(R.id.uploadDate);
        uploadPrice = findViewById(R.id.uploadPrice);
        saveItem = findViewById(R.id.saveItem);
        upload = findViewById(R.id.upload);

        // Initialize Firebase authentication
        authProfile = FirebaseAuth.getInstance();
        firebaseUsers = authProfile.getCurrentUser();

        // Register activity result launcher for image selection
        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult act_result) {
                        if(act_result.getResultCode() == Activity.RESULT_OK){
                            // Handle image selection
                            Intent data = act_result.getData();
                            uri_data = data.getData();
                            if(uri_data !=null){
                                Log.d("URI", uri_data.toString());
                                // Load selected image using Glide library
                                Glide.with(Upload.this)
                                        .load(uri_data)
                                        .into(upload);
                            }
                        } else {
                            Toast.makeText(Upload.this, "Please Select Image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // Set click listener for "Save" button
        saveItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addInfo();
            }
        });

        // Set click listener for "Upload" button to select image
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPicker = new Intent(Intent.ACTION_PICK);
                photoPicker.setType("image/*");
                activityResultLauncher.launch(photoPicker);
            }
        });
    }

    // Method to upload data to Firebase Realtime Database
    public void uploadData() {
        // Get data from input fields
        String name = uploadName.getText().toString();
        String desc = uploadDesc.getText().toString();
        String date = uploadDate.getText().toString();
        String price = uploadPrice.getText().toString();

        // Create Info object with data
        Info dataClass = new Info(name, desc, date, price, image);

        // Generate a unique key for the data item
        String itemId = FirebaseDatabase.getInstance().getReference("Android Asm")
                .child(authProfile.getCurrentUser().getUid())
                .push()
                .getKey();

        // Save data to Firebase Realtime Database
        FirebaseDatabase.getInstance().getReference("Android Asm").child(authProfile.getCurrentUser().getUid())
                .child(itemId)
                .setValue(dataClass)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(Upload.this, "Saved", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Upload.this, e.getMessage().toString(), Toast.LENGTH_SHORT);
                    }
                });
    }

    // Method to add data to Firebase Storage and get the download URL
    public void addInfo()
    {

        // Get data from input fields
        String name = uploadName.getText().toString();
        String desc = uploadDesc.getText().toString();
        String price = uploadPrice.getText().toString();
        String date = uploadDate.getText().toString();

        // Check if the name is empty
        if (name.isEmpty()) {
            Toast.makeText(Upload.this, "Name is required", Toast.LENGTH_SHORT).show();
            return; // Stop execution if name is empty
        }

        // Check if the name is empty
        if (desc.isEmpty()) {
            Toast.makeText(Upload.this, "Description is required", Toast.LENGTH_SHORT).show();
            return; // Stop execution if description is empty
        }

        // Check if the name is empty
        if (price.isEmpty()) {
            Toast.makeText(Upload.this, "Price is required", Toast.LENGTH_SHORT).show();
            return; // Stop execution if price is empty
        }

        // Check if the name is empty
        if (date.isEmpty()) {
            Toast.makeText(Upload.this, "Date is required", Toast.LENGTH_SHORT).show();
            return; // Stop execution if date is empty
        }

        if (uri_data != null) {
            // Reference to Firebase Storage
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Images Data")
                    .child(authProfile.getCurrentUser().getUid());

            // Upload image to Firebase Storage
            UploadTask uploadTask = storageReference.putFile(uri_data);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // Get download URL of the uploaded image
                    Task<Uri> uri_dataTask = taskSnapshot.getStorage().getDownloadUrl();
                    uri_dataTask.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri_data) {
                            // Save the image URL to the "image" variable
                            image = uri_data.toString();

                            // Load the uploaded image using Glide library
                            if (image != null) {
                                Glide.with(Upload.this)
                                        .load(image)
                                        .into(upload);
                            }

                            // Upload remaining data to Firebase Realtime Database
                            uploadData();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            e.printStackTrace();
                            Toast.makeText(Upload.this, "Unable To Download: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    e.printStackTrace();
                    Toast.makeText(Upload.this, "Unable To Upload: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        else {
            Toast.makeText(Upload.this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }
}
