package VaryingPermeabilityModel;

import Buhlmann.*;

import java.util.ArrayList;
import java.util.Arrays;

public class RunVPM {
    public double surfaceTension;
    public double crumblingCompression;
    public double minimumInitialRadius;

    public VPM model;
    public int descentRate;
    public int ascentRate;
    public double meterToBar;
    public double surfacePressure;
    public boolean lastStop6m;
    public int decoStopSearchTime;
    public double p3m;


    public ArrayList<GasMix> gasList;
    public ArrayList<GasMix> travelGasList;
    public ArrayList<DecoStop> decompressionStopTable = new ArrayList<>();
    public ArrayList<Step> diveSteps = new ArrayList<>();

    public RunVPM(){
        this.surfaceTension = 0.179;
        this.crumblingCompression = 2.57;
        this.minimumInitialRadius = 0.8;

        this.model = new VPM();
        this.descentRate = 20;
        this.ascentRate = 10;
        this.meterToBar = 0.09985;
        this.surfacePressure = 1.01325;
        this.decoStopSearchTime = 8;
        this.p3m = 3 * meterToBar;
        this.lastStop6m = false;

        this.gasList = new ArrayList<>();
        this.travelGasList = new ArrayList<>();
    }

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
     * @return next dive step after time spent at current depth
     */
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
        diveSteps.add(step);
        ArrayList<Stage> stages = descentStages(absolutePressure, gasList);

        for (int i = 0; i < stages.size(); i++){
            if (i > 0){
                step = gasSwitch(step, stages.get(i).getGas());
                diveSteps.add(step);
            }
            double time = pressureToTime(stages.get(i).getAbsolutePressure() - step.getAbsolutePressure(),
                    descentRate);
            step = nextStepDescent(step, time, gas);
            diveSteps.add(step);
        }

        GasMix last = gasList.get(gasList.size() - 1);
        if (Math.abs(step.getAbsolutePressure() - depthToPressure(last.getDepth())) < Math.pow(10, -10)){
            if (last == gas){
                throw new GasConfigException("Bottom gas in travel gas mix");
            }
            step = gasSwitch(step, last);
            diveSteps.add(step);
        }
        return diveSteps;
    }

    public ArrayList<Step> diveAscent(Step startingStep, ArrayList<GasMix> gasList){
        double pCrush = Equations.pCrush(startingStep.getAbsolutePressure(), surfacePressure);
        double pminSS = allowedSupersaturationValue(pCrush);
        return null;
    }

    public double allowedSupersaturationValue(double pCrush){
        return Equations.pssMin(surfaceTension, crumblingCompression, minimumInitialRadius, crumblingCompression, pCrush);
    }

    // FIXME: Continue from here (Step 2)
    public void findFirstDecoStop(double pMinSS, CompartmentData loadingData){

        for(int i = 0; i < loadingData.getTissues().length; i++){

        }
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

    //TODO: Finish by adding ascent stages
    public ArrayList<Step> plan(double maxDepth, int bottomTime) throws PressureException,
            GradientFactorException, GasConfigException, EngineException {
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
        if (t <= 0) {
            throw new EngineException("Bottom time shorter than descent time");
        }
        step = stepNext(step, t, bottomGas);
        diveSteps.add(step);

        // FIXME: Remove comments to get the ascending step
        // ArrayList<Step> ascentSteps = diveAscent(step, gasList);
        // diveSteps.addAll(ascentSteps);
        return diveSteps;
    }
}
