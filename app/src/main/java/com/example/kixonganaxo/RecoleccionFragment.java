package com.example.kixonganaxo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecoleccionFragment extends Fragment {

    private static final String COLECTA_ID = "COLECTA_ID";
    private final String TAG = "KixongaNaxo";
    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String colectaId;
    private SimpleAdapter adapter;
    private List<Map<String, String>> data = new ArrayList<>();
    private List<String> listaIdEtiquetas = new ArrayList<>();

    public RecoleccionFragment() {
        // Required empty public constructor
    }

    public static RecoleccionFragment newInstance(String id) {
        RecoleccionFragment fragment = new RecoleccionFragment();
        Bundle args = new Bundle();
        args.putString(COLECTA_ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            colectaId = getArguments().getString(COLECTA_ID);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_recoleccion, container, false);
        initListaEtiquetas(v);
        return v;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo cmi =
                (AdapterView.AdapterContextMenuInfo) menuInfo;
        menu.add(1, cmi.position, 0, "Eliminar");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        String etiquetaId = listaIdEtiquetas.get(itemId);

        db.collection("etiquetas").document(etiquetaId).delete();
        listaIdEtiquetas.remove(itemId);
        data.remove(itemId);
        adapter.notifyDataSetChanged();

        return true;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!(context instanceof OnFragmentInteractionListener)) {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    // Interface obligatoria
    interface OnFragmentInteractionListener {
    }

    // Métodos privados
    private void initListaEtiquetas(final View v) {
        ListView listView = v.findViewById(R.id.ejemplares_recolectados);
        registerForContextMenu(listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String etiquetaId = listaIdEtiquetas.get(position);
                Intent i = new Intent(getActivity(), Recolectar.class);
                i.putExtra("COLECTA_ID", colectaId);
                i.putExtra("ETIQUETA_ID", etiquetaId);
                startActivity(i);
            }
        });

        Map<String, String> colector = new HashMap<>();
        colector.put("id_usuario", user.getUid());
        colector.put("nombre_usuario", user.getDisplayName());

        db.collection("etiquetas")
                .whereEqualTo("id_colecta", colectaId)
                .whereEqualTo("colector", colector)
                .orderBy("nombre_comun", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot querySnapshot,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            String error = e.getMessage();
                            Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
                            return;
                        }
                        resetListaEtiquetas();

                        TextView listaVacia = v.findViewById(R.id.mensajeVacio);
                        if ( querySnapshot.getDocuments().size() == 0) {
                            listaVacia.setVisibility(View.VISIBLE);
                            return;
                        }
                        listaVacia.setVisibility(View.GONE);

                        for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                            listaIdEtiquetas.add(documentSnapshot.getId());

                            Map<String, String> datum = new HashMap<>(2);
                            datum.put("Text1", documentSnapshot.getData().get("nombre_comun").toString());
                            GeoPoint geoPoint = (GeoPoint) documentSnapshot.getData().get("ubicacion");
                            double latitud = geoPoint.getLatitude();
                            double longitud = geoPoint.getLongitude();
                            datum.put("Text2", "Lat: " + latitud + " Lng: " + longitud);
                            data.add(datum);
                        }

                        ListView listView = v.findViewById(R.id.ejemplares_recolectados);
                        adapter = new SimpleAdapter(
                                getActivity(),
                                data,
                                R.layout.lista_dos_items,
                                new String[] {"Text1","Text2"},
                                new int[] {R.id.text1, R.id.text2});
                        listView.setAdapter(adapter);
                    }
                });
    }

    private void resetListaEtiquetas() {
        listaIdEtiquetas.clear();
        data.clear();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
            adapter = null;
        }
    }
}