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
    public static String fichero = Environment.getExternalStorageDirectory().getAbsolutePath()+"/audio.mp3";

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
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setOutputFile(fichero);

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

    public void detenerGrabacion() {
        mediaRecorder.stop();
        mediaRecorder.release();
    }
}
