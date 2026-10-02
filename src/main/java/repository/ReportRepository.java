package repository;

import database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Supplies the aggregate counts required by operational reports.
 *
 * Every query here is read-only and derived from stored records, so reports
 * never introduce or depend on additional stored fields.
 */
public class ReportRepository {

    public long countIncidents() {
        return count("SELECT COUNT(*) FROM incidents");
    }

    public long countDispatches() {
        return count("SELECT COUNT(*) FROM dispatches");
    }

    public long countResources() {
        return count("SELECT COUNT(*) FROM resources");
    }

    public long countActiveDispatches() {

        return count(
                "SELECT COUNT(*) FROM dispatches "
                        + "WHERE status IN ('CREATED', 'IN_PROGRESS')"
        );
    }

    public long countResourcesNotAvailable() {
        return count("SELECT COUNT(*) FROM resources WHERE status <> 'AVAILABLE'");
    }

    public long countCompletedDispatchesPerResource(long resourceId) {

        return count(
                "SELECT COUNT(*) FROM dispatches "
                        + "WHERE resource_id = ? AND status = 'COMPLETED'"
        , resourceId);
    }

    public Map<String, Long> countIncidentsByStatus() {
        return countBy("SELECT status, COUNT(*) FROM incidents GROUP BY status");
    }

    public Map<String, Long> countIncidentsBySeverity() {
        return countBy("SELECT severity, COUNT(*) FROM incidents GROUP BY severity");
    }

    public Map<String, Long> countDispatchesByStatus() {
        return countBy("SELECT status, COUNT(*) FROM dispatches GROUP BY status");
    }

    public Map<String, Long> countResourcesByStatus() {
        return countBy("SELECT status, COUNT(*) FROM resources GROUP BY status");
    }

    private long count(String sql) {
        return count(sql, null);
    }

    private long count(String sql, Long parameter) {

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            if (parameter != null) {
                statement.setLong(1, parameter);
            }

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return resultSet.getLong(1);
                }

                return 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to calculate report value.", e);
        }
    }

    private Map<String, Long> countBy(String sql) {

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            Map<String, Long> counts = new LinkedHashMap<>();

            while (resultSet.next()) {
                counts.put(resultSet.getString(1), resultSet.getLong(2));
            }

            return counts;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to calculate report breakdown.", e);
        }
    }
}