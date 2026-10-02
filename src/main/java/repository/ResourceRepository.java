package repository;

import database.DatabaseConnection;
import model.EmergencyResource;
import model.Location;
import model.ResourceStatus;
import model.ResourceType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ResourceRepository {

    private final LocationRepository locationRepository;

    public ResourceRepository(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public long save(EmergencyResource resource) {

        try (Connection connection = DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {

                long resourceId = save(resource, connection);

                connection.commit();

                return resourceId;

            } catch (SQLException e) {

                connection.rollback();

                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save resource.", e);
        }
    }

    private long save(EmergencyResource resource, Connection connection)
            throws SQLException {

        String sql = """
            INSERT INTO resources (type, status, location_id)
            VALUES (?, ?, ?)
            """;

        long locationId = locationRepository.save(resource.getLocation(), connection);

        try (PreparedStatement statement = connection.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS
        )) {

            statement.setString(1, resource.getType().name());
            statement.setString(2, resource.getStatus().name());
            statement.setLong(3, locationId);

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected != 1) {
                throw new SQLException("Resource was not inserted.");
            }

            try (ResultSet resultSet = statement.getGeneratedKeys()) {

                if (!resultSet.next()) {
                    throw new SQLException("Generated resource ID was not returned.");
                }

                return resultSet.getLong(1);
            }
        }
    }

    public Optional<EmergencyResource> findById(long id) {

        String sql = """
            SELECT r.id, r.type, r.status, r.location_id,
                   l.latitude, l.longitude
            FROM resources r
            JOIN locations l ON l.id = r.location_id
            WHERE r.id = ?
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return Optional.of(map(resultSet));
                }

                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load resource.", e);
        }
    }

    public List<EmergencyResource> findByStatus(ResourceStatus status) {

        String sql = """
            SELECT r.id, r.type, r.status, r.location_id,
                   l.latitude, l.longitude
            FROM resources r
            JOIN locations l ON l.id = r.location_id
            WHERE r.status = ?
            ORDER BY r.id
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status.name());

            try (ResultSet resultSet = statement.executeQuery()) {

                List<EmergencyResource> resources = new ArrayList<>();

                while (resultSet.next()) {
                    resources.add(map(resultSet));
                }

                return resources;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load resources.", e);
        }
    }

    public void updateStatus(long id, ResourceStatus status) {

        String sql = "UPDATE resources SET status = ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status.name());
            statement.setLong(2, id);

            if (statement.executeUpdate() != 1) {
                throw new RuntimeException(
                        "Resource status was not updated for ID " + id + ".");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update resource status.", e);
        }
    }

    /**
     * Transitions a resource out of {@code expectedStatus} only while it is still
     * in that state.
     *
     * PostgreSQL evaluates this statement atomically, so when two transactions
     * attempt the same transition exactly one of them changes the row. The
     * boolean result reports which outcome occurred, and the caller is
     * responsible for rolling back when it returns {@code false}.
     *
     * @return {@code true} if this caller performed the transition
     */
    public boolean transitionStatusIfCurrent(long id,
                                             ResourceStatus expectedStatus,
                                             ResourceStatus newStatus,
                                             Connection connection)
            throws SQLException {

        String sql = "UPDATE resources SET status = ? WHERE id = ? AND status = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, newStatus.name());
            statement.setLong(2, id);
            statement.setString(3, expectedStatus.name());

            return statement.executeUpdate() == 1;
        }
    }

    private EmergencyResource map(ResultSet resultSet) throws SQLException {

        Location location = new Location(
                resultSet.getDouble("latitude"),
                resultSet.getDouble("longitude")
        );

        return new EmergencyResource(
                resultSet.getLong("id"),
                ResourceType.valueOf(resultSet.getString("type")),
                ResourceStatus.valueOf(resultSet.getString("status")),
                location
        );
    }
}
