package org.workflowsim;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class util {
    public static boolean isNumeric(String str){
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if( !isNum.matches() ){
            return false;
        }
        return true;
    }
    private static final Random random;
    public static final int MAX_TRIES = 100;

    static {
        random = new Random();
    }

    public static double randomDouble(double min, double max) {
        if (min == max) {
            return min;
        } else {
            return min + random.nextDouble() * (max - min);
        }
    }
}
