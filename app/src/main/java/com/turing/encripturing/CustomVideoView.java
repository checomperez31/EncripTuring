package com.turing.encripturing;

import android.widget.VideoView;
import android.content.Context;
import android.util.*;
/**
 * Created by Peludo on 05/11/2017.
 * Clase de VideoView modificada, ésta hereda de VideoView
 * para poder implementar los listener para cuando se ponga
 * pause o play al video
 */

public class CustomVideoView extends VideoView {

    private PlayPauseListener mListener;

    public CustomVideoView(Context context){
        super(context);
    }

    public CustomVideoView(Context context, AttributeSet attrs){
        super(context,attrs);
    }

    public CustomVideoView(Context context, AttributeSet attrs, int defStyle){
        super(context,attrs, defStyle);
    }

    public void setPlayPauseListener(PlayPauseListener listener){
        mListener=listener;
    }

    @Override
    public void pause(){
        super.pause();
        if(mListener != null){
            mListener.onPause();
        }
    }

    @Override
    public void start(){
        super.start();
        if(mListener != null){
            mListener.onPlay();
        }
    }

    public static interface PlayPauseListener{
        void onPlay();
        void onPause();
    }
}
