package Buhlmann;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;

public class Run{
    public static ZHL16BGF model;
    public static int ascentRate;
    public static int descentRate;
    public static double meterToBar;
    public static double surfacePressure;
    public static boolean lastStop6m;
    public static int decoStopSearchTime;
    public static double p3m;

    public static ArrayList<GasMix> gasList;
    public static ArrayList<GasMix> travelGasList = new ArrayList<>();
    public static ArrayList<DecoStop> decompressionStopTable = new ArrayList<>();
    public static ArrayList<Step> steps = new ArrayList<>();

    public Run(){
        model = new ZHL16BGF();
        ascentRate = 10;
        descentRate = 20;
        meterToBar = 0.09985;
        surfacePressure = 1.01325;
        decoStopSearchTime = 8;
        p3m = 3 * meterToBar;
        lastStop6m = false;

        gasList = new ArrayList<>();
        travelGasList = new ArrayList<>();
    }

    // Helper functions
    /**
     * Convert depth (in meters) to pressure (in bars)
     * @param depth
     * @return
     */
    public static double depthToPressure(double depth){
        return depth * meterToBar + surfacePressure;
    }

    /**
     * Everything above here has been checked and reformatted :)
     */
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static void addGas(int depth, int o2){
        int he = 0;
        gasList.add(new GasMix(depth, o2, 100 - o2 - he, he));
    }

    public static void addGas(int depth, int o2, boolean travel){
        int he = 0;
        if (travel == true) {
            travelGasList.add(new GasMix(depth, o2, 100 - o2 - he, he));
        } else{
            gasList.add(new GasMix(depth, o2, 100 - o2 - he, he));
        }
    }

    public static void addGas(int depth, int o2, int he){
        gasList.add(new GasMix(depth, o2, 100 - o2 - he, he));
    }

    public static void addGas(int depth, int o2, int he, boolean travel){
        if (travel == true){
            travelGasList.add(new GasMix(depth, o2, 100 - o2 - he, he));
        } else{
            gasList.add(new GasMix(depth, o2, 100 - o2 - he, he));
        }
    }

    /**
     * Used to sort the GasMix arrays based on depth
     * @param list
     * @return sorted arrayList
     */
    public static ArrayList<GasMix> sortList(ArrayList<GasMix> list){
        int size = list.size();
        int[] listArray = new int[size];
        ArrayList<GasMix> sortedList = new ArrayList<>();

        for (int i = 0; i < size; i++){
            listArray[i] = list.get(i).getDepth();
        }
        Arrays.sort(listArray);
        for (int i = 0; i < size; i++){
            for (int j = 0; j < list.size(); j++){
                if (list.get(j).getDepth() == listArray[i]){
                    sortedList.add(list.get(j));
                    list.remove(j);
                }
            }
        }
        return sortedList;
    }

    /**
     * Calculates all dive steps
     * @param maxDepth
     * @param bottomTime
     * @return
     */
    public static ArrayList<Step> plan(double maxDepth, int bottomTime){
        Step step = null;
        decompressionStopTable.clear();
        GasMix bottomGas = gasList.get(0);
        gasList = sortList(travelGasList);
        gasList.add(bottomGas);

        double absolutePressure = depthToPressure(maxDepth);
        for (Step s:diveDescent(absolutePressure, gasList)){
            steps.add(s);
            step = s;
        }

        ArrayList<GasMix> listToSort = gasList;
        listToSort.remove(0);
        gasList = sortList(listToSort);
        gasList.add(0, bottomGas);

        double t = bottomTime - step.getTime(); //TODO: Exception handling if t < 0

        step = stepNext(step, t, bottomGas);
        steps.add(step);

        ArrayList<Step> ascentSteps = diveAscent(step, gasList);
        for (Step s: ascentSteps){
            steps.add(s);
        }

        return steps;
    }

    // FIXME
    public static ArrayList<Step> plan(double maxDepth, int bottomTime, boolean descent){
        Step step = null;
        decompressionStopTable.clear();
        GasMix bottomGas = gasList.get(0);
        gasList = sortList(travelGasList);
        gasList.add(bottomGas);

        double absolutePressure = depthToPressure(maxDepth);
        for (Step s:diveDescent(absolutePressure, gasList)){
            steps.add(s);
            step = s;
        }

        ArrayList<GasMix> listToSort = gasList;
        listToSort.remove(0);
        gasList = sortList(listToSort);
        gasList.add(0, bottomGas);

        double t = bottomTime - step.getTime(); //TODO: Exception handling if t < 0

        step = stepNext(step, t, bottomGas);
        steps.add(step);

        ArrayList<Step> ascentSteps = diveAscent(step, gasList);
        for (Step s: ascentSteps){
            steps.add(s);
        }

        return steps;
    }

