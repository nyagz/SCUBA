package VaryingPermeabilityModel;

import Buhlmann.*;

import java.util.ArrayList;
import java.util.Arrays;

// TODO: descent and dive to be done the same way as Buhlmann's but ascent should use VPM instead of ZHL16
public class VPM extends ZHL16B{
    public double startP_N2;
    public double startP_He;
    public double[] n2_k;
    public double[] he_k;

    public double gfLow;
    public double gfHigh;
    public double waterVapourPressure;

    public VPM(){
        super();
        this.n2_k = kConst(N2_halfLife);
        this.he_k = kConst(He_halfLife);
        this.startP_He = 0;
        this.startP_N2 = 0.7902;
        this.gfLow = 0.3;
        this.gfHigh = 0.85;
        this.waterVapourPressure = 0.0627;
    }

    public ArrayList<Step> plan(double maxDepth, int bottomTime) throws GasConfigException, EngineError {
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
        diveSteps.add(step);

        return diveSteps;
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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /** Ignore everything below here*/

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

    private ArrayList<Step> diveDescent(double absolutePressure, ArrayList<GasMix> gasList) {
    }

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

    private ArrayList<GasMix> sortList(ArrayList<GasMix> list) {
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

    public static void main(String[] args){
        // System.out.println(planDive());

        // System.out.println("For the first compartment:");
        // System.out.println("Descent pressure: " + plan.get("descent")[0]);
        // System.out.println("Dive pressure: " + plan.get("dive")[0]);
        // System.out.println("Ascent pressure: " + plan.get("ascent")[0]);
    }

}
