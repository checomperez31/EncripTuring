package com.turing.encripturing;

/**
 * Created by smp_3 on 03/12/2017.
 */

public class DatosEncriptar {
    private static DatosEncriptar ourInstance = new DatosEncriptar();
    int llave[][];
    int llaveDes[][];

    public static synchronized DatosEncriptar getInstance() {
        if(ourInstance == null){
            ourInstance = new DatosEncriptar();
        }
        return ourInstance;
    }

    private DatosEncriptar() {}

    public int[][] getLlave(){return llave;}

    public int[][] getLlaveDes(){return llaveDes;}

    public void setLlave(int[][] llave){this.llave = llave;}

    public void setLlaveDes(int[][] llaveDes){this.llaveDes = llaveDes;}
}
