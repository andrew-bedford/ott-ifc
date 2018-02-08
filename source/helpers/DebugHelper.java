package helpers;

public class DebugHelper {
    private static boolean _debug = false;

    public static void enableDebugMode() { _debug = true; }
    public static void disableDebugMode() { _debug = false; }

    public static void println(String s) { if (_debug) { System.out.println(s); } }

}
