package Buhlmann;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;

public class Run{
    public ZHL16GF model;
    public int ascentRate;
    public int descentRate;
    public double meterToBar;
    public double surfacePressure;
    public boolean lastStop6m;
    public int decoStopSearchTime;
    public double p3m;

    public ArrayList<GasMix> gasList;
    public ArrayList<GasMix> travelGasList = new ArrayList<>();
    public ArrayList<DecoStop> decompressionStopTable = new ArrayList<>();
    public ArrayList<Step> steps = new ArrayList<>();

    public Run(){
        this.model = new ZHL16GF();
        this.ascentRate = 10;
        this.descentRate = 20;
        this.meterToBar = 0.09985;
        this.surfacePressure = 1.01325;
        this.decoStopSearchTime = 8;
        this.p3m = 3 * meterToBar;
        this.lastStop6m = false;

        this.gasList = new ArrayList<>();
        this.travelGasList = new ArrayList<>();
    }

    // Helper functions
    /**
     * Convert depth (in meters) to pressure (in bars)
     * @param depth
     * @return
     */
    public double depthToPressure(double depth){
        return depth * meterToBar + surfacePressure;
    }

    /**
     * Converts pressure (in bars) to depth (in meters)
     * @param absolutePressure
     * @return
     */
    public double pressureToDepth(double absolutePressure){
        return Math.round((absolutePressure - surfacePressure) / meterToBar);
    }

    /**
     * Convert time into pressure change using the depth change rate
     * @param time
     * @param rate
     * @return
     */
    public double timeToPressure(double time, double rate){
        return time * rate * meterToBar;
    }

    /**
     * Convert pressure change to time using the depth change rate
     * @param pressure
     * @param rate
     * @return
     */
    public double pressureToTime(double pressure, double rate){
        return pressure / rate / meterToBar;
    }

    /**
     * Converts pressure to meters that's divisible by 3
     * @param absolutePressure
     * @return
     */
    public double pressureMetersDiv3(double absolutePressure){
        double result = Math.ceil((absolutePressure - surfacePressure) / (3 * meterToBar));
        return result * (meterToBar * 3) + surfacePressure;
    }

    /**
     * @param startPressure - absolute pressure at the starting depth [bar]
     * @param endPressure - absolute pressure at the ending depth [bar]
     * @return - number of decompression stops needed
     */
    public int stops(double startPressure, Double endPressure){
        if (endPressure == null){
            endPressure = surfacePressure;
        }
        double k = (startPressure - endPressure) / (3 * meterToBar);
        return (int) k;
    }

    public int stops(double startPressure){
        double k = (startPressure - surfacePressure) / (3 * meterToBar);
        return (int) k;
    }

    /**
     * @param absolutePressure - absolute pressure at current depth [bar]
     * @param compartmentData - Decompression model data
     * @return true/false iff the ceiling limit for the decompression limit hasn't been violated
     */
    public boolean ceilingLimitNotViolated(double absolutePressure, CompartmentData compartmentData)
            throws GradientFactorException {
        return absolutePressure >= model.ceiling(compartmentData);
    }

    /**
     * @param absolutePressure - absolute pressure of current depth [bar]
     * @param time - time of ascent [min]
     * @param data - Decompression model data
     * @param gf - gf for ceiling
     * @return true/false if diver can ascend from current depth without violating their ascent ceiling
     */
    public boolean canAscend(double absolutePressure, double time, CompartmentData data, double gf)
            throws GradientFactorException {
        double pressure = absolutePressure - timeToPressure(time, ascentRate);
        return pressure >= model.ceiling(data, gf);
    }

    /**
     * Calculates the first dive step
     * @param absolutePressure - absolute pressure of dive starting depth
     * @param gas - GasMix
     * @return first dive step
     */
    public Step stepStart(double absolutePressure, GasMix gas){
        CompartmentData data = model.initialisePressure(surfacePressure);
        Step step = new Step(DivePhase.START, absolutePressure, 0, gas, data);
        return step;
    }

