package com.example.kixonganaxo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
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
    private static final String COLECTA_ID = "DocID";
    private final String TAG = "KixongaNaxo";
    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private OnFragmentInteractionListener mListener;
    private String colectaId;
    private ListAdapter adapter;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_recoleccion, container, false);
        initListaEtiquetas(v);
        return v;
    }

    private void initListaEtiquetas(View v) {
        data.clear();
        ListView listView = v.findViewById(R.id.ejemplares_recolectados);
        adapter = new SimpleAdapter(
                getActivity(),
                data,
                android.R.layout.simple_list_item_2,
                new String[] {"Text1","Text2"},
                new int[] {android.R.id.text1, android.R.id.text2});
        listView.setAdapter(adapter);

        registerForContextMenu(listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String etiquetaId = listaIdEtiquetas.get(position);
                Intent i = new Intent(getActivity(), Recolectar.class);
                i.putExtra("ColectaID", colectaId);
                i.putExtra("EtiquetaID", etiquetaId);
                startActivity(i);
            }
        });

        Map<String, String> infoUsuario = new HashMap<>();
        infoUsuario.put("id_usuario", user.getUid());
        infoUsuario.put("nombre_usuario", user.getDisplayName());

        db.collection("etiquetas")
                .whereEqualTo("id_colecta", colectaId)
                .whereEqualTo("colector", infoUsuario)
                .orderBy("nombre_comun", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot querySnapshot,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        listaIdEtiquetas.clear();
                        data.clear();

                        for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                            listaIdEtiquetas.add(documentSnapshot.getId());

                            Map<String, String> datum = new HashMap<>(2);
                            datum.put("Text1", documentSnapshot.getData().get("nombre_comun").toString());
                            GeoPoint geoPoint = (GeoPoint) documentSnapshot.getData().get("ubicacion");
                            Double latitud = geoPoint.getLatitude();
                            Double longitud = geoPoint.getLongitude();
                            datum.put("Text2", "Latitud: " + latitud + " Longitud: " + longitud);
                            data.add(datum);
                        }

                        ((SimpleAdapter) adapter).notifyDataSetChanged();
                    }
                });

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
        ((SimpleAdapter) adapter).notifyDataSetChanged();

        return true;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
    }
}
