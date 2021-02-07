package Buhlmann;

import java.util.ArrayList;

public class Run<T>{
    private static final int ascentRate = 10;
    private static final int descentRate = 20;
    private static final double meterToBar = 0.09985;
    private static final double surfacePressure = 1.01325;

    private static boolean lastStop6m = false;

    private static ArrayList<GasMix> gasList = new ArrayList<>();
    private static ArrayList<DecoStop> decompressionStops = new ArrayList<>();
    private static ArrayList<Step> steps = new ArrayList<>();


    // TODO: Function to plan dive once given the max depth to dive to and time spent at the bottom depth (in minutes)
    public static void plan(double maxDepth, int bottomTime){ }

    // TODO: Complete
    /**
     * Checks dive steps needed to ascent from the current depth to the surface
     * @param startingStep
     * @param gasList
     * @return
     */
    public static ArrayList<Step> diveAscent(DiveStep startingStep, ArrayList<GasMix> gasList){
        ArrayList<Step> steps = new ArrayList<>();
        GasMix bottomGas = gasList.get(0);
        // FIXME: NDL ascents can be performed without bottom gas, look into these conditions
        Step step = NDL(startingStep, bottomGas);
        //FIXME: Double check if this should be (== null) or (!= null)
        if (step != null){
            steps.add(step);
            return steps;
        }
        step = new Step(DivePhase.ASCENT, startingStep.getAbsolutePressure(),startingStep.getTime(),bottomGas,
                startingStep.getData());
        ArrayList<Stage> stages = decoFreeAscentStages(gasList);
        for(Step s: freeStagedAscent(step, stages)){
            steps.add(s);
        }

        stages = decompressionAscentStages(step.getAbsolutePressure(), gasList);
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
        double ceilingLimit = ZHL16.Ceiling(startingStep.getData());
        if (step.getAbsolutePressure() < ceilingLimit){
            step = null;
        }
        return step;
    }

    // public static ArrayList<DecoFreeStage> decoFreeAscentStages(ArrayList<GasMix> gasList){
    //     ArrayList<Mix> gasMixes = new ArrayList<>();
    //     ArrayList<DecoFreeStage> decoFreeStages = new ArrayList<>();
//
    //     int size = gasList.size();
    //     for (int i = 0; i < gasList.size() - 1; i++){
    //         Mix temp = new Mix(gasList.get(i), gasList.get(size - i));
    //         gasMixes.add(temp);
    //     }
//
    //     for (Mix mix:gasMixes){
    //         DecoFreeStage temp = new DecoFreeStage(depthToPressure(((mix.getSecondGas().getDepth() - 1) / 4) * 3),
    //                 gasList.get(gasList.size()-1));
    //         decoFreeStages.add(temp);
    //     }
    //     return decoFreeStages;
    // }

    /**
     * Calculates the next dive startingStep while ascending
     * @param currentStep
     * @param time
     * @param gas
     * @return
     */
    public static Step nextDiveStepAscent(DiveStep currentStep, double time, GasMix gas){
        CompartmentData data = tissuePressureAscent(currentStep.getAbsolutePressure(), time, gas,
                currentStep.getData());
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
        CompartmentData data = tissuePressureAscent(currentStep.getAbsolutePressure(), time, gas,
                currentStep.getData());
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
        CompartmentData data = tissuePressureAscent(currentStep.getAbsolutePressure(), time, gas,
                currentStep.getData());
        double pressure = currentStep.getAbsolutePressure() - (time * ascentRate * meterToBar);
        return new Step(phase, pressure, currentStep.getTime() + time, gas, data);
    }

    /**
     * Calculates the next dive step while ascending (if dive phase and gradient factor are specified)
     * @param currentStep
     * @param time
     * @param gas
     * @param phase
     * @param gf
     * @return
     */
    public static Step nextDiveStepAscent(DiveStep currentStep, double time, GasMix gas, DivePhase phase, double gf){
        CompartmentData data = tissuePressureAscent(currentStep.getAbsolutePressure(), time, gas,
                currentStep.getData());
        double pressure = currentStep.getAbsolutePressure() - (time * ascentRate * meterToBar);
        data.setGf(gf);
        return new Step(phase, pressure, currentStep.getTime() + time, gas, data);

    }

    /**
     * Calculates tissues pressure after ascent
     * @param absolutePressure
     * @param time
     * @param gas
     * @param data
     * @return
     */
    public static CompartmentData tissuePressureAscent(double absolutePressure, double time, GasMix gas,
                                                       CompartmentData data){
        double rate = -ascentRate * meterToBar;
        return ZHL16.loadTissues(absolutePressure, time, gas, rate, data);
    }

