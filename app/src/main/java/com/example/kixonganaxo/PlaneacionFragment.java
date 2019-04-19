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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class PlaneacionFragment extends Fragment {
    private final String TAG = "KixongaNaxo";
    private static final String COLECTA_ID = "COLECTA_ID";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String colectaId;

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
        View view = inflater.inflate(R.layout.fragment_planeacion, container, false);
        initFormatoPlaneacion(view);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
        } else {
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
    private void initFormatoPlaneacion(final View v) {
        db.collection("colectas").document(colectaId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            String error = e.getMessage();
                            Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
                            return;
                        }

                        TextView responsable = v.findViewById(R.id.responsable);
                        TextView objetivo = v.findViewById(R.id.objetivo);
                        TextView tipo = v.findViewById(R.id.tipo);
                        TextView fecha = v.findViewById(R.id.fecha);
                        TextView lugar = v.findViewById(R.id.lugar);
                        TextView especies = v.findViewById(R.id.especies);
                        TextView material = v.findViewById(R.id.material);
                        TextView infoConsulta = v.findViewById(R.id.infoConsulta);
                        TextView infoAdicional = v.findViewById(R.id.infoAdicional);

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
                    }
                });
    }
}