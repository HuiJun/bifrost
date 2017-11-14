package org.midgardarmy.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigUtils {

    private static final Logger logger = LoggerFactory.getLogger(ConfigUtils.class);

    public static Properties properties = new Properties();

    public static void init() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream resourceAsStream = classLoader.getResourceAsStream("application.properties");

        if (resourceAsStream != null) {
            try {
                properties.load(resourceAsStream);
            } catch (IOException e) {
                logger.debug(e.getLocalizedMessage());
            } finally {
                try {
                    resourceAsStream.close();
                } catch (IOException e) {
                    logger.debug(e.getLocalizedMessage());
                }
            }
        }
    }

    public static String get(String key) {
        if (ConfigUtils.properties.isEmpty()) {
            init();
        }
        return ConfigUtils.properties.getProperty(key);
    }

    public static void setProperty(String key, String value) {
        if (ConfigUtils.properties.isEmpty()) {
            init();
        }
        ConfigUtils.properties.setProperty(key, value);
    }

    private ConfigUtils() {}

}
