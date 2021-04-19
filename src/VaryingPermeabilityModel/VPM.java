package VaryingPermeabilityModel;

import Buhlmann.*;
import Buhlmann.Equations;

import java.util.ArrayList;
import java.util.Arrays;

// TODO: descent and dive to be done the same way as Buhlmann's but ascent should use VPM instead of ZHL16
public class VPM extends ZHL16B {
    public int compartments;
    public double startP_N2;
    public double startP_He;
    public double[] n2_k;
    public double[] he_k;

    public double gfLow;
    public double gfHigh;
    public double waterVapourPressure;

    public VPM() {
        super();
        this.compartments = 16;
        this.n2_k = kConst(N2_halfLife);
        this.he_k = kConst(He_halfLife);
        this.startP_He = 0;
        this.startP_N2 = 0.7902;
        this.gfLow = 0.3;
        this.gfHigh = 0.85;
        this.waterVapourPressure = 0.0627;
    }

    /**
     * Calculate the gas decay constant for each tissue compartment
     * @param halfLife
     * @return
     */
    public double[] kConst(double[] halfLife){
        double[] k = new double[compartments];
        for (int i = 0; i < halfLife.length; i++){
            k[i] = Math.log(2) / halfLife[i];
        }
        return k;
    }

    /**
     * Initialises the pressure of inert gasses in all tissue compartments
     * @param surfacePressure
     * @return
     */
    public CompartmentData initialisePressure(double surfacePressure){
        double pN2 = startP_N2 * (surfacePressure - waterVapourPressure);
        double pHe = startP_He;
        TissueLoader[] tissues = new TissueLoader[compartments];
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
    public CompartmentData loadTissues(double absolutePressure, double time, GasMix gas, double pressureRate,
                                       CompartmentData initialPressureData){
        TissueLoader[] loaders = tissueLoaders(absolutePressure, gas, pressureRate, time,
                initialPressureData.getTissues());

        return new CompartmentData(loaders, initialPressureData.getGf());
    }

    public TissueLoader[] tissueLoaders(double absolutePressure, GasMix gas, double pressureRateChange,
                                        double time, TissueLoader[] initialPressure){
        double[] n2Loader, heLoader;

        TissueLoader[] result = new TissueLoader[compartments];
        for (int i = 0; i < result.length; i++){
            n2Loader = tissueLoader(absolutePressure, (double) gas.getN2() / 100, pressureRateChange, n2_k[i],
                    time, initialPressure[i].getN2Loader());
            heLoader = tissueLoader(absolutePressure, (double) gas.getHe() / 100, pressureRateChange, he_k[i],
                    time, initialPressure[i].getHeLoader());
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
    public double[] tissueLoader(double absolutePressure, double f_gas, double pressureRateChange,
                                 double k_const, double time, double initialPressure){
        double p_alv, r;
        p_alv = f_gas * (absolutePressure - waterVapourPressure);
        r = f_gas * pressureRateChange;
        double[] schreinerResult = new double[compartments];
        for (int i = 0; i < schreinerResult.length; i++){
            schreinerResult[i] = Equations.schreiner(initialPressure, p_alv, time, k_const, r);
        }
        return schreinerResult;
    }
}