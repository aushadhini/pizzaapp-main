// AccountActivity.java
package com.example.pizzaapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AccountActivity extends AppCompatActivity {

    private ImageView imgAvatar;
    private TextView tvName, tvPhone, tvEmail;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) toolbar.setNavigationOnClickListener(v -> onBackPressed());

        imgAvatar = findViewById(R.id.imgAvatar);
        tvName    = findViewById(R.id.tvName);
        tvPhone   = findViewById(R.id.tvPhone);
        tvEmail   = findViewById(R.id.tvEmail);

        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();

        // tap profile card -> go to editor (NOT AccountActivity again)
        findViewById(R.id.profileCard).setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class)));

        // placeholders
        findViewById(R.id.rowSavedAddresses).setOnClickListener(v ->
                Toast.makeText(this, "Saved Addresses", Toast.LENGTH_SHORT).show());
        findViewById(R.id.rowChangeNumber).setOnClickListener(v ->
                Toast.makeText(this, "Change Number", Toast.LENGTH_SHORT).show());
        findViewById(R.id.rowChangeEmail).setOnClickListener(v ->
                Toast.makeText(this, "Change Email", Toast.LENGTH_SHORT).show());
        findViewById(R.id.rowChangePassword).setOnClickListener(v ->
                Toast.makeText(this, "Change Password", Toast.LENGTH_SHORT).show());
        findViewById(R.id.rowDeleteAccount).setOnClickListener(v ->
                Toast.makeText(this, "Account Deletion", Toast.LENGTH_SHORT).show());

        // logout -> Login, clear back stack
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            auth.signOut();
            Intent i = new Intent(AccountActivity.this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Intent i = new Intent(this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
            return;
        }

        tvEmail.setText(user.getEmail() == null ? "" : user.getEmail());

        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(this::applyProfileToUI)
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show());
    }

    private void applyProfileToUI(DocumentSnapshot snap) {
        if (snap != null && snap.exists()) {
            tvName.setText(nvl(snap.getString("name")));
            tvPhone.setText(nvl(snap.getString("phone")));
            String url = snap.getString("photoUrl");
            if (url != null && !url.isEmpty()) {
                Glide.with(this)
                        .load(url)
                        .placeholder(R.drawable.ic_person_circle_24)
                        .error(R.drawable.ic_person_circle_24)
                        .circleCrop()
                        .into(imgAvatar);
                return;
            }
        }
        Glide.with(this).load(R.drawable.ic_person_circle_24).circleCrop().into(imgAvatar);
    }

    private static String nvl(String s) { return s == null ? "" : s; }
}
