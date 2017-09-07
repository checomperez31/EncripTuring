/**
 * Created by dbf_6 on 02/09/2017.
 */
package com.turing.encripturing;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

public class MediaPlayerService extends Service implements
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener,
        MediaPlayer.OnBufferingUpdateListener,
        AudioManager.OnAudioFocusChangeListener{

    //VARIABLES GLOBALES
    private MediaPlayer mediaPlayer;//ruta al archivo de audio
    private String mediaFile;
    private int resumePosition; //gudardar el valor de play/pausa
    private AudioManager audioManager;
    //VARIABLES GLOBALES


    private final IBinder iBinder = new LocalBinder();

    @Override
    public  IBinder onBind(Intent intent){
        return iBinder;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent){
        //invocado indicando un status de buffer
        //recurso siendo stremeado
    }

    @Override
    public void onCompletion(MediaPlayer mp){
        //cuando la reproducción de un archivo se ha completado
        stopMedia();
        //detener el servicio
        stopSelf();
    }

    //Manejar errores
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra){
        //cuando existe una error en una operación asincrona
        switch (what){
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer Error", "error en media no valido para la reproducción" +extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("MediaPlayer Error", "Media Error el servidor murio " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MediaError", "Media Error desconocido" +extra);
                break;
        }
        return false;

    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra){
        //comunicar cierta informaicón
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp){
        //media listo para playback
        playMedia();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp){
        //completada una operacion de busqueda
    }

    /**
     * Focus del audio
     */
    @Override
    public void onAudioFocusChange(int focusState){
        //audio focus del sistema es actualizado
        //Se invoca cuando el focus del audio del sistema cambia
        switch (focusState){
            case AudioManager.AUDIOFOCUS_GAIN: //resumir reproducción
                if(mediaPlayer == null) { initMediaPlayer();}
                else if (!mediaPlayer.isPlaying()){ mediaPlayer.start();}

                mediaPlayer.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                //se perdio el focus del audio
                if (mediaPlayer.isPlaying()){mediaPlayer.stop();}

                mediaPlayer.release();
                mediaPlayer = null;
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                //se pierde el focus del audio por un periodo corto
                if (mediaPlayer.isPlaying()) mediaPlayer.pause();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                //Se pierde el focus pero puede seguir la reproducción con menor volumen
                //notificaciones por ejemplo
                if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    public  class  LocalBinder extends Binder {
        public  MediaPlayerService getService(){
            return MediaPlayerService.this;
        }
    }

    private void initMediaPlayer(){
        mediaPlayer = new MediaPlayer();
        //preparar los eventos escucha de mediaPlayer
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnInfoListener(this);
        //resetear el MediaPlayer para que no apunte a otra fuente de datos
        mediaPlayer.reset();

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try{
            //establecer el origen de los arvhios a mediaFile
            mediaPlayer.setDataSource(mediaFile);
        } catch (IOException e){
            e.printStackTrace();
            stopSelf();
        }
        mediaPlayer.prepareAsync();
    }

    /**
     *
     * funciones para manejar las acciones básicas para reproducir
     * play, pausa, stop y resumir
     */

    private void playMedia(){
        if(!mediaPlayer.isPlaying()){
            mediaPlayer.start();
        }
    }

    private void stopMedia(){
        if(mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            resumePosition = mediaPlayer.getCurrentPosition();
        }
    }

    private void pauseMedia(){
        if(mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            resumePosition = mediaPlayer.getCurrentPosition();
        }
    }

    private void resumeMedia(){
        if(!mediaPlayer.isPlaying()){
            mediaPlayer.seekTo(resumePosition);
            mediaPlayer.start();
        }
    }

    private boolean requestAudioFocus(){
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, audioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == audioManager.AUDIOFOCUS_REQUEST_GRANTED){
            //se obtuvo el focus
            return true;
        }
        //no se pudo ganar el focus
        return false;
    }
    private boolean removeAudioFocus(){
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                audioManager.abandonAudioFocus(this);
    }

    /**
     * onStartCommand
     * maneja la inicialización del reproductor y el focus para asegurar que
     * no haya mas aplcaciones reproduciendo contenido
     */
    @Override
    //El sistema llama al metodo cuando una actividad solicita que el servicio sea iniciado
    public int onStartCommand(Intent intent, int flags, int startId){
        try{
            mediaFile = intent.getExtras().getString("media");
        }catch (NullPointerException e){
            stopSelf();
        }

        //solicitar el focus del audio
        if (requestAudioFocus() == false){
            //focus no obtenido
            stopSelf();
        }

        if (mediaFile != null && mediaFile != ""){
            initMediaPlayer();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * OnDestroy
     * los recursos del reproductor son liberados
     * Como el servicio se detendra no es necesario controlar los servicios de media
     * Libera el focus del audio
     */
    @Override
    public void onDestroy(){
        super.onDestroy();
        if (mediaPlayer !=null){
            stopMedia();
            mediaPlayer.release();
        }
        removeAudioFocus();
    }
}
