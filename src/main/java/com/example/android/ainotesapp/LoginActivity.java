package com.example.android.ainotesapp;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    EditText emailLogin , passwordLogin;
    Button loginButton;
    TextView goToSighUp,forgetPassword;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        emailLogin = findViewById(R.id.emailLogin);
        passwordLogin = findViewById(R.id.passwordLogin);
        loginButton = findViewById(R.id.loginButton);
        goToSighUp = findViewById(R.id.goToSignup);
        forgetPassword = findViewById(R.id.forgotPassword);


        auth = FirebaseAuth.getInstance();

        forgetPassword.setOnClickListener( v -> {
            String email  = emailLogin.getText().toString().trim();

            if( email.isEmpty()){
                Toast.makeText(LoginActivity.this, "Enter your email first", Toast.LENGTH_SHORT).show();
            }else{
                auth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Reset link sent to your email", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        loginButton.setOnClickListener(v -> {
            String email = emailLogin.getText().toString().trim();
            String password = passwordLogin.getText().toString().trim();

            if(TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }
            if(TextUtils.isEmpty(password) || password.length() < 6){
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null && user.isEmailVerified()) {
                                // Allow login
                                startActivity(new Intent(LoginActivity.this, AllNotesActivity.class));
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this,
                                        "Please verify your email before logging in.",
                                        Toast.LENGTH_LONG).show();

                                auth.signOut(); // Important: Don't keep unverified user logged in
                            }
                        } else {
                            Toast.makeText(LoginActivity.this,
                                    "Login failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
            });
        goToSighUp.setOnClickListener( v -> {
            startActivity(new Intent(this , SignupActivity.class));
            finish();
        });
    }
    protected void onStart() {
        super.onStart();
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            startActivity(new Intent(this , AllNotesActivity.class));
            finish();
        }
    }
}