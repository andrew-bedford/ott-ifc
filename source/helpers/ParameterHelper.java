package helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParameterHelper {
    private static Map<String, List<String>> _parameters = new HashMap<String, List<String>>();

    public static void parse(String[] args) {
        List<String> options = null;
        for (int i = 0; i < args.length; i++) {
            final String a = args[i];

            if (a.charAt(0) == '-') {
                if (a.length() < 2) {
                    System.err.println("Error at argument " + a);
                    return;
                }

                options = new ArrayList<String>();
                _parameters.put(a.substring(1), options);
            }
            else if (options != null) {
                options.add(a);
            }
            else {
                System.err.println("Illegal parameter usage");
                return;
            }
        }
    }

    //TODO Find better name
    public static boolean isEmpty() {
        return _parameters.isEmpty();
    }

    public static boolean contains(String parameter) {
        return _parameters.containsKey(parameter);
    }

    public static String get(String parameter, Integer index) {
        return _parameters.get(parameter).get(index);
    }

}
