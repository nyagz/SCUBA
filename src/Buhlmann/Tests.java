package Buhlmann;
// TODO: Delete the class
public class Tests {

    public static void testingDecoStop(){
        DecoStop test = new DecoStop(3,2);
        System.out.println("Checking it's empty");
        if (ZHL16.deco_stops.size() == 0){
            System.out.println("It's empty, good");
        } else{
            System.out.println("Oh no, it didn't work");
        }

        ZHL16.deco_stops.add(test);
        DecoStop test2 = new DecoStop(2, 3);
        ZHL16.deco_stops.add(test2);
        System.out.println("Total length of decompression stops: " + DecoStop.totalDecoStops(ZHL16.deco_stops));
        System.out.println("Added");
        System.out.println("New total number of decompression stops: " + ZHL16.deco_stops.size());

        DecoStop firstStop = ZHL16.deco_stops.get(0);
        System.out.println("Depth of first stop: " + firstStop.getDepth());
        System.out.println("Min of first stop: " + firstStop.getMin());
    }

    public static void testingDiveProfile(){
        CompartmentData data = Run.initialisePressure(1);
        data = ZHL16.loadTissues(1, 1.5, Gases.EAN32, 2, data);
    }
}
