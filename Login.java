package com.example.cet343assignment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;
import java.lang.Object;

public class Login extends AppCompatActivity {
    // Declare UI elements
    EditText loginEmail, loginPassword;
    TextView signupRedirectText;
    Button loginButton;
    private FirebaseAuth authProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize UI elements and Firebase Authentication instance
        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);
        signupRedirectText = findViewById(R.id.signupRedirectText);
        loginButton = findViewById(R.id.login_button);
        authProfile = FirebaseAuth.getInstance();

        // Set onClickListener for loginButton
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get user input values
                String validateEmail = loginEmail.getText().toString();
                String validatePassword = loginPassword.getText().toString();

                // Validate user input
                if (validateEmail.isEmpty()) {
                    Toast.makeText(Login.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                    loginEmail.setError("Required Email");
                    loginEmail.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(validateEmail).matches()) {
                    Toast.makeText(Login.this, "Please re-enter your email!", Toast.LENGTH_SHORT).show();
                    loginEmail.setError("Invalid Email");
                    loginEmail.requestFocus();
                } else if (validatePassword.isEmpty()) {
                    Toast.makeText(Login.this, "Please enter your password", Toast.LENGTH_SHORT).show();
                    loginPassword.setError("Invalid Password");
                    loginPassword.requestFocus();
                } else {
                    // Call the method to perform login
                    loginUser(validateEmail, validatePassword);
                }
            }

            // Method to perform login using Firebase Authentication
            private void loginUser(String validateEmail, String validatePassword) {
                authProfile.signInWithEmailAndPassword(validateEmail, validatePassword).addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // If login is successful, show a success message
                            Toast.makeText(Login.this, "Login Successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Login.this, MainActivity.class);
                            startActivity(intent);
                        } else {
                            // If login fails, show an error message
                            Toast.makeText(Login.this, "Please Try Again", Toast.LENGTH_SHORT).show();
                            loginPassword.setError("Incorrect Password");
                            loginPassword.requestFocus();
                        }
                    }
                });
            }
        });

        // Set onClickListener for signupRedirectText to redirect to SignUp activity
        signupRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, SignUp.class);
                startActivity(intent);
            }
        });
    }
}
