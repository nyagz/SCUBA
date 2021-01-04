package BuhlmannEquation;


import java.util.ArrayList;

/**
 * Calculates dive steps required to ascent from current depth to the surface
 */
public class Acsent {
    public static final int surfacePressure = Buhlmann.surfacePressure;
    public static final int ascentRate = 10;
    public static final int descentRate = 20;

    public static boolean lastStop6m = false;

    // TODO: Complete
    public static void ndl(){
        double gradientFactor = Buhlmann.gfHigh;

        // return null;
    }

    // TODO: Complete
    public static void diveAscent(){ }

    // TODO: Complete
    public static void firstStop(){ }

    // TODO: Complete
    public static int decoStopLength(){
        int t = 0;
        int t_s = 0;
        int dt = 64;

        return t;
    }

    //TODO
    public static void numberStops(){ }

    public static void main(String[] args){

    }
}
