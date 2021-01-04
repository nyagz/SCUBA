package VaryingPermeabilityModel;

import static BuhlmannEquation.Buhlmann.*;

public class VPM {

    public static final double surfaceTension = 0.179;
    public static final double crumblingCompression = 2.57;
    public static final double minimumInitialRadius = 0.8;

    public static void allowedSupersaturation(){

    }

    public static void main(String[] args){
        System.out.println(planDive());

        // System.out.println("For the first compartment:");
        // System.out.println("Descent pressure: " + plan.get("descent")[0]);
        // System.out.println("Dive pressure: " + plan.get("dive")[0]);
        // System.out.println("Ascent pressure: " + plan.get("ascent")[0]);
    }

}
