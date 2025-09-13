package com.example.pizzaapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmailLogin, tilPasswordLogin;
    private TextInputEditText etEmailLogin, etPasswordLogin;
    private Button btnLoginNow;
    private TextView tvToSignup, tvForgot;
    private CheckBox cbRemember;

    // Remember-only prefs (Firebase manages the real session)
    private SharedPreferences prefs;
    private static final String PREFS = "pm_session";
    private static final String KEY_EMAIL = "email";

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) toolbar.setNavigationOnClickListener(v -> onBackPressed());

        tilEmailLogin = findViewById(R.id.tilEmailLogin);
        tilPasswordLogin = findViewById(R.id.tilPasswordLogin);
        etEmailLogin = findViewById(R.id.etEmailLogin);
        etPasswordLogin = findViewById(R.id.etPasswordLogin);
        btnLoginNow = findViewById(R.id.btnLoginNow);
        tvToSignup = findViewById(R.id.tvToSignup);
        tvForgot = findViewById(R.id.tvForgot);
        cbRemember = findViewById(R.id.cbRemember);

        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        auth  = FirebaseAuth.getInstance();

        // Prefill remembered email
        String rememberedEmail = prefs.getString(KEY_EMAIL, "");
        if (!rememberedEmail.isEmpty()) {
            etEmailLogin.setText(rememberedEmail);
            cbRemember.setChecked(true);
        }

        // Clear errors while typing
        addClearErrorWatcher(etEmailLogin, tilEmailLogin);
        addClearErrorWatcher(etPasswordLogin, tilPasswordLogin);

        btnLoginNow.setOnClickListener(v -> attemptLogin());

        // If you DON'T have a SignUpActivity yet, comment this out.
        tvToSignup.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class)));

        tvForgot.setOnClickListener(v -> sendResetEmail());
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser current = auth.getCurrentUser();
        if (current != null) {
            // already signed in by Firebase -> go home
            goHomeAndFinish();
        }
    }

    private void attemptLogin() {
        String email = safeText(etEmailLogin);
        String pw = safeText(etPasswordLogin);

        boolean valid = true;
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmailLogin.setError("Enter a valid email");
            valid = false;
        }
        if (pw.isEmpty()) {
            tilPasswordLogin.setError("Enter your password");
            valid = false;
        }
        if (!valid) return;

        // Firebase email/password sign in
        btnLoginNow.setEnabled(false);
        auth.signInWithEmailAndPassword(email, pw)
                .addOnCompleteListener(task -> {
                    btnLoginNow.setEnabled(true);
                    if (task.isSuccessful()) {
                        // Remember email toggle
                        if (cbRemember.isChecked()) {
                            prefs.edit().putString(KEY_EMAIL, email).apply();
                        } else {
                            prefs.edit().remove(KEY_EMAIL).apply();
                        }
                        Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show();
                        goHomeAndFinish();
                    } else {
                        tilPasswordLogin.setError("Invalid email or password");
                        Toast.makeText(this,
                                task.getException() == null ? "Login failed" : task.getException().getLocalizedMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void sendResetEmail() {
        String email = safeText(etEmailLogin);
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmailLogin.setError("Enter your email to reset");
            return;
        }
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Reset link sent to your email", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show());
    }

    private void goHomeAndFinish() {
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    private static String safeText(TextInputEditText et) {
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
