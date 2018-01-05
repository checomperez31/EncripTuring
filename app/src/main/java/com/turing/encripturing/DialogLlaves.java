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
    private TextView txt00, txt01, txt02, txt10, txt11, txt12, txt20, txt21, txt22, mensaje;
    private TextView dtxt00, dtxt01, dtxt02, dtxt10, dtxt11, dtxt12, dtxt20, dtxt21, dtxt22;
    private Button btnGenerar, btnAceptar, btnCancelar;
    int[][] llave, llaveDes;
    private Matrix mat;
    DatosEncriptar datosEncriptar;
    private boolean canceled = false, keygenerated = false, encrypt = true;
    private RelativeLayout matrizEnc, matrizDes;
    private int modulo = 128;

    public void setModulo(int modulo) {
        this.modulo = modulo;
        Log.i("RGB", "modulo: " + modulo);
    }

    public DialogLlaves(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_matriz);
        setCancelable(false);

        llave = new int[3][3];
        llaveDes = new int[3][3];
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

        btnGenerar = findViewById(R.id.dialog_matriz_btn_generar);
        btnAceptar = findViewById(R.id.dialog_matriz_btn_aceptar);
        btnCancelar = findViewById(R.id.dialog_matriz_btn_cancelar);
        mensaje = findViewById(R.id.dialog_matriz_mensaje);

        matrizEnc = findViewById(R.id.layout_matriz_llave);
        matrizDes = findViewById(R.id.layout_matriz_llave_des);

        if(datosEncriptar.getLlave() != null){
            mensaje.setText(R.string.dialog_matriz_mensaje_volver_generar);
            llave = datosEncriptar.getLlave();
            llenarUI();
            if(datosEncriptar.getLlaveDes() != null){
                llaveDes = datosEncriptar.getLlaveDes();
                llenarUIDes();
            }
            else{
                matrizDes.setVisibility(View.GONE);
            }
        }
        else{
            mensaje.setText(R.string.dialog_matriz_mensaje_generar);
            matrizDes.setVisibility(View.GONE);
            btnAceptar.setEnabled(false);
        }

        btnGenerar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keygenerated = true;
                encrypt = true;
                matrizDes.setVisibility(View.GONE);
                matrizEnc.setOnClickListener(null);
                btnAceptar.setEnabled(false);
                btnCancelar.setEnabled(false);
                btnGenerar.setEnabled(false);
                boolean generar = true;
                do{
                    generarLlave();
                    mat.set_matrix(llave);
                    mat.set_modulo(modulo);
                    int determinate = mat.get_determinant();
                    if(obtenerMCD(determinate, modulo) == 1){
                        generar = false;
                        Log.i("DET", determinate + "");
                        Log.i("DET", obtenerMCD(determinate, modulo) + "");
                    }
                }
                while(generar);

                mat.print_matrix(llave);
                llaveDes = mat.get_inverse();
                mat.print_matrix(llaveDes);
                mat.print_matrix(mat.multiply(llaveDes));
                llenarUI();
                btnAceptar.setEnabled(true);
                btnCancelar.setEnabled(true);
                btnGenerar.setEnabled(true);
            }
        });

        btnAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(keygenerated){
                    datosEncriptar.setLlave(llave);
                    datosEncriptar.setLlaveDes(llaveDes);
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

        matrizEnc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                matrizDes.setBackgroundResource(R.color.White);
                matrizEnc.setBackgroundResource(R.color.colorPrimaryTr);
                encrypt = true;
            }
        });

        matrizDes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                matrizEnc.setBackgroundResource(R.color.White);
                matrizDes.setBackgroundResource(R.color.colorPrimaryTr);
                encrypt = false;
            }
        });
    }

    public void generarLlave(){
        for (int i = 0; i < llave.length; i++){
            for (int j = 0; j < llave.length; j++){
                llave[i][j] = (int)(Math.random()*modulo)+1;
            }
        }
    }

    public void llenarUI(){
        txt00.setText(llave[0][0] + "");
        txt01.setText(llave[0][1] + "");
        txt02.setText(llave[0][2] + "");
        txt10.setText(llave[1][0] + "");
        txt11.setText(llave[1][1] + "");
        txt12.setText(llave[1][2] + "");
        txt20.setText(llave[2][0] + "");
        txt21.setText(llave[2][1] + "");
        txt22.setText(llave[2][2] + "");
    }

    public void llenarUIDes(){
        dtxt00.setText(llaveDes[0][0] + "");
        dtxt01.setText(llaveDes[0][1] + "");
        dtxt02.setText(llaveDes[0][2] + "");
        dtxt10.setText(llaveDes[1][0] + "");
        dtxt11.setText(llaveDes[1][1] + "");
        dtxt12.setText(llaveDes[1][2] + "");
        dtxt20.setText(llaveDes[2][0] + "");
        dtxt21.setText(llaveDes[2][1] + "");
        dtxt22.setText(llaveDes[2][2] + "");
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
