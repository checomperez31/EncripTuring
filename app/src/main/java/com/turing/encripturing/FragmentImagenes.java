package com.turing.encripturing;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.os.EnvironmentCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentImagenes.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FragmentImagenes#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentImagenes extends Fragment {

    private VideoView reproductor;
    private MediaController mc;
    private RelativeLayout.LayoutParams paramsNotFullScreen;
    private FloatingActionButton btn_SeleccionarVideo;
    private Context context;
    private final int SELECT_VIDEO = 201;
    private final int GRABAR_VIDEO = 202;
    private Uri pathVideo;
    private RelativeLayout layoutVideo;
    private int position = 0;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public FragmentImagenes() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentImagenes.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentImagenes newInstance(String param1, String param2) {
        FragmentImagenes fragment = new FragmentImagenes();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fragment_imagenes, container, false);

        btn_SeleccionarVideo = view.findViewById(R.id.btn_SeleccionarVideo);
        agregarVideo();
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

    /**
     * Método para setear el reproductor y su contenido una vez se creó el fragment y la vista del mismo
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){
        reproductor = getView().findViewById(R.id.reproductorVideo);
        //Listener para aplicar el media controller al tamaño del video una vez que esté cargado
        reproductor.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                    //Listener en caso de que el video se redimensione y posicionar de nuevo el media controller
                    @Override
                    public void onVideoSizeChanged(MediaPlayer mediaPlayer, int i, int i1) {
                        mc = new MediaController(getActivity());
                        //IMPORTANTE asignar el media controller al videoView antes de posicionarlo
                        //de lo contrario se colocará en la parte de abajo de la pantalla sobreponiendose
                        reproductor.setMediaController(mc);
                        mc.setAnchorView(reproductor);
                    }
                });
            }
        });
        reproductor.start();
        reproductor.requestFocus();
    }

    /**
     * Método para guardar la posición actual del video
     * @param savedInstanceState
     */
    @Override
    public  void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        //Usamos onSaveInstance para poder almacenar la posición de reproducción del video
        savedInstanceState.putInt("Position", reproductor.getCurrentPosition());
        reproductor.pause();
    }

    /**
     * Método para recuperar la posición del video cada que se gira la pantalla
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState != null) {
            //Usamos onRestoreInstanceState para poder reproducir la reproducción del video de la posicion almacenada
            position = savedInstanceState.getInt("Position");
            reproductor.seekTo(position);

        }
    }

    /**
     * Posible método para poder correr el video en fullscreen, sigue en pruebas para lograrlo dentro de un fragment
     * por lo que está comentado
     * @param /**newConfig
     */
    /*
    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) //Si la orientación cambia a horizontal
        {
            paramsNotFullScreen = (RelativeLayout.LayoutParams) reproductor.getLayoutParams();
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(paramsNotFullScreen);
            params.setMargins(0,0,0,0);
            params.height=ViewGroup.LayoutParams.MATCH_PARENT;
            params.width=ViewGroup.LayoutParams.MATCH_PARENT;
            params.width=ViewGroup.LayoutParams.MATCH_PARENT;
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            reproductor.setLayoutParams(params);
        }else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)
        {
            reproductor.setLayoutParams(paramsNotFullScreen);
        }
    }*/

    public void agregarVideo(){
        btn_SeleccionarVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOptions();
            }
        });
    }

    public void showOptions()
    {
        final CharSequence[] option = {"Grabar video", "Elegir del Explorador", "Cancelar"};
        final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);
        builder.setTitle("Elige una opción");
        builder.setItems(option, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(option[which] == "Grabar video"){
                    Intent intent = new Intent();
                    intent.setAction(MediaStore.ACTION_VIDEO_CAPTURE);
                    Log.i("VIDEO", "Llega hasta aqui");
                    getActivity().startActivityForResult(intent, GRABAR_VIDEO);
                }else if(option[which] == "Elegir del Explorador"){
                    Intent intent = new Intent();
                    intent.setType("video/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    Log.i("RUTA", "Llega hasta aqui");
                    getActivity().startActivityForResult(Intent.createChooser(intent,"Select Video "), SELECT_VIDEO);
                }else {
                    dialog.dismiss();
                }
            }
        });

        builder.show();
    }

    /**
     * Funcion para extraer los datos del video, su path e instanciarlo para reproducirlo
     * @param path - Uri que proviene
     */
    public void obtenerVideo(Uri path)
    {
        Log.i("RUTA", path.toString());

        pathVideo = path;

        String fileName;
        if (path.getScheme().equals("file")) {
            layoutVideo = getView().findViewById(R.id.layoutVideo);
            layoutVideo.setVisibility(RelativeLayout.VISIBLE);
            reproductor = getView().findViewById(R.id.reproductorVideo);
            reproductor.setVideoURI(pathVideo);
            //Listener para aplicar el media controller al tamaño del video una vez que esté cargado
            reproductor.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                        //Listener en caso de que el video se redimensione y posicionar de nuevo el media controller
                        @Override
                        public void onVideoSizeChanged(MediaPlayer mediaPlayer, int i, int i1) {
                            mc = new MediaController(getActivity());
                            //IMPORTANTE asignar el media controller al videoView antes de posicionarlo
                            //de lo contrario se colocará en la parte de abajo de la pantalla sobreponiendose
                            reproductor.setMediaController(mc);
                            mc.setAnchorView(reproductor);
                        }
                    });
                }
            });
            reproductor.start();
            reproductor.requestFocus();
        } else {
            Cursor cursor = null;
            try {
                cursor = getActivity().getContentResolver().query(path, new String[]{
                        MediaStore.Images.ImageColumns.DISPLAY_NAME
                }, null, null, null);

                if (cursor != null && cursor.moveToFirst()) {
                    layoutVideo = getView().findViewById(R.id.layoutVideo);
                    layoutVideo.setVisibility(RelativeLayout.VISIBLE);
                    reproductor = getView().findViewById(R.id.reproductorVideo);
                    reproductor.setVideoURI(pathVideo);
                    //Listener para aplicar el media controller al tamaño del video una vez que esté cargado
                    reproductor.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mediaPlayer) {
                            mediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                                //Listener en caso de que el video se redimensione y posicionar de nuevo el media controller
                                @Override
                                public void onVideoSizeChanged(MediaPlayer mediaPlayer, int i, int i1) {
                                    mc = new MediaController(getActivity());
                                    //IMPORTANTE asignar el media controller al videoView antes de posicionarlo
                                    //de lo contrario se colocará en la parte de abajo de la pantalla sobreponiendose
                                    reproductor.setMediaController(mc);
                                    mc.setAnchorView(reproductor);
                                }
                            });
                        }
                    });
                    reproductor.start();
                    reproductor.requestFocus();
                }
            } finally {

                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }

    public VideoView getReproductor(){
        return reproductor;
    }
}
