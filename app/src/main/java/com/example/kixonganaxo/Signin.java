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
import com.google.firebase.auth.UserProfileChangeRequest;

public class Signin extends AppCompatActivity {
    private final String TAG = "KixongaNaxo";
    private FirebaseAuth mAuth;
    private ProgressDialog alerta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        mAuth = FirebaseAuth.getInstance();

        TextView login = findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Signin.this, Login.class);
                startActivity(i);
            }
        });

        final TextInputLayout nombre = findViewById(R.id.nombre);
        final TextInputLayout correo = findViewById(R.id.correo);
        final TextInputLayout contrasena = findViewById(R.id.contrasena);
        Button registrar = findViewById(R.id.registrar);
        registrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = nombre.getEditText().getText().toString();
                final String email = correo.getEditText().getText().toString();
                final String password = contrasena.getEditText().getText().toString();

                alerta = ProgressDialog.show(Signin.this, "",
                        "Registrando usuario...", true);

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(Signin.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                alerta.dismiss();

                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();

                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest
                                            .Builder()
                                            .setDisplayName(name).build();

                                    user.updateProfile(profileUpdates);
                                    user.sendEmailVerification();

                                    Intent i = new Intent(Signin.this, Inicio.class);
                                    startActivity(i);
                                    finish();
                                } else {
                                    Toast.makeText(Signin.this,
                                            "Error al registrar usuario. Intente de nuevo.",
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                }
        });
    }
}
