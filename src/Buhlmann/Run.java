package Buhlmann;

import java.util.ArrayList;

public class Run{
    private static final int ascentRate = 10;
    private static final int descentRate = 20;
    private static final double meterToBar = 0.09985;
    private static boolean lastStop6m = false;

    private ArrayList<GasMix> gasList = new ArrayList<>();
    private ArrayList<DecoStop> decoStops = new ArrayList<>();

    // TODO: Function to plan dive once given the max depth to dive to and time spent at the bottom depth (in minutes)
    public static void plan(double maxDepth, int bottomTime){ }

    // Checks dive steps needed to ascent from the current depth to the surface
    public static void diveAscent(double absolutePressure, ArrayList<GasMix> gasList){
        GasMix bottomGas = gasList.get(0);
        
    }

    public static void NDL(double absolutePressure, GasMix gas){

    }
    public static void main(String args[]){
        Tests.testingDiveProfile();
        System.out.println();
        Tests.testingCeiling();
        System.out.println();
        Tests.testingDecoStop();
    }
}
