package Buhlmann;

public class Mix {
    private GasMix firstGas;
    private GasMix secondGas;

    public Mix(GasMix firstGas, GasMix secondGas){
        this.firstGas = firstGas;
        this.secondGas = secondGas;
    }

    public GasMix getFirstGas() {
        return firstGas;
    }

    public void setFirstGas(GasMix firstGas) {
        this.firstGas = firstGas;
    }

    public GasMix getSecondGas() {
        return secondGas;
    }

    public void setSecondGas(GasMix secondGas) {
        this.secondGas = secondGas;
    }
}
