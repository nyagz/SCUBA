package Buhlmann;

import java.util.ArrayList;

// TODO: Delete the class when finished with testing
public class Tests {

    // public Tests(Object obj){
    //     if(obj instanceof ZHL16BGF){
    //         System.out.println("B");
    //     } else{
    //         System.out.println("C");
    //     }
    // }
//
    // public static void testingDecoStop(){
    //     DecoStop test = new DecoStop(3,2);
    //     System.out.println("Checking it's empty");
    //     if (ZHL16GF.deco_stops.size() == 0){
    //         System.out.println("It's empty, good");
    //     } else{
    //         System.out.println("Oh no, it didn't work");
    //     }
//
    //     ZHL16.deco_stops.add(test);
    //     DecoStop test2 = new DecoStop(2, 3);
    //     ZHL16.deco_stops.add(test2);
    //     System.out.println("Total length of decompression stops: " + DecoStop.totalDecoStops(ZHL16.deco_stops));
    //     System.out.println("Added");
    //     System.out.println("New total number of decompression stops: " + ZHL16.deco_stops.size());
//
    //     DecoStop firstStop = ZHL16.deco_stops.get(0);
    //     System.out.println("Depth of first stop: " + firstStop.getDepth());
    //     System.out.println("Min of first stop: " + firstStop.getMin());
    // }
//
    // public static void testingDiveProfile(){
    //     CompartmentData data = ZHL16.initialisePressure(1);
    //     data = ZHL16.loadTissues(1, 1.5, Gases.EAN32, 2, data);
//
    //     TissueLoader[] tissues = data.getTissues();
    //     System.out.println(tissues[0].getN2Loader());
    // }
//
    // public static void testingCeiling() throws GradientFactorException {
    //     CompartmentData data = ZHL16.initialisePressure(1);
    //     if(ZHL16.ceiling(data) == 0.6636871173176457){
    //         System.out.println("Yay, it works!");
    //     } else {
    //         System.out.println("Oh no :(");
    //     }
    //     double[] newCeilings = ZHL16.tissueCeiling(data); //TODO: Figre out what this is for? Lord knows
    // }
//
    // public static void testingPlanning() throws GradientFactorException, GasConfigException, PressureException,
    //         EngineError {
    //    Run engine = ZHL16.create();
    //    engine.addGas(0, 21);
    //    ArrayList<Step> profile = engine.plan(35, 40);
    //    // for (Step p: profile){
    //    //     System.out.println(p);
    //    // }
    // }
//
    // public static void testingInstanceObj(){
    //     ZHL16BGF model = new ZHL16BGF();
    //     ZHL16CGF model2 = new ZHL16CGF();
    //     Tests a = new Tests(model);
    //     Tests b = new Tests(model2);
    // }
//
    // public static void testingEmptyArrayList(){
    //     ArrayList<GasMix> gasList = new ArrayList<>();
    //     if(gasList.isEmpty()){
    //         System.out.println("Empty");
    //     } else{
    //         System.out.println("Not empty");
    //     }
    // }
//
    // public static void testingSort(){
    //     int[] a = new int[]{1,4,2,5,3};
    //     Arrays.sort(a);
    //     for(int i: a){
    //         System.out.println(i);
    //     }
    // }

    public static void testZHL16B() throws PressureException, GasConfigException, EngineError, GradientFactorException {
        RunB engine = new RunB();
        engine.addGas(0, 21);
        ArrayList<Step> profile = engine.plan(35, 40);
        System.out.println("Dive steps:");
        for (Step p: profile){
            System.out.println("Step(phase = " + p.getPhase() + ", abs_p = " + p.getAbsolutePressure() + ", time = " +
                    p.getTime() + ", gf = " + p.getData().getGf() + ")");
        }
        System.out.println();
        System.out.println("Decompression stops:");
        for (DecoStop d: engine.decompressionStopTable){
            System.out.println("DecoStop(depth = " + d.getDepth() + ", time = " + d.getMin() + ")");
        }
    }

    public static void testZHL16C() throws PressureException, GasConfigException, EngineError, GradientFactorException {
        RunC engine = new RunC();
        engine.addGas(0, 21);
        ArrayList<Step> profile = engine.plan(35, 40);
        System.out.println("Dive steps:");
        for (Step p: profile){
            System.out.println("Step(phase = " + p.getPhase() + ", abs_p = " + p.getAbsolutePressure() + ", time = " +
                    p.getTime() + ", gf = " + p.getData().getGf() + ")");
        }
        System.out.println();
        System.out.println("Decompression stops:");
        for (DecoStop d: engine.decompressionStopTable){
            System.out.println("DecoStop(depth = " + d.getDepth() + ", time = " + d.getMin() + ")");
        }
    }

    public static void main(String args[]) throws PressureException, GasConfigException, EngineError, GradientFactorException {
        // testingPlanning();
        // ZHL16BGF model = new ZHL16BGF();
        // testingInstanceObj();
        // testingEmptyArrayList();
        // testingSort();
        // testZHL16B();
        testZHL16C();
    }
}