    /**
     * @param step - current dive step
     * @param time - time spent at the current depth[min]
     * @param bottomGas - GasMix
     * @param phase - Dive phase
     * @return next dive step after time spent at current depth
     */
    public Step stepNext(Step step, double time, GasMix bottomGas, DivePhase phase){
        CompartmentData data = tissuePressureDive(step.getAbsolutePressure(), time, bottomGas, step.getData());
        return new Step(phase, step.getAbsolutePressure(), step.getTime() + time, bottomGas, data);
    }

    public Step stepNext(Step step, double time, GasMix bottomGas){
        CompartmentData data = tissuePressureDive(step.getAbsolutePressure(), time, bottomGas, step.getData());
        return new Step(DivePhase.DIVE, step.getAbsolutePressure(), step.getTime() + time, bottomGas, data);
    }

    /**
     * Calculate the next dive step while descending for a certain amount of time
     * @param step - current dive step
     * @param time - time to descent from current step
     * @param gas - Gas mix
     * @param phase - Dive phase
     * @return next dive step after descending for time
     */
    public Step nextStepDescent(Step step, double time, GasMix gas, DivePhase phase, double gf){
        CompartmentData data = tissuePressureDescent(step.getAbsolutePressure(), time, gas, step.getData());
        double pressure = step.getAbsolutePressure() + timeToPressure(time, descentRate);
        data.setGf(gf);
        return new Step(phase, pressure, step.getTime() + time, gas, data);
    }

    public Step nextStepDescent(Step step, double time, GasMix gas, DivePhase phase){
        CompartmentData data = tissuePressureDescent(step.getAbsolutePressure(), time, gas, step.getData());
        double pressure = step.getAbsolutePressure() + timeToPressure(time, descentRate);
        return new Step(phase, pressure, step.getTime() + time, gas, data);
    }

    public Step nextStepDescent(Step step, double time, GasMix gas){
        CompartmentData data = tissuePressureDescent(step.getAbsolutePressure(), time, gas, step.getData());
        double pressure = step.getAbsolutePressure() + timeToPressure(time, descentRate);
        return new Step(DivePhase.DESCENT, pressure, step.getTime() + time, gas, data);
    }

    /**
     * @param step - current dive step
     * @param time - time to ascent
     * @param gas - Gas mix
     * @param gf - gradient factor
     * @param phase - dive phase
     * @return next dive step when ascending for a certain amount of time
     */
    public Step nextStepAscent(Step step, double time, GasMix gas, double gf, DivePhase phase){
        CompartmentData data = tissuePressureAscent(step.getAbsolutePressure(), time, gas, step.getData());
        double pressure = step.getAbsolutePressure() - timeToPressure(time, ascentRate);
        data.setGf(gf);
        return new Step(phase, pressure, step.getTime() + time, gas, data);
    }

    public Step nextStepAscent(Step step, double time, GasMix gas){
        CompartmentData data = tissuePressureAscent(step.getAbsolutePressure(), time, gas, step.getData());
        double pressure = step.getAbsolutePressure() - timeToPressure(time, ascentRate);
        return new Step(DivePhase.ASCENT, pressure, step.getTime() + time, gas, data);
    }

    public Step nextStepAscent(Step step, double time, GasMix gas, double gf){
        CompartmentData data = tissuePressureAscent(step.getAbsolutePressure(), time, gas, step.getData());
        double pressure = step.getAbsolutePressure() - timeToPressure(time, ascentRate);
        data.setGf(gf);
        return new Step(DivePhase.ASCENT, pressure, step.getTime() + time, gas, data);
    }

    public Step nextStepAscent(Step step, double time, GasMix gas, DivePhase phase){
        CompartmentData data = tissuePressureAscent(step.getAbsolutePressure(), time, gas, step.getData());
        double pressure = step.getAbsolutePressure() - timeToPressure(time, ascentRate);
        return new Step(phase, pressure, step.getTime() + time, gas, data);
    }

