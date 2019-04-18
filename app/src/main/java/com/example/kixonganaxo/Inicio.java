package com.example.kixonganaxo;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Inicio extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final String TAG = "KixongaNaxo";
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseUser user = mAuth.getCurrentUser();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth.AuthStateListener mAuthListener;
    private List<Map<String, String>> data = new ArrayList<>();
    private List<String> listaIdColectas = new ArrayList<>();
    private SimpleAdapter adapter;
    private int opcionSelect = 0;
    private ProgressDialog alerta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        userListener();
        initListaColectas("titulo", Query.Direction.ASCENDING);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.inicio, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_orderBy) {
            AlertDialog.Builder builder = new AlertDialog.Builder(Inicio.this);
            builder.setTitle("Ordenar por")
                    .setSingleChoiceItems(R.array.opcionesOrden, opcionSelect, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            opcionSelect = which;
                        }
                    })
                    .setPositiveButton("ASCENDENTE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            ordenarListaColectas(opcionSelect, Query.Direction.ASCENDING);
                        }
                    })
                    .setNegativeButton("DESCENDENTE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            ordenarListaColectas(opcionSelect, Query.Direction.DESCENDING);
                        }
                    });
            builder.create().show();
        }

        if (id == R.id.action_sync) {
            initListaColectas("titulo", Query.Direction.ASCENDING);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_exit:
                cerrarSesion();
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // Métodos privados
    private void userListener() {
        mAuthListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (user != null) {
                    NavigationView navigationView = findViewById(R.id.nav_view);
                    View headerView = navigationView.getHeaderView(0);

                    TextView nombreUsuario = headerView.findViewById(R.id.nombreUsuario);
                    nombreUsuario.setText(user.getDisplayName());

                    TextView correoUsuario = headerView.findViewById(R.id.correoUsuario);
                    correoUsuario.setText(user.getEmail());

                    if(!user.isEmailVerified()) {
                        Toast.makeText(Inicio.this,
                                "Verifique su dirección de correo.",
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    Intent i = new Intent(Inicio.this, Login.class);
                    startActivity(i);
                    finish();
                }
            }
        };
    }

    private void initListaColectas(String campo, Query.Direction orden) {
        alerta = ProgressDialog.show(Inicio.this, "",
                "Cargando...", true);

        CollectionReference colectasRef = db.collection("colectas");
        colectasRef.whereEqualTo("publico", true).orderBy(campo, orden)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        alerta.dismiss();
                        resetListaColectas();

                        TextView listaVacia = findViewById(R.id.mensajeVacio);
                        if (task.getResult().size() == 0) {
                            listaVacia.setVisibility(View.VISIBLE);
                            return;
                        }
                        listaVacia.setVisibility(View.GONE);

                        for (DocumentSnapshot document : task.getResult()) {
                            listaIdColectas.add(document.getId());

                            Map<String, String> datum = new HashMap<String, String>(3);
                            datum.put("Text1", document.getData().get("titulo").toString());
                            datum.put("Text2",document.getData().get("lugar").toString());
                            datum.put("Text3", document.getData().get("fecha").toString());
                            data.add(datum);
                        }

                        ListView listaColectas = findViewById(R.id.lista_colectas);
                        adapter = new SimpleAdapter(Inicio.this, data,
                                R.layout.lista_tres_items,
                                new String[] {"Text1", "Text2", "Text3"},
                                new int[] {R.id.text1, R.id.text2, R.id.text3});
                        listaColectas.setAdapter(adapter);
                    }
                });

        ListView listaColectas = findViewById(R.id.lista_colectas);
        listaColectas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String colectaId = listaIdColectas.get(position);
                Intent i = new Intent(Inicio.this, Colecta.class);
                i.putExtra("COLECTA_ID", colectaId);
                startActivity(i);
            }
        });
    }

    private void resetListaColectas() {
        listaIdColectas.clear();
        data.clear();

        if (adapter != null) {
            adapter.notifyDataSetChanged();
            adapter = null;
        }
    }

    private void ordenarListaColectas(int menuOpcion, Query.Direction orden) {
        String campo = "titulo";

        switch (menuOpcion) {
            case 0:
                campo = "titulo";
                break;
            case 1:
                campo = "titulo";
                break;
            case 2:
                campo = "lugar";
                break;
            default:
        }
        initListaColectas(campo, orden);
    }

    private void cerrarSesion() {
        mAuth.signOut();
        Intent i = new Intent(Inicio.this, Login.class);
        startActivity(i);
        finish();
    }
}