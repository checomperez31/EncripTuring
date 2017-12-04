package com.turing.encripturing;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
/**
 * Created by smp_3 on 03/12/2017.
 */

public class DialogLlaves extends Dialog{
    private TextView txt00, txt01, txt02, txt10, txt11, txt12, txt20, txt21, txt22;
    private Button btnGenerar, btnAceptar;
    int[][] llave;

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

        txt00 = findViewById(R.id.dialog_matriz00);
        txt01 = findViewById(R.id.dialog_matriz01);
        txt02 = findViewById(R.id.dialog_matriz02);
        txt10 = findViewById(R.id.dialog_matriz10);
        txt11 = findViewById(R.id.dialog_matriz11);
        txt12 = findViewById(R.id.dialog_matriz12);
        txt20 = findViewById(R.id.dialog_matriz20);
        txt21 = findViewById(R.id.dialog_matriz21);
        txt22 = findViewById(R.id.dialog_matriz22);

        btnGenerar = findViewById(R.id.btn_generar_matriz);
        btnAceptar = findViewById(R.id.btn_aceptar_matriz);

        btnGenerar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Matrix mat = new Matrix();
                boolean generar = true;
                do{
                    generarLlave();
                    mat.set_matrix(llave);
                    mat.set_modulo(129);
                    int determinate = mat.get_determinant();
                    if(obtenerMCD(determinate, 129) == 1){
                        generar = false;
                        Log.i("DET", determinate + "");
                        Log.i("DET", obtenerMCD(determinate, 129) + "");
                    }
                }
                while(generar);
                DatosEncriptar datosEncriptar = DatosEncriptar.getInstance();
                datosEncriptar.setLlave(llave);
                mat.print_matrix(llave);
                datosEncriptar.setLlaveDes(mat.get_inverse());
                mat.print_matrix(datosEncriptar.getLlaveDes());
                mat.print_matrix(mat.multiply(datosEncriptar.getLlaveDes()));
                llenarUI();

            }
        });

        btnAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    public void generarLlave(){
        for (int i = 0; i < llave.length; i++){
            for (int j = 0; j < llave.length; j++){
                llave[i][j] = (int)(Math.random()*128)+1;
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

    public int obtenerMCD(int a, int b){
        if(b == 0){
            return a;
        }
        else{
            return obtenerMCD(b, a%b);
        }
    }
}
