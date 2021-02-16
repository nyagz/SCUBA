package Buhlmann;

import java.util.ArrayList;

// TODO: Delete the class when finished with testing
public class Tests {

    public static void testingDecoStop(){
        DecoStop test = new DecoStop(3,2);
        System.out.println("Checking it's empty");
        if (ZHL16.deco_stops.size() == 0){
            System.out.println("It's empty, good");
        } else{
            System.out.println("Oh no, it didn't work");
        }

        ZHL16.deco_stops.add(test);
        DecoStop test2 = new DecoStop(2, 3);
        ZHL16.deco_stops.add(test2);
        System.out.println("Total length of decompression stops: " + DecoStop.totalDecoStops(ZHL16.deco_stops));
        System.out.println("Added");
        System.out.println("New total number of decompression stops: " + ZHL16.deco_stops.size());

        DecoStop firstStop = ZHL16.deco_stops.get(0);
        System.out.println("Depth of first stop: " + firstStop.getDepth());
        System.out.println("Min of first stop: " + firstStop.getMin());
    }

    public static void testingDiveProfile(){
        CompartmentData data = ZHL16.initialisePressure(1);
        data = ZHL16.loadTissues(1, 1.5, Gases.EAN32, 2, data);

        TissueLoader[] tissues = data.getTissues();
        System.out.println(tissues[0].getN2Loader());
    }

    public static void testingCeiling(){
        CompartmentData data = ZHL16.initialisePressure(1);
        if(ZHL16.Ceiling(data) == 0.6636871173176457){
            System.out.println("Yay, it works!");
        } else {
            System.out.println("Oh no :(");
        }
        double[] newCeilings = ZHL16.tissueCeiling(data); //FIXME: What is this for? Lord knows
    }

    public static void testingPlanning(){
        Run engine = ZHL16.create();
        engine.addGas(0, 21);
        ArrayList<Step> profile = engine.plan(35, 40);
        for (Step p: profile){
            System.out.println(p);
        }
    }
}
