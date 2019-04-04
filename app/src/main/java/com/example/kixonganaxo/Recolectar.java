package com.example.kixonganaxo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
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

import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.SetOptions;

import java.io.Serializable;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Recolectar extends AppCompatActivity implements
    InfoBasicaFragment.OnFragmentInteractionListener,
    EtiquetaFragment.OnFragmentInteractionListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private final String TAG = "KixongaNaxo";

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private String colectaId;
    private FirebaseUser user;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Map<String, Object> docData = new HashMap<>();
    private String etiquetaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recolectar);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        colectaId = getIntent().getExtras().getString("DocID");
        docData.put("id_colecta", colectaId);

        user = FirebaseAuth.getInstance().getCurrentUser();
        Map<String, String> colector = new HashMap<>();
        colector.put("id_usuario", user.getUid());
        colector.put("nombre_usuario", user.getDisplayName());
        docData.put("colector", colector);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String fecha = sdf.format(new Date());
        docData.put("fecha_colecta", fecha);

        DocumentReference etiquetaRef = db.collection("etiquetas").document();
        etiquetaId = etiquetaRef.getId();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Recolectar.this);
        builder.setMessage("¿Desea salir sin guardar la información?")
                .setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                })
                .setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

        builder.create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_recolectar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.guardar) {
            //Nombre común
            TextInputLayout nombreComun = findViewById(R.id.nombre_comun);
            String nombre = nombreComun.getEditText().getText().toString();

            //Latitud
            TextInputLayout lat = findViewById(R.id.latitud);
            String sLat = lat.getEditText().getText().toString();
            if (sLat.isEmpty()) {
                sLat = "0.0";
            }
            Double latitud = Double.parseDouble(sLat);

            //Longitud
            TextInputLayout lng = findViewById(R.id.latitud);
            String sLng = lat.getEditText().getText().toString();
            if (sLng.isEmpty()) {
                sLng = "0.0";
            }
            Double longitud = Double.parseDouble(sLng);

            GeoPoint ubicacion = new GeoPoint(latitud, longitud);

            //Hábito de la planta
            RadioGroup radioGroup = findViewById(R.id.habito);
            int radioButtonID = radioGroup.getCheckedRadioButtonId();
            View radioButton = radioGroup.findViewById(radioButtonID);
            int idx = radioGroup.indexOfChild(radioButton);
            RadioButton r = (RadioButton) radioGroup.getChildAt(idx);
            String habito = r.getText().toString();

            //DAP
            TextInputLayout diametro = findViewById(R.id.dap);
            String sDiametro = diametro.getEditText().getText().toString();
            if (sDiametro.isEmpty()) {
                sDiametro = "0.0";
            }
            float dap = Float.parseFloat(sDiametro);

            //Abundancia
            TextInputLayout ab = findViewById(R.id.abundancia);
            String sAb = ab.getEditText().getText().toString();
            if (sAb.isEmpty()) {
                sAb = "0.0";
            }
            float abundancia = Float.parseFloat(sAb);

            //Lugar
            TextInputLayout lug = findViewById(R.id.caracteristicas_lugar);
            String lugar = lug.getEditText().getText().toString();

            //Flores
            TextInputLayout flor = findViewById(R.id.descripcion_flores);
            String flores = flor.getEditText().getText().toString();

            //Hojas
            TextInputLayout hoj = findViewById(R.id.descripcion_hojas);
            String hojas = hoj.getEditText().getText().toString();

            //Latex
            TextInputLayout latx = findViewById(R.id.descripcion_latex);
            String latex = latx.getEditText().getText().toString();

            docData.put("nombre_comun", nombre);
            docData.put("ubicacion", ubicacion);
            docData.put("habito", habito);
            docData.put("dap", dap);
            docData.put("abundancia", abundancia);
            docData.put("caracteristicas_lugar", lugar);
            docData.put("descripcion_flores", flores);
            docData.put("descripcion_hojas", hojas);
            docData.put("descripcion_latex", latex);

            if ( nombre.isEmpty()) {
                Toast.makeText(Recolectar.this,
                        "Ingrese el nombre del ejemplar.",
                        Toast.LENGTH_SHORT).show();
            }else {
                db.collection("etiquetas")
                        .document(etiquetaId)
                        .set(docData);
                Intent intent = new Intent();
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    InfoBasicaFragment infoBasicaFragment = new InfoBasicaFragment();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("DocDatos", (Serializable) docData);
                    infoBasicaFragment.setArguments(bundle);
                    return  infoBasicaFragment;
                case 1:
                    EtiquetaFragment etiquetaFragment = new EtiquetaFragment();
                    return  etiquetaFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }
    }
}
