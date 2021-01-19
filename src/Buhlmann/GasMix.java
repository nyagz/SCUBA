package Buhlmann;

public class GasMix {
    private static int depth;
    private static int o2;
    private static int n2;
    private static int he;

    public GasMix(int depth, int o2, int n2, int he){
        this.depth = depth;
        this.o2 = o2;
        this.n2 = n2;
        this.he = he;
    }

    public static int getDepth() {
        return depth;
    }

    public static int getO2() {
        return o2;
    }

    public static int getN2() {
        return n2;
    }

    public static int getHe() {
        return he;
    }
}