    /**
     * @param absolutePressure - absolute pressure [bar]
     * @param time - time at depth
     * @param gas - gas mix
     * @param data - decompression model data
     * @return tissue gas loading after a certain amount of time at a certain depth
     */
    public CompartmentData tissuePressureDive(double absolutePressure, double time, GasMix gas,
                                                     CompartmentData data){
        return model.loadTissues(absolutePressure, time, gas, 0, data);
    }

    /**
     * @param pressure - absolute pressure [bar]
     * @param time - time of descent [min]
     * @param gas - gas mix
     * @param data - decompression model data
     * @return tissues gas after descent
     */
    public CompartmentData tissuePressureDescent(double pressure, double time, GasMix gas, CompartmentData data){
        double rate = descentRate * meterToBar;
        return model.loadTissues(pressure, time, gas, rate, data);
    }

    /**
     * @param absolutePressure - absolute pressure [bar]
     * @param time - time of ascent [min]
     * @param gas - gas mix
     * @param data - decompression model data
     * @return tissues pressure after ascent
     */
    public CompartmentData tissuePressureAscent(double absolutePressure, double time, GasMix gas,
                                                       CompartmentData data){
        double rate = -ascentRate * meterToBar;
        return model.loadTissues(absolutePressure, time, gas, rate, data);
    }

    /**
     * @param step - current dive step
     * @param gas - gas mix
     * @return Switch gas mix in new dive step
     */
    public static Step gasSwitch(Step step, GasMix gas){
        step.setGas(gas);
        step.setPhase(DivePhase.GAS_SWITCH);
        return step;
    }

    /**
     * The last gas on the gas mix list is bottom gas, all the rest are travel gases
     * @param absolutePressure - absolute pressure of destination deoth [bar]
     * @param gasList - list of all gas mixes
     * @return Descent from surface to absolute pressure of destination
     */
    public ArrayList<Step> diveDescent(double absolutePressure, ArrayList<GasMix> gasList) throws
            GasConfigException {
        boolean descent = true;
        GasMix gas = gasList.get(0);
        Step step = stepStart(surfacePressure, gas);
        steps.add(step);
        ArrayList<Stage> stages = descentStages(absolutePressure, gasList);

        for (int i = 0; i < stages.size(); i++){
            if (i > 0){
                step = gasSwitch(step, stages.get(i).getGas());
                steps.add(step);
            }
            double time = pressureToTime(stages.get(i).getAbsolutePressure() - step.getAbsolutePressure(),
                    descentRate);
            step = nextStepDescent(step, time, gas);
            steps.add(step);
        }

        GasMix last = gasList.get(gasList.size() - 1);
        if (Math.abs(step.getAbsolutePressure() - depthToPressure(last.getDepth())) < Math.pow(10, -10)){
            if (last == gas){
                throw new GasConfigException("Bottom gas in travel gas mix");
            }
            step = gasSwitch(step, last);
            steps.add(step);
        }
        return steps;
    }

    /**
     * @param startingStep - starting dive step
     * @param gasList - list of all gases
     * @return dive steps needed to ascent from the current depth to the surface
     */
    public ArrayList<Step> diveAscent(Step startingStep, ArrayList<GasMix> gasList)
            throws GradientFactorException, PressureException {
        GasMix bottomGas = gasList.get(0);
        ArrayList<Step> steps = new ArrayList<>();
        Step step = NDL(startingStep, bottomGas); // returns null if NDL is not possible
        if (step != null){ // no-decompression limit dive is possible
            steps.add(step);
            return steps;
        }

        step = startingStep;

        ArrayList<Stage> stages = decoFreeAscentStages(gasList);
        ArrayList<Step> newSteps = freeStagedAscent(step, stages);
        steps.addAll(newSteps);

        step = newSteps.get(newSteps.size() - 1);

        if (Math.abs(step.getAbsolutePressure() - surfacePressure) < Math.pow(10, -10)){
            throw new PressureException("Shouldn't be at the surface - this stage is a non-ndl dive");
        }

        stages = decompressionAscentStages(step.getAbsolutePressure(), gasList);
        steps.addAll(decompressionStagedAscent(step, stages));
        return steps;
    }

