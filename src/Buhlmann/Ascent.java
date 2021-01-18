package Buhlmann;


/**
 * Calculates dive steps required to ascent from current depth to the surface
 */
public class Ascent {
    public static final int surfacePressure = ZHL16.surfacePressure;
    public static final int ascentRate = 10;
    public static final int descentRate = 20;
    public static final double meterToBar = 0.09985;

    public static boolean lastStop6m = false;

    // TODO: Complete
    // Checks if the dive is a non-decompression Dive (no deco stops needed)
    public static int ndlDive(double pressure){
        double gradientFactor = ZHL16.gfHigh;
        double p = pressure - ZHL16.surfacePressure;
        double time = p / ascentRate / meterToBar;
        return 0;
    }

    // TODO: Complete
    // Calculates dive steps needed to ascent from current depth to the surface
    public static void diveAscent(){ }

    // TODO: Complete
    // Calculate depth of first deco stop
    public static void firstStop(double currentDepth, double targetDepth, double[] pressures){
        double ceiling = ceilingLimit(pressures);
        ceiling = Math.ceil(ceiling / 3) * 3;
        ceiling = Math.max(ceiling, targetDepth);
    }

    // TODO: Complete
    // Calculates the length of the decompression stop
    public static int decoStopLength(){
        int t = 0;
        int t_s = 0;
        int dt = 64;

        return t;
    }

    // Calculates the number of decompression stops needed from start to end depths
    public static int numberStops(double startPressure, double finalPressure){
        int stops = (int) ((startPressure - finalPressure) / (3 * meterToBar));
        return stops;
    }

    //
    public static double ceilingLimit(double[] data){
        double max = data[0];

        for(double p: data){
            max = Math.max(p, max);
        }

        return max;
    }

    public static void main(String[] args){
        double[] max = new double[]{1, 2.3, 5, 6, 4, 2.9};
        System.out.println(ceilingLimit(max));
    }
}
