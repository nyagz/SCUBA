package Buhlmann;

public class Step {
    private DivePhase phase;
    private double absolutePressure;
    private double time;
    private GasMix gas;
    private CompartmentData data;

    public Step(DivePhase phase, double absolutePressure, double time, GasMix gas, CompartmentData data){
        this.phase = phase;
        this.absolutePressure = absolutePressure;
        this.time = time;
        this.gas = gas;
        this.data = data;
    }

    public Step(double absolutePressure, CompartmentData data, double time){
        this.absolutePressure = absolutePressure;
        this.data = data;
        this.time = time;
    }

    public DivePhase getPhase(){
        return phase;
    }

    public void setPhase(DivePhase phase){
        this.phase = phase;
    }

    public double getAbsolutePressure(){
        return absolutePressure;
    }

    public void setAbsolutePressure(double absolutePressure){
        this.absolutePressure = absolutePressure;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public CompartmentData getData() {
        return data;
    }

    public void setData(CompartmentData data) {
        this.data = data;
    }

    public GasMix getGas(){
        return gas;
    }

    public void setGas(GasMix gas) {
        this.gas = gas;
    }
}
