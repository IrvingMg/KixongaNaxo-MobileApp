package com.example.kixonganaxo;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import static android.app.Activity.RESULT_OK;

public class InfoBasicaFragment extends Fragment {
    private static final String ETIQUETA_ID = "ETIQUETA_ID";
    private static final String NUEVA_ETIQUETA = "NUEVA_ETIQUETA";
    private static final String PATH_FOTOS = "PATH_FOTOS";
    private static final String PATH_NOTAS = "PATH_NOTAS";
    private final String TAG = "KixongaNaxo";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private MediaRecorder recorder = null;
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};
    private String fotoName = null;
    private List<String> dataFotos = new ArrayList<>();
    private ArrayAdapter<String> adapterFotos;
    private String notaName = null;
    private List<String> dataNotas = new ArrayList<>();
    private ArrayAdapter<String> adapterNotas;
    private String etiquetaId;
    private boolean nuevaEtiqueta;
    private String directorioNotas;
    private String directorioFotos;
    private ProgressDialog alerta;

    public InfoBasicaFragment() {
        // Required empty public constructor
    }

    public static InfoBasicaFragment newInstance(String id, Boolean bandera,
                                                 String fotos, String notas) {
        InfoBasicaFragment fragment = new InfoBasicaFragment();
        Bundle args = new Bundle();
        args.putString(ETIQUETA_ID, id);
        args.putBoolean(NUEVA_ETIQUETA, bandera);
        args.putString(PATH_FOTOS, fotos);
        args.putString(PATH_NOTAS, notas);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            etiquetaId = getArguments().getString(ETIQUETA_ID);
            nuevaEtiqueta = getArguments().getBoolean(NUEVA_ETIQUETA);
            directorioFotos = getArguments().getString(PATH_FOTOS);
            directorioNotas = getArguments().getString(PATH_NOTAS);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info_basica, container, false);

        initGPS(view);
        initGrabadora(view);
        initCamara(view);
        if (!nuevaEtiqueta) {
            initInfoBasica(view);
        }

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                if (dataFotos.size() == 0) {
                    TextView listaVacia = getView().findViewById(R.id.mensajeVacioFotos);
                    listaVacia.setVisibility(View.GONE);
                }
                dataFotos.add(fotoName);
                adapterFotos.notifyDataSetChanged();
                fotoName = "";

                ListView listaFotos = getView().findViewById(R.id.lista_fotos);
                setListViewHeightBasedOnChildren(listaFotos);
                Toast.makeText(getActivity(), "Fotografía guardada en: " + directorioFotos,
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 200:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) getActivity().finish();

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
    private void initGPS(final View v) {
        Button button = v.findViewById(R.id.GPS);

        final LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                TextInputLayout latitud = v.findViewById(R.id.latitud);
                TextInputLayout longitud = v.findViewById(R.id.longitud);
                latitud.getEditText().setText(String.valueOf(location.getLatitude()));
                longitud.getEditText().setText(String.valueOf(location.getLongitude()));
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };

        final Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(true);
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);

        final Looper looper = null;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(getContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(getContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                LocationManager locationManager = (LocationManager) getActivity()
                        .getSystemService(Context.LOCATION_SERVICE);
                locationManager.requestSingleUpdate(criteria, locationListener, looper);
            }
        });
    }

    private void initGrabadora(View v) {
        dataNotas.clear();
        ListView listaNotas = v.findViewById(R.id.lista_notas);
        listaNotas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setType("*/*");
                startActivity(intent);
            }
        });

        adapterNotas = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, dataNotas);
        listaNotas.setAdapter(adapterNotas);
        initListaView(directorioNotas, dataNotas, adapterNotas, "notas", v);
        setListViewHeightBasedOnChildren(listaNotas);

        ActivityCompat.requestPermissions(getActivity(), permissions, 200);
        final Button record = v.findViewById(R.id.audio);
        record.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                startRecording();
                alerta = new ProgressDialog(getActivity());
                alerta.setMessage("Grabando...");
                alerta.setCancelable(false);
                alerta.setButton(DialogInterface.BUTTON_NEGATIVE, "Detener", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dataNotas.size() == 0) {
                            TextView listaVacia = getView().findViewById(R.id.mensajeVacioNotas);
                            listaVacia.setVisibility(View.GONE);
                        }

                        stopRecording();
                        ListView listaNotas = getView().findViewById(R.id.lista_notas);
                        setListViewHeightBasedOnChildren(listaNotas);
                        Toast.makeText(getActivity(), "Nota guardada en: " + directorioFotos,
                                Toast.LENGTH_LONG).show();
                    }
                });
                alerta.show();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void startRecording() {
        notaName = directorioNotas + File.separator + UUID.randomUUID().toString() +".aac";
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
        recorder.setOutputFile(notaName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        try {
            recorder.prepare();
        } catch (IOException e) {
            String error = e.getMessage();
            Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
        }
        recorder.start();
    }

    private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;

        int index = notaName.lastIndexOf("/");
        String nota = notaName.substring(index + 1);
        dataNotas.add(nota);
        adapterNotas.notifyDataSetChanged();
    }

    private void initCamara(View v) {
        dataFotos.clear();
        ListView listaFotos = v.findViewById(R.id.lista_fotos);
        listaFotos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setType("*/*");
                startActivity(intent);
            }
        });
        adapterFotos = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, dataFotos);
        listaFotos.setAdapter(adapterFotos);
        initListaView(directorioFotos, dataFotos, adapterFotos, "fotos", v);
        setListViewHeightBasedOnChildren(listaFotos);

        Button foto = v.findViewById(R.id.camara);
        foto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });
    }

    public void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri file = Uri.fromFile(getOutputMediaFile());
        intent.putExtra(MediaStore.EXTRA_OUTPUT, file);
        startActivityForResult(intent, 100);
    }

    private  File getOutputMediaFile(){
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        fotoName = "IMG_"+ timeStamp + ".jpg";
        return new File(directorioFotos + File.separator + "IMG_"+ timeStamp + ".jpg");
    }

    private void initInfoBasica(final View v) {
        db.collection("etiquetas")
                .document(etiquetaId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            String error = e.getMessage();
                            Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
                            return;
                        }
                        Map<String, Object> docEtiqueta = documentSnapshot.getData();

                        TextInputLayout nombreComun = v.findViewById(R.id.nombre_comun);
                        String nombre = docEtiqueta.get("nombre_comun").toString();
                        nombreComun.getEditText().setText(nombre);
                        TextInputLayout latitud = v.findViewById(R.id.latitud);
                        TextInputLayout longitud = v.findViewById(R.id.longitud);
                        GeoPoint geoPoint = (GeoPoint) docEtiqueta.get("ubicacion");
                        double lat = geoPoint.getLatitude();
                        double lng = geoPoint.getLongitude();
                        latitud.getEditText().setText(""+lat);
                        longitud.getEditText().setText(""+lng);
                    }
                });
    }

    private void initListaView(String directorio, List<String> datosLista,
                               ArrayAdapter<String> adapterLista, String tipoLista, final View v) {
        List<String> nuevosDatos = new ArrayList<>();
        File dir = new File(directorio);
        if (dir.exists()) {
            Collections.addAll(nuevosDatos, dir.list());
        }

        if (nuevosDatos.size() > 0) {
            datosLista.clear();
            datosLista.addAll(nuevosDatos);
            adapterLista.notifyDataSetChanged();
        } else {
            TextView listaVacia;
            if (tipoLista.equals("fotos")) {
                listaVacia = v.findViewById(R.id.mensajeVacioFotos);
            } else {
                listaVacia = v.findViewById(R.id.mensajeVacioNotas);
            }
            listaVacia.setVisibility(View.VISIBLE);
        }
    }

    // Método tomado de:
    // https://stackoverflow.com/questions/22710093/android-listview-not-scrollable/22710364
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int i = 0, len = listAdapter.getCount(); i < len; i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }
}