package Buhlmann;

public class CompartmentData {
    private TissueLoader[] tissues;
    private double gf;

    public CompartmentData(TissueLoader[] tissues, double gf){
        this.tissues = tissues;
        this.gf = gf;
    }

    public TissueLoader[] getTissues() {
        return tissues;
    }

    public double getGf() {
        return gf;
    }
}
