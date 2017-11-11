package org.midgardarmy.divinepride.templates;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class BaseTemplate {

    private static final Logger logger = LoggerFactory.getLogger(BaseTemplate.class);

    static Map<String, String> replacements = new HashMap<>();

    public static String clean(String text) {
        StringBuilder result = new StringBuilder();
        result.append(text);
        logger.debug(text);
        replacements.forEach((k, v) -> {
            if (!k.isEmpty()) {
                String tmp = result.toString().replaceAll(k, v);
                logger.debug(result.toString());
                result.setLength(0);
                result.append(tmp);
            }
        });
        return result.toString();
    }
}