    /**
     * Calculates the first dive step
     * @param absolutePressure
     * @param gas
     * @return
     */
    public static Step stepStart(double absolutePressure, GasMix gas){
        CompartmentData data = ZHL16.initialisePressure(surfacePressure);
        Step step = new Step(DivePhase.START, absolutePressure, 0, gas, data);
        return step;
    }

    /**
     * Calculate the next dive step at constant depth that's advanced by a set amount of time
     * @param step
     * @param time
     * @param bottomGas
     * @return
     */
    public static Step stepNext(Step step, double time, GasMix bottomGas){
        CompartmentData data = tissuePressureDive(step.getAbsolutePressure(), time, bottomGas, step.getData());
        return new Step(DivePhase.DIVE, step.getAbsolutePressure(), step.getTime() + time, bottomGas, data);
    }

    /**
     * Calculate the next dive step that's advanced by a set amount of time
     * @param step
     * @param time
     * @param bottomGas
     * @param phase
     * @return
     */
    public static Step stepNext(Step step, double time, GasMix bottomGas, DivePhase phase){
        CompartmentData data = tissuePressureDive(step.getAbsolutePressure(), time, bottomGas, step.getData());
        return new Step(phase, step.getAbsolutePressure(), step.getTime() + time, bottomGas, data);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        /** Descent **/

    public static ArrayList<Step> diveDescent(double absolutePressure, ArrayList<GasMix> gasList){
        GasMix gas = gasList.get(0);
        Step step = stepStart(surfacePressure, gas);
        // yield step
        ArrayList<Stage> stages = descentStages(absolutePressure, gasList);
        for (int i = 0; i < stages.size(); i++){
            if (i > 0){
                step = gasSwitch(step, stages.get(i).getGas());
                steps.add(step);
            }
            double time = pressureToTime(stages.get(i).getGas().getDepth() - step.getAbsolutePressure(),
                    descentRate);
            step = nextStepDescent(step, time, gas);
            steps.add(step);
        }

        GasMix last = gasList.get(gasList.size() - 1);
        if (Math.abs(step.getAbsolutePressure() - depthToPressure(last.getDepth())) < Math.pow(10, 10)){
            step = gasSwitch(step, last);
            steps.add(step);
        }
        return steps;
    }

    /**
     * Calculate the stages for the dives descent
     * @param absolutePressure
     * @param gasList
     * @return
     */
    public static ArrayList<Stage> descentStages(double absolutePressure, ArrayList<GasMix> gasList){
        ArrayList<Stage> descentStages = new ArrayList<>();
        ArrayList<Mix> mixes = new ArrayList<>();
        for (int i = 0; i < gasList.size() - 1; i++){
            mixes.add(new Mix(gasList.get(i), gasList.get(i+1)));
        }
        for(Mix m: mixes){
            descentStages.add(new Stage(depthToPressure(m.getSecondGas().getDepth()), m.getFirstGas()));
        }
        GasMix lastGas = gasList.get(gasList.size() - 1);
        if (Math.abs(depthToPressure(lastGas.getDepth()) - absolutePressure) > 0){
            descentStages.add(new Stage(absolutePressure, lastGas));
        }
        return descentStages;
    }

    public static Step nextStepDescent(Step step, double time, GasMix gas){
        CompartmentData data = tissuePressureDescent(step.getAbsolutePressure(), time, gas, step.getData());
        double pressure = step.getAbsolutePressure() + timeToPressure(time, descentRate);
        return new Step(DivePhase.DESCENT, pressure, step.getTime() + time, gas, data);
    }

    /**
     * Calculates the tissues gas after descent
     * @param pressure
     * @param time
     * @param gas
     * @param data
     * @return
     */
    public static CompartmentData tissuePressureDescent(double pressure, double time, GasMix gas, CompartmentData data){
        double rate = descentRate * meterToBar;
        data = ZHL16.loadTissues(pressure, time, gas, rate, data);
        return data;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        /** Ascent **/

    /**
     * Checks dive steps needed to ascent from the current depth to the surface
     * @param startingStep
     * @param gasList
     * @return
     */
    public static ArrayList<Step> diveAscent(Step startingStep, ArrayList<GasMix> gasList){
        GasMix bottomGas = gasList.get(0);
        ArrayList<Step> steps = new ArrayList<>();
        Step step = NDL(startingStep, bottomGas); // returns null if NDL is not possible
        if (step != null){ // return
            steps.add(step);
            return steps;
        }

        step = startingStep;

        ArrayList<Stage> stages = decoFreeAscentStages(gasList);
        for(Step s: freeStagedAscent(step, stages)){
            steps.add(s);
        }

        stages = decompressionAscentStages(step.getAbsolutePressure(), gasList);
        ArrayList<Step> temp = decompressionStagedAscent(step, stages);
        for (Step s: temp){
            steps.add(s);
        }
        return steps;
    }

    /**
     * Determines if a NDL ascent is possible from the starting step
     * @param startingStep
     * @param gas
     * @return dive step if NDL dive is possible
     */
    public static Step NDL(Step startingStep, GasMix gas){
        double gf = ZHL16.gfHigh;
        double pressure = startingStep.getAbsolutePressure() - surfacePressure;
        double time = pressureToTime(pressure, ascentRate);
        Step step = nextStepAscent(startingStep, time, gas, gf);
        startingStep.getData().setGf(gf);
        double ceilingLimit = ZHL16.ceiling(startingStep.getData());
        if (step.getAbsolutePressure() < ceilingLimit){
            step = null;
        }
        return step;
    }

    /**
     * Calcuate the stages for a DCS-free ascent
     * @param gasList
     * @return
     */
    public static ArrayList<Stage> decoFreeAscentStages(ArrayList<GasMix> gasList){
        ArrayList<Mix> gasMixes = new ArrayList<>();
        ArrayList<Stage> decoFreeStages = new ArrayList<>();

        for (int i = 0; i < gasList.size() - 1; i++){
            Mix temp = new Mix(gasList.get(i), gasList.get(1 + i));
            gasMixes.add(temp);
        }

        for (Mix mix:gasMixes){
            Stage temp = new Stage(depthToPressure(((mix.getSecondGas().getDepth() - 1) / 4) * 3),
                    mix.getFirstGas());
            decoFreeStages.add(temp);
        }
        decoFreeStages.add(new Stage(surfacePressure, gasList.get(gasList.size() - 1)));
        return decoFreeStages;
    }

    /**
     *
     * @param start
     * @param stages
     * @return
     */
    public static ArrayList<Step> freeStagedAscent(Step start, ArrayList<Stage> stages){
        ArrayList<Step> steps = new ArrayList<>();
        ArrayList<Step> tempSteps;
        Step step = start;
        for (Stage stage:stages){
            if (step.getGas() != stage.getGas()){
                tempSteps = ascentSwitchGas(step, stage.getGas());
                if (ceilingLimitNotViolated(tempSteps.get(tempSteps.size() - 1).getAbsolutePressure(),
                        tempSteps.get(tempSteps.size() - 1).getData())) {
                    step = tempSteps.get(tempSteps.size() - 1);
                    steps.add(step);
                } else{
                    break;
                }
            }
            Step s = findFirstDecoStop(step, stage.getAbsolutePressure(), stage.getGas());
            if (s == step){
                break; // already at the deco zone
            } else{
                step = s;
                steps.add(step);
                if (Math.abs(step.getAbsolutePressure() - stage.getGas().getDepth()) > Math.pow(10, 10)){
                    break;
                }
            }
        }

        return steps;
    }

    /**
     * Staged ascent within the decompression zone
     * @param start
     * @param stages
     * @return
     */
    public static ArrayList<Step> decompressionStagedAscent(Step start, ArrayList<Stage> stages){
        ArrayList<Step> steps = new ArrayList<>();
        GasMix bottomGas = gasList.get(0);
        ArrayList<DecoStops> decoStages = decompressionStops(start, stages);
        Step step = start;
        for (DecoStops d: decoStages){
            // Switch the gas
            if (step.getAbsolutePressure() >= depthToPressure(d.getGas().getDepth()) && d.getGas() != bottomGas){
                for(Step s: ascentSwitchGas(step, d.getGas())){
                    steps.add(s);
                }
            }
            // Execute deco stop
            Step end = decompressionStop(step, d.getAscentTime(), d.getGas(), d.getNextGf());
            decompressionStopTable.add(new DecoStop(pressureToDepth(step.getAbsolutePressure()),
                    end.getTime() - step.getTime()));

            step = end;
            steps.add(step);
            // Ascend to next deco stop
            step = nextStepAscent(step, d.getAscentTime(), d.getGas(), d.getNextGf());
            steps.add(step);
        }

        return steps;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /** Need to sort these out **/
    /**
     * Calculates the next dive startingStep while ascending
     * @param currentStep
     * @param time
     * @param gas
     * @return
     */
    public static Step nextDiveStepAscent(Step currentStep, double time, GasMix gas){
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
    public static Step nextDiveStepAscent(Step currentStep, double time, GasMix gas, double gf){
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
    public static Step nextDiveStepAscent(Step currentStep, double time, GasMix gas, DivePhase phase){
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
    public static Step nextDiveStepAscent(Step currentStep, double time, GasMix gas, DivePhase phase, double gf){
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

    public static ArrayList<Step> ascentSwitchGas(Step step, GasMix gas){
        ArrayList<Step> steps = new ArrayList<>();
        double gasPressure = depthToPressure(gas.getDepth());
        if (Math.abs(step.getAbsolutePressure() - gasPressure) < Math.pow(10, 10)){
            steps.add(gasSwitch(step, gas));
        } else{
            double time = pressureToTime(step.getAbsolutePressure() - gasPressure, ascentRate);
            Step s1 = nextStepAscent(step, time, step.getGas());
            steps.add(s1);

            Step s2 = gasSwitch(s1, gas);
            steps.add(s2);

            double pressure = depthToPressure(gas.getDepth() / 9);
            time = pressureToTime(s2.getAbsolutePressure() - pressure, ascentRate);
            Step s3 = nextStepAscent(s2, time, gas);
            steps.add(s3);
        }

        return steps;
    }

    public static Step gasSwitch(Step step, GasMix gas){
        step.setGas(gas);
        step.setPhase(DivePhase.GAS_SWITCH);
        return step;
    }

    /**
     * Returns true iff the ceiling limit for the decompression limit hasn't been violated
     * @param absolutePressure
     * @param compartmentData
     * @return true/false
     */
    public static boolean ceilingLimitNotViolated(double absolutePressure, CompartmentData compartmentData){
        return absolutePressure >= ZHL16.ceiling(compartmentData);
    }

    /**
     * TODO: Write description
     * @param absolutePressure
     * @param gasList
     * @return
     */
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

    public static ArrayList<DecoStops> decompressionStops(Step step, ArrayList<Stage> stages){
        ArrayList<DecoStops> decoStops = new ArrayList<>();
        int k = stops(step.getAbsolutePressure());
        double gfStep = (ZHL16.gfHigh - ZHL16.gfLow) / k;
        double ts3m = pressureToTime(3 * meterToBar, ascentRate);
        double gf = step.getData().getGf();

        double pressure = step.getAbsolutePressure();
        double sixMeterStop = surfacePressure + 2 * (3 * meterToBar);
        for (Stage s:stages){
            double n = stops(pressure, (double) s.getGas().getDepth());
            for (int i = 0; i < n; i++){
                gf += gfStep;
                if (lastStop6m && Math.abs(pressure - i * (meterToBar * 3) - sixMeterStop) < Math.pow(10, 10)){
                    decoStops.add(new DecoStops(s.getGas().getDepth(), s.getGas(), 2 * ts3m,
                            gf + gfStep));
                    break;
                } else{
                    decoStops.add(new DecoStops(s.getGas().getDepth(), s.getGas(), ts3m, gf));
                }
            }
            pressure = s.getGas().getDepth();
        }

        return decoStops;
    }

    public static int stops(double startPressure){
        double k = (startPressure - surfacePressure) / (3 * meterToBar);
        return (int) k;
    }

    public static int stops(double startPressure, Double endPressure){
        if (endPressure == null){
            endPressure = surfacePressure;
        }
        double k = (startPressure - endPressure) / (3 * meterToBar);
        return (int) k;
    }

    /**
     * Calculates a decompression stop
     * @param step
     * @param nextTime
     * @param gas
     * @param gf
     * @return
     */
    public static Step decompressionStop(Step step, double nextTime, GasMix gas, double gf){
        CompartmentData data = tissuePressureDive(step.getAbsolutePressure(), 1, gas, step.getData());
        if (canAscend(step.getAbsolutePressure(), nextTime, data, gf)){
            return new Step(DivePhase.DECO_STOP, step.getAbsolutePressure(), step.getTime() + 1, gas, data);
        }

        double maxTime = 8;
        double time = recurseWhile(1, data, maxTime, step, gf, gas, nextTime).getKey();
        CompartmentData newData = recurseWhile(1, data, maxTime, step, gf, gas, nextTime).getValue();

        int n = 7;
        int k = bisectFind(n, step, gf, gas, newData, nextTime);
        k += 1; // at k diver should still stay at deco stop as, exec_deco_stop is true so ascend a minute later;

        time = time + k; // final time of a deco stop
        step = stepNext(step, time, gas, DivePhase.DECO_STOP);

        return step;
    }

    public static Pair<Double, CompartmentData> recurseWhile(double time, CompartmentData data, double maxTime,
                                                             Step step, double gf, GasMix gas, double nextTime) {
        double newTime = nextTime;
        Pair<Double, CompartmentData> result = nextF(time, data, maxTime, step, gas);
        Pair<Object, Object> args = null;
        while (invF(newTime, data, step, gf)) { // always true?
            args =  new Pair(result.getKey(), result.getValue());
            result = nextF(time, data, maxTime, step, gas);
            newTime = result.getKey();
        }
        if (args == null) {
            return new Pair(time, data);
        } else {
            return new Pair(args.getKey(), args.getValue());
        }
    }

    public static Pair<Double, CompartmentData> nextF(double time, CompartmentData data, double maxTime, Step step, GasMix gas){
        return new Pair(time + maxTime, tissuePressureDive(step.getAbsolutePressure(), maxTime, gas, data));
    }

    public static CompartmentData nextF(double k, Step step, GasMix gas, CompartmentData data){
        return tissuePressureDive(step.getAbsolutePressure(), k, gas, data);
    }


    public static boolean invF(double nextTime, CompartmentData data, Step step, double gf){
        if (canAscend(step.getAbsolutePressure(), nextTime, data, gf) == true){
            return false;
        } else{
            return true;
        }
    }

    public static int bisectFind(int n, Step step, double gf, GasMix gas, CompartmentData data, double nextTime){
        int lo = 1;
        int hi = n + 1;
        int k;
        while (lo < hi){
            k = (lo + hi) / 2;
            if (canAscend(step.getAbsolutePressure(), k, nextF(k,step, gas, data), gf)){
                lo = k + 1;
            } else{
                hi = k;
            }
        }
        return hi - 1; // hi is first k for which canAscend(k) is not true, so canAscend(hi - 1) is true
    }

    /**
     * Checks if diver can ascend from current depth without violating their ascent ceiling
     * @param absolutePressure
     * @param time
     * @param data
     * @param gf
     * @return
     */
    public static boolean canAscend(double absolutePressure, double time, CompartmentData data, double gf){
        double pressure = absolutePressure - timeToPressure(time, ascentRate);
        return pressure >= ZHL16.ceiling(data, gf);
    }
    ////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Finds the first decompression stop using Schreiner's
     * @param startingStep
     * @param absolutePressure
     * @param gas
     * @return
     */
    public static Step findFirstDecoStop(Step startingStep, double absolutePressure, GasMix gas){
        Step step = startingStep;
        double limit = ZHL16.ceiling(step.getData(), step.getData().getGf());
        limit = pressureMetersDiv3(limit);
        limit = Math.max(absolutePressure, limit);
        double t = pressureToTime(step.getAbsolutePressure() - limit, ascentRate);

        while(step.getAbsolutePressure() > limit && step.getAbsolutePressure() > absolutePressure){
            step = nextStepAscent(step, t, gas);
            limit = ZHL16.ceiling(step.getData(), step.getData().getGf());
            limit = pressureMetersDiv3(limit);
            limit = Math.max(absolutePressure, limit);
            t = pressureToTime(step.getAbsolutePressure() - limit, ascentRate);
        }

        Step stop = step;

        return stop;
    }

    /**
     * Calculates the next dive step when ascending for a certain amount of time
     * @param step
     * @param time
     * @param gas
     * @return
     */
    public static Step nextStepAscent(Step step, double time, GasMix gas){
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

    // TODO: CHECKED
    /**
     * Converts pressure (in bars) to depth (in meters)
     * @param absolutePressure
     * @return
     */
    public static double pressureToDepth(double absolutePressure){
        return Math.round((absolutePressure - surfacePressure) / meterToBar);
    }

    // TODO: CHECKED
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
    public static CompartmentData tissuePressureDive(double absolutePressure, double time, GasMix gas, CompartmentData data){
        return ZHL16.loadTissues(absolutePressure, time, gas, 0, data);
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
        return pressure >= ZHL16.ceiling(data);
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
        return pressure >= ZHL16.ceiling(data);
    }


    public static void main(String args[]){
        Tests.testingDiveProfile();
        System.out.println();
        Tests.testingCeiling();
        System.out.println();
        Tests.testingDecoStop();
        System.out.println();
        Tests.testingPlanning();
    }
}
