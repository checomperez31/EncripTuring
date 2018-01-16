package com.turing.encripturing;

/**
 * Created by smp_3 on 03/12/2017.
 */

public class DatosEncriptar {
    private static DatosEncriptar ourInstance = new DatosEncriptar();
    int llaveVideo[][];
    int llaveDesVideo[][];
    int llaveAudio[][];
    int llaveDesAudio[][];

    public static synchronized DatosEncriptar getInstance() {
        if(ourInstance == null){
            ourInstance = new DatosEncriptar();
        }
        return ourInstance;
    }

    private DatosEncriptar() {}

    public int[][] getLlaveVideo() {
        return llaveVideo;
    }

    public void setLlaveVideo(int[][] llaveVideo) {
        this.llaveVideo = llaveVideo;
    }

    public int[][] getLlaveDesVideo() {
        return llaveDesVideo;
    }

    public void setLlaveDesVideo(int[][] llaveDesVideo) {
        this.llaveDesVideo = llaveDesVideo;
    }

    public int[][] getLlaveAudio() {
        return llaveAudio;
    }

    public void setLlaveAudio(int[][] llaveAudio) {
        this.llaveAudio = llaveAudio;
    }

    public int[][] getLlaveDesAudio() {
        return llaveDesAudio;
    }

    public void setLlaveDesAudio(int[][] llaveDesAudio) {
        this.llaveDesAudio = llaveDesAudio;
    }
}
