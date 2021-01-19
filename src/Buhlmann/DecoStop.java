package Buhlmann;

import java.util.ArrayList;

public class DecoStop {
    private double depth;
    private int min;

    public DecoStop(double depth, int min){
        this.depth = depth;
        this.min = min;
    }

    public double getDepth() {
        return depth;
    }

    public int getMin() {
        return min;
    }

    public static int totalDecoStops(ArrayList<DecoStop> deco_stops){
        int total = 0;
        for (DecoStop p: deco_stops){
            total += p.getMin();
        }
        return total;
    }
}
