package com.turing.encripturing;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by Sergio on 13/01/2018.
 */
public class DialogProgress extends Dialog {

    private TextView titulo, porcentaje, mensaje;
    private ProgressBar progreso;

    public DialogProgress(Context context) {
        super(context);
        create();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_progress);
        titulo = findViewById(R.id.dialog_progress_titulo);
        porcentaje = findViewById(R.id.dialog_progress_percent);
        mensaje = findViewById(R.id.dialog_progress_description);
        progreso = findViewById(R.id.dialog_progress_spinner);
    }

    public void setProgress(int progress){
        if(progress <= 100){
            porcentaje.setText(progress + "%");
            progreso.setProgress(progress);
        }
    }

    public void setDescription(String mensaje){
        this.mensaje.setText(mensaje);
    }

    public void setTitle(String title){
        titulo.setText(title);
    }

    public int getProgress(){
        return progreso.getProgress();
    }

    public void setIndeterminate(boolean determinate){
        progreso.setIndeterminate(determinate);
    }
}