    /**
     * Calcuate the stages for a DCS-free ascent
     * @param gasList
     * @return
     */
    public static ArrayList<Stage> decoFreeAscentStages(ArrayList<GasMix> gasList){
        ArrayList<Mix> gasMixes = new ArrayList<>();
        ArrayList<Stage> decoFreeStages = new ArrayList<>();

        int size = gasList.size();
        for (int i = 0; i < gasList.size() - 1; i++){
            Mix temp = new Mix(gasList.get(i), gasList.get(size - i));
            gasMixes.add(temp);
        }

        for (Mix mix:gasMixes){
            Stage temp = new Stage(depthToPressure(((mix.getSecondGas().getDepth() - 1) / 4) * 3),
                    gasList.get(gasList.size()-1));
            decoFreeStages.add(temp);
        }
        return decoFreeStages;
    }

    public static ArrayList<Step> freeStagedAscent(Step start, ArrayList<Stage> stages){
        Step step = start;
        DiveStep diveStep = new DiveStep(step.getAbsolutePressure(), step.getData(), step.getTime());
        ArrayList<Step> steps = new ArrayList<>();
        ArrayList<Step> currentSteps;
        for (Stage stage:stages){
            //FIXME: Finish
            if (step.getGas() != stage.getGas()){
                currentSteps = ascentSwitchGas(step, stage.getGas());
                if (ceilingLimitNotViolated(currentSteps.get(currentSteps.size() - 1).getAbsolutePressure(),
                        currentSteps.get(currentSteps.size() - 1).getData())) {
                    step = currentSteps.get(currentSteps.size() - 1);
                    // Gas switch performed
                } else {
                    break;
                    // Gas switch into deco zone, revert
                }
            }
            Step s = findFirstDecoStop(diveStep, stage.getAbsolutePressure(), stage.getGas());
            if (s == step){
                break; //Already at deco zone
            } else {
                step = s;
                steps.add(step);
                if (Math.abs(step.getAbsolutePressure() - stage.getAbsolutePressure()) > Math.pow(10, 10)){
                    // Decompression stop found
                    break;
                }
            }
        }
        return steps;
    }

    public static ArrayList<Step> ascentSwitchGas(Step step, GasMix gas){
        DiveStep diveStep1 = new DiveStep(step.getAbsolutePressure(), step.getData(), step.getTime());
        ArrayList<Step> steps = new ArrayList<>();
        double pressure;
        double time;
        pressure = depthToPressure(gas.getDepth());
        if (Math.abs(step.getAbsolutePressure() - pressure) < Math.pow(10, 10)){
            step.setPhase(DivePhase.GAS_SWITCH);
            step.setGas(gas);
            steps.add(step);
        } else {
            time = pressureToTime(step.getAbsolutePressure() - pressure, ascentRate);
            Step step1 = nextDiveStepAscent(diveStep1, time, step.getGas());

            Step step2 = step1;
            step2.setPhase(DivePhase.GAS_SWITCH);
            step2.setGas(gas);
            steps.add(step2);

            pressure = depthToPressure(Math.floor(gas.getDepth() /3) * 3);
            time = pressureToTime(step2.getAbsolutePressure() - pressure, ascentRate);
            DiveStep diveStep2 = new DiveStep(step.getAbsolutePressure(), step.getData(), step.getTime());
            Step step3 = nextDiveStepAscent(diveStep2, time, gas);
            steps.add(step3);
        }
        return steps;
    }

    //FIXME: Is this ever gonna be used?
    public static Step gasSwitch(Step step, GasMix gas){
        Step newStep = new Step(DivePhase.GAS_SWITCH, step.getAbsolutePressure(), step.getTime(), gas, step.getData());
        return newStep;
    }

    /**
     * Returns true iff the ceiling limit for the decompression limit hasn't been violated
     * @param absolutePressure
     * @param compartmentData
     * @return true/false
     */
    public static boolean ceilingLimitNotViolated(double absolutePressure, CompartmentData compartmentData){
        return absolutePressure >= ZHL16.Ceiling(compartmentData);
    }

    public static ArrayList<Stage> decompressionAscentStages(double absolutePressure, ArrayList<GasMix> gasList){
        ArrayList<Mix> gasMixes = new ArrayList<>();
        ArrayList<Stage> stages = new ArrayList<>();

        int end = gasList.size();
        for (int i = 0; i < gasList.size() - 1; i++){
            Mix temp = new Mix(gasList.get(i), gasList.get(end - i));
            gasMixes.add(temp);
        }

        for (Mix mix:gasMixes){
            Stage temp = new Stage(depthToPressure(Math.floor(mix.getSecondGas().getDepth() / 3) * 3),
                    gasList.get(gasList.size() - 1));
            stages.add(temp);
        }
        return stages;
    }

