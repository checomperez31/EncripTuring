package com.turing.encripturing;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;

/**
 * Created by smp_3 on 11/01/2018.
 */

public class DialogHistogramas extends Dialog {

    public DialogHistogramas(Context context){
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_histogramas);
        setCancelable(true);
    }
}
