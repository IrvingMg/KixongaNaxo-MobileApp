package com.example.kixonganaxo;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;

import javax.annotation.Nullable;

public class MapaFragment extends SupportMapFragment implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener {

    private static final String COLECTA_ID = "COLECTA_ID";
    private final String TAG = "KixongaNaxo";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String colectaId;

    public MapaFragment() {
        // Required empty public constructor
    }

    public static MapaFragment newInstance(String id) {
        MapaFragment fragment = new MapaFragment();
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
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        initMapa(googleMap);
        initHistorialEjemplares(googleMap);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
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
    private void initMapa(final GoogleMap mapa) {
        db.collection("colectas").document(colectaId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            String error = e.getMessage();
                            Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
                            return;
                        }

                        Map<String, Object> infoColecta = documentSnapshot.getData();
                        double latitud = Double.parseDouble(infoColecta.get("latitud").toString());
                        double longitud = Double.parseDouble(infoColecta.get("longitud").toString());
                        LatLng lugarColecta = new LatLng(latitud, longitud);
                        mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(lugarColecta, 16));
                    }
                });
    }

    private void initHistorialEjemplares(final GoogleMap mapa) {
        db.collection("etiquetas")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            String error = e.getMessage();
                            Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
                            return;
                        }

                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            String nombre_comun = documentSnapshot.getData().get("nombre_comun").toString();
                            String fecha_colecta = documentSnapshot.getData().get("fecha_colecta").toString();
                            GeoPoint ubicacion = (GeoPoint) documentSnapshot.getData().get("ubicacion");
                            LatLng coordenadas = new LatLng(ubicacion.getLatitude(), ubicacion.getLongitude());

                            Marker mEjemplar = mapa.addMarker(new MarkerOptions()
                                    .position(coordenadas)
                                    .title(nombre_comun)
                                    .snippet("Fecha: " + fecha_colecta)
                                    .snippet("Lat: " + ubicacion.getLatitude() + " Lng: " + ubicacion.getLongitude()));
                            mEjemplar.setTag(0);
                        }
                    }
                });

        mapa.setOnMarkerClickListener(this);
    }
}