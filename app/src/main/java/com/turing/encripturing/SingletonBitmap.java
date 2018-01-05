package com.turing.encripturing;

import android.graphics.Bitmap;
import android.util.Log;

/**
 * Created by Alan Malagón on 05/01/2018.
 */

public class SingletonBitmap {
        private static SingletonBitmap ourInstance = new SingletonBitmap();
        private Bitmap bm;

        public static synchronized SingletonBitmap getInstance() {
            if(ourInstance == null){
                ourInstance = new SingletonBitmap();
            }
            return ourInstance;
        }

        private SingletonBitmap() {}

        public Bitmap getBm(){return bm;}

        public void setBm(Bitmap bm){this.bm = bm;}
}
