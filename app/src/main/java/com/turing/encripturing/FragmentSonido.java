package com.turing.encripturing;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

public class FragmentSonido extends Fragment{

    private OnFragmentInteractionListener mListener;

    private Context context;

    private final int SELECT_AUDIO = 200;

    //Boton agregar sonido
    private FloatingActionButton btn_add;

    private EditText editTitulo, editSize;
    private Button encriptar, encriptar2;

    //Variables globales para el reproductor
    private MediaPlayerService player;
    boolean serviceBound = false;

    private DialogRecord dialogRecord;
    private DialogLlaves dialogLlaves;

    private static String RECORD_DIRECTORY = "ENC";
    public static String directorio = Environment.getExternalStorageDirectory().getAbsolutePath();
    private boolean directorioCreado = false;

    //Instanciar WaveForme
    private TextInputEditText startText, endText;
    private float density;
    private ImageButton playButton, rewindButton, ffwdButton;
    private WaveformView waveformView;
    private TextView info;
    private MarkerView startMarker, endMarker;

    private String filenameOr;

    private GraficaSonido graficaOriginal;

    //Instanciar WaveFormEncriptada
    private EditText editTituloEnc, editSizeEnc;
    private TextInputEditText startTextEnc, endTextEnc;
    private float densityEnc;
    private ImageButton playButtonEnc, rewindButtonEnc, ffwdButtonEnc;
    private WaveformView waveformViewEnc;
    private TextView infoEnc;
    private MarkerView startMarkerEnc, endMarkerEnc;

    private GraficaSonido graficaEnc;

    private File encriptedFile;

    ProgressDialog progressDialog;

    private Handler handler;

    private Long tiempoAntes, tiempoDespues;

    public FragmentSonido() {
        // Required empty public constructor
    }

