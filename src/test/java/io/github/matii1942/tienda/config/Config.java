package io.github.matii1942.tienda.config;

import java.time.Duration;

/**
 * Settings read from system properties first, then environment variables.
 *
 * <pre>
 *   mvn test -DbaseUrl=https://my-store.onrender.com -Dheadless=false
 *   BASE_URL=http://localhost:3001 mvn test
 * </pre>
 */
public final class Config {

    private Config() {
    }

    /** Where the store is running. The API and the storefront share this origin. */
    public static String baseUrl() {
        String url = read("baseUrl", "BASE_URL", "http://localhost:3001");
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    /** Run Chrome without a window. Set to false to watch the UI scenarios run. */
    public static boolean headless() {
        return Boolean.parseBoolean(read("headless", "HEADLESS", "true"));
    }

    /** How long the UI waits for something to appear before failing. */
    public static Duration uiTimeout() {
        return Duration.ofSeconds(Long.parseLong(read("uiTimeoutSeconds", "UI_TIMEOUT_SECONDS", "10")));
    }

    private static String read(String property, String env, String fallback) {
        String value = System.getProperty(property);

        // An unresolved Maven placeholder means "not set".
        if (value == null || value.isBlank() || value.startsWith("${")) {
            value = System.getenv(env);
        }

        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
