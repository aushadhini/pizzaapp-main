package com.example.pizzaapp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvForgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Firebase
        auth = FirebaseAuth.getInstance();

        // Bind views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        // Prefill email from SignUp
        String prefill = getIntent().getStringExtra("prefill_email");
        if (prefill != null) etEmail.setText(prefill);

        // Login button
        btnLogin.setOnClickListener(v -> doLogin());

        // Forgot password
        tvForgotPassword.setOnClickListener(v -> resetPassword());
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 🔴 Only auto-skip if NOT explicitly forced to show login
        boolean forceLogin = getIntent().getBooleanExtra("forceLogin", false);
        if (!forceLogin && FirebaseAuth.getInstance().getCurrentUser() != null) {
            goHome();
        }
    }

    private void doLogin() {
        String email = safeText(etEmail);
        String pass  = safeText(etPassword);

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email required");
            etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(pass)) {
            etPassword.setError("Password required");
            etPassword.requestFocus();
            return;
        }

        btnLogin.setEnabled(false);

        auth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    btnLogin.setEnabled(true);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Welcome!", Toast.LENGTH_SHORT).show();
                        goHome();
                    } else {
                        String msg = (task.getException() != null)
                                ? task.getException().getMessage()
                                : "Login failed";
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void resetPassword() {
        String email = safeText(etEmail);
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Enter your email first", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Reset email sent", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }



    private String safeText(EditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
    private void goHome() {
        Intent i = new Intent(LoginActivity.this,DashboardActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish(); // prevents back to Login
    }


}
