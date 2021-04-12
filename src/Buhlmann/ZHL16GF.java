package Buhlmann;

public class ZHL16GF {
    public static int compartments = 16;
    public static double[] N2_A;
    public static double[] N2_B;
    public static double[] He_A;
    public static double[] He_B;
    public static double[] N2_HalfLife;
    public static double[] He_HalfLife;
    public static double startP_N2;
    public static double startP_He;
    public static double[] n2_k;
    public static double[] he_k;

    public static double gfLow = 0.3;
    public static double gfHigh = 0.85;
    public static double waterVapourPressure = 0.0627;

    public ZHL16GF(Object gf) {
        N2_A = null;
        N2_B = null;
        He_A = null;
        He_B = null;
        N2_HalfLife = null;
        He_HalfLife = null;
        startP_He = 0;
        startP_N2 = 0.7902;

        if (gf instanceof ZHL16BGF){
            n2_k = kConst(ZHL16BGF.N2_halfLife);
            he_k = kConst(ZHL16BGF.He_halfLife);
        } else{
            n2_k = kConst(ZHL16CGF.N2_halfLife);
            he_k = kConst(ZHL16CGF.He_HalfLife);
        }

    }

    /**
     * Initialises the pressure of inert gasses in all tissue compartments
     * @param surfacePressure
     * @return
     */
    public static CompartmentData initialisePressure(double surfacePressure){
        double pN2 = startP_N2 * (surfacePressure - waterVapourPressure);
        double pHe = startP_He;
        TissueLoader[] tissues = new TissueLoader[16];
        for (int i = 0; i < tissues.length; i++){
            tissues[i] = new TissueLoader(pN2, pHe);
        }
        return new CompartmentData(tissues, gfLow);
    }

    /**
     *Calculate gas loading for all tissue compartments
     * @param absolutePressure
     * @param time
     * @param gas
     * @param pressureRate
     * @param initialPressureData
     * @return
     */
    public static CompartmentData loadTissues(double absolutePressure, double time, GasMix gas, double pressureRate,
                                              CompartmentData initialPressureData){
        TissueLoader[] loaders = tissueLoaders(absolutePressure, gas, pressureRate, time,
                initialPressureData.getTissues());

        return new CompartmentData(loaders, initialPressureData.getGf());
    }

    /**
     * Calculates the pressure of ascent ceiling limit
     * @param data
     * @return
     */
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

    /**
     * Calculate the gas decay constant for each tissue compartment
     * @param halfLife
     * @return
     */
    public static double[] kConst(double[] halfLife){
        double[] k = new double[16];
        for (int i = 0; i < halfLife.length; i++){
            k[i] = Math.log(2) / halfLife[i];
        }
        return k;
    }

    /**
     * Exponential function for time and gas decay
     * @param time
     * @param k
     * @return
     */
    public static double exp(double time, double k){
        return Math.exp(-k * time);
    }


    public static TissueLoader[] tissueLoaders(double absolutePressure, GasMix gas, double pressureRateChange,
                                               double time, TissueLoader[] initialPressure){
        double[] n2Loader, heLoader;

        TissueLoader[] result = new TissueLoader[16];
        for (int i = 0; i < result.length; i++){
            n2Loader = tissueLoader(absolutePressure, (double) gas.getN2() / 100, pressureRateChange, n2_k[i], time,
                    initialPressure[i].getN2Loader());
            heLoader = tissueLoader(absolutePressure, (double) gas.getHe() / 100, pressureRateChange, he_k[i], time,
                    initialPressure[i].getHeLoader());
            result[i] = new TissueLoader(n2Loader[i], heLoader[i]);
        }
        return result;
    }

    /**
     * Loads the tissue compartment with inert gas
     * @param absolutePressure
     * @param f_gas
     * @param pressureRateChange
     * @param k_const
     * @return
     */
    public static double[] tissueLoader(double absolutePressure, double f_gas, double pressureRateChange,
                                      double k_const, double time, double initialPressure){
        double p_alv, r;
        p_alv = f_gas * (absolutePressure - waterVapourPressure);
        r = f_gas * pressureRateChange;
        double[] schreinerResult = new double[16];
        for (int i = 0; i < schreinerResult.length; i++){
            schreinerResult[i] = Equations.schreiner(initialPressure, p_alv, time, k_const, r);
        }
        return schreinerResult;
    }

    /**
     * Calculates the pressure of ascent for each tissue compartment
     * @param data
     * @return
     */
    public static double[] tissueCeiling(CompartmentData data) throws GradientFactorException {
        TissueLoader[] tissues = data.getTissues();
        double[] ceilings = new double[16];
        for (int i = 0; i < tissues.length; i++){
            ceilings[i] = Equations.buhlmannEquation(tissues[i].getN2Loader(), tissues[i].getHeLoader(),
                    ZHL16BGF.N2_A[i], ZHL16BGF.N2_B[i], ZHL16BGF.He_A[i], ZHL16BGF.He_B[i], data.getGf());
        }
        return ceilings;
    }

    public static double[] tissueCeiling(CompartmentData data, Double gf) throws GradientFactorException{
        if (gf == null){
            gf = gfLow;
        }
        if(gf < 0 || gf <1.5){
            throw new GradientFactorException("Gradient factor out of range");
        }

        TissueLoader[] tissues = data.getTissues();
        double[] ceilings = new double[16];
        for (int i = 0; i < tissues.length; i++){
            ceilings[i] = Equations.buhlmannEquation(tissues[i].getN2Loader(), tissues[i].getHeLoader(),
                    ZHL16BGF.N2_A[i], ZHL16BGF.N2_B[i], ZHL16BGF.He_A[i], ZHL16BGF.He_B[i], gf);
        }
        return ceilings;
    }
}
