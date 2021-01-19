package Buhlmann;

import javafx.util.Pair;

import java.util.ArrayList;

public class ZHL16 {
    /** From BuhlmannEquation.Buhlmann
    public static final double startP_he = 0;
    public static final double startP_N2 = 0.7902; // Starting pressure of N2
    public static final double waterVapourPressure = 0.0627;
    public static final int surfacePressure = 1;
    public static final double surface_Pressure = 0.09985; //Check which value should be being used

    public static final double f_gas = 0.68; // EAN32

    // Gradient factors
    public static final double gfLow = 0.3;
    public static final double gfHigh = 0.85;
     */

    /** From BuhlmannEquation.Ascent
     * public static final int surfacePressure = Buhlmann.surfacePressure;
     *     public static final int ascentRate = 10;
     *     public static final int descentRate = 20;
     *     public static final double meterToBar = 0.09985;
     *
     *     public static boolean lastStop6m = false;
     */

    public static final double waterVapourPressure = 0.0627;
    public static final int surfacePressure = 1;
    public static final double startP_he = 0;
    public static final double startP_N2 = 0.7902;

    public static final double f_gas = 0.68; // EAN32

    // Gradient factors
    public static final double gfLow = 0.3;
    public static final double gfHigh = 0.85;

    public static final int ascentRate = 10;
    public static final int descentRate = 20;
    public static final double meterToBar = 0.09985;

    public static boolean lastStop6m = false;
    // public static DecoStopTable deco_table = new DecoStopTable();
    public static ArrayList<DecoStop> deco_stops = new ArrayList<>();

    // Loads tissue compartments with gas specified in the gasMix
    public static TissueLoader tissueLoaders(double absolutePressure, GasMix gas, double pressureRateChange, double time){
        double n2_loader, he_loader;
        n2_loader = tissueLoader(absolutePressure, gas.getN2() / 100, pressureRateChange, ZHL16BGF.N2_halfLife, time);
        he_loader = tissueLoader(absolutePressure, gas.getHe() / 100, pressureRateChange, ZHL16BGF.He_halfLife, time);
        return new TissueLoader(n2_loader, he_loader);
    }

    // TODO: Sort out the return
    // Function to load tissue compartments with inert gas using Schreiner's equation
    public static double tissueLoader(double absolutePressure, double f_gas, double pressureRateChange, double[] halfLife, double time){
        double[] k = new double[16];
        double p_alv, r, p_i;

        p_i = startP_N2 * (surfacePressure - waterVapourPressure);
        p_alv = f_gas * (absolutePressure - waterVapourPressure);
        // k
        r = f_gas * pressureRateChange;
        for (int i = 0; i < halfLife.length; i++){
            k[i] = Math.log(2) / halfLife[i];
            Equations.schreiner(p_i, p_alv, time, k[i], r);
        }

        return 0;
    }

    //TODO: Determine what's being returned and when I need to actually use this
    public static void loadTissues(double absolutePressure, double time, GasMix gas, double pressureRate, Object model){
        double n2Loader, heLoader;
        TissueLoader tissues = tissueLoaders(absolutePressure, gas, pressureRate, time);
        n2Loader = tissues.getN2Loader();
        heLoader = tissues.getHeLoader();

        //return something
    }

    // FIXME: Delete later
    // Just to check I know how to code tbh
    public static void testingDecoStop(){

        DecoStop test = new DecoStop(3,2);
        System.out.println("Checking it's empty");
        if (deco_stops.size() == 0){
            System.out.println("It's empty, good");
        } else{
            System.out.println("Oh no, it didn't work");
        }

        deco_stops.add(test);
        DecoStop test2 = new DecoStop(2, 3);
        deco_stops.add(test2);
        System.out.println("Total length of decompression stops: " + DecoStop.totalDecoStops(deco_stops));
        System.out.println("Added");
        System.out.println("New total number of decompression stops: " + deco_stops.size());

        DecoStop firstStop = deco_stops.get(0);
        System.out.println("Depth of first stop: " + firstStop.getDepth());
        System.out.println("Min of first stop: " + firstStop.getMin());
    }

    public static void main(String args[]){
        testingDecoStop();

        // if(Equations.buhlmannEquation(0.74065446, 0, 1.1696, 0.5578, 0, 0, 0.3) == 0.31488600902007363){
        //     System.out.println("It works!");
        // } else {
        //     System.out.println("It no works :(");
        // }

    }


}
