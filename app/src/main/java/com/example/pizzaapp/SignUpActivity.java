package com.example.pizzaapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
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
        setContentView(R.layout.activity_signup); // ensure this XML exists with matching IDs

        db = new DBHelper(this);

        // Bind views (must match your activity_signup.xml ids)
        tilName      = findViewById(R.id.tilName);
        tilEmail     = findViewById(R.id.tilEmail);
        tilPassword  = findViewById(R.id.tilPassword);
        tilConfirm   = findViewById(R.id.tilConfirm);

        etName       = findViewById(R.id.etName);
        etEmail      = findViewById(R.id.etEmail);
        etPassword   = findViewById(R.id.etPassword);
        etConfirm    = findViewById(R.id.etConfirm);

        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        tvHaveAccount    = findViewById(R.id.tvHaveAccount);

        // Clear errors while typing
        addClearErrorWatcher(etName, tilName);
        addClearErrorWatcher(etEmail, tilEmail);
        addClearErrorWatcher(etPassword, tilPassword);
        addClearErrorWatcher(etConfirm, tilConfirm);

        btnCreateAccount.setOnClickListener(v -> attemptSignup());
        tvHaveAccount.setOnClickListener(v ->
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class)));
    }

    private void attemptSignup() {
        // Reset visible errors
        tilName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirm.setError(null);

        String name  = textOf(etName);
        String email = textOf(etEmail);
        String pw    = textOf(etPassword);
        String pw2   = textOf(etConfirm);

        boolean valid = true;

        if (name.length() < 2) {
            tilName.setError("Please enter your name");
            valid = false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Enter a valid email");
            valid = false;
        }
        if (pw.length() < 6) {
            tilPassword.setError("Use at least 6 characters");
            valid = false;
        }
        if (!TextUtils.equals(pw, pw2)) {
            tilConfirm.setError("Passwords don’t match");
            valid = false;
        }
        if (!valid) return;

        // Try to create the user in SQLite
        btnCreateAccount.setEnabled(false);
        boolean created = db.registerUser(name, email, pw);
        btnCreateAccount.setEnabled(true);

        if (created) {
            Toast.makeText(this, "Account created 🎉", Toast.LENGTH_SHORT).show();

            // Go to Login and prefill the email
            Intent i = new Intent(this, LoginActivity.class);
            i.putExtra("prefill_email", email);
            startActivity(i);
            finish();
        } else {
            // DBHelper should return false if email already exists
            tilEmail.setError("Email already exists");
        }
    }

    private static String textOf(TextInputEditText et) {
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
