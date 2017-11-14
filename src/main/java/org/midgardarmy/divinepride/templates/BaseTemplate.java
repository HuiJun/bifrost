package org.midgardarmy.divinepride.templates;

import java.util.HashMap;
import java.util.Map;

public class BaseTemplate {

    static Map<String, String> replacements = new HashMap<>();

    public static String clean(String text) {
        StringBuilder result = new StringBuilder();
        result.append(text);
        replacements.forEach((k, v) -> {
            if (!k.isEmpty()) {
                String tmp = result.toString().replaceAll(k, v);
                result.setLength(0);
                result.append(tmp);
            }
        });
        return result.toString();
    }

    BaseTemplate() {}

}
