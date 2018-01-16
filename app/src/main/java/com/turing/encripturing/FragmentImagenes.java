package com.turing.encripturing;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.media.MediaPlayer;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
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

import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.api.SequenceEncoder;
import org.jcodec.common.AndroidUtil;
import org.jcodec.common.DemuxerTrack;
import org.jcodec.common.DemuxerTrackMeta;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Frame;
import org.jcodec.common.model.Picture;
import org.jcodec.containers.mp4.demuxer.MP4Demuxer;
import org.jcodec.scale.BitmapUtil;
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

import java.io.ByteArrayOutputStream;
import java.io.Console;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;


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
    private Bitmap bm;
    private String mParam1;
    private String mParam2;
    private int[] px;
    private ImageView histograma;
    private ImageView histogramaLum;
    private ImageView imgViewBN;
    //Creamos los arrays requeridos y convertimos el bitmap a Mat para que pueda acomodarse la info de la misma a los arrelgos
    private Mat rgba;
    private Mat bn;

    //Variables para encriptación
    private Long tiempoAntes, tiempoDespues;
    private Button btnEncriptar;
    private DialogLlaves dialogLlaves;
    private Bitmap frameEncriptado;
    private ImageView imgViewEncriptado;

    /**
    Variables de Checo
    **/
    private static Button btnFrames, btnSig, btnAnt;
    private static ImageView imgFrames;
    public static Bitmap[] frames;
    public static FrameGrab grab;
    private File fileVideo;
    private static ProgressDialog progressDialog;
    private static int numberOfFramesEncriptados = 0;
    private static int numberOfFramesExtracted = 0;
    private static Handler handler;
    private int positionFrame = 0;
    public static double timeforFrame = 0.33;//33 ms para cada frame da un aproximado de 3 frames por segundo
    private static double time = 0;
    public static final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

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

        btnFrames = view.findViewById(R.id.btnFrames);
        imgFrames = view.findViewById(R.id.framesContainer);
        btnSig = view.findViewById(R.id.btnSigFrame);
        btnAnt = view.findViewById(R.id.btnAntFrame);
        btnEncriptar = view.findViewById(R.id.btn_encriptar);
        handler = new Handler();
        imgViewEncriptado = view.findViewById(R.id.ImgViewEncriptado);

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
        btnEncriptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogLlaves = new DialogLlaves(context, 2);
                dialogLlaves.setModulo(256);
                dialogLlaves.show();
                dialogLlaves.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        if(!dialogLlaves.getCancelled()){
                            DatosEncriptar datos = DatosEncriptar.getInstance();
                            if(datos.getLlaveVideo() != null && frames!=null){
                                encriptarFramesThread();
                            }else{
                                Toast.makeText(getActivity(), "No has obtenido los frames de un video",Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }
        });
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

        btnFrames.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFramesOfVideo();
            }
        });

        btnSig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                positionFrame++;
                if(positionFrame==frames.length-1 || positionFrame>frames.length-1){
                    positionFrame = frames.length-1;
                    imgFrames.setImageBitmap(frames[frames.length-1]);
                }
                imgFrames.setImageBitmap(frames[positionFrame]);
            }
        });
        btnAnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                positionFrame--;
                if(positionFrame==0 || positionFrame < 0){
                    positionFrame = 0;
                    imgFrames.setImageBitmap(frames[0]);
                }
                imgFrames.setImageBitmap(frames[positionFrame]);
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
                fileVideo = new File(data);
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
                        imgViewEncriptado.setVisibility(ImageView.GONE);
                    }
                    @Override
                    public void onPause() {
                        /*
                        Bloque de codigo de chequiño :v
                         */

                        //TERMINA
                        //mensaje que se muestra cuando está pausado
                        Toast.makeText(getActivity(),"Está pausado"
                                +reproductor.getCurrentPosition(),Toast.LENGTH_SHORT).show();
                        //Extraemos el frame en el instante que se da pause
                        bm = mmdr.getFrameAtTime(reproductor.getCurrentPosition()*1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                        SingletonBitmap.getInstance().setBm(bm);
                        Log.i("SINGLETON", "Setea el BM desde el on Pause");
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
        if(rgbaSize.width==1920 && rgbaSize.height == 1080 || rgbaSize.width==1440 && rgbaSize.height == 1080){
            binWidth = 7.5;
        }if(rgbaSize.width==1080 && rgbaSize.height == 1920 || rgbaSize.width==1080 && rgbaSize.height == 1440){
            binWidth = 4.25;
        }if(rgbaSize.width==1280 && rgbaSize.height == 720 ){
            binWidth = 5;
        }

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
        if(bnSize.width==1920 && bnSize.height == 1080 || bnSize.width==1440 && bnSize.height == 1080){
            binWidth = 7.5;
        }if(bnSize.width==1080 && bnSize.height == 1920 || bnSize.width==1080 && bnSize.height == 1440){
            binWidth = 4.25;
        }if(bnSize.width==1280 && bnSize.height == 720 ){
            binWidth = 5;
        }

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

    /**
     * Funcion para extraer los frames de un video
     */
    public void getFramesOfVideo(){
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Obteniendo Frames ");
        new Thread(){
            @Override
            public void run(){
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.show();
                        progressDialog.setProgress(0);
                    }
                });

                try {
                    grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(fileVideo));
                    int durationOfVideo = reproductor.getDuration();
                    frames = new Bitmap[(durationOfVideo/330) + 1];//calculamos el numero total de frames
                    Log.i("Debug", "NumberOfFrames: " + frames.length);
                    int framesThread = frames.length/NUMBER_OF_CORES;//calculamos el numero de hilos por nucleo
                    int initialPosition = 0;
                    double initialTime = 0;
                    /*DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute(new HilosPorFrame(
                            initialPosition,
                            (frames.length - 1),
                            initialTime,
                            grab
                    ));*/
                    for(int i = 1; i <= NUMBER_OF_CORES; i++){
                        if(i != NUMBER_OF_CORES){
                            DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute(new HilosPorFrame(
                                    initialPosition,
                                    initialPosition + framesThread,
                                    initialTime,
                                    FrameGrab.createFrameGrab(NIOUtils.readableChannel(fileVideo))
                            ));
                        }
                        else{//Ultimo fragmento de frames
                            DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute(new HilosPorFrame(
                                    initialPosition,
                                    (frames.length - 1),
                                    initialTime,
                                    FrameGrab.createFrameGrab(NIOUtils.readableChannel(fileVideo))
                            ));
                        }
                        initialPosition = initialPosition + framesThread + 1;
                        initialTime = initialTime + (timeforFrame * framesThread) + timeforFrame;
                    }
                }
                catch(JCodecException jce){
                    Log.e("JCOED", "");
                }
                catch(IOException ioe){
                    Log.e("IOE", "");
                }
            }

        }.start();
    }

    public static void reportProgressFrames(){
        numberOfFramesExtracted++;
        progressDialog.setProgress((numberOfFramesExtracted * 100)/FragmentImagenes.frames.length);
        if(numberOfFramesExtracted == frames.length){
            progressDialog.dismiss();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    imgFrames.setImageBitmap(frames[0]);
                    btnSig.setEnabled(true);
                    btnAnt.setEnabled(true);
                    numberOfFramesExtracted = 0;
                }
            });

        }
    }

    public static String getFechaActual(){
        Date ahora = new Date();
        SimpleDateFormat formateador = new SimpleDateFormat("yyyyMMDDhhmmss");
        return formateador.format(ahora);
    }

    public static void generatVideo(){
    }

    public static void reportProgressFramesEncriptados(){
        Log.i("PROGRESO", Integer.toString(numberOfFramesEncriptados) + "=" + Integer.toString((numberOfFramesEncriptados * 100)/FragmentImagenes.frames.length));
        numberOfFramesEncriptados++;
        progressDialog.setProgress((numberOfFramesEncriptados * 100)/FragmentImagenes.frames.length);
        if(numberOfFramesEncriptados == frames.length){
            progressDialog.dismiss();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    imgFrames.setImageBitmap(frames[0]);
                    crearDirectorio();
                    numberOfFramesEncriptados = 0;
                }
            });

        }
    }

    public static void crearDirectorio(){
        FileOutputStream fos = null;
        for(int i=0; i<frames.length; i++){
            File file = new File("storage/emulated/0/ENC/tmp/img00"+ (i+1) + ".jpeg");
            Bitmap bmTMP = frames[i];
            try{
                if(file.exists()){
                    file.delete();
                }
                fos = new FileOutputStream(file);
                bmTMP.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
            }
            catch (IOException ioe){
                ioe.printStackTrace();
            }
        }
    }

    /**
     * Función para encriptar un frame individualmente
     * @param encrypt
     */
    private void encriptar(final boolean encrypt){
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        if(encrypt){
            progressDialog.setTitle("Encriptando");
        }
        else
        {
            progressDialog.setTitle("Desencriptando");
        }

        progressDialog.setCancelable(false);
        new Thread(){
            @Override
            public void run(){

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.show();
                        progressDialog.setProgress(0);
                    }
                });

                tiempoAntes = System.currentTimeMillis();
                DatosEncriptar datos = DatosEncriptar.getInstance();
                int[][] llave;
                if(dialogLlaves.getEncrypt()){
                    llave = datos.getLlaveVideo();
                }
                else{
                    llave = datos.getLlaveDesVideo();
                }
                Bitmap bmLocal = SingletonBitmap.getInstance().getBm();
                frameEncriptado = Bitmap.createBitmap(bmLocal.getWidth(),bm.getHeight(), Bitmap.Config.ARGB_8888);
                int[] arregloEnc = new int[3];
                int contador = 0;
                Log.i("ENCRIPTADO", "Cols: "+bmLocal.getWidth());
                Log.i("ENCRIPTADO", "Rows: "+bmLocal.getHeight());
                for (int i = 0; i < bmLocal.getWidth(); i++) {
                    for (int j = 0; j < bmLocal.getHeight(); j++) {
                        int pixel = bmLocal.getPixel(i, j);
                        int A = Color.alpha(pixel);
                        int R = Color.red(pixel);
                        int G = Color.green(pixel);
                        int B = Color.blue(pixel);
                        contador++;
                        arregloEnc[0] = ((R * llave[0][0]) + (G * llave[0][1]) + (B * llave[0][2]))%256;
                        arregloEnc[1] = ((R * llave[1][0]) + (G * llave[1][1]) + (B * llave[1][2]))%256;
                        arregloEnc[2] = ((R * llave[2][0]) + (G * llave[2][1]) + (B * llave[2][2]))%256;
                        if(i==0 && j==0){
                            Log.i("ENCRIPTADO", Double.toString(R));
                            Log.i("ENCRIPTADO", Double.toString(G));
                            Log.i("ENCRIPTADO", Double.toString(B));
                            Log.i("ENCRIPTADO", Double.toString(arregloEnc[0]*256));
                            Log.i("ENCRIPTADO", Double.toString(arregloEnc[1]*256));
                            Log.i("ENCRIPTADO", Double.toString(arregloEnc[2]*256));
                            Log.i("ENCRIPTADO", Double.toString(arregloEnc[0]%256));
                            Log.i("ENCRIPTADO", Double.toString(arregloEnc[1]%256));
                            Log.i("ENCRIPTADO", Double.toString(arregloEnc[2]%256));
                        }
                        frameEncriptado.setPixel(i,j, Color.argb(A,arregloEnc[0],arregloEnc[1],arregloEnc[2]));
                        progressDialog.setProgress((contador * 100) / (bm.getHeight()*bm.getHeight()));
                    }
                }
                SingletonBitmap.getInstance().setBm(frameEncriptado);
                Log.i("SINGLETON", "Setea el BM desde el thread");
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        imgViewEncriptado.setImageBitmap(SingletonBitmap.getInstance().getBm());
                        Log.i("SINGLETON", "Obtiene el BM desde el el final del thread");
                        imgViewEncriptado.setVisibility(ImageView.VISIBLE);
                    }
                });

            }
        }.start();
    }

    public static class HilosPorFrame implements Runnable{
        double initialTime;
        int initialPosition, finalPoisition;
        FrameGrab frameGrab;

        public HilosPorFrame(int initialPosition, int finalPosition, double initialTime, FrameGrab frameGrab){
            this.initialPosition = initialPosition;
            this.finalPoisition = finalPosition;
            this.initialTime = initialTime;
            this.frameGrab = frameGrab;
        }

        public synchronized void buscarSegundo(){
            try {
                for(int i = initialPosition; i <= finalPoisition; i++){
                    frameGrab.seekToSecondPrecise(initialTime);
                    Picture picture = frameGrab.getNativeFrame();
                    FragmentImagenes.frames[i] = AndroidUtil.toBitmap(picture);
                    Log.i("TEST", "Termino de extraer Frame " + i);
                    FragmentImagenes.reportProgressFrames();
                    initialTime+=FragmentImagenes.timeforFrame;
                }
            }
            catch(IOException ioe){
                Log.e("IOE", "");
            }
            catch(JCodecException jce){
                Log.e("JCOED", "");
            }
        }

        @Override
        public void run() {
            buscarSegundo();
        }
    }

    public void encriptarFramesThread(){
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Encriptando/Desencriptando Frames ");
        new Thread(){
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.show();
                        progressDialog.setProgress(0);
                    }
                });
                int framesThread = frames.length / NUMBER_OF_CORES;//calculamos el numero de hilos por nucleo
                int initialPosition = 0;
                for (int i = 1; i <= NUMBER_OF_CORES; i++) {
                    //Log.i("ENCRIPTADO", Integer.toString(initialPosition));
                    if (i != NUMBER_OF_CORES) {
                        DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute(new EncriptarFrames(
                                initialPosition,
                                initialPosition + framesThread,
                                Arrays.copyOfRange(frames, initialPosition, initialPosition + framesThread),
                                dialogLlaves
                        ));
                    } else {//Ultimo fragmento de frames
                        Log.i("PROGRESO", "Inicia ultimo segmento de frames");
                        DefaultExecutorSupplier.getInstance().forBackgroundTasks().execute(new EncriptarFrames(
                                initialPosition,
                                frames.length - 1,
                                Arrays.copyOfRange(frames, initialPosition, frames.length),
                                dialogLlaves
                        ));
                        Log.i("PROGRESO", "Terminó ultimo segmento de frames");
                    }
                    initialPosition = initialPosition + framesThread;
                }
            }

        }.start();
    }

    public static class EncriptarFrames implements Runnable{
        int initialPosition;
        int finalPosition;
        Bitmap[] framesAEncriptar;
        DialogLlaves dialogLlaves;


        public EncriptarFrames(int initialPosition, int finalPosition, Bitmap[] framesAEncriptar, DialogLlaves dialogLlaves){
            this.initialPosition = initialPosition;
            this.finalPosition = finalPosition;
            this.framesAEncriptar = framesAEncriptar;
            this.dialogLlaves = dialogLlaves;
        }

        public synchronized void encriptarFrames() {
            //Log.i("ENCRIPTADO", Integer.toString(framesAEncriptar.length));
            Bitmap[] bitmapsEncriptados = new Bitmap[framesAEncriptar.length];
            DatosEncriptar datos = DatosEncriptar.getInstance();
            int[][] llave;
            if (dialogLlaves.getEncrypt()) {
                llave = datos.getLlaveVideo();
            } else {
                llave = datos.getLlaveDesVideo();
            }
            for (int o = 0; o < framesAEncriptar.length; o++) {
                Bitmap bmLocal = framesAEncriptar[o];
                Bitmap frameEncriptado = Bitmap.createBitmap(bmLocal.getWidth(), bmLocal.getHeight(), Bitmap.Config.ARGB_8888);
                int[] arregloEnc = new int[3];
                for (int i = 0; i < bmLocal.getWidth(); i++) {
                    for (int j = 0; j < bmLocal.getHeight(); j++) {
                        int pixel = bmLocal.getPixel(i, j);
                        int A = Color.alpha(pixel);
                        int R = Color.red(pixel);
                        int G = Color.green(pixel);
                        int B = Color.blue(pixel);
                        arregloEnc[0] = ((R * llave[0][0]) + (G * llave[0][1]) + (B * llave[0][2])) % 256;
                        arregloEnc[1] = ((R * llave[1][0]) + (G * llave[1][1]) + (B * llave[1][2])) % 256;
                        arregloEnc[2] = ((R * llave[2][0]) + (G * llave[2][1]) + (B * llave[2][2])) % 256;
                        frameEncriptado.setPixel(i, j, Color.argb(A, arregloEnc[0], arregloEnc[1], arregloEnc[2]));
                    }
                }
                FragmentImagenes.reportProgressFramesEncriptados();
                bitmapsEncriptados[o] = frameEncriptado;
                Log.i("SUBENCRIPTADO", Integer.toString(o) +"/"+ Integer.toString(framesAEncriptar.length-1));
            }
            Log.i("ENCRIPTADO", Integer.toString(initialPosition) + ", " + Integer.toString(finalPosition));
            for(int p = 0; p < framesAEncriptar.length; p ++){
                frames[initialPosition+p]=bitmapsEncriptados[p];
            }
        }

        @Override
        public void run(){
            encriptarFrames();
        }
    }
    /*private void encriptarFrames(final boolean encrypt, final Bitmap[] framesAEncriptar){
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        if(encrypt){
            progressDialog.setTitle("Encriptando");
        }
        else
        {
            progressDialog.setTitle("Desencriptando");
        }

        progressDialog.setCancelable(false);
        new Thread(){
            @Override
            public void run(){

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.show();
                        progressDialog.setProgress(0);
                    }
                });

                tiempoAntes = System.currentTimeMillis();
                DatosEncriptar datos = DatosEncriptar.getInstance();
                int[][] llave;
                if(dialogLlaves.getEncrypt()){
                    llave = datos.getLlave();
                }
                else{
                    llave = datos.getLlaveDes();
                }
                for(int o= 0; o<framesAEncriptar.length; o++) {
                    Bitmap bmLocal = framesAEncriptar[o];
                    frameEncriptado = Bitmap.createBitmap(bmLocal.getWidth(), bmLocal.getHeight(), Bitmap.Config.ARGB_8888);
                    int[] arregloEnc = new int[3];
                    int contador = 0;
                    Log.i("ENCRIPTADO", "Cols: " + bmLocal.getWidth());
                    Log.i("ENCRIPTADO", "Rows: " + bmLocal.getHeight());
                    for (int i = 0; i < bmLocal.getWidth(); i++) {
                        for (int j = 0; j < bmLocal.getHeight(); j++) {
                            int pixel = bmLocal.getPixel(i, j);
                            int A = Color.alpha(pixel);
                            int R = Color.red(pixel);
                            int G = Color.green(pixel);
                            int B = Color.blue(pixel);
                            contador++;
                            arregloEnc[0] = ((R * llave[0][0]) + (G * llave[0][1]) + (B * llave[0][2])) % 256;
                            arregloEnc[1] = ((R * llave[1][0]) + (G * llave[1][1]) + (B * llave[1][2])) % 256;
                            arregloEnc[2] = ((R * llave[2][0]) + (G * llave[2][1]) + (B * llave[2][2])) % 256;
                            if (i == 0 && j == 0) {
                                Log.i("ENCRIPTADO", Double.toString(R));
                                Log.i("ENCRIPTADO", Double.toString(G));
                                Log.i("ENCRIPTADO", Double.toString(B));
                                Log.i("ENCRIPTADO", Double.toString(arregloEnc[0] * 256));
                                Log.i("ENCRIPTADO", Double.toString(arregloEnc[1] * 256));
                                Log.i("ENCRIPTADO", Double.toString(arregloEnc[2] * 256));
                                Log.i("ENCRIPTADO", Double.toString(arregloEnc[0] % 256));
                                Log.i("ENCRIPTADO", Double.toString(arregloEnc[1] % 256));
                                Log.i("ENCRIPTADO", Double.toString(arregloEnc[2] % 256));
                            }
                            frameEncriptado.setPixel(i, j, Color.argb(A, arregloEnc[0], arregloEnc[1], arregloEnc[2]));
                        }
                    }
                    frames[o] = frameEncriptado;
                    progressDialog.setProgress((o * 100) / (framesAEncriptar.length));
                    Log.i("SINGLETON", "Setea el BM desde el thread");
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        imgViewEncriptado.setImageBitmap(SingletonBitmap.getInstance().getBm());
                        Log.i("SINGLETON", "Obtiene el BM desde el el final del thread");
                        imgViewEncriptado.setVisibility(ImageView.VISIBLE);
                    }
                });

            }
        }.start();
    }*/

}
