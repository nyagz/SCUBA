package Buhlmann;

import java.util.ArrayList;

public class Run{
    private static final int ascentRate = 10;
    private static final int descentRate = 20;
    private static final double meterToBar = 0.09985;
    private static final double surfacePressure = 1.01325;

    private static boolean lastStop6m = false;

    private static ArrayList<GasMix> gasList = new ArrayList<>();
    private static ArrayList<Step> steps = new ArrayList<>();


    // TODO: Function to plan dive once given the max depth to dive to and time spent at the bottom depth (in minutes)
    public static void plan(double maxDepth, int bottomTime){ }

    // TODO: Complete
    // Checks dive steps needed to ascent from the current depth to the surface
    public static ArrayList<Step> diveAscent(DiveStep startingStep, ArrayList<GasMix> gasList){
        GasMix bottomGas = gasList.get(0);
        // FIXME: NDL ascents can be performed without bottom gas, look into these conditions
        Step step = NDL(startingStep, bottomGas);
        if (step == null){
            steps.add(new Step(DivePhase.ASCENT, startingStep.getAbsolutePressure(), startingStep.getTime(),
                    bottomGas, startingStep.getData()));
            return steps;
        }
        Stage stages;
        // FIXME: Figure out what on earth is supposed to be happening here
        return steps;
    }

    /**
     * Determines if a NDL ascent is possible from the starting step
     * @param startingStep
     * @param gas
     * @return
     */
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

    /**
     * Calculates the next dive startingStep while ascending
     * @param currentStep
     * @param time
     * @param gas
     * @return
     */
    public static Step nextDiveStepAscent(DiveStep currentStep, double time, GasMix gas){
        CompartmentData data = tissuePressureAscent(currentStep.getAbsolutePressure(), time, gas, currentStep.getData());
        double pressure = currentStep.getAbsolutePressure() - (time * ascentRate * meterToBar);
        return new Step(DivePhase.ASCENT, pressure, currentStep.getTime() + time, gas, data);
    }

    /**
     * Calculates the next dive step while ascending (if gradient factor is specified)
     * @param currentStep
     * @param time
     * @param gas
     * @param gf
     * @return
     */
    public static Step nextDiveStepAscent(DiveStep currentStep, double time, GasMix gas, double gf){
        CompartmentData data = tissuePressureAscent(currentStep.getAbsolutePressure(), time, gas, currentStep.getData());
        double pressure = currentStep.getAbsolutePressure() - (time * ascentRate * meterToBar);
        data.setGf(gf);
        return new Step(DivePhase.ASCENT, pressure, currentStep.getTime() + time, gas, data);
    }

    /**
     * Calculates the next dive step while ascending (if phase is specified)
     * @param currentStep
     * @param time
     * @param gas
     * @param phase
     * @return
     */
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

    // Calcuate the stages for a DCS-free ascent
    public static Stage freeAscentStages(ArrayList<GasMix> gasList){

        return null;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    public static DiveStep findFirstDecoStop(DiveStep startingStep, double absolutePressure, GasMix gas){
        double limit = ZHL16.ascentCeiling(new CompartmentData(startingStep.getData().getTissues(),
                startingStep.getData().getGf()));
        limit = Math.ceil((limit - surfacePressure) / (3 * meterToBar)) * (3 * meterToBar) + surfacePressure;
        limit = Math.max(limit, absolutePressure);

        return null;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Converts pressure to meters that's divisible by 3
     * @param absolutePressure
     * @return
     */
    public static double pressureMetersDiv3(double absolutePressure){
        double result = Math.ceil((absolutePressure - surfacePressure) / (3 * meterToBar));
        return result * (meterToBar * 3) + surfacePressure;
    }

    /**
     * Convert depth (in meters) to pressure (in bars)
     * @param depth
     * @return
     */
    public static double depthToPressure(double depth){
        return depth * meterToBar + surfacePressure;
    }

    /**
     * Converts pressure (in bars) to depth (in meters)
     * @param absolutePressure
     * @return
     */
    public static double pressureToDepth(double absolutePressure){
        return (absolutePressure - surfacePressure) / meterToBar;
    }

    /**
     * Convert time into pressure change using the depth change rate
     * @param time
     * @param rate
     * @return
     */
    public static double timeToPressure(double time, double rate){
        return time * rate * meterToBar;
    }

    /**
     * Convert pressure change to time using the depth change rate
     * @param pressure
     * @param rate
     * @return
     */
    public static double pressureToTime(double pressure, double rate){
        return pressure / rate / meterToBar;
    }

    public static void main(String args[]){
        Tests.testingDiveProfile();
        System.out.println();
        Tests.testingCeiling();
        System.out.println();
        Tests.testingDecoStop();
    }
}
