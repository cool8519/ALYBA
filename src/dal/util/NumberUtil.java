package dal.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NumberUtil {

	public static DecimalFormat DF_NO_DOT = new DecimalFormat("#");
	public static DecimalFormat DF_NO_DOT_THOUSAND = new DecimalFormat("#,###");
	public static DecimalFormat DF_ONLY_DOT3 = new DecimalFormat(".000");
	public static DecimalFormat DF_ONLY_DOT2 = new DecimalFormat(".00");
	public static DecimalFormat DF_ONLY_DOT1 = new DecimalFormat(".0");
	public static DecimalFormat DF_PERCENT_DOT3 = new DecimalFormat("##0.000");
	public static DecimalFormat DF_PERCENT_DOT2 = new DecimalFormat("##0.00");
	public static DecimalFormat DF_PERCENT_DOT1 = new DecimalFormat("##0.0");

	public static String numberToString(Object o) {
		return numberToString(o, DF_NO_DOT_THOUSAND);
	}

	public static String numberToString(Object o, NumberFormat format) {
		if(o == null || format == null) {
			return null;
		} else {
			return format.format(o);
		}
	}

	public static String numberToString(double d, NumberFormat format) {
		if(d == Double.NaN || format == null) {
			return null;
		} else {
			return format.format(d);
		}
	}

	public static String numberToString(long l, NumberFormat format) {
		if(format == null) {
			return null;
		} else {
			return format.format(l);
		}
	}

	public static boolean isNumeric(String str, boolean allowFloat) {
		if(str == null || "".equals(str.trim())) {
			return false;
		}
		if(allowFloat) {
	        if(str.charAt(str.length()-1) == '.') {
	            return false;
	        }
	        if(str.charAt(0) == '-') {
	            if(str.length() == 1) {
	                return false;
	            }
	            return withDecimalsParsing(str, 1);
	        }
	        return withDecimalsParsing(str, 0);			
		} else {
			for(char c : str.toCharArray()) {
				if(!Character.isDigit(c)) {
					return false;
				}
			}
			return true;
		}
	}

	public static boolean isNumeric(String str) {
		return isNumeric(str, false);
	}

    private static boolean withDecimalsParsing(String str, int beginIdx) {
        int decimalPoints = 0;
        for(int i = beginIdx; i < str.length(); i++) {
            boolean isDecimalPoint = str.charAt(i) == '.';
            if(isDecimalPoint) {
                decimalPoints++;
            }
            if(decimalPoints > 1) {
                return false;
            }
            if(!isDecimalPoint && !Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

	public static int getRandomNumber(int bound) {
		if(bound < 0)
			return -1;
		Random rand = new Random();
		return rand.nextInt(bound);
	}

	public static String getTwoDigitNumber(int num) {
		return ((num < 10) ? "0" : "") + String.valueOf(num);
	}

	public static List<Double> toDoubleList(List<String> list) throws Exception {
		List<Double> result = new ArrayList<Double>(list.size());
		for(String s : list) {
			if(isNumeric(s, true)) {
				result.add(new Double(s));
			} else {
				result.add(Double.NaN);
			}
		}
		return result;
	}

}
