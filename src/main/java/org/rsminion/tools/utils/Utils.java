package org.rsminion.tools.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utils {

    public static final String[] EMPTY_ARRAY = new String[0];

    public static boolean checkIntArrayMatch(int[] arr1, int... arr2) {
        return Arrays.equals(arr1, arr2);
    }

    public static boolean isBetween(int value, int min, int max) {
        return value > min && value < max;
    }

    public static boolean isNumber(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static <T> List<T> removeEmpty(T[] arr) {
        List<T> resultList = new ArrayList<>();
        for(T t : arr) {
            if(t != null) resultList.add(t);
        }
        return resultList;
    }

    public static String formatAsClass(String name) {
        return String.format("L%s;", name);
    }

    public static String stripClassFormat(String formatted) {
        return formatted.substring(1, formatted.length() - 1);
    }
}
