package com.turing.encripturing;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

/**
 * Created by smp_3 on 11/01/2018.
 */

public class DialogHistogramas extends Dialog {

    private Button btnAceptar, btnCancelar;
    private ImageView imgViewColor, imgViewBN, imgViewHistColor, imgViewHistBN;
    private Bitmap bmImgColor, bmImgBN, bmHistograma, bmHistogramaBN;


    public DialogHistogramas(Context context){
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_histogramas);
        setCancelable(true);
        btnAceptar = findViewById(R.id.dialog_histogramas_btn_aceptar);
        btnCancelar = findViewById(R.id.dialog_histogramas_btn_cancelar);
        imgViewColor = findViewById(R.id.dialog_histogramas_img_original);
        imgViewBN = findViewById(R.id.dialog_histogramas_img_bn);
        imgViewHistColor = findViewById(R.id.dialog_histogramas_hist_original);
        imgViewHistBN = findViewById(R.id.dialog_histogramas_hist_bn);

        bmImgColor = SingletonHistogramas.getInstance().getBmImgColor();
        bmImgBN = SingletonHistogramas.getInstance().getBmImgBN();
        bmHistograma = SingletonHistogramas.getInstance().getBmHistograma();
        bmHistogramaBN = SingletonHistogramas.getInstance().getBmHistogramaBN();

        if(bmImgColor != null && bmImgBN != null && bmHistograma !=null && bmHistogramaBN != null)
        {
            imgViewColor.setImageBitmap(bmImgColor);
            imgViewBN.setImageBitmap(bmImgBN);
            imgViewHistColor.setImageBitmap(bmHistograma);
            imgViewHistBN.setImageBitmap(bmHistogramaBN);
        }

        btnAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
