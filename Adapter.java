package com.example.cet343assignment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

// Adapter class for the RecyclerView
public class Adapter extends RecyclerView.Adapter<MyViewHolder> {
    // Constants
    private static final int PERMISSION_REQUEST_CODE = 1001;

    // Firebase authentication
    private FirebaseAuth authProfile;
    private FirebaseUser firebaseUsers;

    // Context and listener
    private Context context;
    private MenuItemClickListener listener;

    // List to store data items
    private List<Info> infoList;

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout and create a ViewHolder
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_recycler, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        // Bind data to views in each item of the RecyclerView
        Info currentItem = infoList.get(position);
        Glide.with(context).load(currentItem.getImageURL()).into(holder.upload_image);
        holder.name.setText(currentItem.getName());
        holder.desc.setText(currentItem.getDesc());
        holder.price.setText(currentItem.getPrice());
        holder.date.setText(currentItem.getDate());
        holder.status.setText(currentItem.getStatus());

        // Set click listener for options icon
        holder.options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int clickedPosition = holder.getAdapterPosition();
                if (clickedPosition != RecyclerView.NO_POSITION) {
                    showPopUpMenu(v, clickedPosition);
                }
            }
        });

        // Set click listener for the card (if needed)
        holder.recCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle card click event if needed
            }
        });
    }

    // Method to set the MenuItemClickListener
    public void setMenuItemClickListener(MenuItemClickListener listener) {
        this.listener = listener;
    }

    // Constructor
    public Adapter(Context context, List<Info> infoList) {
        this.context = context;
        this.infoList = infoList;
    }

    @Override
    public int getItemCount() {
        return infoList.size();
    }

    // Method to check and request SMS permission before sending SMS
    private void sendSMSWithPermissionCheck(String phoneNumber, String message) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            // Request the permission
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.SEND_SMS}, PERMISSION_REQUEST_CODE);
        } else {
            // Permission is already granted, proceed with sending SMS
            SMS(phoneNumber, message);
        }
    }

    // Method to show the popup options menu
    public void showPopUpMenu(View view, int position) {
        if (position != RecyclerView.NO_POSITION) {
            final Info task = infoList.get(position);
            if (task != null) {
                // Create and show the PopupMenu
                PopupMenu popupMenu = new PopupMenu(context, view);
                popupMenu.getMenuInflater().inflate(R.menu.options, popupMenu.getMenu());
                
                // Set click listener for each menu item
                popupMenu.setOnMenuItemClickListener(item -> {
                    int ID = item.getItemId();
                    if (ID == R.id.optionEdit) {
                        // Handle edit option
                        if (listener != null) {
                            listener.onEditOptClicked(task);
                        }
                        return true;
                    } else if (ID == R.id.optionDelete) {
                        // Handle delete option
                        if (listener != null) {
                            listener.onDeleteOptClicked(task);
                        }
                        return true;
                    } else if (ID == R.id.optionPurchased) {
                        // Handle mark complete option
                        listener.onPurchasedClicked(task);
                        saveStatus(task);
                        return true;
                    } else if (ID == R.id.optionSMS) {
                        // Handle send SMS option
                        if (listener != null) {
                            listener.sendSMS(task);
                        }
                        return true;
                    } else if (ID == R.id.optionLocation) {
                        // Handle tag location option
                        if (listener != null) {
                            listener.tagLoc(task);
                        }
                        return true;
                    }
                    return false;
                });

                // Show the PopupMenu
                popupMenu.show();
            }
        }
    }

    // Interface to handle item click events
    public interface MenuItemClickListener {
        void onOptClicked(int position);
        void onEditOptClicked(Info info);
        void onDeleteOptClicked(Info info);
        void onPurchasedClicked(Info info);
        void sendSMS(Info info);
        void tagLoc(Info info);
    }

    private void saveStatus(Info info)
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Android Asm");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String Id = user != null ? user.getUid() :"";
        if(!Id.isEmpty() && info.getKey() != null)
        {
            reference.child(Id).child(info.getKey()).child("status").setValue(info.getStatus())
                    .addOnSuccessListener(new OnSuccessListener<Void>()
                    {
                        @Override
                        public void onSuccess(Void Void)
                        {
                            Toast.makeText(context, "Status updated",Toast.LENGTH_SHORT ).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener()
                    {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            Toast.makeText(context, "Failed to update status", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    // Method to delete data from Firebase Realtime Database and update the RecyclerView
    public void deleteData(int position) {
        if (!infoList.isEmpty() && position >= 0 && position < infoList.size()) {
            final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Android Asm");

            Info info = infoList.get(position);
            String key = info.getKey();
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            if (key != null && !key.isEmpty()) {
                reference.child(userId).child(key).removeValue()
                        .addOnSuccessListener(Void -> {
                            Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
                            if (position < infoList.size()) {
                                infoList.remove(position);
                                notifyItemRemoved(position);
                                notifyDataSetChanged();
                                Intent intent = new Intent(context, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);
                            }
                        }).addOnFailureListener(e -> {
                            Toast.makeText(context, "Unable To Delete", Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(context, "Empty", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Method to update data by starting the Edit activity
    public void updateData(int position, Info info) {
        info= infoList.get(position);
        authProfile = FirebaseAuth.getInstance();
        firebaseUsers = authProfile.getCurrentUser();

        if (position != RecyclerView.NO_POSITION) {
            Intent intent = new Intent(context, Edit.class)
                    .putExtra("Name", info.getName())
                    .putExtra("description", info.getDesc())
                    .putExtra("price", info.getPrice())
                    .putExtra("date", info.getDate())
                    .putExtra("uploadImage", info.getImageURL())
                    .putExtra("Key", info.getKey());
            context.startActivity(intent);
        }
    }

    // Method to show a dialog with the item details for sending SMS
    public void showItemDetails(String name, String description, String price) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.activity_sms, null);
        Button sendButton = dialogView.findViewById(R.id.sendButton);
        EditText phoneNumberEditText = dialogView.findViewById(R.id.phoneNumberEditText);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();
        sendButton.setOnClickListener(v -> {
            String phoneNumber = phoneNumberEditText.getText().toString().trim();
            String message = "Title:" + name + "\nDescription:" + description + "\nPrice:" + price;

            if (!phoneNumber.isEmpty()) {
                sendSMSWithPermissionCheck(phoneNumber, message);
                dialog.dismiss();
            } else {
                Toast.makeText(context, "Phone Number Is Required", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to tag the location by starting the Map activity
    public void tagLocation(Info info) {
        if (info != null && info.getLatitude() >= 0.0 && info.getLongitude() >= 0.0) {
            double existingLatitude = info.getLatitude();
            double existingLongitude = info.getLongitude();
            String name = info.getName();
            String ID = info.getKey();
            Intent intent = new Intent(context, Map.class);
            intent.putExtra("itemName", name);
            intent.putExtra("itemId", ID);
            intent.putExtra("latitude", existingLatitude);
            intent.putExtra("longitude", existingLongitude);
            context.startActivity(intent);
        } else {
            Toast.makeText(context, "No Marker", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to send SMS
    private void SMS(String phoneNumber, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(context, "Successful", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
// ViewHolder class to hold the views for each item in the RecyclerView
class MyViewHolder extends RecyclerView.ViewHolder {
    ImageView upload_image;
    TextView name, desc, price, date;
    CardView recCard;
    ImageView options;
    TextView status;
    Adapter.MenuItemClickListener listener;


    public MyViewHolder(@NonNull View itemV) {
        super(itemV);

        // Initialize views
        upload_image = itemV.findViewById(R.id.recImage);
        name = itemV.findViewById(R.id.title);
        desc = itemV.findViewById(R.id.description);
        price = itemV.findViewById(R.id.price);
        date = itemV.findViewById(R.id.dateTextView);
        recCard = itemV.findViewById(R.id.recCard);
        options = itemV.findViewById(R.id.options);
        status = itemV.findViewById(R.id.status);

        // Set click listener for options icon
        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onOptClicked(getAdapterPosition());
                }
            }
        });
    }
}
