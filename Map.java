package com.example.cet343assignment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import android.Manifest;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Map extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener{
    private  GoogleMap gooM;
    int desiredZoomLevel = 20;
    private DatabaseReference databaseRef;
    private ValueEventListener markerValueEventListener;
    private Marker existingMarker;
    private static final int PERMISSION_REQUEST_CODE = 10;
    private FusedLocationProviderClient fusedLocation;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        SupportMapFragment map = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView);
        if(map !=null)
        {
            map.getMapAsync(this);

            fusedLocation = LocationServices.getFusedLocationProviderClient(this);
            databaseRef = FirebaseDatabase.getInstance().getReference("geotaggedItems");

            getUserLocation();
        }
        else
        {
            Toast.makeText(this,"Map initialization failed", Toast.LENGTH_SHORT).show();
        }
        // Get intent data and retrieve/display markers if available
        Intent intentMarket = getIntent();
        String itemId = null;
        if (intentMarket != null)
        {
            itemId = intentMarket.getStringExtra("itemId");
            if (itemId != null)
            {
                retrieveAndDisplayMarkers(itemId);
            }
            else {
                Toast.makeText(this, "No ID found", Toast.LENGTH_SHORT).show();
            }
        }
        // Set click listeners
        Button back = findViewById(R.id.back);
        Button zoomInButton = findViewById(R.id.zoomInButton);
        Button zoomOutButton = findViewById(R.id.zoomOutButton);

        back.setOnClickListener(view ->
        {// Navigate back to the main activity
            Intent intent = new Intent(Map.this, MainActivity.class);
            startActivity(intent);
        });

        zoomInButton.setOnClickListener(view ->
        {// Zoom in
            if (gooM != null) {
                float currentZoom = gooM.getCameraPosition().zoom;
                gooM.animateCamera(CameraUpdateFactory.zoomTo(currentZoom + 1));
            }
        });
        zoomOutButton.setOnClickListener(view ->
        {// Zoom out
            if (gooM != null) {
                float currentZoom = gooM.getCameraPosition().zoom;
                gooM.animateCamera(CameraUpdateFactory.zoomTo(currentZoom - 1));
            }
        });
    }

    @Override// Handle location permission request result
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {// Permission granted, get user location
                getUserLocation();
            }else
            {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getUserLocation() // Get user location
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
        {
            fusedLocation.getLastLocation()
                    .addOnSuccessListener(this, location ->
                    {
                        if(location != null)
                        {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();

                            Intent intent = getIntent();
                            if (intent != null)
                            {
                                String itemId = intent.getStringExtra("itemId");
                                String Name = intent.getStringExtra("itemName");

                                if (Name != null)
                                {
                                    createAndStoreGeoTagged("Item", itemId, latitude, longitude);
                                }
                            }
                        }
                    });
        } else
        {// Request location permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        }
    }

    // Create and store geotagged item in the Firebase database
    private void createAndStoreGeoTagged(String itemId,String Name, double latitude, double longitude)
    {
        Info infoClass = new Info(Name, latitude, longitude);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userMarkersRef = databaseRef.child(userId);
        userMarkersRef.child(itemId).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    Info existingData = dataSnapshot.getValue(Info.class);
                    if(existingData != null)
                    {
                        existingData.setLatitude(latitude);
                        existingData.setLongitude(longitude);
                        userMarkersRef.child(itemId).setValue(existingData)
                                .addOnSuccessListener(aVoid ->
                                {
                                    Toast.makeText(Map.this, "Marker updated", Toast.LENGTH_SHORT).show();
                                    retrieveAndDisplayMarkers(itemId);
                                })
                                .addOnFailureListener(e -> Toast.makeText(Map.this, "Failed to update ", Toast.LENGTH_SHORT).show());
                    }
                }else
                {
                    DatabaseReference itemRef = userMarkersRef.child(itemId);
                    itemRef.setValue(infoClass)
                            .addOnSuccessListener(aVoid->
                            {
                                Toast.makeText(Map.this, "Marker stored", Toast.LENGTH_SHORT).show();
                                retrieveAndDisplayMarkers(itemId);
                            })
                            .addOnFailureListener(e -> Toast.makeText(Map.this, "Failed to stored ", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                Toast.makeText(Map.this, "Checking Failed", Toast.LENGTH_SHORT).show();

            }
        });
    }

    // Retrieve and display markers from Firebase database
    private void retrieveAndDisplayMarkers(String itemId)
    {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userMarkersRef = databaseRef.child(userId);

        userMarkersRef.child(itemId).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    Info infoClass = dataSnapshot.getValue(Info.class);
                    if(infoClass != null)
                    {
                        LatLng markerposition = new LatLng(infoClass.getLatitude(), infoClass.getLongitude());
                        gooM.addMarker(new MarkerOptions().position(markerposition).title(infoClass.getName()));
                    }
                }else
                {
                    Toast.makeText(Map.this, "Marker not found", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                Toast.makeText(Map.this, "Retrieval Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Callback when the map is ready
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gooM = googleMap;
        gooM.setOnMapClickListener(this);
        if (gooM != null && gooM.getUiSettings() != null) {
            gooM.getUiSettings().setZoomGesturesEnabled(true);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_CODE);
        } else {
            fusedLocation.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();

                            Intent intent = getIntent();
                            if (intent != null) {
                                String Name = intent.getStringExtra("Name");
                                String itemId = intent.getStringExtra("itemId");
                                if (Name != null) {
                                    retrieveAndDisplayMarkers(itemId);
                                    LatLng itemLocation = new LatLng(latitude, longitude);
                                    gooM.moveCamera(CameraUpdateFactory.newLatLngZoom(itemLocation, desiredZoomLevel));
                                }
                            }
                        }
                    });
        }
    }


    // Callback when the map is clicked
    @Override
    public void onMapClick(@NonNull LatLng latLng)
    {
        gooM.clear();
        Intent intent = getIntent();
        String itemId = intent.getStringExtra("itemId");
        String Name = intent.getStringExtra("itemName");
        if (Name != null && itemId != null) {
            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(Name);
            existingMarker = gooM.addMarker(markerOptions);

            createAndStoreGeoTagged(itemId, Name, latLng.latitude, latLng.longitude);
        } else {
            Toast.makeText(this, "Invalid item data", Toast.LENGTH_SHORT).show();
        }
    }

    // Cleanup when the activity is destroyed
    @Override
    protected void onDestroy(){
        if (markerValueEventListener != null){
            databaseRef.removeEventListener(markerValueEventListener);

        }
        super.onDestroy();
    }
}
