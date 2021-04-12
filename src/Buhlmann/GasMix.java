package Buhlmann;

public class GasMix {
    private double depth;
    private int o2;
    private int n2;
    private int he;

    public GasMix(int depth, int o2, int n2, int he){
        this.depth = depth;
        this.o2 = o2;
        this.n2 = n2;
        this.he = he;
    }

    public double getDepth() {
        return depth;
    }

    public int getO2() {
        return o2;
    }

    public int getN2() {
        return n2;
    }

    public int getHe() {
        return he;
    }
}