    /**
     * @param startingStep - starting dive step
     * @param gas - gas mix
     * @return dive step if NDL dive is possible
     */
    public Step NDL(Step startingStep, GasMix gas) throws GradientFactorException {
        double gf = model.gfHigh;
        double pressure = startingStep.getAbsolutePressure() - surfacePressure;
        double time = pressureToTime(pressure, ascentRate);
        Step step = nextStepAscent(startingStep, time, gas, gf);
        double ceilingLimit = model.ceiling(step.getData(), gf);
        if (step.getAbsolutePressure() < ceilingLimit){
            step = null; //
        }
        return step;
    }

    /**
     * @param startingStep - starting dive step
     * @param absolutePressure - absolute pressure of target depth
     * @param gas - gas mix
     * @return first decompression stop using Schreiner's
     */
    public Step findFirstDecoStop(Step startingStep, double absolutePressure, GasMix gas)
            throws PressureException, GradientFactorException {
        if (startingStep.getAbsolutePressure() < absolutePressure){
            throw new PressureException("Starting step's pressure should be less than the absolute pressure at the " +
                    "target depth");
        }

        Step step = startingStep;
        double limit = model.ceiling(step.getData(), step.getData().getGf());
        limit = pressureMetersDiv3(limit);
        limit = Math.max(absolutePressure, limit);
        double t = pressureToTime(step.getAbsolutePressure() - limit, ascentRate);

        while(step.getAbsolutePressure() > limit && step.getAbsolutePressure() > absolutePressure){
            step = nextStepAscent(step, t, gas);
            limit = model.ceiling(step.getData(), step.getData().getGf());
            limit = pressureMetersDiv3(limit);
            limit = Math.max(absolutePressure, limit);
            t = pressureToTime(step.getAbsolutePressure() - limit, ascentRate);
        }

        Step stop = step;

        if(stop.getAbsolutePressure() - absolutePressure <= Math.pow(10, -10)){
            throw new PressureException("Pressure is way too high");
        }
        return stop;
    }

