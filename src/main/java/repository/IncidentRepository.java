package repository;

import database.DatabaseConnection;
import model.Incident;
import model.Location;
import model.Severity;
import model.Status;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    public Optional<Incident> findById(long id) {

        String sql = """
            SELECT i.id, i.description, i.severity, i.status, i.created_at,
                   l.latitude, l.longitude
            FROM incidents i
            JOIN locations l ON l.id = i.location_id
            WHERE i.id = ?
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (!resultSet.next()) {
                    return Optional.empty();
                }

                Location location = new Location(
                        resultSet.getDouble("latitude"),
                        resultSet.getDouble("longitude")
                );

                Incident incident = new Incident(
                        resultSet.getLong("id"),
                        resultSet.getString("description"),
                        Severity.valueOf(resultSet.getString("severity")),
                        Status.valueOf(resultSet.getString("status")),
                        location,
                        resultSet.getObject("created_at", LocalDateTime.class)
                );

                return Optional.of(incident);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load incident.", e);
        }
    }

    public List<Incident> findByStatus(Status status) {

        String sql = """
            SELECT i.id, i.description, i.severity, i.status, i.created_at,
                   l.latitude, l.longitude
            FROM incidents i
            JOIN locations l ON l.id = i.location_id
            WHERE i.status = ?
            ORDER BY i.id
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status.name());

            try (ResultSet resultSet = statement.executeQuery()) {

                List<Incident> incidents = new ArrayList<>();

                while (resultSet.next()) {

                    Location location = new Location(
                            resultSet.getDouble("latitude"),
                            resultSet.getDouble("longitude")
                    );

                    incidents.add(new Incident(
                            resultSet.getLong("id"),
                            resultSet.getString("description"),
                            Severity.valueOf(resultSet.getString("severity")),
                            Status.valueOf(resultSet.getString("status")),
                            location,
                            resultSet.getObject("created_at", LocalDateTime.class)
                    ));
                }

                return incidents;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load incidents.", e);
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
