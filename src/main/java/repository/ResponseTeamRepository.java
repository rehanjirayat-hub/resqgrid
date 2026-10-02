package repository;

import database.DatabaseConnection;
import model.ResponseTeam;
import model.ResponseTeamStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ResponseTeamRepository {

    public long save(ResponseTeam team) {

        String sql = "INSERT INTO response_teams (status) VALUES (?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql,
                     Statement.RETURN_GENERATED_KEYS
             )) {

            statement.setString(1, team.getStatus().name());

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected != 1) {
                throw new SQLException("Response team was not inserted.");
            }

            try (ResultSet resultSet = statement.getGeneratedKeys()) {

                if (!resultSet.next()) {
                    throw new SQLException("Generated response team ID was not returned.");
                }

                return resultSet.getLong(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save response team.", e);
        }
    }

    public Optional<ResponseTeam> findById(long id) {

        String sql = "SELECT id, status FROM response_teams WHERE id = ?";

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
            throw new RuntimeException("Failed to load response team.", e);
        }
    }

    public List<ResponseTeam> findByStatus(ResponseTeamStatus status) {

        String sql = """
            SELECT id, status
            FROM response_teams
            WHERE status = ?
            ORDER BY id
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status.name());

            try (ResultSet resultSet = statement.executeQuery()) {

                List<ResponseTeam> teams = new ArrayList<>();

                while (resultSet.next()) {
                    teams.add(map(resultSet));
                }

                return teams;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load response teams.", e);
        }
    }

    public void updateStatus(long id, ResponseTeamStatus status) {

        String sql = "UPDATE response_teams SET status = ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status.name());
            statement.setLong(2, id);

            if (statement.executeUpdate() != 1) {
                throw new RuntimeException(
                        "Response team status was not updated for ID " + id + ".");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update response team status.", e);
        }
    }

    private ResponseTeam map(ResultSet resultSet) throws SQLException {

        long id = resultSet.getLong("id");
        ResponseTeamStatus status =
                ResponseTeamStatus.valueOf(resultSet.getString("status"));

        return new ResponseTeam(id, status);
    }
}
