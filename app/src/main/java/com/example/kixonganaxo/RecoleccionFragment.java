package com.example.kixonganaxo;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
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

        ListView listView = v.findViewById(R.id.ejemplares_recolectados);
        ListAdapter adapter = new SimpleAdapter(
                getActivity(),
                data,
                android.R.layout.simple_list_item_2,
                new String[] {"Text1","Text2"}, // Array of cursor columns to bind to.
                new int[] {android.R.id.text1, android.R.id.text2});
        listView.setAdapter(adapter);

        db.collection("etiquetas")
                .whereEqualTo("id_colecta", docId)
                .whereEqualTo("colector", infoUsuario).get()
        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if(querySnapshot.isEmpty() == false) {
                        textView.setVisibility(View.GONE);
                        for (DocumentSnapshot documentSnapshot : querySnapshot) {
                            Map<String, String> datum = new HashMap<String, String>(2);
                            datum.put("Text1", documentSnapshot.get("nombre_comun").toString());
                            GeoPoint geoPoint = (GeoPoint) documentSnapshot.get("ubicacion");
                            Double latitud = geoPoint.getLatitude();
                            Double longitud = geoPoint.getLongitude();
                            datum.put("Text2", "Latitud: " + latitud + " Longitud: " + longitud);
                            data.add(datum);
                        }

                    } else {
                        textView.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        return v;
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
