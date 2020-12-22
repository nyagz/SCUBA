import java.util.Arrays;

public class Buhlmann {

    private static double[][] ZHL16ATissues;
    private static double[][] ZHL16BTissues;
    private static double[][] ZHL16CTissues;

    public Buhlmann(){
        /*
        All tissues are in form:
        N^2 half time (mins), N^2 A value, N^2 B value, He half time (mins), He A value, He B value
         */
        // ZHL16A is the initial version of Buhlmann decompression model
        ZHL16ATissues = new double[][] {
                {4.0, 1.2599, 0.5050, 1.51, 1.7424, 0.4245},
                {8.0, 1.0000, 0.6514, 3.02, 1.3830, 0.5747},
                {12.5, 0.8618, 0.7222, 4.72, 1.1919, 0.6527},
                {18.5, 0.7562, 0.7725, 6.99, 1.0458, 0.7223},
                {27.0, 0.6667, 0.8125, 10.21, 0.9220, 0.7582},
                {38.3, 0.5933, 0.8434, 14.48, 0.8205, 0.7957},
                {54.3, 0.5282, 0.8693, 20.53, 0.7305, 0.8279},
                {77.0, 0.4701, 0.8910, 29.11, 0.6502, 0.8553},
                {109.0, 0.4187, 0.9092, 41.20, 0.5950, 0.8757},
                {146.0, 0.3798, 0.9222, 55.19, 0.5545, 0.8903},
                {187.0, 0.3497, 0.9319, 70.69, 0.5333, 0.8997},
                {239.0, 0.3223, 0.9403, 90.34, 0.5189, 0.9073},
                {305.0, 0.2971, 0.9477, 115.29, 0.5181, 0.9122},
                {390.0, 0.2737, 0.9544, 147.42, 0.5176, 0.9171},
                {498.0, 0.2523, 0.9602, 188.24, 0.5172, 0.9217},
                {635.0, 0.2327, 0.9653, 240.03, 0.5119, 0.9267}
        };

        // ZHL16B is a modification used for dive table calculations
        ZHL16BTissues = new double[][] {
                {4.0, 1.2599, 0.5050, 1.51, 1.7424, 0.4245},
                {8.0, 1.0000, 0.6514, 3.02, 1.3830, 0.5747},
                {12.5, 0.8618, 0.7222, 4.72, 1.1919, 0.6527},
                {18.5, 0.7562, 0.7725, 6.99, 1.0458, 0.7223},
                {27.0, 0.6667, 0.8125, 10.21, 0.9220, 0.7582},
                {38.3, 0.5600, 0.8434, 14.48, 0.8205, 0.7957},
                {54.3, 0.4947, 0.8693, 20.53, 0.7305, 0.8279},
                {77.0, 0.4500, 0.8910, 29.11, 0.6502, 0.8553},
                {109.0, 0.4187, 0.9092, 41.20, 0.5950, 0.8757},
                {146.0, 0.3798, 0.9222, 55.19, 0.5545, 0.8903},
                {187.0, 0.3497, 0.9319, 70.69, 0.5333, 0.8997},
                {239.0, 0.3223, 0.9403, 90.34, 0.5189, 0.9073},
                {305.0, 0.2971, 0.9477, 115.29, 0.5181, 0.9122},
                {390.0, 0.2737, 0.9544, 147.42, 0.5176, 0.9171},
                {498.0, 0.2523, 0.9602, 188.24, 0.5172, 0.9217},
                {635.0, 0.2327, 0.9653, 240.03, 0.5119, 0.9267}
        };

        // ZHL16C is the version used in most dive computers
        ZHL16CTissues = new double[][] {
                {4.0, 1.2599, 0.5050, 1.51, 1.7424, 0.4245},
                {8.0, 1.0000, 0.6514, 3.02, 1.3830, 0.5747},
                {12.5, 0.8618, 0.7222, 4.72, 1.1919, 0.6527},
                {18.5, 0.7562, 0.7725, 6.99, 1.0458, 0.7223},
                {27.0, 0.6200, 0.8125, 10.21, 0.9220, 0.7582},
                {38.3, 0.5043, 0.8434, 14.48, 0.8205, 0.7957},
                {54.3, 0.4410, 0.8693, 20.53, 0.7305, 0.8279},
                {77.0, 0.4000, 0.8910, 29.11, 0.6502, 0.8553},
                {109.0, 0.3750, 0.9092, 41.20, 0.5950, 0.8757},
                {146.0, 0.3500, 0.9222, 55.19, 0.5545, 0.8903},
                {187.0, 0.3295, 0.9319, 70.69, 0.5333, 0.8997},
                {239.0, 0.3065, 0.9403, 90.34, 0.5189, 0.9073},
                {305.0, 0.2835, 0.9477, 115.29, 0.5181, 0.9122},
                {390.0, 0.2610, 0.9544, 147.42, 0.5176, 0.9171},
                {498.0, 0.2480, 0.9602, 188.24, 0.5172, 0.9217},
                {635.0, 0.2327, 0.9653, 240.03, 0.5119, 0.9267}
        };
    }

