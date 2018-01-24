package com.turing.encripturing;

import android.graphics.Bitmap;

/**
 * Created by Alan Malagón on 24/01/2018.
 */

public class SingletonHistogramas {
        private static SingletonHistogramas ourInstance = new SingletonHistogramas();
        private Bitmap bmImgColor, bmImgBN, bmHistograma, bmHistogramaBN;

        public static synchronized SingletonHistogramas getInstance() {
            if(ourInstance == null){
                ourInstance = new SingletonHistogramas();
            }
            return ourInstance;
        }

    private SingletonHistogramas() {}

    public Bitmap getBmImgColor() {
        return bmImgColor;
    }

    public void setBmImgColor(Bitmap bmImgColor) {
        this.bmImgColor = bmImgColor;
    }

    public Bitmap getBmImgBN() {
        return bmImgBN;
    }

    public void setBmImgBN(Bitmap bmImgBN) {
        this.bmImgBN = bmImgBN;
    }

    public Bitmap getBmHistograma() {
        return bmHistograma;
    }

    public void setBmHistograma(Bitmap bmHistograma) {
        this.bmHistograma = bmHistograma;
    }

    public Bitmap getBmHistogramaBN() {
        return bmHistogramaBN;
    }

    public void setBmHistogramaBN(Bitmap bmHistogramaBN) {
        this.bmHistogramaBN = bmHistogramaBN;
    }
}
