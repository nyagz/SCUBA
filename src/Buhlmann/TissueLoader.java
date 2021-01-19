package Buhlmann;

public class TissueLoader {
    private final double n2Loader;
    private final double heLoader;

    public TissueLoader(double n2Loader, double heLoader){
        this.n2Loader = n2Loader;
        this.heLoader = heLoader;
    }

    public double getN2Loader(){
        return n2Loader;
    }

    public double getHeLoader(){
        return heLoader;
    }
}