    /**
     * @param absolutePressure - absolute pressure of destination depth [bar]
     * @param gasList - list of all gases
     * @return stages for the dives descent
     */
    public ArrayList<Stage> descentStages(double absolutePressure, ArrayList<GasMix> gasList){
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

    /**
     * @param gasList - list of all gases
     * @return stages for a DCS-free ascent
     */
    public ArrayList<Stage> decoFreeAscentStages(ArrayList<GasMix> gasList){
        ArrayList<Mix> gasMixes = new ArrayList<>();
        ArrayList<Stage> decoFreeStages = new ArrayList<>();
        double temp2;

        for (int i = 0; i < gasList.size() - 1; i++){
            Mix temp = new Mix(gasList.get(i), gasList.get(1 + i));
            gasMixes.add(temp);
        }

        for (Mix mix:gasMixes){
            temp2 = depthToPressure((mix.getSecondGas().getDepth() - 1.0) / 3.0);
            Stage temp = new Stage((Math.floor(temp2) + 1) * 3, mix.getFirstGas());
            decoFreeStages.add(temp);
        }
        decoFreeStages.add(new Stage(surfacePressure, gasList.get(gasList.size() - 1)));
        return decoFreeStages;
    }

    /**
     * @param absolutePressure - absolute pressure of destination [bar]
     * @param gasList - list of all gases
     * @return stages for decompression ascent
     */
    public ArrayList<Stage> decompressionAscentStages(double absolutePressure, ArrayList<GasMix> gasList)
            throws PressureException {
        ArrayList<Stage> stages = new ArrayList<>();
        ArrayList<Mix> gasMixes = new ArrayList<>();
        double temp2;

        if(absolutePressure < surfacePressure){
            throw new PressureException("Starting absolute pressure is too low");
        }
        for (int i = 0; i < gasList.size() - 1; i++){
            Mix temp = new Mix(gasList.get(i), gasList.get(1 + i));
            gasMixes.add(temp);
        }
        for (Mix mix:gasMixes){
            if(depthToPressure(mix.getSecondGas().getDepth()) < absolutePressure){
                temp2 = depthToPressure(mix.getSecondGas().getDepth() / 3.0);
                Stage temp = new Stage(depthToPressure(Math.floor(temp2) * 3),
                        mix.getFirstGas());
                stages.add(temp);
            }
        }
        stages.add(new Stage(surfacePressure, gasList.get(gasList.size() - 1)));
        return stages;
    }

    /**
     * Gas mix rules:
     *      1. There's one non-travel gas mix on gas mix list
     *      2. If no travel gas mixes, then first gas mix is bottom gas and its switch depth is 0m.
     *      3. All travel gas mixes have different switch depth.
     *      4. All decompression gas mixes have different switch depth.
     *      5. All decompression gas mixes have switch depth greater than zero.
     *      6. There is no gas mix with switch depth deeper than maximum dive depth.
     * @param depth - max dive depth
     * @throws GasConfigException if gas mix rules are violated
     */
    public void validateGasList(double depth) throws GasConfigException {
        if(gasList.isEmpty()){
            throw new GasConfigException("No bottom gas mix has been added");
        }
        if(travelGasList.isEmpty() && gasList.get(0).getDepth() != 0){
            throw new GasConfigException("Bottom gas switch depth isn't 0m");
        }
        int k = travelGasList.size();
        if (travelGasList.size() != 0){
            double[] depths = new double[travelGasList.size()];
            for(int i = 0; i < travelGasList.size(); i++) {
                depths[i] = travelGasList.get(i).getDepth();
            }
            Arrays.sort(depths);
            if(removeDuplicateDepths(depths).size() != k){
                throw new GasConfigException("Two or more travel gases have the same switch depth");
            }
            k = gasList.size() - 1;
            depths = new double[gasList.size() - 1];
            for(int i = 1; i < gasList.size(); i++){
                depths[i] = gasList.get(i).getDepth();
            }
            Arrays.sort(depths);
            if(removeDuplicateDepths(depths).size() != k){
                throw new GasConfigException("Two or more decompression gasses have the same switch depth");
            }
            for(double d: depths){
                if(d == 0){
                    throw new GasConfigException("Decompression gas mix switch depth is 0m");
                }
            }

            for (GasMix m: gasList){
                if(m.getDepth() > depth){
                    throw new GasConfigException("Gas mix switch deeper than maximum dive depth");
                }
            }
            for (GasMix m: travelGasList){
                if(m.getDepth() > depth){
                    throw new GasConfigException("Gas mix switch deeper than maximum dive depth");
                }
            }
        }
    }

    /**
     * @param depths - array to remove duplicates from
     * @return -Array list of integers for array (depths) without any duplicated entries
     */
    public ArrayList<Double> removeDuplicateDepths(double[] depths){
        if (depths.length == 0){
            return null;
        }
        ArrayList<Double> nodubs = new ArrayList<>();
        nodubs.add(depths[0]);
        for(int i = 1; i < depths.length; i++){
            if(nodubs.get(nodubs.size() - 1) != depths[i]){
                nodubs.add(depths[i]);
            }
        }
        return nodubs;
    }


    /**
     * @param step - current dive step
     * @param gas - gas to switch to
     * @return dive step once gas has been switched
     */
    public ArrayList<Step> ascentSwitchGas(Step step, GasMix gas) throws PressureException {
        ArrayList<Step> steps = new ArrayList<>();
        double gasPressure = depthToPressure(gas.getDepth());
        if(step.getAbsolutePressure() - gasPressure > p3m){
            throw new PressureException("Pressure is too high");
        }
        if (Math.abs(step.getAbsolutePressure() - gasPressure) < Math.pow(10, -10)){
            steps.add(gasSwitch(step, gas));
        } else{
            if(step.getAbsolutePressure() < gasPressure){
                throw new PressureException("Pressure is high");
            }

            double time = pressureToTime(step.getAbsolutePressure() - gasPressure, ascentRate);
            Step s1 = nextStepAscent(step, time, step.getGas());

            Step s2 = gasSwitch(s1, gas);

            double pressure = depthToPressure(Math.floor(gas.getDepth() / 3.0) * 3);
            time = pressureToTime(s2.getAbsolutePressure() - pressure, ascentRate);
            Step s3 = nextStepAscent(s2, time, gas);

            steps.add(s1);
            steps.add(s2);
            steps.add(s3);
        }
        return steps;
    }

    /**
     * @param start - starting dive step
     * @param stages - dive stages
     * @return ascent until first decompression stop
     */
    public ArrayList<Step> freeStagedAscent(Step start, ArrayList<Stage> stages) throws GradientFactorException,
            PressureException {
        ArrayList<Step> steps = new ArrayList<>();
        ArrayList<Step> tempSteps;

        Step step = start;
        for (Stage stage:stages){
            if (step.getGas() != stage.getGas()){
                tempSteps = ascentSwitchGas(step, stage.getGas());
                if (ceilingLimitNotViolated(tempSteps.get(tempSteps.size() - 1).getAbsolutePressure(),
                        tempSteps.get(tempSteps.size() - 1).getData())) {
                    step = tempSteps.get(tempSteps.size() - 1);
                    steps.addAll(tempSteps); // performed gas switch
                    steps.add(step);
                } else{
                    break;
                }
            }
            //checks if there's a first decompression stop at the ascent stage
            Step s = findFirstDecoStop(step, stage.getAbsolutePressure(), stage.getGas());
            if (s == step){
                break; // already at the deco zone
            } else{
                step = s;
                steps.add(step);
                if (Math.abs(step.getAbsolutePressure() - stage.getAbsolutePressure()) > Math.pow(10, -10)){
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
    public ArrayList<Step> decompressionStagedAscent(Step start, ArrayList<Stage> stages)
            throws GradientFactorException, PressureException {
        ArrayList<Step> steps = new ArrayList<>();
        ArrayList<Step> temp;
        GasMix bottomGas = gasList.get(0);
        ArrayList<DecoStops> decoStages = decompressionStops(start, stages);
        Step step = start;
        for (DecoStops d: decoStages){
            // Switch the gas
            if (step.getAbsolutePressure() >= depthToPressure(d.getGas().getDepth()) && d.getGas() != bottomGas){
                temp = ascentSwitchGas(step, d.getGas());
                for(Step s: temp){
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

    /**
     * @param step - current dive step
     * @param stages - decompression ascent stages
     * @return A collection of decompression stops
     */
    public ArrayList<DecoStops> decompressionStops(Step step, ArrayList<Stage> stages){
        ArrayList<DecoStops> decoStops = new ArrayList<>();
        int n;
        int k = stops(step.getAbsolutePressure());
        double gfStep = (model.gfHigh - model.gfLow) / k;
        double ts3m = pressureToTime(p3m, ascentRate);
        double gf = step.getData().getGf();

        double pressure = step.getAbsolutePressure();
        double sixMeterStop = surfacePressure + 2 * (3 * meterToBar);
        for (Stage s:stages){
            n = stops(pressure, s.getAbsolutePressure());
            for (int i = 0; i < n; i++){
                gf += gfStep;
                if (lastStop6m && Math.abs(pressure - i * (meterToBar * 3) - sixMeterStop) < Math.pow(10, -10)){
                    decoStops.add(new DecoStops(s.getAbsolutePressure(), s.getGas(), 2 * ts3m,
                            gf + gfStep));
                    break;
                } else{
                    decoStops.add(new DecoStops(s.getAbsolutePressure(), s.getGas(), ts3m, gf));
                }
            }
            pressure = s.getAbsolutePressure();
        }
        return decoStops;
    }

    // FIXME: AHHHHHHHHHHHHHHHHHHHHHHH
    /**
     * Calculates a decompression stop
     * @param step - current decompression step
     * @param nextTime - time to ascend to next decompression stop [min]
     * @param gas - GasMix
     * @param gf - gradient factor of next decompression stop
     * @return a decompression stop
     */
    public Step decompressionStop(Step step, double nextTime, GasMix gas, double gf)
            throws GradientFactorException {
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

    /**
     * @param depth - switch depth of the gas mix
     * @param o2 - percentage of gas that's O2
     * @param he - percentage of gas that's He
     * @param travel - boolean to show if gas is a travel gas (if not specified assume true)
     */
    public void addGas(int depth, int o2, int he, boolean travel){
        if (travel == true){
            travelGasList.add(new GasMix(depth, o2, 100 - o2 - he, he));
        } else{
            gasList.add(new GasMix(depth, o2, 100 - o2 - he, he));
        }
    }
    public void addGas(int depth, int o2){
        int he = 0;
        gasList.add(new GasMix(depth, o2, 100 - o2 - he, he));
    }

    public void addGas(int depth, int o2, boolean travel){
        int he = 0;
        if (travel == true) {
            travelGasList.add(new GasMix(depth, o2, 100 - o2 - he, he));
        } else{
            gasList.add(new GasMix(depth, o2, 100 - o2 - he, he));
        }
    }

    public void addGas(int depth, int o2, int he){
        gasList.add(new GasMix(depth, o2, 100 - o2 - he, he));
    }

    /**
     * Used to sort the GasMix arrays based on depth
     * @param list - list to be sorted
     * @return sorted arrayList
     */
    public static ArrayList<GasMix> sortList(ArrayList<GasMix> list){
        int size = list.size();
        double[] listArray = new double[size];
        ArrayList<GasMix> sortedList = new ArrayList<>();

        for (int i = 0; i < size; i++){
            listArray[i] = list.get(i).getDepth();
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
    public ArrayList<Step> plan(double maxDepth, int bottomTime) throws PressureException,
            GradientFactorException, GasConfigException, EngineError {
        Step step = null;
        ArrayList<Step> tempSteps;
        decompressionStopTable.clear();
        validateGasList(maxDepth);
        GasMix bottomGas = gasList.get(0);
        gasList = sortList(travelGasList);
        gasList.add(bottomGas);
        double absolutePressure = depthToPressure(maxDepth);
        tempSteps = diveDescent(absolutePressure, gasList);
        step = tempSteps.get(tempSteps.size() - 1);
        ArrayList<GasMix> listToSort = gasList;
        listToSort.remove(0);
        gasList = sortList(listToSort);
        gasList.add(0, bottomGas);

        double t = bottomTime - step.getTime();
        if (t <= 0){
            throw new EngineError("Bottom time shorter than descent time");
        }
        step = stepNext(step, t, bottomGas);
        steps.add(step);

        ArrayList<Step> ascentSteps = diveAscent(step, gasList);
        // FIXME: Should this for loop be deleted?
        // for (Step s: ascentSteps){
        //     steps.add(s);
        // }
        steps.addAll(ascentSteps);
        return steps;
    }

    // TODO: Check if needs to be updated based on plan above
    public ArrayList<Step> plan(double maxDepth, int bottomTime, boolean descent) throws PressureException,
            GradientFactorException, GasConfigException, EngineError {
        Step step = null;

        decompressionStopTable.clear();
        validateGasList(maxDepth);
        GasMix bottomGas = gasList.get(0);
        gasList = sortList(travelGasList);
        gasList.add(bottomGas);

        double absolutePressure = depthToPressure(maxDepth);
        if(descent){
            for (Step s:diveDescent(absolutePressure, gasList)){
                steps.add(s);
            }
        } else{
            step = stepStart(absolutePressure, bottomGas);
            steps.add(step);
        }

        ArrayList<GasMix> listToSort = gasList;
        listToSort.remove(0);
        gasList = sortList(listToSort);
        gasList.add(0, bottomGas);

        double t = bottomTime - step.getTime();
        if (t <= 0){
            throw new EngineError("Bottom time shorter than descent time");
        }
        step = stepNext(step, t, bottomGas);
        steps.add(step);

        ArrayList<Step> ascentSteps = diveAscent(step, gasList);
        for (Step s: ascentSteps){
            steps.add(s);
        }
        steps.addAll(diveAscent(step, gasList));
        return steps;
    }

    //FIXME: AHHHHHHHHHHHH
    public Pair<Double, CompartmentData> recurseWhile(double time, CompartmentData data, double maxTime, Step step,
                                                      double gf, GasMix gas, double nextTime)
            throws GradientFactorException {
        double newTime = nextTime;
        Pair<Double, CompartmentData> result = nextF(time, data, maxTime, step, gas);
        Pair<Object, Object> args = null;
        while (invF(newTime, result.getValue(), step, gf)) { // always true?
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

    public Pair<Double, CompartmentData> nextF(double time, CompartmentData data, double maxTime, Step step, GasMix gas){
        return new Pair(time + maxTime, tissuePressureDive(step.getAbsolutePressure(), maxTime, gas, data));
    }

    public CompartmentData nextF(double k, Step step, GasMix gas, CompartmentData data){
        return tissuePressureDive(step.getAbsolutePressure(), k, gas, data);
    }


    public boolean invF(double nextTime, CompartmentData data, Step step, double gf) throws GradientFactorException {
        if (canAscend(step.getAbsolutePressure(), nextTime, data, gf) == true){
            return false;
        } else{
            return true;
        }
    }

    public int bisectFind(int n, Step step, double gf, GasMix gas, CompartmentData data, double nextTime) throws GradientFactorException {
        int lo = 1;
        int hi = n + 1;
        int k;
        while (lo < hi){
            k = (int) Math.floor((lo + hi) / 2.0);
            if (!canAscend(step.getAbsolutePressure(), nextTime, tissuePressureDive(step.getAbsolutePressure(), k, gas, data), gf)){
                lo = k + 1;
            } else{
                hi = k;
            }
        }
        return hi - 1; // hi is first k for which canAscend(k) is not true, so canAscend(hi - 1) is true
    }
    /**
     * FIXME: Add from above here
     * Everything above here has been checked and reformatted :)
     */
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        /** Descent **/
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        /** Ascent **/
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /** Need to sort these out **/

    ////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Checks if it's possible to ascend without violating the ceiling limit
     * @param absolutePressure
     * @param time
     * @param data
     * @return
     */
    public boolean possibleAscent(double absolutePressure, double time, CompartmentData data) throws GradientFactorException {
        double pressure = absolutePressure - timeToPressure(time, ascentRate);
        return pressure >= model.ceiling(data);
    }

    /**
     * Checks if it's possible to ascend without violating the ceiling limit (with specified gradient factor)
     * @param absolutePressure
     * @param time
     * @param data
     * @param gf
     * @return
     */
    public boolean possibleAscent(double absolutePressure, double time, CompartmentData data, double gf) throws GradientFactorException {
        double pressure = absolutePressure - timeToPressure(time, ascentRate);
        data.setGf(gf);
        return pressure >= model.ceiling(data);
    }


    public static void main(String args[]) throws GradientFactorException, PressureException, GasConfigException {
        // Tests.testingDiveProfile();
        // System.out.println();
        // Tests.testingCeiling();
        // System.out.println();
        // Tests.testingDecoStop();
        // System.out.println();
        // Tests.testingPlanning();
    }
}
