package Buhlmann;

import java.util.ArrayList;

public class DecoStop {
    private double depth;
    private double min;

    public DecoStop(double depth, double min){
        this.depth = depth;
        this.min = min;
    }

    public double getDepth() {
        return depth;
    }

    public double getMin() {
        return min;
    }

    public static double totalDecoStops(ArrayList<DecoStop> deco_stops){
        double total = 0;
        for (DecoStop p: deco_stops){
            total += p.getMin();
        }
        return total;
    }
}
