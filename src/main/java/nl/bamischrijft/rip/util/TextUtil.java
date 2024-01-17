package nl.bamischrijft.rip.util;

import me.lucko.helper.text3.Text;

import java.util.Map;

public class TextUtil {

    public static String format(String input, Object... placeholders) {
        for (int i = 0; i < placeholders.length; i++) {
            input = input.replace(String.format("{%s}", i), String.valueOf(placeholders[i]));
        }
        return Text.colorize(input);
    }

    public static String formatMap(String input, Map<String, String> map) {
        for (String placeholder : map.keySet()) {
            input = input.replace(String.format("{%s}", placeholder), map.get(placeholder));
        }
        return Text.colorize(input);
    }

}
