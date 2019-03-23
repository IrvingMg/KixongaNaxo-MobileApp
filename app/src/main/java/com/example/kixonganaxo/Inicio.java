package com.example.kixonganaxo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
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
import android.widget.ListView;
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
    private DocumentSnapshot lastVisible;
    private List<Map<String, String>> data = new ArrayList<Map<String, String>>();
    private List<String> listIDs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

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
        paginaColectas();
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
        if (id == R.id.action_settings) {
            return true;
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

    private void paginaColectas() {
        final ListView listaColectas = findViewById(R.id.lista_colectas);
        final CollectionReference colectasRef = db.collection("colectas");
        Query firstQuery = colectasRef.orderBy("titulo", Query.Direction.ASCENDING).limit(8);

        firstQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {


                for (DocumentSnapshot document : task.getResult()) {
                    listIDs.add(document.getId());

                    Map<String, String> datum = new HashMap<String, String>(3);
                    datum.put("Text1", document.getData().get("titulo").toString());
                    datum.put("Text2",document.getData().get("lugar").toString());
                    datum.put("Text3", document.getData().get("fecha").toString());
                    data.add(datum);
                }

                final SimpleAdapter adapter = new SimpleAdapter(Inicio.this, data,
                        R.layout.multi_lines,
                        new String[] {"Text1", "Text2", "Text3"},
                        new int[] {R.id.text1, R.id.text2, R.id.text3});
                listaColectas.setAdapter(adapter);
                lastVisible = task.getResult().getDocuments().get(task.getResult().size() - 1);

                listaColectas.setOnScrollListener(new AbsListView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(AbsListView view, int scrollState) {
                        if(scrollState == SCROLL_STATE_TOUCH_SCROLL) {
                            Log.d(TAG, "Scroll...");
                        }

                    }

                    @Override
                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                        Log.d(TAG, "First: " + firstVisibleItem + " VisCount: " + visibleItemCount
                        + " Total: " + totalItemCount);
                        if(firstVisibleItem + visibleItemCount == totalItemCount) {
                            Log.d(TAG, "Cargar más...");

                            Query nextQuery = colectasRef.orderBy("titulo", Query.Direction.ASCENDING).startAfter(lastVisible).limit(8);
                            nextQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> t) {
                                    if (t.isSuccessful()) {
                                        for (DocumentSnapshot document : t.getResult()) {
                                            listIDs.add(document.getId());

                                            Map<String, String> datum = new HashMap<String, String>(3);
                                            datum.put("Text1", document.getData().get("titulo").toString());
                                            datum.put("Text2",document.getData().get("lugar").toString());
                                            datum.put("Text3", document.getData().get("fecha").toString());
                                            data.add(datum);
                                        }

                                        if(t.getResult().getDocuments().size() > 0) {
                                            adapter.notifyDataSetChanged();
                                            lastVisible = t.getResult().getDocuments().get(t.getResult().size() - 1);
                                        }
                                    }
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    private void listaColectas() {
        ListView listaColectas = findViewById(R.id.lista_colectas);
        ArrayList<String> listaIDs = new ArrayList<String>(10);

        List<Map<String, String>> data = new ArrayList<Map<String, String>>();
        //Ciclo
        Map<String, String> datum = new HashMap<String, String>(2);
        datum.put("Text1", "First line of text");
        datum.put("Text2","Second line of text");
        datum.put("Text3","Third line of text");
        data.add(datum);
        Map<String, String> datum2 = new HashMap<String, String>(2);
        datum2.put("Text1", "First line of text");
        datum2.put("Text2","Second line of text");
        datum2.put("Text3","Third line of text");
        data.add(datum2);
        //Fin ciclo

        SimpleAdapter adapter = new SimpleAdapter(this, data,
                R.layout.multi_lines,
                new String[] {"Text1", "Text2", "Text3"},
                new int[] {R.id.text1, R.id.text2, R.id.text3});

        listaColectas.setAdapter(adapter);
        listaColectas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "Item en posición: " + position);
            }
        });
    }
}
