package Buhlmann;

public class Equations {

    public static double schreiner(double initialPressure, double inspiredGasPressure, double exposureTime,
                                   double gasDecayConstant, double rateChangeGas){
        return inspiredGasPressure + rateChangeGas * (exposureTime - 1 / gasDecayConstant) -
                (inspiredGasPressure - initialPressure - rateChangeGas/gasDecayConstant) *
                        Math.exp(-1 * gasDecayConstant * exposureTime);
    }

    public static double buhlmannEquation(double pressureN2, double pressureHe, double n2A, double n2B,
                                        double heA, double heB, Double gf){
        double pressure, gradientFactor, a, b, p_l;
        if (gf == null){
            gradientFactor = ZHL16.gfLow;
        } else{
            gradientFactor = gf;
        }
        pressure = pressureN2 + pressureHe;
        a = (n2A * pressureN2 + heA * pressureHe) / pressure;
        b = (n2B * pressureN2 + heB * pressureHe) / pressure;
        p_l = buhlmannP_l(pressure, a, b, gradientFactor);

        return p_l;
    }

    public static double buhlmannP_l(double pressure, double a, double b, double gf){
        return (pressure - a * gf) / (gf / b + 1.0 - gf);
    }
}
