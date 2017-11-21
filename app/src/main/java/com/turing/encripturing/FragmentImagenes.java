package com.turing.encripturing;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.media.MediaPlayer;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
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
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.Console;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Collections;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentImagenes.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FragmentImagenes#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentImagenes extends Fragment {

    //**************************************************
    //Instanciamos un nuevo objeto de la clase modificada de VideoView
    private CustomVideoView reproductor;
    //**************************************************
    //instanciamos el objeto que permitirá obtener el frame y luego graficar los valores RGB
    private MediaMetadataRetriever mmdr = new MediaMetadataRetriever();
    //**************************************************
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
    private int[] px;
    private ImageView histograma;
    private ImageView histogramaLum;
    private ImageView imgViewBN;
    //Creamos los arrays requeridos y convertimos el bitmap a Mat para que pueda acomodarse la info de la misma a los arrelgos
    private Mat rgba;
    private Mat bn;

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
    public void obtenerVideo(Uri path) {
        Log.i("RUTA", path.toString());
        final String docId = DocumentsContract.getDocumentId(path);
        final String[] split = docId.split(":");
        Log.i("RUTA", split[1]);
        String fileName;
        String data;
        Cursor cursor = null;
        try {
            String[] busqueda = {MediaStore.Files.FileColumns._ID, MediaStore.Files.FileColumns.DISPLAY_NAME, MediaStore.Files.FileColumns.DATA};
            cursor = getActivity().getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, busqueda, "_id=?", new String[]{split[1]}, null, null);
            cursor.moveToFirst();
            int colum_index = cursor.getColumnIndex(MediaStore.Files.FileColumns._ID);
            fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME));
            data = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
            Log.i("RUTA", "Posición cursor: " + cursor.getString(colum_index) + " " + fileName + " " + data);

            if (cursor != null && cursor.moveToFirst()) {
                layoutVideo = getView().findViewById(R.id.layoutVideo);
                layoutVideo.setVisibility(RelativeLayout.VISIBLE);

                //Modifiqué la asignación del view para que éste pudiera establecerse correctamente
                reproductor = getView().findViewById(R.id.reproductorVideo);

                reproductor.setVideoURI(Uri.parse(data));
                Log.i("RUTA", data);
                mmdr.setDataSource(data);


                //el listener que detecta cuando el video esta en play o pause
                reproductor.setPlayPauseListener(new CustomVideoView.PlayPauseListener() {
                    @Override
                    public void onPlay() {
                        //mensaje que se muestra cuando está reproduciendo
                        Toast.makeText(getActivity(),"Está reproduciendo",Toast.LENGTH_SHORT).show();
                        histograma = getView().findViewById(R.id.histogramaImgView);
                        imgViewBN = getView().findViewById(R.id.imgViewBN);
                        histogramaLum = getView().findViewById(R.id.histogramaLumImgView);
                        histograma.setVisibility(ImageView.GONE);
                        imgViewBN.setVisibility(ImageView.GONE);
                        histogramaLum.setVisibility(ImageView.GONE);
                    }
                    @Override
                    public void onPause() {
                        //mensaje que se muestra cuando está pausado
                        Toast.makeText(getActivity(),"Está pausado"
                                +reproductor.getCurrentPosition(),Toast.LENGTH_SHORT).show();
                        //Extraemos el frame en el instante que se da pause
                        Bitmap bm = mmdr.getFrameAtTime(reproductor.getCurrentPosition()*1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                        Mat bmMat = new Mat();
                        Utils.bitmapToMat(bm, bmMat);
                        Mat bmGrayMat = new Mat();
                        //Convertirmos la imagen a escala de grises
                        Bitmap bmGray = createContrast(bm,bmMat,bmGrayMat);



                        //Definimos el contenido de nuestro ImageView en el layout con los datos del histograma y los hacemos visibles
                        histograma = getView().findViewById(R.id.histogramaImgView);
                        histograma.setImageBitmap(getHistogramaColor(bm));
                        histograma.setVisibility(ImageView.VISIBLE);
                        imgViewBN = getView().findViewById(R.id.imgViewBN);
                        imgViewBN.setImageBitmap(bmGray);
                        imgViewBN.setVisibility(ImageView.VISIBLE);
                        histogramaLum = getView().findViewById(R.id.histogramaLumImgView);
                        histogramaLum.setImageBitmap(getHistogramaBN(bmGray));
                        histogramaLum.setVisibility(ImageView.VISIBLE);
                        Log.i("TESTEO", "Sale");
                    }
                });

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

    public VideoView getReproductor(){
        return reproductor;
    }

    public static Bitmap createContrast(Bitmap img, Mat src, Mat fin){

        //Llamamos a la clase ImgProc de la librería OpenCV3.1 y le hacemos que nuestro Mat de entrada se haga Blanco y negro
        Imgproc.cvtColor(src, fin, Imgproc.COLOR_RGB2GRAY);
        //Creamos un bitmap del tamaño de la imagen para almacenar la información
        Bitmap bmOut = Bitmap.createBitmap(img.getWidth(), img.getHeight(), img.getConfig());
        //Por último le asignamos la información al bitmap del Mat que ya tiene la información de la img en Blanco y Negro
        Utils.matToBitmap(fin,bmOut);
        return bmOut;
    }

    public Bitmap getHistogramaColor(Bitmap src){
        rgba = new Mat();
        Utils.bitmapToMat(src, rgba);
        //Obtenemos el tamaño del bitmap
        Size rgbaSize = rgba.size();

        //Definimos la cantidad de barras en el histograma
        int histSize = 256;
        MatOfInt histogramSize = new MatOfInt(histSize);

        //Definimos la altura del histograma y  el ancho de la barra
        int histogramHeight = (int) rgbaSize.height;
        double binWidth = 2.5; //A menor sea el número menos separación existe entre las barras

        //Definimos el rango de valores
        MatOfFloat histogramRange = new MatOfFloat(0f, 256f);

        //Creamos 2 listas separadas:
        // -Una para los colores
        // -Otra para los canales
        // (Ambos serán usados como conjuntos de datos por separado)
        Scalar[] colorsRgb = new  Scalar[]{new Scalar(200,0,0,255), new Scalar(0,200,0,255), new Scalar(0,0,200,255)};
        MatOfInt[] channels = new MatOfInt[]{new MatOfInt(0), new MatOfInt(1), new MatOfInt(2)};

        //Creamos un arreglo para guardarlo en el histograma y un segundo arreglo, en el cual el gráfico del histograma será dibujado
        Mat[] histograms = new Mat[]{new Mat(), new Mat(), new Mat()};
        Mat histMatBitmap = new Mat(rgbaSize, rgba.type());

        //Calculamos el histograma para cada canal
        for (int i = 0; i < channels.length; i++){
            //Usando el método Imgproc.calchist() se calcula el histograma para cada canal. Proviene de la librería OpenCV
            Imgproc.calcHist(Collections.singletonList(rgba), channels[i], new Mat(), histograms[i],  histogramSize, histogramRange);
            //Se estandarizan los valores de datos para que cuadren todos a la altura y tamaño
            Core.normalize(histograms[i], histograms[i], histogramHeight, 0, Core.NORM_INF);
            //Creamos 2 puntos por cada barra del histograma y generamos una linea entre esos 2 puntos usando la librería OpenCV
            for(int j = 0; j < histSize; j++){
                Point p1 = new Point(binWidth * (j-1), histogramHeight - Math.round(histograms[i].get(j-1,0)[0]));
                Point p2 = new Point(binWidth *(j), histogramHeight - Math.round(histograms[i].get(j,0)[0]));
                Imgproc.line(histMatBitmap, p1, p2, colorsRgb[i], 2, 8, 0);
            }
        }

        //Se crea un bitmap de las dimensiones del fotograma
        Bitmap bmFinal = Bitmap.createBitmap(histMatBitmap.cols(), histMatBitmap.rows(), Bitmap.Config.ARGB_8888);
        //Se le asignan los datos del histograma al bitmap
        Utils.matToBitmap(histMatBitmap,bmFinal);

        return bmFinal;
    }

    public Bitmap getHistogramaBN(Bitmap src){
        bn = new Mat();
        Utils.bitmapToMat(src, bn);
        //Obtenemos el tamaño del bitmap
        Size bnSize = bn.size();

        //Definimos la cantidad de barras en el histograma
        int histSize = 256;
        MatOfInt histogramSize = new MatOfInt(histSize);

        //Definimos la altura del histograma y  el ancho de la barra
        int histogramHeight = (int) bnSize.height;
        double binWidth = 2.5; //A menor sea el número menos separación existe entre las barras

        //Definimos el rango de valores
        MatOfFloat histogramRange = new MatOfFloat(0f, 256f);

        //Creamos un
        Scalar colorGris = new Scalar(0.0, 0.0, 0.0, 255.0);

        //Creamos un arreglo para guardarlo en el histograma y un segundo arreglo, en el cual el gráfico del histograma será dibujado
        Mat[] histograms = new Mat[]{new Mat()};
        Mat histMatBitmap = new Mat(bnSize, bn.type());

        //Calculamos el histograma para un solo canal
            //Usando el método Imgproc.calchist() se calcula el histograma para cada canal. Proviene de la librería OpenCV
            Imgproc.calcHist(Collections.singletonList(bn),new MatOfInt(1), new Mat(), histograms[0],  histogramSize, histogramRange);
            //Se estandarizan los valores de datos para que cuadren todos a la altura y tamaño
            Core.normalize(histograms[0], histograms[0], histogramHeight, 0, Core.NORM_INF);
            //Creamos 2 puntos por cada barra del histograma y generamos una linea entre esos 2 puntos usando la librería OpenCV
            for(int j = 0; j < histSize; j++){
                Point p1 = new Point(binWidth * (j-1), histogramHeight - Math.round(histograms[0].get(j-1,0)[0]));
                Point p2 = new Point(binWidth *(j), histogramHeight - Math.round(histograms[0].get(j,0)[0]));
                Imgproc.line(histMatBitmap, p1, p2, colorGris, 2, 8, 0);
            }

        //Se crea un bitmap de las dimensiones del fotograma
        Bitmap bmFinal = Bitmap.createBitmap(histMatBitmap.cols(), histMatBitmap.rows(), Bitmap.Config.ARGB_8888);
        //Se le asignan los datos del histograma al bitmap
        Utils.matToBitmap(histMatBitmap,bmFinal);

        return bmFinal;
    }

    //Función para generar matriz de números random del 1 al 99 xdxd
    public int[][] generarLlave(){
        int[][] llave = new int[3][3];
        for (int i = 0; i<=llave.length; i++){
            for (int j = 0; j<=llave[i].length; j++){
                llave[i][j] = (int)(Math.random()*99)+1;
            }
        }

        return llave;
    }
    
}
