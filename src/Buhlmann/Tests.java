package Buhlmann;

import java.util.ArrayList;

import static Buhlmann.Gases.getAir;

public class Tests {
    public static void testBoth() throws PressureException, GasConfigException, EngineException, GradientFactorException {
        RunB engine = new RunB();
        engine.addGas(0, 21);
        ArrayList<Step> profile = engine.plan(35, 40);
        System.out.println("Dive steps:");
        for (Step p: profile){
            System.out.println("Step(phase = " + p.getPhase() + ", abs_p = " + p.getAbsolutePressure() + ", time = " +
                    p.getTime() + ", gf = " + p.getData().getGf() + ")");
        }
        System.out.println();
        System.out.println("Decompression stops:");
        for (DecoStop d: engine.decompressionStopTable){
            System.out.println("DecoStop(depth = " + d.getDepth() + ", time = " + d.getMin() + ")");
        }
        System.out.println();
        System.out.println("ZHL16C:");
        RunC engine2 = new RunC();
        engine2.addGas(0, 21);
        ArrayList<Step> profile2 = engine2.plan(35, 40);
        System.out.println("Dive steps:");
        for (Step p: profile2){
            System.out.println("Step(phase = " + p.getPhase() + ", abs_p = " + p.getAbsolutePressure() + ", time = " +
                    p.getTime() + ", gf = " + p.getData().getGf() + ")");
        }
        System.out.println();
        System.out.println("Decompression stops:");
        for (DecoStop d: engine2.decompressionStopTable){
            System.out.println("DecoStop(depth = " + d.getDepth() + ", time = " + d.getMin() + ")");
        }
    }

    public static void testZHL16B() throws PressureException, GasConfigException, EngineException, GradientFactorException {
        RunB engine = new RunB();
        engine.addGas(0, 32, 68);
        ArrayList<Step> profile = engine.plan(35, 40);
        System.out.println("Dive steps:");
        for (Step p: profile){
            System.out.println("Step(phase = " + p.getPhase() + ", abs_p = " + p.getAbsolutePressure() + ", time = " +
                    p.getTime() + ", gf = " + p.getData().getGf() + ")");
        }
        System.out.println();
        System.out.println("Decompression stops:");
        for (DecoStop d: engine.decompressionStopTable) {
            System.out.println("DecompressionStop(depth = " + d.getDepth() + ", time = " + d.getMin() + ")");
        }

    }

    public static void testZHL16C() throws PressureException, GasConfigException, EngineException, GradientFactorException {
        RunC engine = new RunC();
        engine.addGas(0, 32, 68);
        ArrayList<Step> profile = engine.plan(35, 40);
        System.out.println("Dive steps:");
        for (Step p: profile){
            System.out.println("Step(phase = " + p.getPhase() + ", abs_p = " + p.getAbsolutePressure() + ", time = " +
                    p.getTime() + ", gf = " + p.getData().getGf() + ")");
        }
        System.out.println();
        System.out.println("Decompression stops:");
        for (DecoStop d: engine.decompressionStopTable){
            System.out.println("DecompressionStop(depth = " + d.getDepth() + ", time = " + d.getMin() + ")");
        }
        System.out.println();


    }

    public static void main(String args[]) throws PressureException, GasConfigException, EngineException,
            GradientFactorException {
        // testZHL16B();
        // testZHL16C();
        testBoth();
    }
}
