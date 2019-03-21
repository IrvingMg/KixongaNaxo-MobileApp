package com.example.kixonganaxo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class Restablecer extends AppCompatActivity {
    private final String TAG = "Kixonga Naxo";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restablecer);

        Button restablecer = findViewById(R.id.restablecer);

        restablecer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "Restablecer");
            }
        });
    }
}
