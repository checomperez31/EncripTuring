package com.turing.encripturing;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
/**
 * Created by smp_3 on 03/12/2017.
 */

public class DialogLlaves extends Dialog{
    //Variables Video
    private TextView txt00, txt01, txt02, txt10, txt11, txt12, txt20, txt21, txt22, mensaje;
    private TextView dtxt00, dtxt01, dtxt02, dtxt10, dtxt11, dtxt12, dtxt20, dtxt21, dtxt22;
    //Variables Audio
    private TextView txt00a, txt01a, txt02a, txt10a, txt11a, txt12a, txt20a, txt21a, txt22a;
    private TextView dtxt00a, dtxt01a, dtxt02a, dtxt10a, dtxt11a, dtxt12a, dtxt20a, dtxt21a, dtxt22a;
    private Button btnGenerar, btnAceptar, btnCancelar;
    int[][] llaveVideo, llaveDesVideo, llaveAudio, llaveDesAudio;
    private Matrix mat;
    DatosEncriptar datosEncriptar;
    private boolean canceled = false, keygenerated = false, encrypt = true;
    private RelativeLayout matrizEncAudio, matrizDesAudio, matrizEncVideo, matrizDesVideo;
    private int modulo = 128, type = 0;
    private TextView audiom, videom;

    public void setModulo(int modulo) {
        this.modulo = modulo;
        Log.i("RGB", "modulo: " + modulo);
    }

    public DialogLlaves(Context context, int type) {
        super(context);
        this.type = type;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_matriz);
        setCancelable(false);

        llaveVideo = new int[3][3];
        llaveDesVideo = new int[3][3];
        llaveAudio = new int[3][3];
        llaveDesAudio = new int[3][3];
        mat = new Matrix();
        datosEncriptar = DatosEncriptar.getInstance();

        txt00 = findViewById(R.id.dialog_matriz00);
        txt01 = findViewById(R.id.dialog_matriz01);
        txt02 = findViewById(R.id.dialog_matriz02);
        txt10 = findViewById(R.id.dialog_matriz10);
        txt11 = findViewById(R.id.dialog_matriz11);
        txt12 = findViewById(R.id.dialog_matriz12);
        txt20 = findViewById(R.id.dialog_matriz20);
        txt21 = findViewById(R.id.dialog_matriz21);
        txt22 = findViewById(R.id.dialog_matriz22);

        dtxt00 = findViewById(R.id.dialog_matrizd00);
        dtxt01 = findViewById(R.id.dialog_matrizd01);
        dtxt02 = findViewById(R.id.dialog_matrizd02);
        dtxt10 = findViewById(R.id.dialog_matrizd10);
        dtxt11 = findViewById(R.id.dialog_matrizd11);
        dtxt12 = findViewById(R.id.dialog_matrizd12);
        dtxt20 = findViewById(R.id.dialog_matrizd20);
        dtxt21 = findViewById(R.id.dialog_matrizd21);
        dtxt22 = findViewById(R.id.dialog_matrizd22);

        txt00a = findViewById(R.id.dialog_matriz00_audio);
        txt01a = findViewById(R.id.dialog_matriz01_audio);
        txt02a = findViewById(R.id.dialog_matriz02_audio);
        txt10a = findViewById(R.id.dialog_matriz10_audio);
        txt11a = findViewById(R.id.dialog_matriz11_audio);
        txt12a = findViewById(R.id.dialog_matriz12_audio);
        txt20a = findViewById(R.id.dialog_matriz20_audio);
        txt21a = findViewById(R.id.dialog_matriz21_audio);
        txt22a = findViewById(R.id.dialog_matriz22_audio);

        dtxt00a = findViewById(R.id.dialog_matrizd00_audio);
        dtxt01a = findViewById(R.id.dialog_matrizd01_audio);
        dtxt02a = findViewById(R.id.dialog_matrizd02_audio);
        dtxt10a = findViewById(R.id.dialog_matrizd10_audio);
        dtxt11a = findViewById(R.id.dialog_matrizd11_audio);
        dtxt12a = findViewById(R.id.dialog_matrizd12_audio);
        dtxt20a = findViewById(R.id.dialog_matrizd20_audio);
        dtxt21a = findViewById(R.id.dialog_matrizd21_audio);
        dtxt22a = findViewById(R.id.dialog_matrizd22_audio);

        audiom = findViewById(R.id.dialog_matriz_audio);
        videom = findViewById(R.id.dialog_matriz_video);

