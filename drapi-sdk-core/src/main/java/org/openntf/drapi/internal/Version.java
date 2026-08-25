package org.openntf.drapi.internal;

import java.io.IOException;
import java.util.Properties;

public final class Version {

    private static final String VERSION;

    static {
        try (var input = Version.class
            .getResourceAsStream("/version.properties")) {

            var properties = new Properties();
            properties.load(input);

            VERSION = properties.getProperty("version");
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static String get() {
        return VERSION;
    }

    private Version() {
    }
}
