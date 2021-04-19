package VaryingPermeabilityModel;

import Buhlmann.DecoStop;
import Buhlmann.GasMix;
import Buhlmann.Step;
import Buhlmann.ZHL16BGF;

import java.util.ArrayList;

public class RunVPM {
    public double surfaceTension;
    public double crumblingCompression;
    public double minimumInitialRadius;

    public VPM model;
    public int descentRate;
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
        this.meterToBar = 0.09985;
        this.surfacePressure = 1.01325;
        this.decoStopSearchTime = 8;
        this.p3m = 3 * meterToBar;
        this.lastStop6m = false;

        this.gasList = new ArrayList<>();
        this.travelGasList = new ArrayList<>();
    }
}
