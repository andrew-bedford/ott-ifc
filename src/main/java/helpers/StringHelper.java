package helpers;

public class StringHelper {

    public static String getStringWithoutNumbersOrApostrophes(String s) {
        return s.replaceAll("[\\d']","");
    }
}
