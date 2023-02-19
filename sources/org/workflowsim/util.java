package org.workflowsim;

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
}
