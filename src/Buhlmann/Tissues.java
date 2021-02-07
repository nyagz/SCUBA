package Buhlmann;

public class Tissues {
    /**
     All tissues are in form:
     N^2 half time (mins), N^2 A value, N^2 B value, He half time (mins), He A value, He B value
     */
    // ZHL16A is the initial version of BuhlmannEquation.Buhlmann decompression model
    public static double[][] ZHL16ATissues = new double[][]{
            {4.0, 1.2599, 0.5050, 1.5, 1.7435, 0.1911},
            {8.0, 1.0000, 0.6514, 3.0, 1.3838, 0.4295},
            {12.5, 0.8618, 0.7222, 4.7, 1.1925, 0.5446},
            {18.5, 0.7562, 0.7725, 7.0, 1.0465, 0.6265},
            {27.0, 0.6667, 0.8125, 10.2, 0.9226, 0.6917},
            {38.3, 0.5933, 0.8434, 14.5, 0.8211, 0.7420},
            {54.3, 0.5282, 0.8693, 20.5, 0.7309, 0.7841},
            {77.0, 0.4701, 0.8910, 29.1, 0.6506, 0.8195},
            {109.0, 0.4187, 0.9092, 41.1, 0.5794, 0.8491},
            {146.0, 0.3798, 0.9222, 55.1, 0.5256, 0.8703},
            {187.0, 0.3497, 0.9319, 70.6, 0.4840, 0.8860},
            {239.0, 0.3223, 0.9403, 90.2, 0.4460, 0.8997},
            {305.0, 0.2971, 0.9477, 115.1, 0.4112, 0.9118},
            {390.0, 0.2737, 0.9544, 147.2, 0.3788, 0.9226},
            {498.0, 0.2523, 0.9602, 187.9, 0.3492, 0.9321},
            {635.0, 0.2327, 0.9653, 239.6, 0.322, 0.9404}
    };

    // ZHL16B is a modification used for dive table calculations
    public static double[][] ZHL16BTissues = new double[][]{
            {4.0, 1.2599, 0.5050, 1.5, 1.7435, 0.1911},
            {8.0, 1.0000, 0.6514, 3.0, 1.3838, 0.4295},
            {12.5, 0.8618, 0.7222, 4.7, 1.1925, 0.5446},
            {18.5, 0.7562, 0.7725, 7.0, 1.0465, 0.6265},
            {27.0, 0.6667, 0.8125, 10.2, 0.9226, 0.6917},
            {38.3, 0.5933, 0.8434, 14.5, 0.8211, 0.7420},
            {54.3, 0.5282, 0.8693, 20.5, 0.7309, 0.7841},
            {77.0, 0.4701, 0.8910, 29.1, 0.6506, 0.8195},
            {109.0, 0.4187, 0.9092, 41.1, 0.5794, 0.8491},
            {146.0, 0.3798, 0.9222, 55.1, 0.5256, 0.8703},
            {187.0, 0.3497, 0.9319, 70.6, 0.4840, 0.8860},
            {239.0, 0.3223, 0.9403, 90.2, 0.4460, 0.8997},
            {305.0, 0.2971, 0.9477, 115.1, 0.4112, 0.9118},
            {390.0, 0.2737, 0.9544, 147.2, 0.3788, 0.9226},
            {498.0, 0.2523, 0.9602, 187.9, 0.3492, 0.9321},
            {635.0, 0.2327, 0.9653, 239.6, 0.322, 0.9404}
    };

    // ZHL16C is the version used in most dive computers
    public static double[][] ZHL16CTissues = new double[][]{
            {4.0, 1.2599, 0.5050, 1.5, 1.7435, 0.1911},
            {8.0, 1.0000, 0.6514, 3.0, 1.3838, 0.4295},
            {12.5, 0.8618, 0.7222, 4.7, 1.1925, 0.5446},
            {18.5, 0.7562, 0.7725, 7.0, 1.0465, 0.6265},
            {27.0, 0.6667, 0.8125, 10.2, 0.9226, 0.6917},
            {38.3, 0.5933, 0.8434, 14.5, 0.8211, 0.7420},
            {54.3, 0.5282, 0.8693, 20.5, 0.7309, 0.7841},
            {77.0, 0.4701, 0.8910, 29.1, 0.6506, 0.8195},
            {109.0, 0.4187, 0.9092, 41.1, 0.5794, 0.8491},
            {146.0, 0.3798, 0.9222, 55.1, 0.5256, 0.8703},
            {187.0, 0.3497, 0.9319, 70.6, 0.4840, 0.8860},
            {239.0, 0.3223, 0.9403, 90.2, 0.4460, 0.8997},
            {305.0, 0.2971, 0.9477, 115.1, 0.4112, 0.9118},
            {390.0, 0.2737, 0.9544, 147.2, 0.3788, 0.9226},
            {498.0, 0.2523, 0.9602, 187.9, 0.3492, 0.9321},
            {635.0, 0.2327, 0.9653, 239.6, 0.322, 0.9404}
    };
}