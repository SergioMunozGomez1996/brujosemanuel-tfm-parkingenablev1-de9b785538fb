package com.example.parkingenable;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "MyPrefsFile";

    private EditText emailUsuario;
    private EditText passwordUsuario;
    private ProgressBar progressBar;
    private Button loginButton;
    private TextView singUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailUsuario = findViewById(R.id.email);
        passwordUsuario = findViewById(R.id.password);
        progressBar = findViewById(R.id.login_progressBar);
        loginButton = findViewById(R.id.login_button);
        singUpButton = findViewById(R.id.singUp_textView);

        singUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent  = new Intent(LoginActivity.this, SingUpActivity.class);
                startActivity(intent);
            }
        });

    }
}