    public static FragmentSonido newInstance(String param1, String param2) {
        FragmentSonido fragment = new FragmentSonido();
        Bundle args = new Bundle();
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
        View view = inflater.inflate(R.layout.fragment_fragment_sonido, container, false);

        btn_add = view.findViewById(R.id.sonido_button_add);

        editTitulo = view.findViewById(R.id.sonido_edit_titulo);
        editTituloEnc = view.findViewById(R.id.sonido_edit_titulo_enc);
        editSize = view.findViewById(R.id.sonido_edit_tam);
        editSizeEnc = view.findViewById(R.id.sonido_edit_tam_enc);
        encriptar = view.findViewById(R.id.sonido_button_encriptar);
        encriptar2 = view.findViewById(R.id.sonido_button_encriptar_enc);

        dialogRecord = new DialogRecord(context);
        handler = new Handler();

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        density = metrics.density;
        densityEnc = density;

        startText = view.findViewById(R.id.sonido_starttext);
        startTextEnc = view.findViewById(R.id.sonido_starttext_enc);

        endText = view.findViewById(R.id.endtext);
        endTextEnc = view.findViewById(R.id.endtext_enc);

        playButton = view.findViewById(R.id.play);
        playButtonEnc = view.findViewById(R.id.play_enc);
        rewindButton = view.findViewById(R.id.rew);
        rewindButtonEnc = view.findViewById(R.id.rew_enc);
        ffwdButton = view.findViewById(R.id.ffwd);
        ffwdButtonEnc = view.findViewById(R.id.ffwd_enc);

        waveformView = view.findViewById(R.id.waveform);
        waveformViewEnc = view.findViewById(R.id.waveform_encript);

        info = view.findViewById(R.id.info);
        infoEnc = view.findViewById(R.id.info_enc);

        startMarker = view.findViewById(R.id.startmarker);
        startMarkerEnc = view.findViewById(R.id.startmarker_enc);

        endMarker = view.findViewById(R.id.endmarker);
        endMarkerEnc = view.findViewById(R.id.endmarker_enc);

        graficaOriginal = new GraficaSonido(density, startText, endText, playButton, rewindButton, ffwdButton, waveformView, info, startMarker, endMarker, editTitulo, editSize);

        graficaEnc = new GraficaSonido(densityEnc, startTextEnc, endTextEnc, playButtonEnc, rewindButtonEnc, ffwdButtonEnc, waveformViewEnc, infoEnc, startMarkerEnc, endMarkerEnc, editTituloEnc, editSizeEnc);

        agregarListeners();
        directorioCreado = new File(directorio, RECORD_DIRECTORY).exists();
        if(!directorioCreado) directorioCreado = new File(directorio, RECORD_DIRECTORY).mkdir();


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
     * Funcion para agregar listeners a los botones del fragment
     */
    private void agregarListeners()
    {
        editTitulo.setKeyListener(null);
        editSize.setKeyListener(null);
        editTituloEnc.setKeyListener(null);
        editSizeEnc.setKeyListener(null);
        startText.setKeyListener(null);
        startTextEnc.setKeyListener(null);
        endText.setKeyListener(null);
        endTextEnc.setKeyListener(null);

        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOptions();
            }
        });

        dialogRecord.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface)
            {
                if(dialogRecord.getIsReleased()){
                    File recordFile = dialogRecord.getFile();
                    editTitulo.setText(recordFile.getName().toString());
                    graficaOriginal.setmFile(dialogRecord.getFile());
                    graficaOriginal.generarGrafica();
                }

            }
        });

        encriptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogLlaves = new DialogLlaves(context);
                dialogLlaves.show();
                dialogLlaves.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        if(!dialogLlaves.getCancelled()){
                            DatosEncriptar datos = DatosEncriptar.getInstance();
                            if(datos.getLlave() != null){
                                encriptar(dialogLlaves.getEncrypt());
                            }
                        }
                    }
                });
            }
        });

        encriptar2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("ISOUND", "Samples " + graficaOriginal.getSoundFile().getNumSamples());
                Log.i("ISOUND", "Bytes " + graficaOriginal.getSoundFile().getDecodedBytes());
                Log.i("ISOUND", "Frames " + graficaOriginal.getSoundFile().getNumFrames());
                Log.i("ISOUND", "SampleRate " + graficaOriginal.getSoundFile().getSampleRate() + "\nSamplesPerFrame " + graficaOriginal.getSoundFile().getSamplesPerFrame());
                Log.i("ISOUND", "Samples " + graficaEnc.getSoundFile().getNumSamples());
                Log.i("ISOUND", "Bytes " + graficaEnc.getSoundFile().getDecodedBytes());
                Log.i("ISOUND", "Frames " + graficaEnc.getSoundFile().getNumFrames());
                Log.i("ISOUND", "SampleRate " + graficaEnc.getSoundFile().getSampleRate() + "\nSamplesPerFrame " + graficaEnc.getSoundFile().getSamplesPerFrame());
                for(int i = 0; i < graficaOriginal.getSoundFile().getDecodedBytes().limit(); i++){
                    if(graficaOriginal.getSoundFile().getDecodedBytes().get(i) == 1){
                        for(int j = 0; j < 100; j++){
                            Log.i("Bytes", i+j + " " +
                                    graficaOriginal.getSoundFile().getDecodedBytes().get(i+j));
                        }
                        break;
                    }

                }

                for(int i = 0; i < graficaEnc.getSoundFile().getDecodedBytes().limit(); i++){
                    if(graficaEnc.getSoundFile().getDecodedBytes().get(i) == 1){
                        for(int j = 0; j < 100; j++){
                            Log.i("Bytes", i+j + " " +
                                    graficaEnc.getSoundFile().getDecodedBytes().get(i+j));
                        }
                        break;
                    }

                }
            }
        });


    }

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
                int offset = 0;
                tiempoAntes = System.currentTimeMillis();
                DatosEncriptar datos = DatosEncriptar.getInstance();
                int[][] llave;
                if(dialogLlaves.getEncrypt()){
                    llave = datos.getLlave();
                }
                else{
                    llave = datos.getLlaveDes();
                    offset = 2;
                }
                int[] arreglo = new int[3];
                int[] arregloSignos = new int[3];
                int[] arregloEnc = new int[3];
                /*Debug*/
                /*Log.i("ISOUND", "Samples " + graficaOriginal.getSoundFile().getNumSamples());
                Log.i("ISOUND", "Frames " + graficaOriginal.getSoundFile().getNumFrames());
                Log.i("ISOUND", "SampleRate " + graficaOriginal.getSoundFile().getSampleRate() + "\nSamplesPerFrame " + graficaOriginal.getSoundFile().getSamplesPerFrame());*/

                /*
                Creamos los buffers, uno con los datos del sonido original y el segundo donde se alojaran los datos encriptados
                 */
                ByteBuffer bufferSonidoOriginal = graficaOriginal.getSoundFile().getDecodedBytes();
                ByteBuffer otroBuffer = ByteBuffer.allocate(bufferSonidoOriginal.limit());
                for(int i = offset; i < bufferSonidoOriginal.limit(); i+=3)
                {
                    /*if(i%100 == 0 && i < 10000)Log.i("DATOSB", i + "--" + bufferSonidoOriginal.get(i) + "");
                    if(i%100 == 0 && i < 10000)Log.i("DATOSB", bufferSonidoOriginal.get(i + 1) + "");
                    if(i%100 == 0 && i < 10000)Log.i("DATOSB", bufferSonidoOriginal.get(i + 2) + "");*/
                    //Asignamos los valores del Buffer al arrelo a encriptar
                    arreglo[0] = bufferSonidoOriginal.get(i);
                    if((i + 1) < bufferSonidoOriginal.limit()) arreglo[1] = bufferSonidoOriginal.get(i + 1);
                    if((i + 2) < bufferSonidoOriginal.limit()) arreglo[2] = bufferSonidoOriginal.get(i + 2);
                    /*if(i%100 == 0 && i < 10000)Log.i("DATOSA", arreglo[0] + "");
                    if(i%100 == 0 && i < 10000)Log.i("DATOSA", arreglo[1] + "");
                    if(i%100 == 0 && i < 10000)Log.i("DATOSA", arreglo[2] + "");*/

                    //Verificamos valor de signos
                    for(int j = 0; j < 3; j++){
                        if(arreglo[j] < 0){
                            arreglo[j] = arreglo[j] * -1;
                            arregloSignos[j] = -1;
                        }
                        else{
                            arregloSignos[j] = 1;
                        }
                    }
                    //Log.i("NENC", "[" + arreglo[0] + ", " + arreglo[1] + ", " + arreglo[2] + "]");

                    //Encriptamos
                    for(int j = 0; j < 3; j++)
                    {
                        arregloEnc[j] = 0;
                        for(int k = 0; k < 3; k++)
                        {
                            arregloEnc[j] += llave[j][k] * arreglo[k];
                        }
                        //Modulo 128
                        //if(i%100 == 0 && i < 10000)Log.i("DATOSENM", arregloEnc[j] + "");
                        arregloEnc[j] = arregloEnc[j]%128;
                        //if(i%100 == 0 && i < 10000)Log.i("DATOSE", arregloEnc[j] + "");
                    }

                    //Insertamos los datos encriptados en el buffer
                    otroBuffer.put(i, ((byte) (arregloEnc[0] * arregloSignos[0])));
                    if((i + 1) < bufferSonidoOriginal.limit())otroBuffer.put(i + 1, ((byte) (arregloEnc[1] * arregloSignos[1])));
                    if((i + 2) < bufferSonidoOriginal.limit())otroBuffer.put(i + 2, ((byte) (arregloEnc[2] * arregloSignos[2])));
                    /*otroBuffer.put(i, bufferSonidoOriginal.get(i));
                    if((i + 1) < bufferSonidoOriginal.limit())otroBuffer.put(i + 1, bufferSonidoOriginal.get(i+1));
                    if((i + 2) < bufferSonidoOriginal.limit())otroBuffer.put(i + 2, bufferSonidoOriginal.get(i+2));*/


                    /*if(i%100 == 0 && i < 10000)Log.i("DATOSDes", otroBuffer.get(i) + "");
                    if(i%100 == 0 && i < 10000)Log.i("DATOSDes", otroBuffer.get(i + 1) + "");
                    if(i%100 == 0 && i < 10000)Log.i("DATOSDes", otroBuffer.get(i + 2) + "");
                    //Log.i("ENC", i + " [" + arregloEnc[0] + ", " + arregloEnc[1] + ", " + arregloEnc[2] + "]");*/
                    progressDialog.setProgress((i * 100) / bufferSonidoOriginal.limit());

                }
                tiempoDespues = System.currentTimeMillis();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.setTitle("Creando Archivo");
                        progressDialog.setIndeterminate(true);
                    }
                });

                if(directorioCreado){
                    String recordName = "";
                    if(encrypt){
                        recordName = "ENC_" + (graficaOriginal.mFile.getName().substring(0, (graficaOriginal.mFile.getName().length() - 4))) + ".wav";
                    }
                    else
                    {

                        recordName = "DES_" + (graficaOriginal.mFile.getName().substring(0, (graficaOriginal.mFile.getName().length() - 4))) + ".wav";
                    }
                    directorio = Environment.getExternalStorageDirectory().getAbsolutePath();
                    directorio = directorio + File.separator + RECORD_DIRECTORY + File.separator + recordName;
                    encriptedFile = new File(directorio);
                    try{
                        WriteWAVFile(encriptedFile, Float.parseFloat(startText.getText().toString()),
                                Float.parseFloat(graficaOriginal.getMaxTime()),
                                graficaOriginal.getSoundFile().getChannels(),
                                graficaOriginal.getSoundFile().getSampleRate(),
                                otroBuffer);
                        MediaScannerConnection.scanFile (context, new String[] {encriptedFile.toString()}, null, null);
                        //Log.e("CFILE", "Archivo creado");
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                graficaEnc.setmFile(encriptedFile);
                                graficaEnc.generarGrafica();
                            }
                        });
                    }
                    catch(IOException ioe){
                        Log.e("CFILE", ioe.getMessage());
                    }

                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        tiempoDespues = tiempoDespues - tiempoAntes;
                        //editTituloEnc.setText("Tiempo encriptado: " + TimeUnit.MILLISECONDS.toSeconds(tiempoDespues) + " seg");
                    }
                });
            }
        }.start();
    }

    /**
     * Despliegue del Alert dialog para seleccionar si grabar sonido o seleccionarlo del almacenamiento
     */
    public void showOptions()
    {
        final CharSequence[] option = {
                getString(R.string.opcion_grabar_sonido),
                getString(R.string.opcion_elegir),
                getString(R.string.opcion_cancelar)};
        final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);
        builder.setTitle(R.string.opcion_titulo);
        builder.setItems(option, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(option[which] == getString(R.string.opcion_grabar_sonido)){
                    dialog.dismiss();
                    dialogRecord.show();
                }else if(option[which] == getString(R.string.opcion_elegir)){
                    Intent intent = new Intent();
                    intent.setType("audio/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    getActivity().startActivityForResult(Intent.createChooser(intent, getString(R.string.opcion_titulo)), SELECT_AUDIO);
                }else {
                    dialog.dismiss();
                }
            }
        });

        builder.show();
    }

    public void getPathFromUri(Uri path){
        String fileName;

        Log.i("FSENC", filenameOr + "\n" + path + "\n" + path.getPath());
        Cursor cursor = null;
        try {
            final String docId = DocumentsContract.getDocumentId(path);
            final String[] split = docId.split(":");
            // where id is equal to
            String sel = MediaStore.Images.Media._ID + "=?";
            cursor = getActivity().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[]{
                    "_data",
                    MediaStore.Files.FileColumns.SIZE,
                    MediaStore.Files.FileColumns.DISPLAY_NAME,
            }, sel, new String[]{
                    split[1]
            }, null);

            if (cursor != null && cursor.moveToFirst()) {
                Log.i("FSENC", "Error en path" + cursor);
                fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME));
                filenameOr = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            }
        }
        catch (RuntimeException re){
            Log.i("FSENC", "Error en path");
            return;
        }
        finally {

            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Funcion para extraer los datos de la cancion, su path e instanciarla para reproducirla
     * @param path - Uri que proviene
     */
    public void obtenerSonido(Uri path)
    {
        filenameOr = null;
        getPathFromUri(path);

        if (filenameOr == null){
            getPathFromUri(MediaStore.Files.getContentUri(path.getPath()));
        }
        if (filenameOr == null){
            getPathFromUri(MediaStore.Audio.Media.getContentUri(path.getPath()));
        }
        if (filenameOr == null){
            getPathFromUri(MediaStore.getDocumentUri(context, path));
        }
        if (filenameOr == null){
            Log.i("IF", "Entra al 1er if");
            graficaOriginal = new GraficaSonido(density, startText, endText, playButton, rewindButton, ffwdButton, waveformView, info, startMarker, endMarker, editTitulo, editSize);
            graficaOriginal.setmFile(new File(path.getPath()));
            graficaOriginal.generarGrafica();
        }
        else{
            Log.i("IF", "Entra al 2do if " + filenameOr);
            graficaOriginal = new GraficaSonido(density, startText, endText, playButton, rewindButton, ffwdButton, waveformView, info, startMarker, endMarker, editTitulo, editSize);
            graficaOriginal.setmFile(new File(filenameOr));
            graficaOriginal.generarGrafica();
        }


    }

    public void WriteFile(File outputFile, float startTime, float endTime, int mChannels, int mSampleRate, ByteBuffer mDecodedBytes, int mBitrate)
            throws java.io.IOException {
        int startOffset = (int)(startTime * mSampleRate) * 2 * mChannels;
        int numSamples = (int)((endTime - startTime) * mSampleRate);

        // Some devices have problems reading mono AAC files (e.g. Samsung S3). Making it stereo.
        //int numChannels = (mChannels == 1) ? 2 : mChannels;
        int numChannels = mChannels;

        String mimeType = "audio/mp4a-latm";
        //int bitrate = 64000 * numChannels;  // rule of thumb for a good quality: 64kbps per channel.
        int bitrate = mBitrate*1000;
        MediaCodec codec = MediaCodec.createEncoderByType(mimeType);
        MediaFormat format = MediaFormat.createAudioFormat(mimeType, mSampleRate, numChannels);
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        codec.start();

        // Get an estimation of the encoded data based on the bitrate. Add 10% to it.
        //int estimatedEncodedSize = (int)((endTime - startTime) * (bitrate / 8) * 1.1);
        int estimatedEncodedSize = (int)((endTime - startTime) * (bitrate / 8) * 1);//quitamos el 10%
        ByteBuffer encodedBytes = ByteBuffer.allocate(estimatedEncodedSize);
        ByteBuffer[] inputBuffers = codec.getInputBuffers();
        ByteBuffer[] outputBuffers = codec.getOutputBuffers();
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        boolean done_reading = false;
        long presentation_time = 0;

        int frame_size = 1024;  // number of samples per frame per channel for an mp4 (AAC) stream.
        byte buffer[] = new byte[frame_size * numChannels * 2];  // a sample is coded with a short.
        mDecodedBytes.position(startOffset);
        //numSamples += (2 * frame_size);  // Adding 2 frames, Cf. priming frames for AAC.
        int tot_num_frames = 1 + (numSamples / frame_size);  // first AAC frame = 2 bytes
        if (numSamples % frame_size != 0) {
            tot_num_frames++;
        }
        int[] frame_sizes = new int[tot_num_frames];
        int num_out_frames = 0;
        int num_frames=0;
        int num_samples_left = numSamples;
        int encodedSamplesSize = 0;  // size of the output buffer containing the encoded samples.
        byte[] encodedSamples = null;
        while (true) {
            // Feed the samples to the encoder.
            int inputBufferIndex = codec.dequeueInputBuffer(100);
            if (!done_reading && inputBufferIndex >= 0) {
                if (num_samples_left <= 0) {
                    // All samples have been read.
                    codec.queueInputBuffer(
                            inputBufferIndex, 0, 0, -1, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    done_reading = true;
                } else {
                    inputBuffers[inputBufferIndex].clear();
                    if (buffer.length > inputBuffers[inputBufferIndex].remaining()) {
                        // Input buffer is smaller than one frame. This should never happen.
                        continue;
                    }
                    // bufferSize is a hack to create a stereo file from a mono stream.
                    int bufferSize = buffer.length;
                    Log.i("bufferSize", ""+bufferSize + " " + mChannels + " " + buffer.length);
                    Log.i("Remaining", mDecodedBytes.remaining() + " " + bufferSize);
                    if (mDecodedBytes.remaining() < bufferSize) {
                        Log.i("Otro que no", "pero si");
                        for (int i=mDecodedBytes.remaining(); i < bufferSize; i++) {
                            buffer[i] = 0;  // pad with extra 0s to make a full frame.
                        }
                        mDecodedBytes.get(buffer, 0, mDecodedBytes.remaining());
                    } else {
                        Log.i("Otro que no", "pero six2");
                        mDecodedBytes.get(buffer, 0, bufferSize);
                    }
                    /*if (mChannels == 1) {
                        //Log.i("Canales", "Un solo canal alv");
                        for (int i=bufferSize - 1; i >= 1; i -= 2) {
                            buffer[2*i + 1] = buffer[i];
                            buffer[2*i] = buffer[i-1];
                            buffer[2*i - 1] = buffer[2*i + 1];
                            buffer[2*i - 2] = buffer[2*i];
                        }
                    }*/
                    num_samples_left -= frame_size;
                    inputBuffers[inputBufferIndex].put(buffer);
                    presentation_time = (long) (((num_frames++) * frame_size * 1e6) / mSampleRate);
                    codec.queueInputBuffer(
                            inputBufferIndex, 0, buffer.length, presentation_time, 0);
                }
            }

            // Get the encoded samples from the encoder.
            int outputBufferIndex = codec.dequeueOutputBuffer(info, 100);
            if (outputBufferIndex >= 0 && info.size > 0 && info.presentationTimeUs >=0) {
                if (num_out_frames < frame_sizes.length) {
                    frame_sizes[num_out_frames++] = info.size;
                }
                if (encodedSamplesSize < info.size) {
                    encodedSamplesSize = info.size;
                    encodedSamples = new byte[encodedSamplesSize];
                }
                outputBuffers[outputBufferIndex].get(encodedSamples, 0, info.size);
                outputBuffers[outputBufferIndex].clear();
                codec.releaseOutputBuffer(outputBufferIndex, false);
                if (encodedBytes.remaining() < info.size) {  // Hopefully this should not happen.
                    //Log.i("No deberia", "Si pasa" + info.size + " " + encodedBytes.remaining());
                    estimatedEncodedSize = (int)(estimatedEncodedSize * 1.1);  // Add 20%.
                    ByteBuffer newEncodedBytes = ByteBuffer.allocate(estimatedEncodedSize);
                    int position = encodedBytes.position();
                    encodedBytes.rewind();
                    newEncodedBytes.put(encodedBytes);
                    encodedBytes = newEncodedBytes;
                    encodedBytes.position(position);
                }
                encodedBytes.put(encodedSamples, 0, info.size);
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                outputBuffers = codec.getOutputBuffers();
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // Subsequent data will conform to new format.
                // We could check that codec.getOutputFormat(), which is the new output format,
                // is what we expect.
            }
            if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                // We got all the encoded data from the encoder.
                break;
            }
        }
        int encoded_size = encodedBytes.position();
        encodedBytes.rewind();
        codec.stop();
        codec.release();
        codec = null;

        // Write the encoded stream to the file, 4kB at a time.
        buffer = new byte[4096];
        try {
            FileOutputStream outputStream = new FileOutputStream(outputFile);
            outputStream.write(
                    MP4Header.getMP4Header(mSampleRate, numChannels, frame_sizes, bitrate));
            while (encoded_size - encodedBytes.position() > buffer.length) {
                encodedBytes.get(buffer);
                outputStream.write(buffer);
            }
            int remaining = encoded_size - encodedBytes.position();
            if (remaining > 0) {
                encodedBytes.get(buffer, 0, remaining);
                outputStream.write(buffer, 0, remaining);
            }
            outputStream.close();
        } catch (IOException e) {
            Log.e("EncripTuring", "Failed to create the .m4a file.");
            Log.e("EncripTuring", e.getMessage());
        }
    }

    private void swapLeftRightChannels(byte[] buffer) {
        byte left[] = new byte[2];
        byte right[] = new byte[2];
        if (buffer.length % 4 != 0) {  // 2 channels, 2 bytes per sample (for one channel).
            // Invalid buffer size.
            return;
        }
        for (int offset = 0; offset < buffer.length; offset += 4) {
            left[0] = buffer[offset];
            left[1] = buffer[offset + 1];
            right[0] = buffer[offset + 2];
            right[1] = buffer[offset + 3];
            buffer[offset] = right[0];
            buffer[offset + 1] = right[1];
            buffer[offset + 2] = left[0];
            buffer[offset + 3] = left[1];
        }
    }

    public void WriteWAVFile(File outputFile, float startTime, float endTime, int mChannels, int mSampleRate, ByteBuffer mDecodedBytes)
            throws java.io.IOException {
        int startOffset = (int)(startTime * mSampleRate) * 2 * mChannels;
        int numSamples = (int)((endTime - startTime) * mSampleRate);

        // Start by writing the RIFF header.
        FileOutputStream outputStream = new FileOutputStream(outputFile);
        outputStream.write(WAVHeader.getWAVHeader(mSampleRate, mChannels, numSamples));

        // Write the samples to the file, 1024 at a time.
        byte buffer[] = new byte[1024 * mChannels * 2];  // Each sample is coded with a short.
        mDecodedBytes.position(startOffset);
        int numBytesLeft = numSamples * mChannels * 2;
        while (numBytesLeft >= buffer.length) {
            if (mDecodedBytes.remaining() < buffer.length) {
                // This should not happen.
                for (int i = mDecodedBytes.remaining(); i < buffer.length; i++) {
                    buffer[i] = 0;  // pad with extra 0s to make a full frame.
                }
                mDecodedBytes.get(buffer, 0, mDecodedBytes.remaining());
            } else {
                mDecodedBytes.get(buffer);
            }
            if (mChannels == 2) {
                swapLeftRightChannels(buffer);
            }
            outputStream.write(buffer);
            numBytesLeft -= buffer.length;
        }
        if (numBytesLeft > 0) {
            if (mDecodedBytes.remaining() < numBytesLeft) {
                // This should not happen.
                for (int i = mDecodedBytes.remaining(); i < numBytesLeft; i++) {
                    buffer[i] = 0;  // pad with extra 0s to make a full frame.
                }
                mDecodedBytes.get(buffer, 0, mDecodedBytes.remaining());
            } else {
                mDecodedBytes.get(buffer, 0, numBytesLeft);
            }
            if (mChannels == 2) {
                swapLeftRightChannels(buffer);
            }
            outputStream.write(buffer, 0, numBytesLeft);
        }
        outputStream.close();
    }

    private class GraficaSonido implements com.turing.encripturing.MarkerView.MarkerListener, WaveformView.WaveformListener{
        private BecomingNoisyReceiver myNoisyAudioStreamReceiver;
        private IntentFilter intentFilter;

        //Cosas del waveform
        private long mLoadingLastUpdateTime;
        private boolean mLoadingKeepGoing;
        private boolean mFinishActivity;
        private ProgressDialog mProgressDialog;
        private SoundFile mSoundFile;
        private File mFile;
        private WaveformView mWaveformView;
        private MarkerView mStartMarker;
        private MarkerView mEndMarker;
        private TextInputEditText mStartText;
        private TextInputEditText mEndText;
        private TextView mInfo;
        private String mInfoContent;
        private ImageButton mPlayButton;
        private ImageButton mRewindButton;
        private ImageButton mFfwdButton;
        private boolean mKeyDown;
        private String mCaption = "";
        private int mWidth;
        private int mMaxPos;
        private int mStartPos;
        private int mEndPos;
        private boolean mStartVisible;
        private boolean mEndVisible;
        private int mLastDisplayedStartPos;
        private int mLastDisplayedEndPos;
        private int mOffset;
        private int mOffsetGoal;
        private int mFlingVelocity;
        private int mPlayStartMsec;
        private int mPlayEndMsec;
        private Handler mHandler;
        private boolean mIsPlaying;
        private SamplePlayer mPlayer;
        private boolean mTouchDragging;
        private float mTouchStart;
        private int mTouchInitialOffset;
        private int mTouchInitialStartPos;
        private int mTouchInitialEndPos;
        private long mWaveformTouchStartMsec;
        private float mDensity;
        private int mMarkerLeftInset;
        private int mMarkerRightInset;
        private int mMarkerTopOffset;
        private int mMarkerBottomOffset;

        private EditText mTitulo, mSize;

        private Thread mLoadSoundFileThread;


        public GraficaSonido(float mDensity, TextInputEditText mStartText, TextInputEditText mEndText, ImageButton mPlayButton, ImageButton mRewindButton, ImageButton mFfwdButton,
                             WaveformView mWaveformView, TextView mInfo, MarkerView mStartMarker, MarkerView mEndMarker, EditText titulo, EditText size){
            intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
            myNoisyAudioStreamReceiver = new BecomingNoisyReceiver();

            mTitulo = titulo;
            mSize = size;

            //Wave form
            mPlayer = null;
            mIsPlaying = false;
            mProgressDialog = null;
            mLoadSoundFileThread = null;
            mSoundFile = null;
            mKeyDown = false;

            mHandler = new Handler();

            this.mDensity = mDensity;

            mMarkerLeftInset = (int)(46 * mDensity);
            mMarkerRightInset = (int)(48 * mDensity);
            mMarkerTopOffset = (int)(10 * mDensity);
            mMarkerBottomOffset = (int)(10 * mDensity);

            this.mStartText = mStartText;
            mStartText.addTextChangedListener(mTextWatcher);

            this.mEndText = mEndText;
            mEndText.addTextChangedListener(mTextWatcher);

            this.mPlayButton = mPlayButton;
            mPlayButton.setOnClickListener(mPlayListener);
            this.mRewindButton = mRewindButton;
            mRewindButton.setOnClickListener(mRewindListener);
            this.mFfwdButton = mFfwdButton;
            mFfwdButton.setOnClickListener(mFfwdListener);

            this.mWaveformView = mWaveformView;
            mWaveformView.setListener(this);

            this.mInfo = mInfo;
            mInfo.setText(mCaption);

            this.mStartMarker = mStartMarker;
            mStartMarker.setListener(this);
            mStartMarker.setAlpha(1f);
            mStartMarker.setFocusable(true);
            mStartMarker.setFocusableInTouchMode(true);
            mStartVisible = true;

            this.mEndMarker = mEndMarker;
            mEndMarker.setListener(this);
            mEndMarker.setAlpha(1f);
            mEndMarker.setFocusable(true);
            mEndMarker.setFocusableInTouchMode(true);
            mEndVisible = true;

            mMaxPos = 0;
            mLastDisplayedStartPos = -1;
            mLastDisplayedEndPos = -1;


            mHandler.postDelayed(mTimerRunnable, 200);
        }

        public void setmFile(File mFile){
            this.mFile = mFile;
            mTitulo.setText(this.mFile.getName());
            DecimalFormat df = new DecimalFormat("#.00");
            mSize.setText(df.format(((double)this.mFile.length())/1024) + " KB");
        }

        public SoundFile getSoundFile(){
            return mSoundFile;
        }

        public void generarGrafica(){
            mLoadingLastUpdateTime = getCurrentTime();
            mLoadingKeepGoing = true;
            mFinishActivity = false;
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setTitle(R.string.dialog_cargando_audio);
            mProgressDialog.setCancelable(true);
            mProgressDialog.setOnCancelListener(
                    new DialogInterface.OnCancelListener() {
                        public void onCancel(DialogInterface dialog) {
                            mLoadingKeepGoing = false;
                            mFinishActivity = true;
                        }
                    });
            mProgressDialog.show();

            final SoundFile.ProgressListener listener =
                    new SoundFile.ProgressListener() {
                        public boolean reportProgress(double fractionComplete) {
                            long now = getCurrentTime();
                            if (now - mLoadingLastUpdateTime > 100) {
                                mProgressDialog.setProgress(
                                        (int) (mProgressDialog.getMax() * fractionComplete));
                                mLoadingLastUpdateTime = now;
                            }
                            return mLoadingKeepGoing;
                        }
                    };

            // Load the sound file in a background thread
            mLoadSoundFileThread = new Thread() {
                public void run() {
                    try {
                        mSoundFile = SoundFile.create(mFile.getAbsolutePath(), listener);

                        if (mSoundFile == null) {
                            mProgressDialog.dismiss();
                            String name = mFile.getName().toLowerCase();
                            String[] components = name.split("\\.");
                            String err;
                            if (components.length < 2) {
                                err = "Error";
                            } else {
                                err = "Error de extension" + " " + components[components.length - 1];
                            }
                            final String finalErr = err;
                            Runnable runnable = new Runnable() {
                                public void run() {
                                    showFinalAlert(new Exception(), finalErr);
                                }
                            };
                            mHandler.post(runnable);
                            return;
                        }
                        mPlayer = new SamplePlayer(mSoundFile);
                    } catch (final Exception e) {
                        mProgressDialog.dismiss();
                        e.printStackTrace();
                        mInfoContent = e.toString();
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                mInfo.setText(mInfoContent);
                            }
                        });

                        Runnable runnable = new Runnable() {
                            public void run() {
                                showFinalAlert(e, "Error");
                            }
                        };
                        mHandler.post(runnable);
                        return;
                    }
                    mProgressDialog.dismiss();
                    if (mLoadingKeepGoing) {
                        Runnable runnable = new Runnable() {
                            public void run() {
                                finishOpeningSoundFile();
                            }
                        };
                        mHandler.post(runnable);
                    } else if (mFinishActivity){

                    }
                }
            };
            mLoadSoundFileThread.start();
        }

        private Runnable mTimerRunnable = new Runnable() {
            public void run() {
                // Updating an EditText is slow on Android.  Make sure
                // we only do the update if the text has actually changed.
                if (mStartPos != mLastDisplayedStartPos &&
                        !mStartText.hasFocus()) {
                    mStartText.setText(formatTime(mStartPos));
                    mLastDisplayedStartPos = mStartPos;
                }

                if (mEndPos != mLastDisplayedEndPos &&
                        !mEndText.hasFocus()) {
                    mEndText.setText(formatTime(mEndPos));
                    mLastDisplayedEndPos = mEndPos;
                }

                mHandler.postDelayed(mTimerRunnable, 200);
            }
        };

        private TextWatcher mTextWatcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s,
                                      int start, int before, int count) {
            }

            public void afterTextChanged(Editable s) {
                if (mStartText.hasFocus()) {
                    try {
                        mStartPos = mWaveformView.secondsToPixels(
                                Double.parseDouble(
                                        mStartText.getText().toString()));
                        updateDisplay();
                    } catch (NumberFormatException e) {
                    }
                }
                if (mEndText.hasFocus()) {
                    try {
                        mEndPos = mWaveformView.secondsToPixels(
                                Double.parseDouble(
                                        mEndText.getText().toString()));
                        updateDisplay();
                    } catch (NumberFormatException e) {
                    }
                    catch(NullPointerException npe){

                    }
                }
            }
        };

        private void setOffsetGoalStart() {
            setOffsetGoal(mStartPos - mWidth / 2);
        }

        private void setOffsetGoalStartNoUpdate() {
            setOffsetGoalNoUpdate(mStartPos - mWidth / 2);
        }

        private void setOffsetGoalEnd() {
            setOffsetGoal(mEndPos - mWidth / 2);
        }

        private void setOffsetGoalEndNoUpdate() {
            setOffsetGoalNoUpdate(mEndPos - mWidth / 2);
        }

        private void setOffsetGoal(int offset) {
            setOffsetGoalNoUpdate(offset);
            updateDisplay();
        }

        private void setOffsetGoalNoUpdate(int offset) {
            if (mTouchDragging) {
                return;
            }

            mOffsetGoal = offset;
            if (mOffsetGoal + mWidth / 2 > mMaxPos)
                mOffsetGoal = mMaxPos - mWidth / 2;
            if (mOffsetGoal < 0)
                mOffsetGoal = 0;
        }

        private synchronized void handlePause() {
            if (mPlayer != null && mPlayer.isPlaying()) {
                mPlayer.pause();
            }
            mWaveformView.setPlayback(-1);
            mIsPlaying = false;
            enableDisableButtons();
        }

        private void enableDisableButtons() {
            if (mIsPlaying) {
                mPlayButton.setImageResource(R.drawable.ic_media_stop);
            } else {
                mPlayButton.setImageResource(R.drawable.ic_media_play);
            }
        }

        private synchronized void updateDisplay() {
            if (mIsPlaying) {
                int now = mPlayer.getCurrentPosition();
                int frames = mWaveformView.millisecsToPixels(now);
                mWaveformView.setPlayback(frames);
                setOffsetGoalNoUpdate(frames - mWidth / 2);
                if (now >= mPlayEndMsec) {
                    handlePause();
                }
            }

            if (!mTouchDragging) {
                int offsetDelta;

                if (mFlingVelocity != 0) {
                    offsetDelta = mFlingVelocity / 30;
                    if (mFlingVelocity > 80) {
                        mFlingVelocity -= 80;
                    } else if (mFlingVelocity < -80) {
                        mFlingVelocity += 80;
                    } else {
                        mFlingVelocity = 0;
                    }

                    mOffset += offsetDelta;

                    if (mOffset + mWidth / 2 > mMaxPos) {
                        mOffset = mMaxPos - mWidth / 2;
                        mFlingVelocity = 0;
                    }
                    if (mOffset < 0) {
                        mOffset = 0;
                        mFlingVelocity = 0;
                    }
                    mOffsetGoal = mOffset;
                } else {
                    offsetDelta = mOffsetGoal - mOffset;

                    if (offsetDelta > 10)
                        offsetDelta = offsetDelta / 10;
                    else if (offsetDelta > 0)
                        offsetDelta = 1;
                    else if (offsetDelta < -10)
                        offsetDelta = offsetDelta / 10;
                    else if (offsetDelta < 0)
                        offsetDelta = -1;
                    else
                        offsetDelta = 0;

                    mOffset += offsetDelta;
                }
            }
            mWaveformView.setParameters(mStartPos, mEndPos, mOffset);
            mWaveformView.invalidate();

            mStartMarker.setContentDescription(
                    getResources().getText(R.string.start_marker) + " " +
                            formatTime(mStartPos));
            mEndMarker.setContentDescription(
                    getResources().getText(R.string.end_marker) + " " +
                            formatTime(mEndPos));

            int startX = mStartPos - mOffset - mMarkerLeftInset;
            if (startX + mStartMarker.getWidth() >= 0) {
                if (!mStartVisible) {
                    // Delay this to avoid flicker
                    mHandler.postDelayed(new Runnable() {
                        public void run() {
                            mStartVisible = true;
                            mStartMarker.setAlpha(1f);
                        }
                    }, 0);
                }
            } else {
                if (mStartVisible) {
                    mStartMarker.setAlpha(0f);
                    mStartVisible = false;
                }
                startX = 0;
            }

            int endX = mEndPos - mOffset - mEndMarker.getWidth() + mMarkerRightInset;
            if (endX + mEndMarker.getWidth() >= 0) {
                if (!mEndVisible) {
                    // Delay this to avoid flicker
                    mHandler.postDelayed(new Runnable() {
                        public void run() {
                            mEndVisible = true;
                            mEndMarker.setAlpha(1f);
                        }
                    }, 0);
                }
            } else {
                if (mEndVisible) {
                    mEndMarker.setAlpha(0f);
                    mEndVisible = false;
                }
                endX = 0;
            }

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(
                    startX,
                    mMarkerTopOffset,
                    -mStartMarker.getWidth(),
                    -mStartMarker.getHeight());
            mStartMarker.setLayoutParams(params);

            params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(
                    endX,
                    mWaveformView.getMeasuredHeight() - mEndMarker.getHeight() - mMarkerBottomOffset,
                    -mStartMarker.getWidth(),
                    -mStartMarker.getHeight());
            mEndMarker.setLayoutParams(params);
        }

        private String formatTime(int pixels) {
            if (mWaveformView != null && mWaveformView.isInitialized()) {
                return formatDecimal(mWaveformView.pixelsToSeconds(pixels));
            } else {
                return "";
            }
        }

        private String formatDecimal(double x) {
            int xWhole = (int)x;
            int xFrac = (int)(100 * (x - xWhole) + 0.5);

            if (xFrac >= 100) {
                xWhole++; //Round up
                xFrac -= 100; //Now we need the remainder after the round up
                if (xFrac < 10) {
                    xFrac *= 10; //we need a fraction that is 2 digits long
                }
            }

            if (xFrac < 10)
                return xWhole + ".0" + xFrac;
            else
                return xWhole + "." + xFrac;
        }

        @Override
        public void markerTouchStart(com.turing.encripturing.MarkerView marker, float x) {
            mTouchDragging = true;
            mTouchStart = x;
            mTouchInitialStartPos = mStartPos;
            mTouchInitialEndPos = mEndPos;
        }

        @Override
        public void markerTouchMove(com.turing.encripturing.MarkerView marker, float x) {
            float delta = x - mTouchStart;

            if (marker == mStartMarker) {
                mStartPos = trap((int)(mTouchInitialStartPos + delta));
                mEndPos = trap((int)(mTouchInitialEndPos + delta));
            } else {
                mEndPos = trap((int)(mTouchInitialEndPos + delta));
                if (mEndPos < mStartPos)
                    mEndPos = mStartPos;
            }

            updateDisplay();
        }

        private int trap(int pos) {
            if (pos < 0)
                return 0;
            if (pos > mMaxPos)
                return mMaxPos;
            return pos;
        }

        @Override
        public void markerTouchEnd(com.turing.encripturing.MarkerView marker) {
            mTouchDragging = false;
            if (marker == mStartMarker) {
                setOffsetGoalStart();
            } else {
                setOffsetGoalEnd();
            }
        }

        @Override
        public void markerFocus(com.turing.encripturing.MarkerView marker) {
            mKeyDown = false;
            if (marker == mStartMarker) {
                setOffsetGoalStartNoUpdate();
            } else {
                setOffsetGoalEndNoUpdate();
            }

            // Delay updaing the display because if this focus was in
            // response to a touch event, we want to receive the touch
            // event too before updating the display.
            mHandler.postDelayed(new Runnable() {
                public void run() {
                    updateDisplay();
                }
            }, 100);
        }

        @Override
        public void markerLeft(com.turing.encripturing.MarkerView marker, int velocity) {
            mKeyDown = true;

            if (marker == mStartMarker) {
                int saveStart = mStartPos;
                mStartPos = trap(mStartPos - velocity);
                mEndPos = trap(mEndPos - (saveStart - mStartPos));
                setOffsetGoalStart();
            }

            if (marker == mEndMarker) {
                if (mEndPos == mStartPos) {
                    mStartPos = trap(mStartPos - velocity);
                    mEndPos = mStartPos;
                } else {
                    mEndPos = trap(mEndPos - velocity);
                }

                setOffsetGoalEnd();
            }

            updateDisplay();
        }

        @Override
        public void markerRight(com.turing.encripturing.MarkerView marker, int velocity) {
            mKeyDown = true;

            if (marker == mStartMarker) {
                int saveStart = mStartPos;
                mStartPos += velocity;
                if (mStartPos > mMaxPos)
                    mStartPos = mMaxPos;
                mEndPos += (mStartPos - saveStart);
                if (mEndPos > mMaxPos)
                    mEndPos = mMaxPos;

                setOffsetGoalStart();
            }

            if (marker == mEndMarker) {
                mEndPos += velocity;
                if (mEndPos > mMaxPos)
                    mEndPos = mMaxPos;

                setOffsetGoalEnd();
            }

            updateDisplay();
        }

        @Override
        public void markerEnter(com.turing.encripturing.MarkerView marker) {

        }

        @Override
        public void markerKeyUp() {
            mKeyDown = false;
            updateDisplay();
        }

        @Override
        public void markerDraw() {

        }

        @Override
        public void waveformTouchStart(float x) {
            mTouchDragging = true;
            mTouchStart = x;
            mTouchInitialOffset = mOffset;
            mFlingVelocity = 0;
            mWaveformTouchStartMsec = getCurrentTime();
        }

        @Override
        public void waveformTouchMove(float x) {
            mOffset = trap((int)(mTouchInitialOffset + (mTouchStart - x)));
            updateDisplay();
        }

        @Override
        public void waveformTouchEnd() {
            mTouchDragging = false;
            mOffsetGoal = mOffset;

            long elapsedMsec = getCurrentTime() - mWaveformTouchStartMsec;
            if (elapsedMsec < 300) {
                if (mIsPlaying) {
                    int seekMsec = mWaveformView.pixelsToMillisecs(
                            (int)(mTouchStart + mOffset));
                    if (seekMsec >= mPlayStartMsec &&
                            seekMsec < mPlayEndMsec) {
                        mPlayer.seekTo(seekMsec);
                    } else {
                        handlePause();
                    }
                } else {
                    onPlay((int)(mTouchStart + mOffset));
                }
            }
        }

        private synchronized void onPlay(int startPosition) {
            if (mIsPlaying) {
                context.unregisterReceiver(myNoisyAudioStreamReceiver);
                handlePause();
                return;
            }

            if (mPlayer == null) {
                // Not initialized yet
                return;
            }

            try {
                mPlayStartMsec = mWaveformView.pixelsToMillisecs(startPosition);
                if (startPosition < mStartPos) {
                    mPlayEndMsec = mWaveformView.pixelsToMillisecs(mStartPos);
                } else if (startPosition > mEndPos) {
                    mPlayEndMsec = mWaveformView.pixelsToMillisecs(mMaxPos);
                } else {
                    mPlayEndMsec = mWaveformView.pixelsToMillisecs(mEndPos);
                }
                mPlayer.setOnCompletionListener(new SamplePlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion() {
                        handlePause();
                    }
                });
                mIsPlaying = true;

                mPlayer.seekTo(mPlayStartMsec);
                context.registerReceiver(myNoisyAudioStreamReceiver, intentFilter);
                mPlayer.start();//Se comienza a reproducir el audio
                updateDisplay();
                enableDisableButtons();
            } catch (Exception e) {
                showFinalAlert(e, "Error al reproducir");
            }
        }

        @Override
        public void waveformFling(float vx) {
            mTouchDragging = false;
            mOffsetGoal = mOffset;
            mFlingVelocity = (int)(-vx);
            updateDisplay();
        }

        @Override
        public void waveformDraw() {
            mWidth = mWaveformView.getMeasuredWidth();
            if (mOffsetGoal != mOffset && !mKeyDown)
                updateDisplay();
            else if (mIsPlaying) {
                updateDisplay();
            } else if (mFlingVelocity != 0) {
                updateDisplay();
            }
        }

        @Override
        public void waveformZoomIn() {
            mWaveformView.zoomIn();
            mStartPos = mWaveformView.getStart();
            mEndPos = mWaveformView.getEnd();
            mMaxPos = mWaveformView.maxPos();
            mOffset = mWaveformView.getOffset();
            mOffsetGoal = mOffset;
            updateDisplay();
        }

        @Override
        public void waveformZoomOut() {
            mWaveformView.zoomOut();
            mStartPos = mWaveformView.getStart();
            mEndPos = mWaveformView.getEnd();
            mMaxPos = mWaveformView.maxPos();
            mOffset = mWaveformView.getOffset();
            mOffsetGoal = mOffset;
            updateDisplay();
        }

        private View.OnClickListener mPlayListener = new View.OnClickListener() {
            public void onClick(View sender) {
                onPlay(mStartPos);
            }
        };

        private View.OnClickListener mRewindListener = new View.OnClickListener() {
            public void onClick(View sender) {
                if (mIsPlaying) {
                    int newPos = mPlayer.getCurrentPosition() - 5000;
                    if (newPos < mPlayStartMsec)
                        newPos = mPlayStartMsec;
                    mPlayer.seekTo(newPos);
                } else {
                    mStartMarker.requestFocus();
                    markerFocus(mStartMarker);
                }
            }
        };

        private View.OnClickListener mFfwdListener = new View.OnClickListener() {
            public void onClick(View sender) {
                if (mIsPlaying) {
                    int newPos = 5000 + mPlayer.getCurrentPosition();
                    if (newPos > mPlayEndMsec)
                        newPos = mPlayEndMsec;
                    mPlayer.seekTo(newPos);
                } else {
                    mEndMarker.requestFocus();
                    markerFocus(mEndMarker);
                }
            }
        };


        private class BecomingNoisyReceiver extends BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                    Log.i("Jack", "Sparrow");
                    if(mIsPlaying){
                        handlePause();
                    }
                }
            }
        }

        private void finishOpeningSoundFile() {
            mWaveformView.setSoundFile(mSoundFile);
            mWaveformView.recomputeHeights(mDensity);

            mMaxPos = mWaveformView.maxPos();
            mLastDisplayedStartPos = -1;
            mLastDisplayedEndPos = -1;

            mTouchDragging = false;

            mOffset = 0;
            mOffsetGoal = 0;
            mFlingVelocity = 0;
            resetPositions();
            if (mEndPos > mMaxPos)
                mEndPos = mMaxPos;

            mCaption =
                    mSoundFile.getFiletype() + ", " +
                            mSoundFile.getSampleRate() + " Hz, " +
                            mSoundFile.getAvgBitrateKbps() + " kbps, " +
                            formatTime(mMaxPos) + " " +
                            "s";
            mInfo.setText(mCaption);

            updateDisplay();
        }

        public String getMaxTime()
        {
            return formatTime(mMaxPos);
        }

        private void resetPositions() {
            mStartPos = mWaveformView.secondsToPixels(0.0);
            mEndPos = mWaveformView.secondsToPixels(15.0);
        }

        private long getCurrentTime() {
            return System.nanoTime() / 1000000;
        }

        private void showFinalAlert(Exception e, CharSequence message) {
            CharSequence title;
            if (e != null) {
                Log.e("Encripturing", "Error: " + message);
                Log.e("Encripturing", "Error" + e.toString());
                title = "Falla";
                getActivity().setResult(100, new Intent());
            } else {
                Log.v("Encripturing", "Success: " + message);
                title = "Exito";
            }

            new AlertDialog.Builder(getActivity())
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(
                            "Ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    //finish();
                                }
                            })
                    .setCancelable(false)
                    .show();
        }


    }//fin de la clase

}