        btnGenerar = findViewById(R.id.dialog_matriz_btn_generar);
        btnAceptar = findViewById(R.id.dialog_matriz_btn_aceptar);
        btnCancelar = findViewById(R.id.dialog_matriz_btn_cancelar);
        mensaje = findViewById(R.id.dialog_matriz_mensaje);

        matrizEncVideo = findViewById(R.id.layout_matriz_llave);
        matrizDesVideo = findViewById(R.id.layout_matriz_llave_des);

        matrizEncAudio = findViewById(R.id.layout_matriz_llave_audio);
        matrizDesAudio = findViewById(R.id.layout_matriz_llave_des_audio);

        if(type == 1){//Audio
            mensaje.setText(R.string.dialog_matriz_mensaje_volver_generar);
            if(datosEncriptar.getLlaveAudio() != null) {
                llaveAudio = datosEncriptar.getLlaveAudio();
                llenarUIAudio();
                if(datosEncriptar.getLlaveDesAudio() != null){
                    llaveDesAudio = datosEncriptar.getLlaveDesAudio();
                    llenarUIDesAudio();
                }
                else{
                    matrizDesAudio.setVisibility(View.GONE);
                }
            }
            else{
                mensaje.setText(R.string.dialog_matriz_mensaje_generar);
                matrizDesAudio.setVisibility(View.GONE);
                btnAceptar.setEnabled(false);
            }
            matrizEncVideo.setVisibility(View.GONE);
            matrizDesVideo.setVisibility(View.GONE);
            videom.setVisibility(View.GONE);
        }
        else if(type == 2){//Video
            mensaje.setText(R.string.dialog_matriz_mensaje_volver_generar);
            if(datosEncriptar.getLlaveVideo() != null){
                llaveVideo = datosEncriptar.getLlaveVideo();
                llenarUIVideo();
                if(datosEncriptar.getLlaveDesVideo() != null){
                    llaveDesVideo = datosEncriptar.getLlaveDesVideo();
                    llenarUIDesVideo();
                }
                else{
                    matrizEncVideo.setVisibility(View.GONE);
                }
            }
            else{
                mensaje.setText(R.string.dialog_matriz_mensaje_generar);
                matrizDesVideo.setVisibility(View.GONE);
                btnAceptar.setEnabled(false);
            }

            matrizEncAudio.setVisibility(View.GONE);
            matrizDesAudio.setVisibility(View.GONE);
            audiom.setVisibility(View.GONE);
        }
        else if(type == 3){//Todos
            mensaje.setText(R.string.dialog_matriz_mensaje_volver_generar);
            if(datosEncriptar.getLlaveAudio() != null) {
                llaveAudio = datosEncriptar.getLlaveAudio();
                llenarUIAudio();
                if(datosEncriptar.getLlaveDesAudio() != null){
                    llaveDesAudio = datosEncriptar.getLlaveDesAudio();
                    llenarUIDesAudio();
                }
                else{
                    matrizDesAudio.setVisibility(View.GONE);
                }

                if(datosEncriptar.getLlaveVideo() != null){
                    llaveVideo = datosEncriptar.getLlaveVideo();
                    llenarUIVideo();
                    if(datosEncriptar.getLlaveDesVideo() != null){
                        llaveDesVideo = datosEncriptar.getLlaveDesVideo();
                        llenarUIDesVideo();
                    }
                    else{
                        matrizEncVideo.setVisibility(View.GONE);
                    }
                }
                else{
                    mensaje.setText(R.string.dialog_matriz_mensaje_generar);
                    matrizDesVideo.setVisibility(View.GONE);
                    btnAceptar.setEnabled(false);
                }
            }
            else{
                mensaje.setText(R.string.dialog_matriz_mensaje_generar);
                matrizDesAudio.setVisibility(View.GONE);
                btnAceptar.setEnabled(false);
            }
        }

