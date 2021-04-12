package Buhlmann;

import java.util.ArrayList;

public class ZHL16 {
    public static final double waterVapourPressure = 0.0627;
    public static final int surfacePressure = 1;
    public static final double startP_he = 0;
    public static final double startP_N2 = 0.7902;

    // Gradient factors
    public static final double gfLow = 0.3;
    public static final double gfHigh = 0.85;

    public static ArrayList<DecoStop> deco_stops = new ArrayList<>();

    public static CompartmentData loadTissues(double absolutePressure, double time, GasMix gas, double pressureRate,
                                              CompartmentData initialPressureData){
        TissueLoader[] loaders = tissueLoaders(absolutePressure, gas, pressureRate, time,
                initialPressureData.getTissues());

        return new CompartmentData(loaders, initialPressureData.getGf());
    }

    // Loads tissue compartments with gas specified in the gasMix
    public static TissueLoader[] tissueLoaders(double absolutePressure, GasMix gas, double pressureRateChange,
                                               double time, TissueLoader[] initialPressure){
        double n2Loader, heLoader;

        TissueLoader[] result = new TissueLoader[16];

        for (int i = 0; i < result.length; i++){
            n2Loader = tissueLoader(absolutePressure, (double) gas.getN2() / 100, pressureRateChange,
                    ZHL16BGF.N2_halfLife[i], time, initialPressure[i].getN2Loader());
            heLoader = tissueLoader(absolutePressure, (double) gas.getHe() / 100, pressureRateChange,
                    ZHL16BGF.He_halfLife[i], time, initialPressure[i].getHeLoader());
            result[i] = new TissueLoader(n2Loader, heLoader);
        }
        return result;
    }

    // Function to load tissue compartments with inert gas using Schreiner's equation
    public static double tissueLoader(double absolutePressure, double f_gas, double pressureRateChange, double halfLife,
                                      double time, double initialPressure){
        double p_alv, r, k;
        p_alv = f_gas * (absolutePressure - waterVapourPressure);
        // k
        r = f_gas * pressureRateChange;
        k = Math.log(2) / halfLife;
        return Equations.schreiner(initialPressure, p_alv, time, k, r);
    }

    // Calculates pressure of the ascent ceiling
    public static double ceiling(CompartmentData data) throws GradientFactorException {
        double[] compartments = tissueCeiling(data);
        double ceiling = compartments[0];
        for(double p: compartments){
            ceiling = Math.max(ceiling, p);
        }
        return ceiling;
    }

    public static double ceiling(CompartmentData data, double gf) throws GradientFactorException {
        double[] compartments = tissueCeiling(data, gf);
        double ceiling = compartments[0];
        for(double p: compartments){
            ceiling = Math.max(ceiling, p);
        }
        return ceiling;
    }

    // Calculates ascent ceiling for each tissue
    public static double[] tissueCeiling(CompartmentData data) throws GradientFactorException {
        TissueLoader[] tissues = data.getTissues();
        double[] ceilings = new double[16];
        for (int i = 0; i < tissues.length; i++){
            ceilings[i] = Equations.buhlmannEquation(tissues[i].getN2Loader(), tissues[i].getHeLoader(),
                    ZHL16BGF.N2_A[i], ZHL16BGF.N2_B[i], ZHL16BGF.He_A[i], ZHL16BGF.He_B[i], data.getGf());
        }
        return ceilings;
    }

    public static double[] tissueCeiling(CompartmentData data, Double gf) throws GradientFactorException {
        if (gf == null){
            gf = gfLow;
        }
        TissueLoader[] tissues = data.getTissues();
        double[] ceilings = new double[16];
        for (int i = 0; i < tissues.length; i++){
            ceilings[i] = Equations.buhlmannEquation(tissues[i].getN2Loader(), tissues[i].getHeLoader(),
                    ZHL16BGF.N2_A[i], ZHL16BGF.N2_B[i], ZHL16BGF.He_A[i], ZHL16BGF.He_B[i], gf);
        }
        return ceilings;
    }

    // Initialises pressures in each compartment assuming the surface pressure is 1
    public static CompartmentData initialisePressure(){
        double pN2 = startP_N2 * (ZHL16.surfacePressure - ZHL16.waterVapourPressure);
        double pHe = startP_he;
        TissueLoader[] tissues = new TissueLoader[16];

        for (int i = 0; i < tissues.length; i++){
            tissues[i] = new TissueLoader(pN2, pHe);
        }
        return new CompartmentData(tissues, ZHL16.gfLow);
    }

    // Initialises pressures in each compartment
    public static CompartmentData initialisePressure(double sp){
        double pN2 = startP_N2 * (sp - ZHL16.waterVapourPressure);
        double pHe = startP_he;
        TissueLoader[] tissues = new TissueLoader[16];
        for (int i = 0; i < tissues.length; i++){
            tissues[i] = new TissueLoader(pN2, pHe);
        }
        return new CompartmentData(tissues, gfLow);
    }

    public static Run create(){
        return new Run();
    }
}
