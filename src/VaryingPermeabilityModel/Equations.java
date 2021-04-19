package VaryingPermeabilityModel;

public class Equations {
    /**
     * Calculates the allowed supersaturation value fot the most conservative allowance of bubbles in the body
     * @param surfaceTension - surface tension 0.179 N/m
     * @param crumblingCompression - crumbling compression, 2.57 N/m
     * @param minimumInitialRadius - minimum initial radius r_0Min, 0.8μm
     * @param compositeParameter - composite parameter related to the critical volume, 250 [bar/min]
     * @param pCrush - constant throughout
     * @return pssMin
     */
    public static double allowedSupersaturationValue(double surfaceTension, double crumblingCompression,
                                                     double minimumInitialRadius, double compositeParameter,
                                                     double pCrush){
        return (2 * surfaceTension *
                        ((crumblingCompression - surfaceTension) / (minimumInitialRadius * crumblingCompression))) +
                (pCrush * (surfaceTension / crumblingCompression));
    }

    public static double pCrush(double pMaxDepth, double pSurface){
        return pMaxDepth - pSurface;
    }

    public static double initialDiveProfile(double pT, double pssMin){
        return pT + pssMin;
    }

    public static double newSupersaturationGradient(double decompressionTime, double halfTime, double surfaceTension,
                                                    double crumblingCompression, double lambda, double pssMin,
                                                    double pCrush){
        double k = Math.log(2 / halfTime);
        double b = pssMin + ((lambda * surfaceTension) / (crumblingCompression * (decompressionTime + (1 / k))));
        double c = (surfaceTension * lambda * pCrush) / (Math.pow(crumblingCompression, 2) *
                (decompressionTime + (1 / k)));
        double pssNew = 0.5 * (b + Math.pow(Math.pow(b, 2) - 4 * c, 0.5));
        return pssNew;
    }
}
