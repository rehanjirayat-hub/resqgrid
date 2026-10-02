package support;

import database.DatabaseConnection;
import model.ResourceCapability;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Clears ResQGrid tables so each test starts from a controlled state.
 *
 * The dispatch engine queries all available resources, so resources left
 * behind by an earlier test would otherwise be considered as candidates and
 * make results depend on execution order.
 */
public final class TestDataCleaner {

    private TestDataCleaner() {
    }

    public static void clearAll() {

        truncate();

        seedCapabilityCatalogue();
    }

    private static void truncate() {

        String sql = """
            TRUNCATE TABLE dispatches,
                     resource_capabilities,
                     team_capabilities,
                     capabilities,
                     response_teams,
                     resources,
                     incidents,
                     locations
            RESTART IDENTITY CASCADE
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute(sql);

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to clear test data.", e);
        }
    }

    private static void seedCapabilityCatalogue() {

        String sql = "INSERT INTO capabilities (name) VALUES (?)";

        for (ResourceCapability capability : ResourceCapability.values()) {

            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setString(1, capability.name());
                statement.executeUpdate();

            } catch (SQLException e) {
                throw new IllegalStateException(
                        "Failed to seed capability catalogue.", e
                );
            }
        }
    }
}