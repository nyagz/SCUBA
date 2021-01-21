package Buhlmann;

public class Step {
    private DivePhase phase;
    private double absolutePressure;
    private double time;
    private GasMix gas;
    private CompartmentData data;

    // FIXME: Potentially delete as this may never be needed (I think)
    public Step(DivePhase phase, double absolutePressure, double time, CompartmentData data){
        this.phase = phase;
        this.absolutePressure = absolutePressure;
        this.time = time;
        this.data = data;
    }

    public Step(DivePhase phase, double absolutePressure, double time, GasMix gas, CompartmentData data){
        this.phase = phase;
        this.absolutePressure = absolutePressure;
        this.time = time;
        this.gas = gas;
        this.data = data;
    }

    public DivePhase getPhase(){
        return phase;
    }

    public double getAbsolutePressure(){
        return absolutePressure;
    }

    public double getTime() {
        return time;
    }

    public CompartmentData getData() {
        return data;
    }

    public GasMix getGas(){
        return gas;
    }
}