    /**
     * Used to calculate inert gas pressure in a tissue compartment
     * Function for ascent and descent gas loading calculations
     * P = P_alv + R * (t - 1/k) - (P_alv - P_i - R/k) * e^(-k*t)
     * Change of 10m depth = change of 1 bar pressure
     */
    public static double schreiner(double initialPressure, double inspiredGasPressure, double exposureTime, double gasDecayConstant, double rateChangeGas){
        return inspiredGasPressure + rateChangeGas * (exposureTime - 1 / gasDecayConstant) - (inspiredGasPressure - initialPressure - rateChangeGas/gasDecayConstant) * Math.exp(-1 * gasDecayConstant * exposureTime);
    }

    public static double[] planDescent(int initialDepth, int finalDepth, int rate, double[] t_hl){
        // Current implementation has made assumptions for p_wvp & f_gas
        double p_i, p_alv, r;
        int p_abs = (int) (1 + (initialDepth / 10));
        double f_gas = 0.68;
        double p_wvp = 0.0627;
        int p_rate = (int) (rate / 10);
        double t = (finalDepth - initialDepth) / rate;
        double[] k = new double[16];
        double[] pressure = new double[16];

        p_i = 0.7902 * (1 - p_wvp);
        p_alv = f_gas * (p_abs - p_wvp);
        r = f_gas * p_rate;
        for (int i = 0; i < 16; i++){
            k[i] = Math.log(2) / t_hl[i];
        }
//
        for (int i = 0; i < 16; i++){
            pressure[i] = schreiner(p_i, p_alv, t, k[i], r);
        }
        // System.out.println(k[1]);
        return pressure;
    }

    // Calculates the pressure will diving at the deepest point of the dive
    public static double[] planDive(int depth, int time, double[] initialPressure, double[] t_hl){
        double[] pressure = new double[16];
        double[] k = new double[16];

        int p_abs = 1 + ((int) depth / 10);
        double p_alv;
        double f_gas = 0.68;
        double p_wvp = 0.0627;

        p_alv = f_gas * (p_abs - p_wvp);
        for (int i = 0; i < 16; i++){
            k[i] = Math.log(2) / t_hl[i];
        }

        for (int i = 0; i < 16; i++){
            pressure[i] = schreiner(initialPressure[i], p_alv, time, k[i], 0);
        }
        return pressure;
    }

    public static double[] planAscent(int initialDepth, int finalDepth, int rate, double[] initialPressure, double[] t_hl){
        double[] pressure = new double[16];
        double[] k = new double[16];

        int p_abs = 1 + ((int) (initialDepth - finalDepth) / 10);
        double p_alv, r;
        double f_gas = 0.68;
        double p_wvp = 0.0627;
        double t = (finalDepth - initialDepth) / rate;

        int p_rate = (int) rate / 10;
        r = f_gas * p_rate;
        p_alv = f_gas * (p_abs - p_wvp);
        for (int i = 0; i < 16; i++){
            k[i] = Math.log(2) / t_hl[i];
        }

        for (int i = 0; i < 16; i++){
            pressure[i] = schreiner(initialPressure[i], p_alv, t, k[i], r);
        }
        return pressure;
    }

    /**
     * Calculates the ascent ceiling in a tissue compartment
     * P_l=(P−A∗gf)/(gf/B+1.0−gf)
     * where p_l is the ascent ceiling
     */
    public static double BuhlmannEquation(double pressure, double coefA, double coefB, double gf){
        return (pressure - coefA * gf)/(gf / coefB + 1 -gf);
    }

    public static void main(String args[]){
        Buhlmann b = new Buhlmann();

        double[] t_hl = new double[16];
        for (int i=0; i < 16; i++){
            t_hl[i] = b.ZHL16ATissues[i][0];
        }

        double[] descentPressure;
        double[] divePressure;
        double[] ascentPressure;

        // Planning a dive
        descentPressure = planDescent(0, 30, 20, t_hl);
        divePressure = planDive(30, 20, descentPressure, t_hl);
        ascentPressure = planAscent(30, 10, 10, divePressure, t_hl);

        for (double p: ascentPressure){
            System.out.println(p);
        }
    }
}