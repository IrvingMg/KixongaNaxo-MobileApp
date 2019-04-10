package com.example.kixonganaxo;

import android.content.Context;
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


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RecoleccionFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RecoleccionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecoleccionFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String DOCID = "DocID";

    // TODO: Rename and change types of parameters
    private final String TAG = "KixongaNaxo";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private String docId;
    private List<Map<String, String>> data = new ArrayList<Map<String, String>>();
    private List<String> etiquetaIDs = new ArrayList<>();

    private OnFragmentInteractionListener mListener;

    public RecoleccionFragment() {
        // Required empty public constructor
    }

    public static RecoleccionFragment newInstance(String docId) {
        RecoleccionFragment fragment = new RecoleccionFragment();
        Bundle args = new Bundle();
        args.putString(DOCID, docId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            docId = getArguments().getString(DOCID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_recoleccion, container, false);
        final TextView textView = v.findViewById(R.id.mensajeVacio);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        Map<String, String> infoUsuario = new HashMap<>();
        infoUsuario.put("id_usuario", user.getUid());
        infoUsuario.put("nombre_usuario", user.getDisplayName());

        data.clear();
        ListView listView = v.findViewById(R.id.ejemplares_recolectados);
        final ListAdapter adapter = new SimpleAdapter(
                getActivity(),
                data,
                android.R.layout.simple_list_item_2,
                new String[] {"Text1","Text2"}, // Array of cursor columns to bind to.
                new int[] {android.R.id.text1, android.R.id.text2});
        listView.setAdapter(adapter);

        registerForContextMenu(listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String etiquetaID = etiquetaIDs.get(position);
                Log.d(TAG, "ID: " + etiquetaID);
            }
        });
        db.collection("etiquetas")
                .whereEqualTo("id_colecta", docId)
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

                        for (DocumentChange change : querySnapshot.getDocumentChanges()) {
                            if (change.getType() == DocumentChange.Type.ADDED) {
                                etiquetaIDs.add(change.getDocument().getId());

                                Map<String, String> datum = new HashMap<String, String>(2);
                                datum.put("Text1", change.getDocument().get("nombre_comun").toString());
                                GeoPoint geoPoint = (GeoPoint) change.getDocument().get("ubicacion");
                                Double latitud = geoPoint.getLatitude();
                                Double longitud = geoPoint.getLongitude();
                                datum.put("Text2", "Latitud: " + latitud + " Longitud: " + longitud);
                                data.add(datum);
                            }
                        }
                        ((SimpleAdapter) adapter).notifyDataSetChanged();
                    }
                });

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
        Log.d(TAG, "" + item.getItemId());
        return true;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
