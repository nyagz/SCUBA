package Buhlmann;


public class DiveStep {
    private double absolutePressure;
    private CompartmentData data;
    private double time;

    public DiveStep(double absolutePressure, CompartmentData data, double time){
        this.absolutePressure = absolutePressure;
        this.data = data;
        this.time = time;
    }

    public double getAbsolutePressure(){
        return absolutePressure;
    }

    public CompartmentData getData(){
        return data;
    }

    public double getTime(){
        return time;
    }
}
