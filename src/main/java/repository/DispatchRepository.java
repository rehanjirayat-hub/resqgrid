package repository;

import database.DatabaseConnection;
import model.Dispatch;
import model.DispatchStatus;
import model.EmergencyResource;
import model.Incident;
import model.ResponseTeam;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Persists dispatch records. The incident, resource and optional response
 * team are referenced by existing IDs; this repository does not create them.
 */
public class DispatchRepository {

    private final IncidentRepository incidentRepository;
    private final ResourceRepository resourceRepository;
    private final ResponseTeamRepository responseTeamRepository;

    public DispatchRepository(IncidentRepository incidentRepository,
                              ResourceRepository resourceRepository,
                              ResponseTeamRepository responseTeamRepository) {

        this.incidentRepository = incidentRepository;
        this.resourceRepository = resourceRepository;
        this.responseTeamRepository = responseTeamRepository;
    }

    public long save(Dispatch dispatch) {

        try (Connection connection = DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {

                long dispatchId = save(dispatch, connection);

                connection.commit();

                return dispatchId;

            } catch (SQLException e) {

                connection.rollback();

                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save dispatch.", e);
        }
    }

    public long save(Dispatch dispatch, Connection connection) throws SQLException {

        String sql = """
            INSERT INTO dispatches (
                incident_id,
                resource_id,
                response_team_id,
                status,
                created_at
            )
            VALUES (?, ?, ?, ?, ?)
            """;

        try (PreparedStatement statement = connection.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS
        )) {

            statement.setLong(1, dispatch.getIncident().getId());
            statement.setLong(2, dispatch.getResource().getId());
            setResponseTeamId(statement, dispatch);
            statement.setString(4, dispatch.getStatus().name());
            statement.setObject(5, dispatch.getCreatedAt());

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected != 1) {
                throw new SQLException("Dispatch was not inserted.");
            }

            try (ResultSet resultSet = statement.getGeneratedKeys()) {

                if (!resultSet.next()) {
                    throw new SQLException("Generated dispatch ID was not returned.");
                }

                return resultSet.getLong(1);
            }
        }
    }

    public Optional<Dispatch> findById(long id) {

        String sql = baseSelect() + " WHERE d.id = ?";

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
            throw new RuntimeException("Failed to load dispatch.", e);
        }
    }

    public List<Dispatch> findByIncident(long incidentId) {

        return findByColumn("d.incident_id", incidentId, "Failed to load dispatches.");
    }

    public List<Dispatch> findByResource(long resourceId) {

        return findByColumn("d.resource_id", resourceId, "Failed to load dispatches.");
    }

    public List<Dispatch> findByStatus(DispatchStatus status) {

        String sql = baseSelect() + " WHERE d.status = ? ORDER BY d.id";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status.name());

            try (ResultSet resultSet = statement.executeQuery()) {

                return collect(resultSet);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load dispatches.", e);
        }
    }

    public void updateStatus(long id, DispatchStatus status) {

        try (Connection connection = DatabaseConnection.getConnection()) {
            updateStatus(id, status, connection);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update dispatch status.", e);
        }
    }

    /**
     * Updates the dispatch status using a caller-supplied connection so the
     * change participates in that caller's transaction.
     */
    public void updateStatus(long id,
                             DispatchStatus status,
                             Connection connection) throws SQLException {

        String sql = "UPDATE dispatches SET status = ? WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status.name());
            statement.setLong(2, id);

            if (statement.executeUpdate() != 1) {
                throw new SQLException(
                        "Dispatch status was not updated for ID " + id + ".");
            }
        }
    }

    private List<Dispatch> findByColumn(
            String column,
            long value,
            String failureMessage) {

        String sql = baseSelect() + " WHERE " + column + " = ? ORDER BY d.id";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, value);

            try (ResultSet resultSet = statement.executeQuery()) {

                return collect(resultSet);
            }

        } catch (SQLException e) {
            throw new RuntimeException(failureMessage, e);
        }
    }

    private List<Dispatch> collect(ResultSet resultSet) throws SQLException {

        List<Dispatch> dispatches = new ArrayList<>();

        while (resultSet.next()) {
            dispatches.add(map(resultSet));
        }

        return dispatches;
    }

    private void setResponseTeamId(
            PreparedStatement statement,
            Dispatch dispatch) throws SQLException {

        if (dispatch.getResponseTeam() == null) {
            statement.setNull(3, Types.BIGINT);
        } else {
            statement.setLong(3, dispatch.getResponseTeam().getId());
        }
    }

    private String baseSelect() {

        return """
            SELECT d.id, d.incident_id, d.resource_id, d.response_team_id,
                   d.status, d.created_at
            FROM dispatches d
            """;
    }

    private Dispatch map(ResultSet resultSet) throws SQLException {

        Incident incident = incidentRepository
                .findById(resultSet.getLong("incident_id"))
                .orElseThrow(() -> new IllegalStateException(
                        "Dispatch references an incident that no longer exists."
                ));

        EmergencyResource resource = resourceRepository
                .findById(resultSet.getLong("resource_id"))
                .orElseThrow(() -> new IllegalStateException(
                        "Dispatch references a resource that no longer exists."
                ));

        ResponseTeam responseTeam = null;

        long responseTeamId = resultSet.getLong("response_team_id");

        if (!resultSet.wasNull()) {
            responseTeam = responseTeamRepository
                    .findById(responseTeamId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Dispatch references a response team that no longer exists."
                    ));
        }

        return new Dispatch(
                resultSet.getLong("id"),
                incident,
                resource,
                responseTeam,
                DispatchStatus.valueOf(resultSet.getString("status")),
                resultSet.getObject("created_at", LocalDateTime.class)
        );
    }
}
