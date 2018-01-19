package helpers;

public class StringHelper {

    public static String getStringWithoutNumbersOrApostrophes(String s) {
        return s.replaceAll("[\\d']","");
    }

    public static String getStringWithoutLetters(String s) {
        return s.replaceAll("[a-zA-Z]","");
    }
}
