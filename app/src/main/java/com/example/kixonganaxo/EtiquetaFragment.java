package com.example.kixonganaxo;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;


public class EtiquetaFragment extends Fragment {
    private static final String ETIQUETA_ID = "EtiquetaID";
    private static final String NUEVA_ETIQUETA = "NuevaEtiqueta";
    private final String TAG = "KixongaNaxo";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private OnFragmentInteractionListener mListener;
    private Map<String, Object> docEtiqueta;
    private String etiquetaId;
    private boolean nuevaEtiqueta;

    public EtiquetaFragment() {
        // Required empty public constructor
    }

    public static EtiquetaFragment newInstance(String id, Boolean bandera) {
        EtiquetaFragment fragment = new EtiquetaFragment();
        Bundle args = new Bundle();
        args.putString(ETIQUETA_ID, id);
        args.putBoolean(NUEVA_ETIQUETA, bandera);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            etiquetaId = getArguments().getString(ETIQUETA_ID);
            nuevaEtiqueta = getArguments().getBoolean(NUEVA_ETIQUETA);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_etiqueta, container, false);
        initEtiquetaColecta(view);
        return view;
    }

    private void initEtiquetaColecta(final View v) {
        if (nuevaEtiqueta == false) {
            db.collection("etiquetas")
                    .document(etiquetaId)
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w(TAG, "Listen error", e);
                                return;
                            }

                            if (documentSnapshot != null && documentSnapshot.exists()) {
                                docEtiqueta = documentSnapshot.getData();

                                RadioGroup habito = v.findViewById(R.id.habito);
                                String habitoVal = docEtiqueta.get("habito").toString();
                                int radioChecked = 0;
                                switch (habitoVal) {
                                    case "Rastrera":
                                        radioChecked = R.id.rastrera;
                                        break;
                                    case "Epifita":
                                        radioChecked = R.id.epifita;
                                        break;
                                    case "Hierba":
                                        radioChecked = R.id.hierba;
                                        break;
                                    case "Arbusto":
                                        radioChecked = R.id.arbusto;
                                        break;
                                    case "Árbol":
                                        radioChecked = R.id.arbol;
                                        break;
                                    default:
                                }
                                habito.check(radioChecked);

                                //DAP
                                String dapVal = docEtiqueta.get("dap").toString();
                                TextInputLayout dap = v.findViewById(R.id.dap);
                                dap.getEditText().setText(dapVal);

                                //Abundancia
                                String abundanciaVal = docEtiqueta.get("abundancia").toString();
                                TextInputLayout abundancia = v.findViewById(R.id.abundancia);
                                abundancia.getEditText().setText(abundanciaVal);

                                //Lugar
                                String lugarVal = docEtiqueta.get("caracteristicas_lugar").toString();
                                TextInputLayout lugar = v.findViewById(R.id.caracteristicas_lugar);
                                lugar.getEditText().setText(lugarVal);

                                //Flores
                                String florVal = docEtiqueta.get("descripcion_flores").toString();
                                TextInputLayout flor = v.findViewById(R.id.descripcion_flores);
                                flor.getEditText().setText(florVal);

                                //Hojas
                                String hojasVal = docEtiqueta.get("descripcion_hojas").toString();
                                TextInputLayout hojas = v.findViewById(R.id.descripcion_hojas);
                                hojas.getEditText().setText(hojasVal);

                                //Latex
                                String latexVal = docEtiqueta.get("descripcion_latex").toString();
                                TextInputLayout latex = v.findViewById(R.id.descripcion_latex);
                                latex.getEditText().setText(latexVal);
                            } else {
                                Log.d(TAG, "Data: null");
                            }
                        }
                    });
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

    public interface OnFragmentInteractionListener {
    }
}
