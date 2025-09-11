package com.example.pizzaapp;



import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import android.text.Editable;
import android.text.TextWatcher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmailLogin, tilPasswordLogin;
    private TextInputEditText etEmailLogin, etPasswordLogin;
    private Button btnLoginNow;
    private TextView tvToSignup, tvForgot;
    private CheckBox cbRemember;

    private DBHelper db;

    // Simple session/remember-me store
    private SharedPreferences prefs;
    private static final String PREFS = "pm_session";
    private static final String KEY_LOGGED_IN = "logged_in";
    private static final String KEY_EMAIL = "email";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Toolbar back
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Views
        tilEmailLogin = findViewById(R.id.tilEmailLogin);
        tilPasswordLogin = findViewById(R.id.tilPasswordLogin);
        etEmailLogin = findViewById(R.id.etEmailLogin);
        etPasswordLogin = findViewById(R.id.etPasswordLogin);
        btnLoginNow = findViewById(R.id.btnLoginNow);
        tvToSignup = findViewById(R.id.tvToSignup);
        tvForgot = findViewById(R.id.tvForgot);
        cbRemember = findViewById(R.id.cbRemember);

        // DB + prefs
        db = new DBHelper(this);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        // Auto-skip if already logged in (optional)
        if (prefs.getBoolean(KEY_LOGGED_IN, false)) {
            goHomeAndFinish();
            return;
        }

        // Prefill remembered email
        String rememberedEmail = prefs.getString(KEY_EMAIL, "");
        if (!rememberedEmail.isEmpty()) {
            etEmailLogin.setText(rememberedEmail);
            cbRemember.setChecked(true);
        }

        // Clear errors when typing
        addClearErrorWatcher(etEmailLogin, tilEmailLogin);
        addClearErrorWatcher(etPasswordLogin, tilPasswordLogin);

        // Actions
        btnLoginNow.setOnClickListener(v -> attemptLogin());
        tvToSignup.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class)));
        tvForgot.setOnClickListener(v ->
                Toast.makeText(LoginActivity.this, "Forgot password? Contact support.", Toast.LENGTH_SHORT).show());
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

        // Verify with SQLite
        if (db.checkLogin(email, pw)) {
            if (cbRemember.isChecked()) {
                prefs.edit()
                        .putBoolean(KEY_LOGGED_IN, true)
                        .putString(KEY_EMAIL, email)
                        .apply();
            } else {
                prefs.edit()
                        .putBoolean(KEY_LOGGED_IN, true) // logged in for this session
                        .remove(KEY_EMAIL)               // don't remember email
                        .apply();
            }
            Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show();
            goHomeAndFinish();
        } else {
            tilPasswordLogin.setError("Invalid email or password");
        }
    }

    private void goHomeAndFinish() {
        // TODO: Implement MainActivity (home screen)
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    // Helpers
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
