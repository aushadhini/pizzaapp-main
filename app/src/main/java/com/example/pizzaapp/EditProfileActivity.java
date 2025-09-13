package com.example.pizzaapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class EditProfileActivity extends AppCompatActivity {

    private TextInputLayout tilName, tilPhone, tilPhoto;
    private TextInputEditText etName, etPhone, etPhoto;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile); // must exist

        // ✅ This id exists in the XML below
        MaterialToolbar toolbar = findViewById(R.id.toolbarEdit);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        toolbar.setTitle("Edit profile");

        tilName  = findViewById(R.id.tilName);
        tilPhone = findViewById(R.id.tilPhone);
        tilPhoto = findViewById(R.id.tilPhoto);

        etName  = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etPhoto = findViewById(R.id.etPhoto);

        Button btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> {
            tilName.setError(null);
            tilPhone.setError(null);

            String name = text(etName);
            String phone = text(etPhone);

            boolean ok = true;
            if (TextUtils.isEmpty(name)) { tilName.setError("Required"); ok = false; }
            if (TextUtils.isEmpty(phone)) { tilPhone.setError("Required"); ok = false; }
            if (!ok) return;

            // TODO: write to Firestore users/{uid}
            Toast.makeText(this, "Saved (stub)", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private static String text(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
}
