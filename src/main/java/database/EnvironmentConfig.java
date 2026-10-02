package database;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Loads database settings from an untracked {@code .env} file so local
 * development does not require exporting variables in every shell.
 *
 * Values already present in the real environment always win, so an explicit
 * {@code RESQGRID_DB_PASSWORD} export overrides anything in the file.
 */
public final class EnvironmentConfig {

    private static final Map<String, String> VALUES = load();

    private EnvironmentConfig() {
    }

    public static String get(String name, String defaultValue) {
        return VALUES.getOrDefault(name, defaultValue);
    }

    private static Map<String, String> load() {

        Map<String, String> values = new HashMap<>();
        values.putAll(readProcessEnvironment());

        Map<String, String> fileValues = readEnvFile();

        fileValues.forEach(values::putIfAbsent);

        return values;
    }

    private static Map<String, String> readProcessEnvironment() {

        Map<String, String> values = new HashMap<>();

        System.getenv().forEach((name, value) -> {

            if (name.startsWith("RESQGRID_")) {
                values.put(name, value);
            }
        });

        return values;
    }

    private static Map<String, String> readEnvFile() {

        Path envFile = Path.of(".env");

        if (!Files.isRegularFile(envFile)) {
            return Map.of();
        }

        Properties properties = new Properties();

        try (InputStream input = Files.newInputStream(envFile)) {

            properties.load(input);

        } catch (IOException e) {
            throw new IllegalStateException("Failed to read .env file.", e);
        }

        Map<String, String> values = new HashMap<>();

        for (String name : properties.stringPropertyNames()) {
            values.put(name, properties.getProperty(name).trim());
        }

        return values;
    }
}
