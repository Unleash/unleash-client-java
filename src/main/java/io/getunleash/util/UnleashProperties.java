package io.getunleash.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class UnleashProperties {
    static Properties appProperties;

    static {
        try (InputStream is =
                UnleashProperties.class.getClassLoader().getResourceAsStream("app.properties")) {
            appProperties = new Properties();
            appProperties.load(is);
        } catch (IOException ioException) {
            appProperties = new Properties();
            appProperties.setProperty("client.specification.version", "4.2.0");
        }
    }

    public static String getProperty(String propName) {
        return appProperties.getProperty(propName);
    }
}
