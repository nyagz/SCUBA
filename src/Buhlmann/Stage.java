package Buhlmann;

public class Stage {
    private double absolutePressure;
    private GasMix gas;

    public Stage(double absolutePressure, GasMix gas){
        this.absolutePressure = absolutePressure;
        this.gas = gas;
    }

    public double getAbsolutePressure() {
        return absolutePressure;
    }

    public GasMix getGas() {
        return gas;
    }
}
