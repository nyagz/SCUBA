package Buhlmann;

public class Run{

    // TODO: Function to plan dive once given the max depth to dive to and time spent at the bottom depth (in minutes)
    public static void plan(double maxDepth, int bottomTime){
    }

    //Initialises pressures in each compartment assuming the surface pressure is 1
    public static CompartmentData initialisePressure(){
        double pN2 = ZHL16.startP_N2 * (ZHL16.surfacePressure - ZHL16.waterVapourPressure);
        double pHe = ZHL16.startP_he;
        TissueLoader[] tissues = new TissueLoader[16];

        for (int i = 0; i < tissues.length; i++){
            tissues[i] = new TissueLoader(pN2, pHe);
        }
        return new CompartmentData(tissues, ZHL16.gfLow);
    }

    //Initialises pressures in each compartment
    public static CompartmentData initialisePressure(double sp){
        double pN2 = ZHL16.startP_N2 * (sp - ZHL16.waterVapourPressure);
        double pHe = ZHL16.startP_he;
        TissueLoader[] tissues = new TissueLoader[16];
        for (int i = 0; i < tissues.length; i++){
            tissues[i] = new TissueLoader(pN2, pHe);
        }
        return new CompartmentData(tissues, ZHL16.gfLow);
    }

    public static void main(String args[]){
        Tests.testingDiveProfile();
        Tests.testingCeiling();
    }
}
