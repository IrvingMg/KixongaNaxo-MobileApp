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


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PlaneacionFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PlaneacionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlaneacionFragment extends Fragment {
    private final String TAG = "KixongaNaxo";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "DocID";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // TODO: Rename and change types of parameters
    private String mParam1;

    private OnFragmentInteractionListener mListener;

    public PlaneacionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PlaneacionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PlaneacionFragment newInstance(String param1, String param2) {
        PlaneacionFragment fragment = new PlaneacionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_planeacion, container, false);

        db.collection("colectas").document(mParam1).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot.exists()) {
                                TextView responsable = getView().findViewById(R.id.responsable);
                                TextView objetivo = getView().findViewById(R.id.objetivo);
                                TextView tipo = getView().findViewById(R.id.tipo);
                                TextView fecha = getView().findViewById(R.id.fecha);
                                TextView lugar = getView().findViewById(R.id.lugar);
                                TextView especies = getView().findViewById(R.id.especies);
                                TextView material = getView().findViewById(R.id.material);
                                TextView infoConsulta = getView().findViewById(R.id.infoConsulta);
                                TextView infoAdicional = getView().findViewById(R.id.infoAdicional);

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
