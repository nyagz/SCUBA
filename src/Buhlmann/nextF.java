package Buhlmann;

public class nextF {
    private double time;
    private CompartmentData data;

    public nextF(double time, CompartmentData data){
        this.time = time;
        this.data = data;
    }

    public double getTime() {
        return time;
    }

    public CompartmentData getData() {
        return data;
    }
}
