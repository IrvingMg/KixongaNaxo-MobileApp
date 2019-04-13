package com.example.kixonganaxo;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import com.example.kixonganaxo.dummy.DummyContent;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.opencensus.internal.StringUtil;

public class Colecta extends AppCompatActivity implements
    PlaneacionFragment.OnFragmentInteractionListener,
    MapaFragment.OnFragmentInteractionListener,
    RecoleccionFragment.OnFragmentInteractionListener {

    private final String TAG = "KixongaNaxo";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private String docId;
    private Map<String, Object> infoColecta;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private ArrayList<Map<String, String>> participantes;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private boolean participa = false;
    private String lugar;
    private ArrayList<String> audios;
    private ArrayList<String> fotos;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_colecta);

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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(participa == true) {
                    Intent i = new Intent(Colecta.this, Recolectar.class);
                    i.putExtra("ColectaID", docId);
                    i.putExtra("Lugar", lugar);
                    startActivityForResult(i, 0);
                }else {
                    Snackbar.make(view,
                            "Por favor, regístrate como participante.",
                            Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }

            }
        });

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        docId = getIntent().getExtras().getString("DocID");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(Colecta.this,
                        "Información guardada.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_colecta, menu);

        db.collection("colectas").document(docId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot.exists()) {
                                infoColecta = documentSnapshot.getData();

                                lugar = infoColecta.get("lugar").toString();

                                String titulo = infoColecta.get("titulo").toString();
                                ActionBar actionBar = getSupportActionBar();
                                actionBar.setTitle(titulo);

                                participantes = (ArrayList<Map<String, String>>) infoColecta.get("participantes");

                                if (participantes.size() != 0) {
                                    String usuarioId = user.getUid();

                                    for (int i = 0; i < participantes.size(); i++) {
                                        String id = participantes.get(i).get("id_usuario");

                                        if (id.equals(usuarioId) == true) {
                                            menu.findItem(R.id.subir_etiquetas).setVisible(true);
                                            participa = true;
                                        } else {
                                            menu.findItem(R.id.nuevo_participante).setVisible(true);
                                        }
                                    }
                                } else {
                                    menu.findItem(R.id.nuevo_participante).setVisible(true);
                                }
                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });
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

                            db.collection("colectas").document(docId)
                                    .set(datos, SetOptions.merge())
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            progressDialog.dismiss();

                                            Toolbar toolbar = findViewById(R.id.toolbar);
                                            toolbar.getMenu().findItem(R.id.nuevo_participante).setVisible(false);
                                            toolbar.getMenu().findItem(R.id.subir_etiquetas).setVisible(true);
                                            participa = true;
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
            File directorioFotos = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS), "KixongaNaxo/Ejemplares/" + docId);
            File directorioNotas = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS), "KixongaNaxo/Notas/" + docId);

            subirArchivos(directorioFotos);
            subirArchivos(directorioNotas);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void subirArchivos(File directorio) {
        String tipoArchivo;
        String nombreArchivo;
        String directorioStorage;
        String etiquetaId;
        boolean subirFotos = false;

        String[] etiquetas = directorio.list();
        for (int i = 0; i < etiquetas.length; i++)
        {
            File dir = new File(directorio, etiquetas[i]);
            audios = new ArrayList<>();
            fotos = new ArrayList<>();

            String[] archivos = dir.list();
            for (int j = 0; j < archivos.length; j++) {
                String pathArchivo = new File(dir, archivos[j]).toString();
                Uri uriArchivo = Uri.fromFile(new File(pathArchivo));

                // Obtiene nombre del archivo
                int index = pathArchivo.lastIndexOf("/");
                nombreArchivo = pathArchivo.substring(index + 1);

                // Obtiene extension del archivo
                index = pathArchivo.lastIndexOf(".");
                tipoArchivo = pathArchivo.substring(index);

                if (tipoArchivo.equals(".jpg") == true) {
                    directorioStorage = "fotos";
                    fotos.add(nombreArchivo);
                    subirFotos = true;
                } else {
                    directorioStorage = "audios";
                    audios.add(nombreArchivo);
                }
                subirCloudStorage(uriArchivo, directorioStorage);
            }

            etiquetaId = etiquetas[i];
            subirFirestore(etiquetaId, subirFotos);
        }
    }

    private void subirFirestore(String etiquetaId, boolean subirFotos) {
        Map<String, Object> etiqueta = new HashMap<>();

        if (subirFotos == true) {
            etiqueta.put("fotografias", fotos);
        } else {
            etiqueta.put("audios", audios);
        }

        db.collection("etiquetas").document(etiquetaId)
                .set(etiqueta, SetOptions.merge());
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

    private void subirCloudStorage(Uri file, String directorioStorage) {
        StorageReference storageRef = storage.getReference();
        int index = indexOcurrencia(file.toString(), "/", 2);
        StorageReference fileRef = storageRef.child(directorioStorage + file.toString().substring(index));
        UploadTask uploadTask = fileRef.putFile(file);

        final ProgressDialog alerta = ProgressDialog.show(Colecta.this, "",
                "Subiendo archivos. Esta operación podría tomar unos minutos.", true);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(Colecta.this, "Error. Por favor, intente de nuevo.",
                        Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                alerta.dismiss();
            }
        });
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
            // getItem is called to instantiate the fragment for the given page.
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
}
