package com.example.pizzaapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPhone, etPassword, etConfirmPassword;
    private Button btnSignUp;
    private TextView tvGoToLogin;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Views
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);

        btnSignUp.setOnClickListener(v -> attemptSignUp());
        tvGoToLogin.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void attemptSignUp() {
        String name = safeText(etFullName);
        String email = safeText(etEmail);
        String phone = safeText(etPhone);
        String pass = safeText(etPassword);
        String confirm = safeText(etConfirmPassword);

        if (TextUtils.isEmpty(name)) {
            etFullName.setError("Full name required");
            etFullName.requestFocus();
            return;
        }
        if (!isValidEmail(email)) {
            etEmail.setError("Enter valid email");
            etEmail.requestFocus();
            return;
        }
        if (!isValidPhone(phone)) {
            etPhone.setError("Enter valid phone (7–15 digits)");
            etPhone.requestFocus();
            return;
        }
        if (pass.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }
        if (!pass.equals(confirm)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        btnSignUp.setEnabled(false);

        // Firebase create user
        auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Save extra fields in Firestore
                        String uid = auth.getCurrentUser().getUid();
                        Map<String, Object> user = new HashMap<>();
                        user.put("fullName", name);
                        user.put("email", email);
                        user.put("phone", phone);
                        user.put("createdAt", System.currentTimeMillis());

                        db.collection("users").document(uid).set(user)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show();
                                    // Go to Login and prefill email
                                    Intent i = new Intent(SignUpActivity.this, LoginActivity.class);
                                    i.putExtra("prefill_email", email);
                                    startActivity(i);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Profile save failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    btnSignUp.setEnabled(true);
                                });

                    } else {
                        String msg = (task.getException() != null) ?
                                task.getException().getMessage() : "Sign up failed";
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                        btnSignUp.setEnabled(true);
                    }
                });
    }

    // Helpers
    private String safeText(EditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPhone(String phone) {
        String digits = phone.replaceAll("\\D+", "");
        return digits.length() >= 7 && digits.length() <= 15;
    }
}
