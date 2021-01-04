package BuhlmannEquation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Buhlmann {

    public static final double startP_he = 0;
    public static final double startP_N2 = 0.7902; //Starting pressure of N2
    public static final double waterVapourPressure = 0.0627;
    public static final int surfacePressure = 1;

    public static final double f_gas = 0.68; //EAN32

    // gf
    public static double gfLow = 0.3;
    public static double gfHigh = 0.85;

    /**
     * Used to calculate inert gas pressure in a tissue compartment
     * Function for ascent and descent gas loading calculations
     * P = P_alv + R * (t - 1/k) - (P_alv - P_i - R/k) * e^(-k*t)
     * Change of 10m depth = change of 1 bar pressure
     */
    public static double schreiner(double initialPressure, double inspiredGasPressure, double exposureTime, double gasDecayConstant, double rateChangeGas){
        return inspiredGasPressure + rateChangeGas * (exposureTime - 1 / gasDecayConstant) - (inspiredGasPressure - initialPressure - rateChangeGas/gasDecayConstant) * Math.exp(-1 * gasDecayConstant * exposureTime);
    }

    /**
     *
     * @param schreinerPressure - pressures in each compartment from schreiner's equation
     * @return
     */
    public static double[] buhlmannP_l(double[] schreinerPressure){
        double[] p_l = new double[16];
        double[] pressure = new double[16];
        double[] a = new double[16];
        double[] b = new double[16];

        for (int i = 0; i < 16; i++){
            pressure[i] = schreinerPressure[i] + startP_he;
            a[i] = (ZHL16BGF.N2_A[i] * schreinerPressure[i] + ZHL16BGF.He_A[i] * startP_he) / pressure[i];
            b[i] = (ZHL16BGF.N2_B[i] * schreinerPressure[i] + ZHL16BGF.He_B[i] * startP_he) / pressure[i];
            p_l[i] = buhlmannEquation(pressure[i], a[i], b[i], gfLow);
        }

        return p_l;
    }

    /**
     * Calculates the ascent ceiling in a tissue compartment
     * P_l=(P−A∗gf)/(gf/B+1.0−gf)
     * where p_l is the ascent ceiling
     */
    public static double buhlmannEquation(double pressure, double coefA, double coefB, double gf){
        return (pressure - coefA * gf)/(gf / coefB + 1 -gf);
    }

    /**
     * Assumptions being made:
     *     - surface pressure is 1 bar
     *     - 10m change is 1 bar pressure
     *
     * @param initialDepth
     * @param finalDepth
     * @param rate
     * @param t_hl
     * @return
     */
    public static double[] planDescent(int initialDepth, int finalDepth, int rate, double[] t_hl){
        // Current implementation has made assumptions for f_gas
        double[] k = new double[16];
        double[] pressure = new double[16];

        double p_i, p_alv, r;
        int p_abs = (surfacePressure + (initialDepth / 10));
        int p_rate = (rate / 10); //Should be positive
        double t = (double) (finalDepth - initialDepth) / rate;

        p_i = startP_N2 * (surfacePressure - waterVapourPressure);
        p_alv = f_gas * (p_abs - waterVapourPressure);
        r = f_gas * p_rate;
        for (int i = 0; i < 16; i++){
            k[i] = Math.log(2) / t_hl[i];
        }

        for (int i = 0; i < 16; i++){
            pressure[i] = schreiner(p_i, p_alv, t, k[i], r);
        }

        return pressure;
    }


    /**
     * Calculates the pressure while diving at the deepest point of the dive
     * p_rate should be zero?
     * @param depth
     * @param time
     * @param initialPressure
     * @param t_hl
     * @return
     */
    public static double[] planDive(int depth, int time, double[] initialPressure, double[] t_hl){
        double[] pressure = new double[16];
        double[] k = new double[16];

        int p_abs = 1 + (depth / 10);
        double p_alv;

        p_alv = f_gas * (p_abs - waterVapourPressure);
        for (int i = 0; i < 16; i++){
            k[i] = Math.log(2) / t_hl[i];
        }

        for (int i = 0; i < 16; i++){
            pressure[i] = schreiner(initialPressure[i], p_alv, time, k[i], 0);
        }
        return pressure;
    }

    /**
     * @param initialDepth
     * @param finalDepth
     * @param rate - rate at which we plan to ascent
     * @param initialPressure - pressure in each compartment at the end of the dive (before we start the ascent)
     * @param t_hl - inert gas half life
     * @return
     */
    public static double[] planAscent(int initialDepth, int finalDepth, int rate, double[] initialPressure, double[] t_hl){
        double[] pressure = new double[16];
        double[] k = new double[16];

        int p_abs = surfacePressure + (initialDepth / 10);
        double p_alv, r;

        double t = (double) (initialDepth - finalDepth) / rate;

        int p_rate = rate / 10; //Should be negative
        if (p_rate >= 0){
            p_rate = p_rate * -1;
        }
        r = f_gas * p_rate;
        p_alv = f_gas * (p_abs - waterVapourPressure);
        for (int i = 0; i < 16; i++){
            k[i] = Math.log(2) / t_hl[i];
        }
        for (int i = 0; i < 16; i++){
            pressure[i] = schreiner(initialPressure[i], p_alv, t, k[i], r);
        }

        return pressure;
    }

    /**
     * Calculate:
     * - pressure of the ascent ceiling of a diver
     * - depth of first decompression stop: a diver cannot ascent from the bottom shallower than ascent ceiling
     * - length of decompression stop: a diver cannot ascent from decompression stop until depth of ascent ceiling decreases
     */
    public static void ceilingLimit(){ }

    /** Dives are planned using ZH-L16B-GF since this is what's used for dive tables*/
    public static Map<String, double[]> planDive(){
        Map<String, double[]> plan = new HashMap<>();
        double[] descentPressure;
        double[] divePressure;
        double[] ascentPressure;

        descentPressure = planDescent(0, 30, 20, ZHL16BGF.N2_halfLife);
        divePressure = planDive(30, 20, descentPressure, ZHL16BGF.N2_halfLife);
        ascentPressure = planAscent(30, 10, 10, divePressure, ZHL16BGF.N2_halfLife);

        plan.put("descent", descentPressure);
        plan.put("dive", divePressure);
        plan.put("ascent", ascentPressure);

        return plan;
    }

    public static void main(String args[]){
        // Planning a dive
        Map<String, double[]> plan = new HashMap<>();
        plan = planDive();

        System.out.println("For the first compartment");
        System.out.println("Descent pressure: " + plan.get("descent")[0]);
        System.out.println("Dive pressure: " + plan.get("dive")[0]);
        System.out.println("Ascent pressure: " + plan.get("ascent")[0]);

        double[] descentTest = buhlmannP_l(plan.get("descent"));
        double[] diveTest = buhlmannP_l(plan.get("dive"));
        double[] ascentTest = buhlmannP_l(plan.get("ascent"));

        System.out.println();
        System.out.println("Descent buhlmann: " + descentTest[0]);
        System.out.println("Dive buhlmann: " + diveTest[0]);
        System.out.println("Ascent buhlmann: " + ascentTest[0]);

        // int compartment = 1;
        // for (double p: ascentPressure){
        //     System.out.println("Compartment " + compartment + ": " + p);
        //     compartment++;
        // }
    }
}