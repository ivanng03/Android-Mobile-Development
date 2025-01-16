package com.example.cet343assignment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements Adapter.MenuItemClickListener
{
    // Declare UI elements
    ImageView options;
    RecyclerView dataRec;
    TextView add;

    // List to hold data items
    List<Info> infoList;

    // Database reference and event listener for Firebase
    DatabaseReference databaseReference;
    ValueEventListener EListener;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        add = findViewById(R.id.additem);
        dataRec = findViewById(R.id.recycler);

        // Check location permission
        checkLocPerm();

        // Get the current user
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser User = auth.getCurrentUser();

        // Set up the RecyclerView and its layout manager
        GridLayoutManager gridLayoutManager = new GridLayoutManager(MainActivity.this, 1);
        dataRec.setLayoutManager(gridLayoutManager);

        // Initialize the list and adapter
        infoList = new ArrayList<>();
        Adapter adapter = new Adapter(MainActivity.this, infoList);
        adapter.setMenuItemClickListener(this);
        dataRec.setAdapter(adapter);

        // Set up the path
        databaseReference = FirebaseDatabase.getInstance().getReference("Android Asm");

        // Check if the user is logged in
        if (User != null)
        {// Get the user's ID and set up a listener for data changes
            String ID = User.getUid();
            DatabaseReference userItemsReference = FirebaseDatabase.getInstance()
                    .getReference("Android Asm")
                    .child(ID);
            EListener = userItemsReference.addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot)
                {
                    // Clear the list and populate it with updated data from Firebase
                    infoList.clear();
                    for (DataSnapshot itemSnapshot : snapshot.getChildren())
                    {
                        Info info = itemSnapshot.getValue(Info.class);
                        info.setKey(itemSnapshot.getKey());
                        infoList.add(info);
                    }
                    // Notify the adapter of the data change
                    adapter.notifyDataSetChanged();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error)
                {
                    // Handle onCancelled event if needed
                }
            });
        }
        // Set onClickListener for the Add
        add.setOnClickListener(new View.OnClickListener()
        {// Open the Upload activity
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Upload.class);
                startActivity(intent);
            }
        });
    }

    // Empty method for checking location permission
    private void checkLocPerm()
    {// Implementation for checking location permission can be added here
    }

    // Interface method implementations for handling item click events
    @Override
    public void onOptClicked(int position)
    {
        // Show options menu when options are clicked
        Adapter adapterOpt = (Adapter) dataRec.getAdapter();
        if (adapterOpt != null)
        {
            adapterOpt.showPopUpMenu(options, position);
        }
    }

    @Override
    public void onEditOptClicked(Info info)
    {
        // Update data when edit is clicked
        Adapter adapterEdit = (Adapter) dataRec.getAdapter();
        int position = infoList.indexOf(info);
        if (adapterEdit != null && position != -1)
        {
            adapterEdit.updateData(position, info);
        }
    }

    @Override
    public void onDeleteOptClicked(Info data)
    {
        // Delete data when delete is clicked
        Adapter adapterDel = (Adapter) dataRec.getAdapter();
        int position = infoList.indexOf(data);
        if (adapterDel != null && position != -1)
        {
            adapterDel.deleteData(position);
        }
    }

    @Override
    public void sendSMS(Info data)
    {
        // Show item details dialog when sending as SMS is clicked
        String itemName = data.getName();
        String itemDesc = data.getDesc();
        String itemPrice = data.getPrice();
        Adapter adapterSMS = (Adapter) dataRec.getAdapter();
        int position = infoList.indexOf(data);
        if (adapterSMS != null && position != -1)
        {
            adapterSMS.showItemDetails(itemName, itemDesc, itemPrice);
        }
    }

    @Override
    public void tagLoc(Info data)
    {
        // Tag location and open Map activity when location is clicked
        Adapter adapterLoc = (Adapter) dataRec.getAdapter();
        int position = infoList.indexOf(data);
        if (adapterLoc != null && position != -1)
        {
            adapterLoc.tagLocation(data);
        }
    }
    @Override
    public void onPurchasedClicked(Info data)
    {// Mark item as purchased or unpurchased when clicked
        Adapter adapterMark = (Adapter) dataRec.getAdapter();
        if (adapterMark != null)
        {
            int position = infoList.indexOf(data);
            if (position != -1)
            {Info clickedItem = infoList.get(position);
                if (clickedItem != null)
                {if ("Purchased".equals(clickedItem.getStatus()))
                    {clickedItem.setStatus("Unpurchased");
                    } else
                    {
                        clickedItem.setStatus("Purchased");
                    }
                    adapterMark.notifyItemChanged(position);
                }
            }
        }
    }
}
