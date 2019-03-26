package com.example.kixonganaxo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Inicio extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private final String TAG = "KixongaNaxo";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListView listaColectas;
    private SimpleAdapter adapter;
    private DocumentSnapshot lastVisible;
    private List<Map<String, String>> data = new ArrayList<Map<String, String>>();
    private List<String> listIDs = new ArrayList<>();
    private int totalItems;
    private int itemsVisibles = 0;
    private  View mProgressBarFooter;
    public String valorOrden;
    private Query.Direction direccionOrden;
    private int opcionSelect = 0;
    private int opcionAux;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        listaColectas = findViewById(R.id.lista_colectas);
        adapter = new SimpleAdapter(Inicio.this, data,
                R.layout.multi_lines,
                new String[] {"Text1", "Text2", "Text3"},
                new int[] {R.id.text1, R.id.text2, R.id.text3});
        listaColectas.setAdapter(adapter);
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = mAuth.getCurrentUser();
                if (user != null) {
                    NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                    View headerView = navigationView.getHeaderView(0);
                    TextView nombreUsuario = headerView.findViewById(R.id.nombreUsuario);
                    nombreUsuario.setText(user.getDisplayName());
                    TextView correoUsuario = headerView.findViewById(R.id.correoUsuario);
                    correoUsuario.setText(user.getEmail());

                    if(user.isEmailVerified() == false) {
                        Toast.makeText(Inicio.this,
                                "Verifique su dirección de correo.",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Intent i = new Intent(Inicio.this, Login.class);
                    startActivity(i);
                    finish();
                }
            }
        };
        mProgressBarFooter = ((LayoutInflater) Inicio.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.progress_bar_footer, null, false);
        paginaColectas("titulo", Query.Direction.ASCENDING);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.inicio, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_orderBy) {
            AlertDialog.Builder builder = new AlertDialog.Builder(Inicio.this);
            // Set the dialog title
            builder.setTitle("Ordenar por")
                    // Specify the list array, the items to be selected by default (null for none),
                    // and the listener through which to receive callbacks when items are selected
                    .setSingleChoiceItems(R.array.opcionesOrden, opcionSelect, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            opcionAux = which;
                        }
                    })
                    .setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            data.clear();
                            adapter.notifyDataSetChanged();
                            itemsVisibles = 0;
                            listIDs.clear();
                            Log.d(TAG, "Reset");

                            opcionSelect = opcionAux;
                            switch (opcionSelect) {
                                case 0:
                                    valorOrden = "titulo";
                                    direccionOrden = Query.Direction.ASCENDING;
                                    break;
                                case 1:
                                    valorOrden = "titulo";
                                    direccionOrden = Query.Direction.DESCENDING;
                                    break;
                                case 2:
                                    valorOrden = "lugar";
                                    direccionOrden = Query.Direction.ASCENDING;
                                    break;
                                case 3:
                                    valorOrden = "lugar";
                                    direccionOrden = Query.Direction.DESCENDING;
                                    break;
                                case 4:
                                    valorOrden = "fecha";
                                    direccionOrden = Query.Direction.ASCENDING;
                                    break;
                                case 5:
                                    valorOrden = "fecha";
                                    direccionOrden = Query.Direction.DESCENDING;
                            }
                            paginaColectas(valorOrden, direccionOrden);
                        }
                    })
                    .setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });

            builder.create().show();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_exit:
                cerrarSesion();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void cerrarSesion() {
        mAuth.signOut();
        Intent i = new Intent(Inicio.this, Login.class);
        startActivity(i);
        finish();
    }

    private void paginaColectas(final String campo, final Query.Direction direccion) {
        final CollectionReference colectasRef = db.collection("colectas");
        final int LIMITE = 5;
        final ProgressBar progreso = findViewById(R.id.progreso);

        colectasRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                totalItems = task.getResult().size();
            }
        });

        Query firstQuery = colectasRef.orderBy(campo, direccion).limit(LIMITE);
        firstQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                itemsVisibles += task.getResult().size();
                for (DocumentSnapshot document : task.getResult()) {
                    listIDs.add(document.getId());

                    Map<String, String> datum = new HashMap<String, String>(3);
                    datum.put("Text1", document.getData().get("titulo").toString());
                    datum.put("Text2",document.getData().get("lugar").toString());
                    datum.put("Text3", document.getData().get("fecha").toString());
                    data.add(datum);
                }
                adapter.notifyDataSetChanged();
                lastVisible = task.getResult().getDocuments().get(task.getResult().size() - 1);

                listaColectas.setOnScrollListener(new AbsListView.OnScrollListener() {

                    @Override
                    public void onScrollStateChanged(AbsListView view, int scrollState) {
                        int threshold = 1;
                        int count = listaColectas.getCount();

                        if(scrollState == SCROLL_STATE_IDLE) {
                            if (listaColectas.getLastVisiblePosition() >= count - threshold && itemsVisibles < totalItems) {
                                listaColectas.addFooterView(mProgressBarFooter);
                                Query nextQuery = colectasRef.orderBy(campo, direccion)
                                        .startAfter(lastVisible).limit(LIMITE);
                                nextQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> t) {
                                        listaColectas.removeFooterView(mProgressBarFooter);
                                        itemsVisibles += t.getResult().size();
                                        if (t.isSuccessful()) {
                                            for (DocumentSnapshot document : t.getResult()) {
                                                listIDs.add(document.getId());

                                                Map<String, String> datum = new HashMap<String, String>(3);
                                                datum.put("Text1", document.getData().get("titulo").toString());
                                                datum.put("Text2",document.getData().get("lugar").toString());
                                                datum.put("Text3", document.getData().get("fecha").toString());
                                                data.add(datum);
                                            }
                                            adapter.notifyDataSetChanged();
                                            lastVisible = t.getResult().getDocuments().get(t.getResult().size() - 1);
                                        }
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                    }
                });
            }
        });

        listaColectas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String colectaID = listIDs.get(position);
                Intent i = new Intent(Inicio.this, Colecta.class);
                i.putExtra("DocID", colectaID);
                startActivity(i);
            }
        });
    }
}
