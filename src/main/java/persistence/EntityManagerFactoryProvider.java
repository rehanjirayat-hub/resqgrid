package persistence;

import database.EnvironmentConfig;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;

/**
 * Creates the {@link EntityManagerFactory} for the ResQGrid persistence unit.
 *
 * Connection settings are supplied here from the environment rather than from
 * {@code persistence.xml}, so credentials are never stored in source control.
 */
public final class EntityManagerFactoryProvider {

    private static final String UNIT_NAME = "resqgrid";

    private EntityManagerFactoryProvider() {
    }

    public static EntityManagerFactory create() {

        Map<String, Object> settings = new HashMap<>();

        settings.put(
                "jakarta.persistence.jdbc.url",
                EnvironmentConfig.get(
                        "RESQGRID_DB_URL",
                        "jdbc:postgresql://localhost:5432/resqgrid"
                )
        );

        settings.put(
                "jakarta.persistence.jdbc.user",
                EnvironmentConfig.get("RESQGRID_DB_USER", "postgres")
        );

        settings.put(
                "jakarta.persistence.jdbc.password",
                EnvironmentConfig.get("RESQGRID_DB_PASSWORD", "")
        );

        return Persistence.createEntityManagerFactory(UNIT_NAME, settings);
    }
}