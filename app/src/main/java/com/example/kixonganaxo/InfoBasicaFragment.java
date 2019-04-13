package com.example.kixonganaxo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;
import static android.support.v4.content.ContextCompat.getDataDir;
import static android.support.v4.content.ContextCompat.getSystemService;

public class InfoBasicaFragment extends Fragment {
    private static final String DATOS = "DatosEtiqueta";
    private static final String NUEVA_ETIQUETA = "NuevaEtiqueta";
    private static final String PATH_FOTOS = "PathFotos";
    private static final String PATH_NOTAS = "PathNotas";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private final String TAG = "KixongaNaxo";
    private OnFragmentInteractionListener mListener;
    private MediaRecorder recorder = null;
    private boolean mStartRecording = true;
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};
    private Map<String, Object> docEtiqueta;
    private String fotoName = null;
    private List<String> dataFotos = new ArrayList<>();
    private ArrayAdapter<String> adapterFotos;
    private String notaName = null;
    private List<String> dataNotas = new ArrayList<>();
    private ArrayAdapter<String> adapterNotas;
    private boolean nuevaEtiqueta;
    private String directorioNotas;
    private String directorioFotos;


    public InfoBasicaFragment() {
        // Required empty public constructor
    }

    public static InfoBasicaFragment newInstance(Map<String, Object> documento, Boolean bandera,
                                                 String fotos, String notas) {
        InfoBasicaFragment fragment = new InfoBasicaFragment();
        Bundle args = new Bundle();
        args.putSerializable(DATOS, (Serializable) documento);
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
            docEtiqueta = (Map<String, Object>) getArguments().getSerializable(DATOS);
            nuevaEtiqueta = getArguments().getBoolean(NUEVA_ETIQUETA);
            directorioFotos = getArguments().getString(PATH_FOTOS);
            directorioNotas = getArguments().getString(PATH_NOTAS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_info_basica, container, false);
        cargarGPS(view);
        cargarGrabadora(view);
        cargarCamara(view);

        return view;
    }

    private void cargarGPS(final View v) {
        Button button = v.findViewById(R.id.GPS);

        final LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                TextInputLayout latitud = v.findViewById(R.id.latitud);
                TextInputLayout longitud = v.findViewById(R.id.longitud);
                Location mlocation = location;
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
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                locationManager.requestSingleUpdate(criteria, locationListener, looper);
            }
        });
    }

    private void cargarGrabadora(View v) {
        // Inicializar lista de audios
        if (nuevaEtiqueta == true) {
            dataNotas.clear();
        } else {
            Log.d(TAG, "Cargar Audios: " + directorioNotas);
        }
        ListView listaNotas = v.findViewById(R.id.lista_notas);
        adapterNotas = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, dataNotas);
        listaNotas.setAdapter(adapterNotas);

        ActivityCompat.requestPermissions(getActivity(), permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        final Button record = v.findViewById(R.id.audio);
        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                    record.setText("Detener");
                    v.setTag(0);
                } else {
                    record.setText("Grabar Nota");
                    v.setTag(1);
                }
                mStartRecording = !mStartRecording;
            }
        });
    }

    private void cargarCamara(View v) {
        // Fotos
        dataFotos.clear();
        ListView listaFotos = v.findViewById(R.id.lista_fotos);
        adapterFotos = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, dataFotos);
        listaFotos.setAdapter(adapterFotos);

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
        return new File(directorioFotos + File.separator +
                "IMG_"+ timeStamp + ".jpg");
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                dataFotos.add(fotoName);
                adapterFotos.notifyDataSetChanged();
                fotoName = "";
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) getActivity().finish();

    }

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

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
            Log.e(TAG, "prepare() failed");
        }

        recorder.start();
    }

    private void stopRecording() {
        int index = notaName.lastIndexOf("/");
        String nota = notaName.substring(index + 1);

        recorder.stop();
        recorder.release();
        recorder = null;

        dataNotas.add(nota);
        adapterNotas.notifyDataSetChanged();
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