        btnGenerar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keygenerated = true;
                encrypt = true;
                if(type == 1){
                    matrizDesAudio.setVisibility(View.GONE);
                    matrizEncAudio.setOnClickListener(null);
                    btnAceptar.setEnabled(false);
                    btnCancelar.setEnabled(false);
                    btnGenerar.setEnabled(false);
                    boolean generar = true;
                    do{
                        generarLlaveAudio();
                        mat.set_matrix(llaveAudio);
                        mat.set_modulo(modulo);
                        int determinate = mat.get_determinant();
                        if(obtenerMCD(determinate, modulo) == 1){
                            generar = false;
                            Log.i("DET", determinate + "");
                            Log.i("DET", obtenerMCD(determinate, modulo) + "");
                        }
                    }
                    while(generar);

                    mat.print_matrix(llaveAudio);
                    llaveDesAudio = mat.get_inverse();
                    mat.print_matrix(llaveDesAudio);
                    mat.print_matrix(mat.multiply(llaveDesAudio));
                    llenarUIAudio();
                    btnAceptar.setEnabled(true);
                    btnCancelar.setEnabled(true);
                    btnGenerar.setEnabled(true);
                }
                else if(type == 2){
                    matrizDesVideo.setVisibility(View.GONE);
                    matrizEncVideo.setOnClickListener(null);
                    btnAceptar.setEnabled(false);
                    btnCancelar.setEnabled(false);
                    btnGenerar.setEnabled(false);
                    boolean generar = true;
                    do{
                        generarLlaveVideo();
                        mat.set_matrix(llaveVideo);
                        mat.set_modulo(modulo);
                        int determinate = mat.get_determinant();
                        if(obtenerMCD(determinate, modulo) == 1){
                            generar = false;
                            Log.i("DET", determinate + "");
                            Log.i("DET", obtenerMCD(determinate, modulo) + "");
                        }
                    }
                    while(generar);

                    mat.print_matrix(llaveVideo);
                    llaveDesVideo = mat.get_inverse();
                    mat.print_matrix(llaveDesVideo);
                    mat.print_matrix(mat.multiply(llaveDesVideo));
                    llenarUIVideo();
                    btnAceptar.setEnabled(true);
                    btnCancelar.setEnabled(true);
                    btnGenerar.setEnabled(true);
                }
                else if(type == 3){
                    matrizDesAudio.setVisibility(View.GONE);
                    matrizEncAudio.setOnClickListener(null);
                    btnAceptar.setEnabled(false);
                    btnCancelar.setEnabled(false);
                    btnGenerar.setEnabled(false);
                    boolean generar = true;
                    do{
                        generarLlaveAudio();
                        mat.set_matrix(llaveAudio);
                        mat.set_modulo(modulo);
                        int determinate = mat.get_determinant();
                        if(obtenerMCD(determinate, modulo) == 1){
                            generar = false;
                            Log.i("DET", determinate + "");
                            Log.i("DET", obtenerMCD(determinate, modulo) + "");
                        }
                    }
                    while(generar);

                    mat.print_matrix(llaveAudio);
                    llaveDesAudio = mat.get_inverse();
                    mat.print_matrix(llaveDesAudio);
                    mat.print_matrix(mat.multiply(llaveDesAudio));
                    llenarUIAudio();
                    btnAceptar.setEnabled(true);
                    btnCancelar.setEnabled(true);
                    btnGenerar.setEnabled(true);

                    matrizDesVideo.setVisibility(View.GONE);
                    matrizEncVideo.setOnClickListener(null);
                    btnAceptar.setEnabled(false);
                    btnCancelar.setEnabled(false);
                    btnGenerar.setEnabled(false);
                    generar = true;
                    do{
                        generarLlaveVideo();
                        mat.set_matrix(llaveVideo);
                        mat.set_modulo(modulo);
                        int determinate = mat.get_determinant();
                        if(obtenerMCD(determinate, modulo) == 1){
                            generar = false;
                            Log.i("DET", determinate + "");
                            Log.i("DET", obtenerMCD(determinate, modulo) + "");
                        }
                    }
                    while(generar);

                    mat.print_matrix(llaveVideo);
                    llaveDesVideo = mat.get_inverse();
                    mat.print_matrix(llaveDesVideo);
                    mat.print_matrix(mat.multiply(llaveDesVideo));
                    llenarUIVideo();
                    btnAceptar.setEnabled(true);
                    btnCancelar.setEnabled(true);
                    btnGenerar.setEnabled(true);

                }

            }
        });

        btnAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(type == 1){
                    if(keygenerated){
                        datosEncriptar.setLlaveAudio(llaveAudio);
                        datosEncriptar.setLlaveDesAudio(llaveDesAudio);
                    }
                }
                else if(type == 2) {
                    if(keygenerated){
                        datosEncriptar.setLlaveVideo(llaveVideo);
                        datosEncriptar.setLlaveDesVideo(llaveDesVideo);
                    }
                }
                else if(type == 3){
                    if(keygenerated){
                        datosEncriptar.setLlaveAudio(llaveAudio);
                        datosEncriptar.setLlaveDesAudio(llaveDesAudio);
                        datosEncriptar.setLlaveVideo(llaveVideo);
                        datosEncriptar.setLlaveDesVideo(llaveDesVideo);
                    }
                }
                dismiss();
            }
        });

        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                canceled = true;
                dismiss();
            }
        });

        matrizEncAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                matrizDesAudio.setBackgroundResource(R.color.White);
                matrizEncAudio.setBackgroundResource(R.color.colorPrimaryTr);
                encrypt = true;
            }
        });

        matrizDesAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                matrizEncAudio.setBackgroundResource(R.color.White);
                matrizDesAudio.setBackgroundResource(R.color.colorPrimaryTr);
                encrypt = false;
            }
        });

        matrizEncVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                matrizDesVideo.setBackgroundResource(R.color.White);
                matrizEncVideo.setBackgroundResource(R.color.colorPrimaryTr);
                encrypt = true;
            }
        });

        matrizDesVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                matrizEncVideo.setBackgroundResource(R.color.White);
                matrizDesVideo.setBackgroundResource(R.color.colorPrimaryTr);
                encrypt = false;
            }
        });
    }

    public void generarLlaveAudio(){
        for (int i = 0; i < llaveAudio.length; i++){
            for (int j = 0; j < llaveAudio.length; j++){
                llaveAudio[i][j] = (int)(Math.random()*modulo)+1;
            }
        }
    }

    public void generarLlaveVideo(){
        for (int i = 0; i < llaveVideo.length; i++){
            for (int j = 0; j < llaveVideo.length; j++){
                llaveVideo[i][j] = (int)(Math.random()*modulo)+1;
            }
        }
    }

    public void llenarUIVideo(){
        txt00.setText(llaveVideo[0][0] + "");
        txt01.setText(llaveVideo[0][1] + "");
        txt02.setText(llaveVideo[0][2] + "");
        txt10.setText(llaveVideo[1][0] + "");
        txt11.setText(llaveVideo[1][1] + "");
        txt12.setText(llaveVideo[1][2] + "");
        txt20.setText(llaveVideo[2][0] + "");
        txt21.setText(llaveVideo[2][1] + "");
        txt22.setText(llaveVideo[2][2] + "");
    }

    public void llenarUIDesVideo(){
        dtxt00.setText(llaveDesVideo[0][0] + "");
        dtxt01.setText(llaveDesVideo[0][1] + "");
        dtxt02.setText(llaveDesVideo[0][2] + "");
        dtxt10.setText(llaveDesVideo[1][0] + "");
        dtxt11.setText(llaveDesVideo[1][1] + "");
        dtxt12.setText(llaveDesVideo[1][2] + "");
        dtxt20.setText(llaveDesVideo[2][0] + "");
        dtxt21.setText(llaveDesVideo[2][1] + "");
        dtxt22.setText(llaveDesVideo[2][2] + "");
    }

    public void llenarUIAudio(){
        txt00a.setText(llaveAudio[0][0] + "");
        txt01a.setText(llaveAudio[0][1] + "");
        txt02a.setText(llaveAudio[0][2] + "");
        txt10a.setText(llaveAudio[1][0] + "");
        txt11a.setText(llaveAudio[1][1] + "");
        txt12a.setText(llaveAudio[1][2] + "");
        txt20a.setText(llaveAudio[2][0] + "");
        txt21a.setText(llaveAudio[2][1] + "");
        txt22a.setText(llaveAudio[2][2] + "");
    }

    public void llenarUIDesAudio(){
        dtxt00a.setText(llaveDesAudio[0][0] + "");
        dtxt01a.setText(llaveDesAudio[0][1] + "");
        dtxt02a.setText(llaveDesAudio[0][2] + "");
        dtxt10a.setText(llaveDesAudio[1][0] + "");
        dtxt11a.setText(llaveDesAudio[1][1] + "");
        dtxt12a.setText(llaveDesAudio[1][2] + "");
        dtxt20a.setText(llaveDesAudio[2][0] + "");
        dtxt21a.setText(llaveDesAudio[2][1] + "");
        dtxt22a.setText(llaveDesAudio[2][2] + "");
    }

    public int obtenerMCD(int a, int b){
        if(b == 0){
            return a;
        }
        else{
            return obtenerMCD(b, a%b);
        }
    }

    public boolean getCancelled(){
        return canceled;
    }

    public boolean getEncrypt(){return encrypt;}
}