    // FIXME: Finish
    public static void decompressionStagedAscent(Step start, ArrayList<Stage> stages){
        GasMix bottomGas = gasList.get(0);
        ArrayList<DecoStops> decoStops = decompressionStops(start, stages);
        Step step = start;
        for (DecoStops decoStop: decoStops){
            if (step.getAbsolutePressure() >= depthToPressure(decoStop.getGas().getDepth()) && decoStop.getGas() != bottomGas){
                // Switch the gas
                ArrayList<Step> steps = ascentSwitchGas(step, decoStop.getGas());
                // FIXME yield step
            }
            Step end = decompressionStopLength(step, decoStop.getAscentTime(), decoStop.getGas(),decoStop.getNextGf());
            decompressionStops.add(new DecoStop(pressureToDepth(step.getAbsolutePressure()), end.getTime() - step.getTime()));
            step = end;
            DiveStep temp = new DiveStep(step.getAbsolutePressure(), step.getData(), step.getTime());
            // FIXME yield step
            // Ascend to next decompression stop
            step = nextDiveStepAscent(temp, decoStop.getAscentTime(), decoStop.getGas(), decoStop.getNextGf());
            // FIXME yield step
        }
    }

    public static ArrayList<DecoStops> decompressionStops(Step step, ArrayList<Stage> stages){
        ArrayList<DecoStops> decoStops = new ArrayList<>();
        int stopsNumber = stops(step.getAbsolutePressure());
        double gfStep = (double) (ZHL16.gfHigh - ZHL16.gfLow) / stopsNumber;
        double p2t = pressureToTime(meterToBar * 3, ascentRate);
        double gf = step.getData().getGf();

        double absolutePressure = step.getAbsolutePressure();
        double sixMeterStop = surfacePressure + 2 * (meterToBar * 3);
        boolean last6m = lastStop6m;
        for (Stage stage : stages){
            int n = stops(absolutePressure, stage.getAbsolutePressure());
            for (int i = 0; i < n; i++){
                gf += gfStep;
                if (last6m && Math.abs(absolutePressure - stopsNumber * (meterToBar * 3) - sixMeterStop) < Math.pow(10, 10)){
                    DecoStops result = new DecoStops(stage.getAbsolutePressure(), stage.getGas(), 2 * p2t,
                            gf + gfStep);
                    decoStops.add(result);
                } else {
                    DecoStops result = new DecoStops(stage.getAbsolutePressure(), stage.getGas(), p2t, gf);
                    decoStops.add(result);
                }
            }
        }
        return decoStops;
    }

    public static int stops(double startPressure){
        double k = (startPressure - surfacePressure) / (3 * meterToBar);
        return (int) k;
    }

