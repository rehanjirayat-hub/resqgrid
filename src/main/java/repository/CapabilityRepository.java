package repository;

import database.DatabaseConnection;
import model.ResourceCapability;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Persists the controlled capability catalogue and the many-to-many links
 * between capabilities and the resources and teams that possess them.
 *
 * Capability membership is an eligibility input, so the lookup methods here
 * deliberately return only what is stored and leave the decision of whether
 * a capability is sufficient to the business layer.
 */
public class CapabilityRepository {

    public long save(ResourceCapability capability) {

        String sql = "INSERT INTO capabilities (name) VALUES (?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql,
                     Statement.RETURN_GENERATED_KEYS
             )) {

            statement.setString(1, capability.name());

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected != 1) {
                throw new SQLException("Capability was not inserted.");
            }

            try (ResultSet resultSet = statement.getGeneratedKeys()) {

                if (!resultSet.next()) {
                    throw new SQLException("Generated capability ID was not returned.");
                }

                return resultSet.getLong(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save capability.", e);
        }
    }

    public Optional<Long> findIdByName(ResourceCapability capability) {

        String sql = "SELECT id FROM capabilities WHERE name = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, capability.name());

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return Optional.of(resultSet.getLong("id"));
                }

                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load capability.", e);
        }
    }

    public void addToResource(long resourceId, ResourceCapability capability) {

        String sql = """
            INSERT INTO resource_capabilities (resource_id, capability_id)
            VALUES (?, ?)
            """;

        long capabilityId = requireCapabilityId(capability);

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, resourceId);
            statement.setLong(2, capabilityId);

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to link capability to resource.", e);
        }
    }

    public void addToTeam(long teamId, ResourceCapability capability) {

        String sql = """
            INSERT INTO team_capabilities (team_id, capability_id)
            VALUES (?, ?)
            """;

        long capabilityId = requireCapabilityId(capability);

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, teamId);
            statement.setLong(2, capabilityId);

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to link capability to response team.", e);
        }
    }

    public List<ResourceCapability> findByResource(long resourceId) {

        String sql = """
            SELECT c.name
            FROM capabilities c
            JOIN resource_capabilities rc ON rc.capability_id = c.id
            WHERE rc.resource_id = ?
            ORDER BY c.name
            """;

        return findByLink(sql, resourceId, "Failed to load resource capabilities.");
    }

    public List<ResourceCapability> findByTeam(long teamId) {

        String sql = """
            SELECT c.name
            FROM capabilities c
            JOIN team_capabilities tc ON tc.capability_id = c.id
            WHERE tc.team_id = ?
            ORDER BY c.name
            """;

        return findByLink(sql, teamId, "Failed to load response team capabilities.");
    }

    public boolean resourceHasCapability(long resourceId, ResourceCapability capability) {

        String sql = """
            SELECT 1
            FROM resource_capabilities rc
            JOIN capabilities c ON c.id = rc.capability_id
            WHERE rc.resource_id = ? AND c.name = ?
            """;

        return exists(sql, resourceId, capability);
    }

    public boolean teamHasCapability(long teamId, ResourceCapability capability) {

        String sql = """
            SELECT 1
            FROM team_capabilities tc
            JOIN capabilities c ON c.id = tc.capability_id
            WHERE tc.team_id = ? AND c.name = ?
            """;

        return exists(sql, teamId, capability);
    }

    private List<ResourceCapability> findByLink(
            String sql,
            long ownerId,
            String failureMessage) {

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, ownerId);

            try (ResultSet resultSet = statement.executeQuery()) {

                List<ResourceCapability> capabilities = new ArrayList<>();

                while (resultSet.next()) {
                    capabilities.add(
                            ResourceCapability.valueOf(resultSet.getString("name"))
                    );
                }

                return capabilities;
            }

        } catch (SQLException e) {
            throw new RuntimeException(failureMessage, e);
        }
    }

    private boolean exists(String sql, long ownerId, ResourceCapability capability) {

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, ownerId);
            statement.setString(2, capability.name());

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to check capability.", e);
        }
    }

    private long requireCapabilityId(ResourceCapability capability) {

        return findIdByName(capability).orElseThrow(() -> new IllegalStateException(
                "Capability is not present in the catalogue: " + capability.name()
        ));
    }
}
