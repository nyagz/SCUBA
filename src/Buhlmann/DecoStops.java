package Buhlmann;

public class DecoStops {
    private double depth;
    private GasMix gas;
    private double ascentTime;
    private double nextGf;

    public DecoStops(double depth, GasMix gas, double ascentTime, double nextGf) {
        this.depth = depth;
        this.gas = gas;
        this.ascentTime = ascentTime;
        this.nextGf = nextGf;
    }

    public double getDepth() {
        return depth;
    }

    public void setDepth(double depth) {
        this.depth = depth;
    }

    public GasMix getGas() {
        return gas;
    }

    public void setGas(GasMix gas) {
        this.gas = gas;
    }

    public double getAscentTime() {
        return ascentTime;
    }

    public void setAscentTime(double ascentTime) {
        this.ascentTime = ascentTime;
    }

    public double getNextGf() {
        return nextGf;
    }

    public void setNextGf(double nextGf) {
        this.nextGf = nextGf;
    }
}
