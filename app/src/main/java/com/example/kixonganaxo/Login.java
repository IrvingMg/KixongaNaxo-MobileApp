package com.example.kixonganaxo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {
    private final String TAG = "KixongaNaxo";
    private FirebaseAuth mAuth;
    private ProgressDialog alerta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        final TextInputLayout correo = findViewById(R.id.correo);
        final TextInputLayout contrasena = findViewById(R.id.contrasena);
        Button login = findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = correo.getEditText().getText().toString();
                final String password = contrasena.getEditText().getText().toString();

                alerta = ProgressDialog.show(Login.this, "",
                        "Iniciando sesión...", true);

                mAuth.signInWithEmailAndPassword( email, password)
                        .addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                alerta.dismiss();

                                if (task.isSuccessful()) {
                                    Intent i = new Intent(Login.this, Inicio.class);
                                    startActivity(i);
                                    finish();
                                } else {
                                    Toast.makeText(Login.this,
                                            "Error al iniciar sesión. Intente de nuevo.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        TextView recuperarContrasena = findViewById(R.id.recuperar);
        recuperarContrasena.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Login.this, Restablecer.class);
                startActivity(i);
            }
        });

        TextView registrar = findViewById(R.id.registrar);
        registrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Login.this, Signin.class);
                startActivity(i);
            }
        });
    }
}
