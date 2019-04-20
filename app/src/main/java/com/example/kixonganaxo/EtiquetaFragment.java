package com.example.kixonganaxo;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.Map;

import javax.annotation.Nullable;


public class EtiquetaFragment extends Fragment {
    private static final String ETIQUETA_ID = "ETIQUETA_ID";
    private static final String NUEVA_ETIQUETA = "NUEVA_ETIQUETA";
    private final String TAG = "KixongaNaxo";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String etiquetaId;
    private boolean nuevaEtiqueta;
    private ArrayAdapter<CharSequence> adapter;

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_etiqueta, container, false);

        Spinner spinner = view.findViewById(R.id.habito);
        spinner.setFocusable(true);
        spinner.setFocusableInTouchMode(true);
        spinner.requestFocus();
        adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.habito_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        initEtiquetaColecta(view);
        return view;
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
    private void initEtiquetaColecta(final View v) {
        if (!nuevaEtiqueta) {
            db.collection("etiquetas")
                    .document(etiquetaId)
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot,
                                            @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                String error = e.getMessage();
                                Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
                                return;
                            }

                            Map<String, Object> docEtiqueta;
                            docEtiqueta = documentSnapshot.getData();

                            String habitoVal = docEtiqueta.get("habito").toString();
                            int habitoValPos = adapter.getPosition(habitoVal);
                            Spinner habito = v.findViewById(R.id.habito);
                            habito.setSelection(habitoValPos);

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
                        }
                    });
        }
    }
}
