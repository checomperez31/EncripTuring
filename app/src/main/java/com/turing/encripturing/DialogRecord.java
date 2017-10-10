package com.turing.encripturing;

import android.animation.Animator;
import android.app.Dialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;

/**
 * Created by smp_3 on 10/09/2017.
 */

public class DialogRecord extends Dialog
{
    private TextView texto;
    private ImageView microfono;
    private boolean grabando = false;
    private MediaRecorder mediaRecorder;
    private static String RECORD_DIRECTORY = "ENC";
    public static String directorio = Environment.getExternalStorageDirectory().getAbsolutePath();
    private boolean directorioCreado = false;

    public DialogRecord(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_record);
        microfono = (ImageView) findViewById(R.id.mic_dialog_record);
        texto = (TextView) findViewById(R.id.text_dialog_record);
        setCancelable(true);

        microfono.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(grabando)
                {
                    texto.setText("Presiona para grabar");
                    grabando = false;
                    microfono.setScaleX(1.0f);
                    microfono.setScaleY(1.0f);
                    microfono.animate().cancel();
                    detenerGrabacion();
                    dismiss();
                }
                else
                {
                    texto.setText("Grabando");
                    grabando = true;
                    animar();
                    grabar();
                }
            }
        });

        File file = new File(directorio, RECORD_DIRECTORY);

        directorioCreado = file.exists();

        if(!directorioCreado) directorioCreado = file.mkdir();
    }

    private void animar()
    {
        microfono.animate().scaleX(1.2f).scaleY(1.2f).setDuration(400).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                microfono.animate().scaleX(1.0f).scaleY(1.0f).setDuration(400).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        animar();
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    public void grabar()
    {
        if(directorioCreado)
        {
            //Creamos el archivo donde se guardara el audio grabado
            Long timestamp = System.currentTimeMillis();
            String recordName = timestamp.toString() + ".mp3";
            directorio = directorio + File.separator + RECORD_DIRECTORY + File.separator + recordName;
            File file = new File(directorio);

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setOutputFile(directorio);

            try {
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                mediaRecorder.prepare();
            } catch (IOException e) {
                Log.e("DREC", "Fallo en grabación");
            }
            mediaRecorder.start();
        }


    }

    public void detenerGrabacion() {
        mediaRecorder.stop();
        mediaRecorder.release();
    }
}
