package com.example.kixonganaxo;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.SetOptions;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
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
        etiquetaId = getIntent().getExtras().getString("ETIQUETA_ID");
        if (etiquetaId == null) {
            initNuevaEtiqueta();
        }
        getDirectorios();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Recolectar.this);
        builder.setMessage("¿Desea salir sin guardar la información?")
                .setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (nuevaEtiqueta) {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_recolectar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.guardar) {
            registrarEtiqueta();

            Intent intent = new Intent();
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    InfoBasicaFragment infoBasicaFragment = new InfoBasicaFragment();
                    Bundle bundleInfo = new Bundle();
                    bundleInfo.putString("ETIQUETA_ID", etiquetaId);
                    bundleInfo.putBoolean("NUEVA_ETIQUETA", nuevaEtiqueta);
                    bundleInfo.putString("PATH_FOTOS", directorioFotos);
                    bundleInfo.putString("PATH_NOTAS", directorioNotas);
                    infoBasicaFragment.setArguments(bundleInfo);
                    return  infoBasicaFragment;
                case 1:
                    EtiquetaFragment etiquetaFragment = new EtiquetaFragment();
                    Bundle bundleEtiqueta = new Bundle();
                    bundleEtiqueta.putString("ETIQUETA_ID", etiquetaId);
                    bundleEtiqueta.putBoolean("NUEVA_ETIQUETA", nuevaEtiqueta);
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

    // Métodos privados
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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
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

    private void borrarDirectorio(String path) {
        File dir = new File(path);
        if (dir.isDirectory())
        {
            String[] children = dir.list();
            for (String aChildren : children) {
                new File(dir, aChildren).delete();
            }
        }
        dir.delete();
    }

    private void registrarEtiqueta() {
        //Nombre común
        TextInputLayout nombreComun = findViewById(R.id.nombre_comun);
        String nombre = nombreComun.getEditText().getText().toString();
        if ( nombre.isEmpty()) {
            Toast.makeText(Recolectar.this,
                    "Ingrese el nombre del ejemplar.",
                    Toast.LENGTH_SHORT).show();

            return;
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
        Spinner spinnerHabito = findViewById(R.id.habito);
        String habito = spinnerHabito.getSelectedItem().toString();
        Log.d(TAG, "Hábito: " + habito);

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
    }
}