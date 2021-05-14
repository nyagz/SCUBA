package VaryingPermeabilityModel;

import Buhlmann.*;

import java.util.ArrayList;

public class Tests {
    public static void testingVPM() throws PressureException, GasConfigException, EngineException, GradientFactorException {
        RunVPM engine = new RunVPM();
        engine.addGas(0, 21);
        ArrayList<Step> profile = engine.plan(35, 40);
        System.out.println("Dve steps:");
        for (Step p: profile){
            System.out.println("Step(phase = " + p.getPhase() + ", abs_p = " + p.getAbsolutePressure() + ", time = " +
                    p.getTime() + ", gf = " + p.getData().getGf() + ")");
        }
    }

    public static void main(String args[]) throws PressureException, GasConfigException, EngineException,
            GradientFactorException {
        testingVPM();
    }
}
