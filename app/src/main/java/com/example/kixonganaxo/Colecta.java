package com.example.kixonganaxo;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Colecta extends AppCompatActivity implements
    PlaneacionFragment.OnFragmentInteractionListener,
    MapaFragment.OnFragmentInteractionListener,
    RecoleccionFragment.OnFragmentInteractionListener {

    private final String TAG = "KixongaNaxo";
    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private String colectaId;
    private String lugarColecta;
    private ArrayList<Map<String, String>> participantes;
    private boolean usuarioParticipante = false;
    private ProgressDialog alerta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_colecta);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        ViewPager mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        colectaId = getIntent().getExtras().getString("COLECTA_ID");

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(usuarioParticipante) {
                    Intent i = new Intent(Colecta.this, Recolectar.class);
                    i.putExtra("COLECTA_ID", colectaId);
                    i.putExtra("LUGAR_COLECTA", lugarColecta);
                    startActivityForResult(i, 0);
                }else {
                    Snackbar.make(view,
                            "Regístrese como participante de la colecta.",
                            Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(Colecta.this,
                        "Información guardada.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_colecta, menu);

        db.collection("colectas").document(colectaId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            Map<String, Object> infoColecta;

                            infoColecta = task.getResult().getData();
                            String titulo = infoColecta.get("titulo").toString();
                            ActionBar actionBar = getSupportActionBar();
                            actionBar.setTitle(titulo);

                            lugarColecta = infoColecta.get("lugar").toString();

                            participantes = (ArrayList<Map<String, String>>) infoColecta.get("participantes");
                            buscarUsuario(user.getUid());

                            if (usuarioParticipante) {
                                menu.findItem(R.id.subir_etiquetas).setVisible(true);
                            } else {
                                menu.findItem(R.id.nuevo_participante).setVisible(true);
                            }
                        } else {
                            String error = task.getException().getMessage();
                            Toast.makeText(Colecta.this, error, Toast.LENGTH_LONG).show();
                        }
                    }
                });
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nuevo_participante) {
            AlertDialog.Builder builder = new AlertDialog.Builder(Colecta.this);
            builder.setMessage("¿Desea registrarse como participante?")
                    .setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            alerta = ProgressDialog.show(Colecta.this, "",
                                    "Registrando participante...", true);

                            registrarParticipante();
                        }
                    })
                    .setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });

            builder.create().show();
        }

        if (id == R.id.subir_etiquetas) {
            File directorioFotos = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS), "KixongaNaxo/Ejemplares/" + colectaId);
            File directorioNotas = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS), "KixongaNaxo/Notas/" + colectaId);

            subirArchivos(directorioFotos);
            subirArchivos(directorioNotas);
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
                    RecoleccionFragment recoleccionFragment = new RecoleccionFragment();
                    recoleccionFragment.setArguments(getIntent().getExtras());
                    return recoleccionFragment;

                case 1:
                    MapaFragment mapaFragment = new MapaFragment();
                    mapaFragment.setArguments(getIntent().getExtras());
                    return mapaFragment;
                case 2:
                    PlaneacionFragment planeacionFragment = new PlaneacionFragment();
                    planeacionFragment.setArguments(getIntent().getExtras());
                    return planeacionFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }
    }

    // Métodos privados
    private void buscarUsuario(String usuarioId) {
        for (int i = 0; i < participantes.size(); i++) {
            String id = participantes.get(i).get("id_usuario");

            if (id.equals(usuarioId)) {
                usuarioParticipante = true;
                return;
            }
        }
    }

    private void registrarParticipante() {
        Map<String, String> usuario = new HashMap<>();
        usuario.put("id_usuario", user.getUid());
        usuario.put("nombre_usuario", user.getDisplayName());
        participantes.add(usuario);

        Map<String, Object> datos = new HashMap<>();
        datos.put("participantes", participantes);

        db.collection("colectas").document(colectaId)
                .set(datos, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        alerta.dismiss();

                        Toolbar toolbar = findViewById(R.id.toolbar);
                        toolbar.getMenu().findItem(R.id.nuevo_participante).setVisible(false);
                        toolbar.getMenu().findItem(R.id.subir_etiquetas).setVisible(true);
                        usuarioParticipante = true;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String error = e.getMessage();
                        Toast.makeText(Colecta.this, error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void subirArchivos(File directorio) {
        String tipoArchivo;
        String nombreArchivo;
        String directorioStorage;
        String etiquetaId;

        String[] etiquetas = directorio.list();
        for (String etiqueta : etiquetas) {
            File dir = new File(directorio, etiqueta);
            ArrayList<String> fotos = new ArrayList<>();
            ArrayList<String> audios = new ArrayList<>();


            String[] archivos = dir.list();
            for (String archivo : archivos) {
                String pathArchivo = new File(dir, archivo).toString();
                Uri uriArchivo = Uri.fromFile(new File(pathArchivo));

                // Obtiene nombre del archivo
                int index = pathArchivo.lastIndexOf("/");
                nombreArchivo = pathArchivo.substring(index + 1);

                // Obtiene extension del archivo
                index = pathArchivo.lastIndexOf(".");
                tipoArchivo = pathArchivo.substring(index);

                if (tipoArchivo.equals(".jpg")) {
                    directorioStorage = "fotos";
                    fotos.add(nombreArchivo);
                } else {
                    directorioStorage = "audios";
                    audios.add(nombreArchivo);
                }
                guardarArchivoCloud(uriArchivo, directorioStorage);
            }
            etiquetaId = etiqueta;
            registrarArchivos(etiquetaId, fotos, audios);
        }
    }

    private void guardarArchivoCloud(Uri file, String directorioStorage) {
        StorageReference storageRef = storage.getReference();
        int index = indexOcurrencia(file.toString(), "/", 2);
        StorageReference fileRef = storageRef.child(directorioStorage + file.toString().substring(index));
        UploadTask uploadTask = fileRef.putFile(file);

        alerta = ProgressDialog.show(Colecta.this, "",
                "Subiendo... Esta operación podría tomar unos minutos.", true);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                String error = exception.getMessage();
                Toast.makeText(Colecta.this, error,
                        Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                alerta.dismiss();
            }
        });
    }

    private int indexOcurrencia(String texto, String subtexto, int ocurrencia) {
        int index = texto.length();

        while (ocurrencia != 0) {
            index = texto.lastIndexOf(subtexto, index - 1);
            if (index == -1) {
                return index;
            }
            ocurrencia--;
        }
        return index;
    }

    private void registrarArchivos(String etiquetaId, ArrayList<String> fotos, ArrayList<String> audios) {
        Map<String, Object> etiqueta = new HashMap<>();

        if (fotos.size() > 0) {
            etiqueta.put("fotografias", fotos);
        } else {
            etiqueta.put("audios", audios);
        }

        db.collection("etiquetas").document(etiquetaId)
                .set(etiqueta, SetOptions.merge());
    }
}