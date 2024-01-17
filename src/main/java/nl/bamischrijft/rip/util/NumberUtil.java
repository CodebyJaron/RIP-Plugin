package nl.bamischrijft.rip.util;

import java.text.DecimalFormat;

public class NumberUtil {

    public static String format(double input, String format) {
        return new DecimalFormat(format).format(input);
    }

}
