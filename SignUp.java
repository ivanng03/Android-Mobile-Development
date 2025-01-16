package com.example.cet343assignment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUp extends AppCompatActivity {

    // UI elements
    EditText signupName;
    EditText signupEmail;
    EditText signupUsername;
    EditText signupPassword;
    TextView loginRedirectText;
    Button signupButton;

    // Firebase Database
    FirebaseDatabase database;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize UI elements
        signupName = findViewById(R.id.signup_name);
        signupEmail = findViewById(R.id.signup_email);
        signupUsername = findViewById(R.id.signup_username);
        signupPassword = findViewById(R.id.signup_password);
        signupButton = findViewById(R.id.signup_button);
        loginRedirectText = findViewById(R.id.loginRedirectText);

        // Set click listener for signup button
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Initialize Firebase Database
                database = FirebaseDatabase.getInstance();
                reference = database.getReference("users");

                // Get user input
                String name = signupName.getText().toString();
                String email = signupEmail.getText().toString();
                String username = signupUsername.getText().toString();
                String password = signupPassword.getText().toString();

                // Create Helper object
                Helper helper = new Helper(name, email, username, password);

                // Save user data to Firebase Realtime Database
                reference.child(username).setValue(helper);

                // Validate user input
                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(SignUp.this, "Please Fill Up Your Name", Toast.LENGTH_LONG).show();
                    signupName.setError("Name Is Required");
                    signupName.requestFocus();
                } else if (TextUtils.isEmpty(email)) {
                    Toast.makeText(SignUp.this, "Please Fill Up Your Email", Toast.LENGTH_LONG).show();
                    signupEmail.setError("Email Is Required");
                    signupEmail.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(SignUp.this, "Please Fill Up Your Email", Toast.LENGTH_LONG).show();
                    signupEmail.setError("Valid Email Is Required");
                    signupEmail.requestFocus();
                } else if (TextUtils.isEmpty(username)) {
                    Toast.makeText(SignUp.this, "Please Fill Up Your Username", Toast.LENGTH_LONG);
                    signupUsername.setError("Username Is Required");
                    signupUsername.requestFocus();
                } else if (TextUtils.isEmpty(password)) {
                    Toast.makeText(SignUp.this, "Please Fill Up Your Password", Toast.LENGTH_LONG).show();
                    signupPassword.setError("Password Is Required");
                    signupPassword.requestFocus();
                } else if (password.length() < 10) {
                    Toast.makeText(SignUp.this, "Password should be at least 10 digits", Toast.LENGTH_LONG).show();
                    signupPassword.setError("Password Too Weak");
                    signupPassword.requestFocus();
                } else {
                    // Perform user registration
                    performRegistration(name, email, username, password);
                    Toast.makeText(SignUp.this, "Sign Up Successfully!", Toast.LENGTH_SHORT).show();
                    // Navigate to the Login activity
                    Intent intent = new Intent(SignUp.this, Login.class);
                    startActivity(intent);
                }
            }

            // Method to perform user registration using Firebase Authentication
            private void performRegistration(String name, String email, String username, String password) {
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(SignUp.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Registration successful
                            Toast.makeText(SignUp.this, "Successful", Toast.LENGTH_SHORT).show();

                            // Get current user
                            FirebaseUser User = firebaseAuth.getCurrentUser();

                            // Create Helper object
                            Helper userHelper = new Helper(name, email, username, password);

                            // Save user data to Firebase Realtime Database
                            DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("Registered Users");
                            userReference.child(User.getUid()).setValue(userHelper).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        // Send email verification
                                        User.sendEmailVerification();

                                        Toast.makeText(SignUp.this, "Registration successful", Toast.LENGTH_LONG).show();

                                        // Navigate to the Login activity
                                        Intent loginIntent = new Intent(SignUp.this, Login.class);
                                        loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                                Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(loginIntent);
                                    } else {
                                        // Error writing to database
                                        Toast.makeText(SignUp.this, "Registration failed!", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                    }
                });
            }
        });

        // Set click listener for login redirect text
        loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the Login activity
                Intent intent = new Intent(SignUp.this, Login.class);
                startActivity(intent);
            }
        });
    }
}
