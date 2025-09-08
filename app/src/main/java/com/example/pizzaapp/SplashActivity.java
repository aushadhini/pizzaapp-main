package com.example.pizzaapp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

public class SplashActivity extends AppCompatActivity {


    @Override protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        Class<?> next = (FirebaseAuth.getInstance().getCurrentUser() != null)
                ? DashboardActivity.class
                : LoginActivity.class;

        Intent i = new Intent(this, next);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // <- key
        startActivity(i);
        finish();
    }
    // In Splash only
    private boolean launched = false;

    private void goOnce(Class<?> target) {
        if (launched) return;
        launched = true;
        Intent i = new Intent(this, target);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }



}
