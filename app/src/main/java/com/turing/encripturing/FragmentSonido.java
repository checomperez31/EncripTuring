package com.turing.encripturing;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class FragmentSonido extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private Context context;

    private Uri pathSound;

    private MediaPlayer mediaPlayer;

    private final int SELECT_PICTURE = 200;

    //Boton agregar sonido
    private FloatingActionButton btn_add;

    private EditText editTitulo;
    private Button play, encriptar;

    //Variables globales para el reproductor
    private MediaPlayerService player;
    boolean serviceBound = false;
    ArrayList<Audio> audioList;

    private DialogRecord dialogRecord;

    public FragmentSonido() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static FragmentSonido newInstance(String param1, String param2) {
        FragmentSonido fragment = new FragmentSonido();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        //playAudio("https://upload.wikimedia.org/wikipedia/commons/6/6c/Grieg_Lyric_Pieces_Kobold.ogg");
        //es necesario tener minimo un archivo de audio, sino crashea
        //loadAudio();
        //playAudio(audioList.get(0).getData());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fragment_sonido, container, false);

        btn_add = (FloatingActionButton) view.findViewById(R.id.sonido_button_add);

        editTitulo = (EditText) view.findViewById(R.id.sonido_edit_titulo);
        play = (Button) view.findViewById(R.id.sonido_button_play);
        encriptar = (Button) view.findViewById(R.id.sonido_button_encriptar);

        dialogRecord = new DialogRecord(context);
        mediaPlayer = new MediaPlayer();
        agregarListeners();

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
        this.context = context;
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
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    /**
     * Enlazar el servicio del reproductor con la Actividad
     *
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;

            Toast.makeText(getActivity(), "Service Bound", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    /**
     * metodo para reproducir el audio
     *
     */
    private void playAudio(String media) {
        //Revisar si el servicio esta activo
        if (!serviceBound) {
            Intent playerIntent = new Intent(getActivity(), MediaPlayerService.class);
            playerIntent.putExtra("media", media);
            getActivity().startService(playerIntent);
            getActivity().bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            //Servicio activo
        }
    }

    /**
     * metodos para evitar que el reproductor crashee
     *
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putBoolean("ServiceState", serviceBound);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //serviceBound = savedInstanceState.getBoolean("ServiceState");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (serviceBound){
            getActivity().unbindService(serviceConnection);
            player.stopSelf();
        }
    }

    /**
     * Obtener datos desde el dispositivo
     *  Recuperar datos desde el dispositivo en orden ascendiente
     */
    private void loadAudio(){
        ContentResolver contentResolver = getActivity().getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);

        if (cursor != null && cursor.getCount() >0){
            audioList = new ArrayList<>();
            while (cursor.moveToNext()){
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));

                //Guardar en la lista de audio
                audioList.add(new Audio(data, title, album, artist));
            }
        }
        cursor.close();
    }

    /**
     * Funcion para agregar listeners a los botones del fragment
     */
    private void agregarListeners()
    {
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOptions();
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying())
                {
                    mediaPlayer.stop();
                    play.setText("Reproducir");
                }
                else
                {
                    mediaPlayer.start();
                    play.setText("Detener");
                }

            }
        });

        dialogRecord.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                String fichero = DialogRecord.fichero;
                editTitulo.setText("Audio Grabado");
                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(fichero);
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    Log.e("FSON", "Fallo en reproducción");
                }
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                play.setText("Reproducir");
                mediaPlayer.stop();
            }
        });
    }

    /**
     * Despliegue del Alert dialog para seleccionar si grabar sonido o seleccionarlo del almacenamiento
     */
    public void showOptions()
    {
        final CharSequence[] option = {"Grabar Sonido", "Elegir del Explorador", "Cancelar"};
        final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);
        builder.setTitle("Elige una opción");
        builder.setItems(option, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(option[which] == "Grabar Sonido"){
                    dialog.dismiss();

                    dialogRecord.show();
                }else if(option[which] == "Elegir del Explorador"){
                    Intent intent = new Intent();
                    intent.setType("audio/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    getActivity().startActivityForResult(Intent.createChooser(intent,"Select Audio "), SELECT_PICTURE);
                }else {
                    dialog.dismiss();
                }
            }
        });

        builder.show();
    }

    /**
     * Funcion para extraer los datos de la cancion, su path e instanciarla para reproducirla
     * @param path - Uri que proviene
     */
    public void obtenerSonido(Uri path)
    {
        Log.i("Sonido", path.toString());

        pathSound = path;

        mediaPlayer = MediaPlayer.create(context, pathSound);

        String fileName;
        if (path.getScheme().equals("file")) {
            fileName = path.getLastPathSegment();
            editTitulo.setText(fileName);
        } else {
            Cursor cursor = null;
            try {
                cursor = getActivity().getContentResolver().query(path, new String[]{
                        MediaStore.Images.ImageColumns.DISPLAY_NAME
                }, null, null, null);

                if (cursor != null && cursor.moveToFirst()) {
                    fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME));
                    editTitulo.setText(fileName);
                }
            } finally {

                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }

}
