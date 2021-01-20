package Buhlmann;

public class Gases {
    public static final GasMix EAN32 = new GasMix(0, 32, 68, 0);
    public static final GasMix Air = new GasMix(0, 21, 79, 0);
    public static final GasMix EAN50 = new GasMix(22, 50, 50, 0);
    public static final GasMix O2 = new GasMix(6, 100, 0, 0);

    public static GasMix getEan32(){
        return EAN32;
    }
}