    public static int stops(double startPressure, double endPressure){
        double k = (startPressure - endPressure) / (3 * meterToBar);
        return (int) k;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Finds the first decompression stop using Schreiner's
     * @param startingStep
     * @param absolutePressure
     * @param gas
     * @return
     */
    public static Step findFirstDecoStop(DiveStep startingStep, double absolutePressure, GasMix gas){
        Step step = null;
        Step stop;
        boolean entered = false;
        double limit = ZHL16.Ceiling(new CompartmentData(startingStep.getData().getTissues(),
                startingStep.getData().getGf()));
        limit = Math.ceil((limit - surfacePressure) / (3 * meterToBar)) * (3 * meterToBar) + surfacePressure;
        limit = Math.max(limit, absolutePressure);
        double time = pressureToTime(absolutePressure - limit, ascentRate);

        while(startingStep.getAbsolutePressure() > limit && startingStep.getAbsolutePressure() > absolutePressure){
            entered = true;
            step = nextStepAscent(startingStep, time, gas);
            limit = ZHL16.Ceiling(step.getData());
            limit = pressureMetersDiv3(limit);
            limit = Math.max(absolutePressure, limit);
            time = pressureToTime(step.getAbsolutePressure() - limit, ascentRate);
        }

        if(entered == true){
            stop = step;
        } else {
            stop = new Step(DivePhase.DECO_STOP, startingStep.getAbsolutePressure(), startingStep.getTime(), gas, startingStep.getData());
        }

        return stop;
    }

    /**
     * Calculates the next dive step when ascending for a certain amount of time
     * @param step
     * @param time
     * @param gas
     * @return
     */
    public static Step nextStepAscent(DiveStep step, double time, GasMix gas){
        CompartmentData data = tissuePressureAscent(step.getAbsolutePressure(), time, gas, step.getData());
        double pressure = step.getAbsolutePressure() - timeToPressure(time, ascentRate);
        return new Step(DivePhase.ASCENT, pressure, step.getTime() + time, gas, data);
    }

    /**
     * Calculates the next dive step when ascending for a certain amount of time (with specified gradient factor)
     * @param step
     * @param time
     * @param gas
     * @param gf
     * @return
     */
    public static Step nextStepAscent(Step step, double time, GasMix gas, double gf){
        CompartmentData data = tissuePressureAscent(step.getAbsolutePressure(), time, gas, step.getData());
        double pressure = step.getAbsolutePressure() - timeToPressure(time, ascentRate);
        data.setGf(gf);
        return new Step(DivePhase.ASCENT, pressure, step.getTime() + time, gas, data);
    }

    /**
     * Calculates the next dive step when ascending for a certain amount of time (with specified dive phase)
     * @param step
     * @param time
     * @param gas
     * @param phase
     * @return
     */
    public static Step nextStepAscent(Step step, double time, GasMix gas, DivePhase phase){
        CompartmentData data = tissuePressureAscent(step.getAbsolutePressure(), time, gas, step.getData());
        double pressure = step.getAbsolutePressure() - timeToPressure(time, ascentRate);
        return new Step(phase, pressure, step.getTime() + time, gas, data);
    }

    /**
     * Calculates the next dive step when ascending for a certain amount of time (with specified dive phase and
     * gradient factor)
     * @param step
     * @param time
     * @param gas
     * @param gf
     * @param phase
     * @return
     */
    public static Step nextStepAscent(Step step, double time, GasMix gas, double gf, DivePhase phase){
        CompartmentData data = tissuePressureAscent(step.getAbsolutePressure(), time, gas, step.getData());
        double pressure = step.getAbsolutePressure() - timeToPressure(time, ascentRate);
        data.setGf(gf);
        return new Step(phase, pressure, step.getTime() + time, gas, data);
    }

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

    /**
     *
     * @param absolutePressure
     * @param time
     * @param gas
     * @param data
     * @return
     */
    public static CompartmentData tissueLoadingAfterTime(double absolutePressure, double time, GasMix gas, CompartmentData data){
        return ZHL16.loadTissues(absolutePressure, time, gas, 0, data);
    }

    @FunctionalInterface
    interface FunctionInvF<One, Two>{
        public Boolean apply(One one, Two two);
    }

    @FunctionalInterface
    interface FunctionNextF<One, Two>{
        public nextF apply(One one, Two two);
    }

    // FIXME _deco_stop
    public static Step decompressionStopLength(Step step, double nextTime, GasMix gas, double nextGf){
        CompartmentData data = tissueLoadingAfterTime(step.getAbsolutePressure(), 1, gas, step.getData());
        if(possibleAscent(step.getAbsolutePressure(), nextTime, data, nextGf)){
            return new Step(DivePhase.DECO_STOP, step.getAbsolutePressure(), step.getTime() + 1, gas, data);
        }

        double maxTime = 8;
        FunctionNextF<Double, CompartmentData> next_f = (time, newData) -> {
            return new nextF(time + maxTime, tissueLoadingAfterTime(step.getAbsolutePressure(), maxTime, gas,
                    data));
        };
        FunctionInvF<Double, CompartmentData> inv_f = (time, tempData) ->
            possibleAscent(step.getAbsolutePressure(), nextTime, data, nextGf);


        return null;
    }

    /**
     * Checks if it's possible to ascend without violating the ceiling limit
     * @param absolutePressure
     * @param time
     * @param data
     * @return
     */
    public static boolean possibleAscent(double absolutePressure, double time, CompartmentData data){
        double pressure = absolutePressure - timeToPressure(time, ascentRate);
        return pressure >= ZHL16.Ceiling(data);
    }

    /**
     * Checks if it's possible to ascend without violating the ceiling limit (with specified gradient factor)
     * @param absolutePressure
     * @param time
     * @param data
     * @param gf
     * @return
     */
    public static boolean possibleAscent(double absolutePressure, double time, CompartmentData data, double gf){
        double pressure = absolutePressure - timeToPressure(time, ascentRate);
        data.setGf(gf);
        return pressure >= ZHL16.Ceiling(data);
    }

    // FIXME
    @SafeVarargs
    public void recursiveFunction(boolean predicate, nextF f, double time, CompartmentData data){
        for(T arg: args){
            f(arg);
        }
        nextF result = f(time, data);
    }

    private nextF f(double time, CompartmentData data) {
    }


    public static void main(String args[]){
        Tests.testingDiveProfile();
        System.out.println();
        Tests.testingCeiling();
        System.out.println();
        Tests.testingDecoStop();
    }
}
