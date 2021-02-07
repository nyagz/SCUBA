package Buhlmann;

import java.util.ArrayList;

public class DecoFreeStage {
    private double pressure;
    private GasMix gasMix;

    public DecoFreeStage(double pressure, GasMix gasMix){
        this.pressure = pressure;
        this.gasMix = gasMix;
    }

    public double getPressure() {
        return pressure;
    }

    public void setPressure(double pressure) {
        this.pressure = pressure;
    }

    public GasMix getGasMix() {
        return gasMix;
    }

    public void setGasMix(GasMix gasMix) {
        this.gasMix = gasMix;
    }
}
