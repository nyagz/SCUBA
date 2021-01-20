package Buhlmann;

import java.util.ArrayList;

public class Run{
    private static final int ascentRate = 10;
    private static final int descentRate = 20;
    private static final double meterToBar = 0.09985;
    private static final double surfacePressure = 1.01325;

    private static boolean lastStop6m = false;


    private ArrayList<GasMix> gasList = new ArrayList<>();
    private ArrayList<DecoStop> decoStops = new ArrayList<>();

    // TODO: Function to plan dive once given the max depth to dive to and time spent at the bottom depth (in minutes)
    public static void plan(double maxDepth, int bottomTime){ }

    // TODO: Complete
    // Checks dive steps needed to ascent from the current depth to the surface
    public static void diveAscent(DiveStep startingStep, ArrayList<GasMix> gasList){
        GasMix bottomGas = gasList.get(0);
        Step step = NDL(startingStep, bottomGas);
        if (step == null){
            //TODO: Figure out what I will do
        } else {
            // TODO: Understand yields
        }
    }

    public static Step NDL(DiveStep startingStep, GasMix gas){
        double gf = ZHL16.gfHigh;
        double pressure = startingStep.getAbsolutePressure() - surfacePressure;
        double time = pressure / ascentRate / meterToBar;
        Step step = nextDiveStepAscent(startingStep, time, gas, gf);
        startingStep.getData().setGf(gf);
        double ceilingLimit = ZHL16.ascentCeiling(startingStep.getData());
        if (step.getAbsolutePressure() < ceilingLimit){
            step = null;
        }
        return step;
    }

    // Calculates the next dive startingStep while ascending
    public static Step nextDiveStepAscent(DiveStep currentStep, double time, GasMix gas){
        CompartmentData data = tissuePressureAscent(currentStep.getAbsolutePressure(), time, gas, currentStep.getData());
        double pressure = currentStep.getAbsolutePressure() - (time * ascentRate * meterToBar);
        return new Step(DivePhase.ASCENT, pressure, currentStep.getTime() + time, gas, data);
    }

    // Calculates the next dive step while ascending
    // If gradient factor is specified
    public static Step nextDiveStepAscent(DiveStep currentStep, double time, GasMix gas, double gf){
        CompartmentData data = tissuePressureAscent(currentStep.getAbsolutePressure(), time, gas, currentStep.getData());
        double pressure = currentStep.getAbsolutePressure() - (time * ascentRate * meterToBar);
        data.setGf(gf);
        return new Step(DivePhase.ASCENT, pressure, currentStep.getTime() + time, gas, data);
    }

    // Calculates the next dive step while ascending
    // If phase is specified
    public static Step nextDiveStepAscent(DiveStep currentStep, double time, GasMix gas, DivePhase phase){
        CompartmentData data = tissuePressureAscent(currentStep.getAbsolutePressure(), time, gas, currentStep.getData());
        double pressure = currentStep.getAbsolutePressure() - (time * ascentRate * meterToBar);
        return new Step(phase, pressure, currentStep.getTime() + time, gas, data);
    }

    // Calculates the next dive step while ascending
    // If dive phase and gradient factor are specified
    public static Step nextDiveStepAscent(DiveStep currentStep, double time, GasMix gas, DivePhase phase, double gf){
        CompartmentData data = tissuePressureAscent(currentStep.getAbsolutePressure(), time, gas, currentStep.getData());
        double pressure = currentStep.getAbsolutePressure() - (time * ascentRate * meterToBar);
        data.setGf(gf);
        return new Step(phase, pressure, currentStep.getTime() + time, gas, data);

    }

    // Calculates tissues pressure after ascent
    public static CompartmentData tissuePressureAscent(double absolutePressure, double time, GasMix gas, CompartmentData data){
        double rate = -ascentRate * meterToBar;
        return ZHL16.loadTissues(absolutePressure, time, gas, rate, data);
    }

    public static void main(String args[]){
        Tests.testingDiveProfile();
        System.out.println();
        Tests.testingCeiling();
        System.out.println();
        Tests.testingDecoStop();
    }
}
