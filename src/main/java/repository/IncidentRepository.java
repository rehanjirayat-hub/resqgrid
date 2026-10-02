package repository;

import database.DatabaseConnection;
import model.Incident;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class IncidentRepository {

    private final LocationRepository locationRepository;

    public IncidentRepository(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public long save(Incident incident) {

        try (Connection connection = DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {

                long incidentId = save(incident, connection);

                connection.commit();

                return incidentId;

            } catch (SQLException e) {

                connection.rollback();

                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save incident.", e);
        }
    }

    private long save(Incident incident, Connection connection) throws SQLException {

        String sql = """
            INSERT INTO incidents (
                description,
                severity,
                status,
                location_id,
                created_at
            )
            VALUES (?, ?, ?, ?, ?)
            """;

        long locationId = locationRepository.save(incident.getLocation(), connection);

        try (PreparedStatement statement = connection.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS
        )) {

            statement.setString(1, incident.getDescription());
            statement.setString(2, incident.getSeverity().name());
            statement.setString(3, incident.getStatus().name());
            statement.setLong(4, locationId);
            statement.setObject(5, incident.getCreatedAt());

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected != 1) {
                throw new SQLException("Incident was not inserted.");
            }

            try (ResultSet resultSet = statement.getGeneratedKeys()) {

                if (!resultSet.next()) {
                    throw new SQLException("Generated incident ID was not returned.");
                }

                return resultSet.getLong(1);
            }
        }
    }
}
