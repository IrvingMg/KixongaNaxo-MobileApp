package com.example.kixonganaxo;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PlaneacionFragment extends Fragment {
    private final String TAG = "KixongaNaxo";
    private static final String COLECTA_ID = "COLECTA_ID";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String colectaId;
    private OnFragmentInteractionListener mListener;

    public PlaneacionFragment() {
        // Required empty public constructor
    }

    public static PlaneacionFragment newInstance(String id) {
        PlaneacionFragment fragment = new PlaneacionFragment();
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
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_planeacion, container, false);

        db.collection("colectas").document(colectaId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot.exists()) {
                                TextView responsable = view.findViewById(R.id.responsable);
                                TextView objetivo = view.findViewById(R.id.objetivo);
                                TextView tipo = view.findViewById(R.id.tipo);
                                TextView fecha = view.findViewById(R.id.fecha);
                                TextView lugar = view.findViewById(R.id.lugar);
                                TextView especies = view.findViewById(R.id.especies);
                                TextView material = view.findViewById(R.id.material);
                                TextView infoConsulta = view.findViewById(R.id.infoConsulta);
                                TextView infoAdicional = view.findViewById(R.id.infoAdicional);

                                List<TextView> formatoPlaneacion = Arrays.asList(responsable, objetivo, tipo, fecha,
                                        lugar, especies, material, infoConsulta, infoAdicional);
                                List<String> campoFormato = Arrays.asList("responsable", "objetivo", "tipo", "fecha",
                                        "lugar", "especies", "material-campo", "info-consulta", "info-adicional");

                                Map<String, Object> document = documentSnapshot.getData();
                                for (int i = 0; i < formatoPlaneacion.size(); i++) {
                                    String campo = campoFormato.get(i);
                                    Object valor = document.get(campo);

                                    if (valor.equals("") != true && valor.toString().equals("[]") != true ) {
                                        formatoPlaneacion.get(i).setText(valor.toString());
                                    }
                                }
                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });

        return view;
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