package com.example.pizzaapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SignUpActivity extends AppCompatActivity {

    private TextInputLayout tilName, tilEmail, tilPassword, tilConfirm;
    private TextInputEditText etName, etEmail, etPassword, etConfirm;
    private Button btnCreateAccount;
    private TextView tvHaveAccount;
    private DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup); // make sure this XML exists

        db = new DBHelper(this);

        tilName = findViewById(R.id.tilName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirm = findViewById(R.id.tilConfirm);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirm = findViewById(R.id.etConfirm);

        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        tvHaveAccount = findViewById(R.id.tvHaveAccount);

        addClearErrorWatcher(etName, tilName);
        addClearErrorWatcher(etEmail, tilEmail);
        addClearErrorWatcher(etPassword, tilPassword);
        addClearErrorWatcher(etConfirm, tilConfirm);

        btnCreateAccount.setOnClickListener(v -> attemptSignup());
        tvHaveAccount.setOnClickListener(v ->
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class)));
    }

    private void attemptSignup() {
        String name = safeText(etName);
        String email = safeText(etEmail);
        String pw = safeText(etPassword);
        String pw2 = safeText(etConfirm);

        boolean valid = true;
        if (name.length() < 2) { tilName.setError("Please enter your full name"); valid = false; }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { tilEmail.setError("Enter a valid email"); valid = false; }
        if (pw.length() < 6) { tilPassword.setError("Use at least 6 characters"); valid = false; }
        if (!pw.equals(pw2)) { tilConfirm.setError("Passwords don’t match"); valid = false; }
        if (!valid) return;

        boolean ok = db.registerUser(name, email, pw);
        if (ok) {
            Toast.makeText(this, "Account created 🎉", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            tilEmail.setError("Email already exists");
        }
    }

    private static String safeText(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private void addClearErrorWatcher(TextInputEditText et, TextInputLayout til) {
        et.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                til.setError(null);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }
}
