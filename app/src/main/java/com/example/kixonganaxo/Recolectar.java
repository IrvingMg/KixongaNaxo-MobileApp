package com.example.kixonganaxo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
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
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.io.File;
import java.io.Serializable;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class Recolectar extends AppCompatActivity implements
    InfoBasicaFragment.OnFragmentInteractionListener,
    EtiquetaFragment.OnFragmentInteractionListener {

    private final String TAG = "KixongaNaxo";
    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String colectaId;
    private String etiquetaId;
    private Map<String, Object> docEtiqueta = new HashMap<>();
    private boolean nuevaEtiqueta = false;
    private String directorioNotas;
    private String directorioFotos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recolectar);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        ViewPager mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        colectaId = getIntent().getExtras().getString("COLECTA_ID");
        etiquetaId = getIntent().getExtras().getString("EtiquetaID");

        if (etiquetaId == null) {
            initNuevaEtiqueta();
        }
        getDirectorios();
    }

    private void getDirectorios() {
        File directoryNotas = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), "KixongaNaxo/Notas/" + colectaId + "/" + etiquetaId);
        directoryNotas.mkdirs();
        directorioNotas = directoryNotas.toString();

        File directoryFotos = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), "KixongaNaxo/Ejemplares/" + colectaId + "/" + etiquetaId);
        directoryFotos.mkdirs();
        directorioFotos = directoryFotos.toString();
    }

    private void initNuevaEtiqueta() {
        nuevaEtiqueta = true;

        DocumentReference etiquetaRef = db.collection("etiquetas").document();
        etiquetaId = etiquetaRef.getId();

        docEtiqueta.put("id_colecta", colectaId);

        Map<String, String> colector = new HashMap<>();
        colector.put("id_usuario", user.getUid());
        colector.put("nombre_usuario", user.getDisplayName());
        docEtiqueta.put("colector", colector);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String fecha = sdf.format(new Date());
        docEtiqueta.put("fecha_colecta", fecha);

        ArrayList<String> fotos = new ArrayList<>();
        docEtiqueta.put("fotografias", fotos);

        String lugar = getIntent().getExtras().getString("LUGAR_COLECTA");
        docEtiqueta.put("lugar", lugar);

        ArrayList<String> audios = new ArrayList<>();
        docEtiqueta.put("audios", audios);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Recolectar.this);
        builder.setMessage("¿Desea salir sin guardar la información?")
                .setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (nuevaEtiqueta == true) {
                            borrarDirectorio(directorioFotos);
                            borrarDirectorio(directorioNotas);
                        }
                        finish();
                    }
                })
                .setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

        builder.create().show();
    }

    private void borrarDirectorio(String path) {
        File dir = new File(path);
        if (dir.isDirectory())
        {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++)
            {
                new File(dir, children[i]).delete();
            }
        }
        dir.delete();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_recolectar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.guardar) {
            //Nombre común
            TextInputLayout nombreComun = findViewById(R.id.nombre_comun);
            String nombre = nombreComun.getEditText().getText().toString();
            if ( nombre.isEmpty()) {
                Toast.makeText(Recolectar.this,
                        "Ingrese el nombre del ejemplar.",
                        Toast.LENGTH_SHORT).show();

                return false;
            }

            //Latitud
            TextInputLayout lat = findViewById(R.id.latitud);
            String sLat = lat.getEditText().getText().toString();
            if (sLat.isEmpty()) {
                sLat = "0.0";
            }
            Double latitud = Double.parseDouble(sLat);

            //Longitud
            TextInputLayout lng = findViewById(R.id.longitud);
            String sLng = lng.getEditText().getText().toString();
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

            // Etiqueta
            docEtiqueta.put("nombre_comun", nombre);
            docEtiqueta.put("ubicacion", ubicacion);
            docEtiqueta.put("habito", habito);
            docEtiqueta.put("dap", dap);
            docEtiqueta.put("abundancia", abundancia);
            docEtiqueta.put("caracteristicas_lugar", lugar);
            docEtiqueta.put("descripcion_flores", flores);
            docEtiqueta.put("descripcion_hojas", hojas);
            docEtiqueta.put("descripcion_latex", latex);

            db.collection("etiquetas")
                    .document(etiquetaId)
                    .set(docEtiqueta, SetOptions.merge());

            Intent intent = new Intent();
            setResult(Activity.RESULT_OK, intent);
            finish();

        }
        return super.onOptionsItemSelected(item);
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
                    Bundle bundleInfo = new Bundle();
                    bundleInfo.putString("EtiquetaID", etiquetaId);
                    bundleInfo.putBoolean("NuevaEtiqueta", nuevaEtiqueta);
                    bundleInfo.putString("PathFotos", directorioFotos);
                    bundleInfo.putString("PathNotas", directorioNotas);
                    infoBasicaFragment.setArguments(bundleInfo);
                    return  infoBasicaFragment;
                case 1:
                    EtiquetaFragment etiquetaFragment = new EtiquetaFragment();
                    Bundle bundleEtiqueta = new Bundle();
                    bundleEtiqueta.putString("EtiquetaID", etiquetaId);
                    bundleEtiqueta.putBoolean("NuevaEtiqueta", nuevaEtiqueta);
                    etiquetaFragment.setArguments(bundleEtiqueta);
                    return  etiquetaFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}