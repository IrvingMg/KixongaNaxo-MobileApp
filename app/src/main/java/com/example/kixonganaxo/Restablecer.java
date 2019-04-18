package com.example.kixonganaxo;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class Restablecer extends AppCompatActivity {
    private final String TAG = "KixongaNaxo";
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String email;
    private ProgressDialog alerta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restablecer);

        Button restablecer = findViewById(R.id.restablecer);
        restablecer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final TextInputLayout correo = findViewById(R.id.correo);
                email = correo.getEditText().getText().toString();

                alerta = ProgressDialog.show(Restablecer.this, "",
                        "Enviando instrucciones...", true);

                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                alerta.dismiss();

                                if (task.isSuccessful()) {
                                    correo.getEditText().setText("");
                                    Toast.makeText(Restablecer.this,
                                            "Revise su correo para restablecer la contraseña.",
                                            Toast.LENGTH_LONG).show();
                                } else {
                                    String error = task.getException().getMessage();
                                    Toast.makeText(Restablecer.this,
                                            error, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });
    }
}