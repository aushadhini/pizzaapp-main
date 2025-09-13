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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignUpActivity extends AppCompatActivity {

    private TextInputLayout tilName, tilEmail, tilPassword, tilConfirm;
    private TextInputEditText etName, etEmail, etPassword, etConfirm;
    private Button btnCreateAccount;
    private TextView tvHaveAccount;

    private FirebaseAuth auth;

    // Flip to false if you don’t want to require email verification
    private static final boolean REQUIRE_EMAIL_VERIFICATION = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup); // Make sure IDs match this file.

        auth = FirebaseAuth.getInstance();

        tilName     = findViewById(R.id.tilName);
        tilEmail    = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirm  = findViewById(R.id.tilConfirm);

        etName      = findViewById(R.id.etName);
        etEmail     = findViewById(R.id.etEmail);
        etPassword  = findViewById(R.id.etPassword);
        etConfirm   = findViewById(R.id.etConfirm);

        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        tvHaveAccount    = findViewById(R.id.tvHaveAccount);

        addClearErrorWatcher(etName, tilName);
        addClearErrorWatcher(etEmail, tilEmail);
        addClearErrorWatcher(etPassword, tilPassword);
        addClearErrorWatcher(etConfirm, tilConfirm);

        btnCreateAccount.setOnClickListener(v -> attemptSignup());
        tvHaveAccount.setOnClickListener(v ->
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class)));
    }

    private void attemptSignup() {
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

        btnCreateAccount.setEnabled(false);

        auth.createUserWithEmailAndPassword(email, pw)
                .addOnCompleteListener(task -> {
                    btnCreateAccount.setEnabled(true);

                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();

                        // Set display name (so you can read it later via user.getDisplayName())
                        if (user != null && !name.isEmpty()) {
                            UserProfileChangeRequest profile =
                                    new UserProfileChangeRequest.Builder()
                                            .setDisplayName(name)
                                            .build();
                            user.updateProfile(profile);
                        }

                        if (REQUIRE_EMAIL_VERIFICATION && user != null) {
                            user.sendEmailVerification()
                                    .addOnSuccessListener(unused ->
                                            Toast.makeText(this, "Verification email sent. Please check your inbox.", Toast.LENGTH_LONG).show())
                                    .addOnFailureListener(e ->
                                            Toast.makeText(this, "Could not send verification: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show());
                            // Keep the flow simple: sign out and send to Login
                            auth.signOut();
                        } else {
                            Toast.makeText(this, "Account created 🎉", Toast.LENGTH_SHORT).show();
                        }

                        // Go to Login and prefill email
                        Intent i = new Intent(this, LoginActivity.class);
                        i.putExtra("prefill_email", email);
                        startActivity(i);
                        finish();

                    } else {
                        // Friendly errors
                        String msg = "Sign up failed. Please try again.";
                        Exception e = task.getException();
                        if (e instanceof FirebaseAuthWeakPasswordException) {
                            msg = "Weak password. Use at least 6 characters.";
                            tilPassword.setError(msg);
                        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            msg = "Invalid email format.";
                            tilEmail.setError(msg);
                        } else if (e instanceof FirebaseAuthUserCollisionException) {
                            msg = "Email already in use.";
                            tilEmail.setError(msg);
                        } else if (e != null && e.getLocalizedMessage() != null) {
                            msg = e.getLocalizedMessage();
                        }
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private static String textOf(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private void addClearErrorWatcher(TextInputEditText et, TextInputLayout til) {
        et.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { til.setError(null); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }
}
