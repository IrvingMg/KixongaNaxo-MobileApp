package com.example.kixonganaxo;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Colecta extends AppCompatActivity implements
    PlaneacionFragment.OnFragmentInteractionListener,
    RecoleccionFragment.OnFragmentInteractionListener{

    private final String TAG = "KixongaNaxo";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String docId;
    private Map<String, Object> infoColecta;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private ArrayList<Map<String, String>> participantes;
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_colecta);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        docId = getIntent().getExtras().getString("DocID");
        db.collection("colectas").document(docId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot.exists()) {
                                infoColecta = documentSnapshot.getData();

                                String titulo = infoColecta.get("titulo").toString();
                                ActionBar actionBar = getSupportActionBar();
                                actionBar.setTitle(titulo);

                                participantes = (ArrayList<Map<String, String>>) infoColecta.get("participantes");

                                if(participantes.size() != 0) {
                                    String nombre = user.getDisplayName();
                                    String usuarioId = user.getUid();

                                    for (int i = 0; i < participantes.size(); i++) {
                                        String id = participantes.get(i).get("id_usuario");

                                        if (id.equals(usuarioId) == true) {
                                            toolbar.getMenu().findItem(R.id.subir_etiquetas).setVisible(true);
                                        } else {
                                            toolbar.getMenu().findItem(R.id.nuevo_participante).setVisible(true);
                                        }
                                    }
                                } else {
                                    toolbar.getMenu().findItem(R.id.nuevo_participante).setVisible(true);
                                }

                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_colecta, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.nuevo_participante) {
            AlertDialog.Builder builder = new AlertDialog.Builder(Colecta.this);
            builder.setMessage("¿Participar en la colecta?")
                    .setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            final ProgressDialog progressDialog = ProgressDialog.show(Colecta.this, "",
                                    "Añadiendo participante...", true);
                            Map<String, String> usuario = new HashMap<>();
                            usuario.put("id_usuario", user.getUid());
                            usuario.put("nombre_usuario", user.getDisplayName());
                            participantes.add(usuario);

                            Map<String, Object> datos = new HashMap<>();
                            datos.put("participantes", participantes);

                            Log.d(TAG, docId);
                            db.collection("colectas").document(docId)
                                    .set(datos, SetOptions.merge())
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            progressDialog.dismiss();

                                            Toolbar toolbar = findViewById(R.id.toolbar);
                                            toolbar.getMenu().findItem(R.id.nuevo_participante).setVisible(false);
                                            toolbar.getMenu().findItem(R.id.subir_etiquetas).setVisible(true);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "Error writing document", e);
                                        }
                                    });
                        }
                    })
                    .setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });

            builder.create().show();
            return true;
        }

        if (id == R.id.subir_etiquetas) {
            Log.d(TAG, "Subir etiquetas...");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            switch (position) {
                case 0:
                    PlaneacionFragment planeacionFragment = new PlaneacionFragment();
                    planeacionFragment.setArguments(getIntent().getExtras());
                    return planeacionFragment;
                case 1:
                    RecoleccionFragment recoleccionFragment = new RecoleccionFragment();
                    recoleccionFragment.setArguments(getIntent().getExtras());
                    return recoleccionFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }
    }
}
