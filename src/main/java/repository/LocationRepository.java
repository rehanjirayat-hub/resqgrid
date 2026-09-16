package repository;

import database.DatabaseConnection;
import model.Location;

import java.sql.*;
import java.util.Optional;

public class LocationRepository {

    public long save(Location location) {

        String sql = """
            INSERT INTO locations (latitude, longitude)
            VALUES (?, ?)
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql,
                     Statement.RETURN_GENERATED_KEYS
             )) {

            statement.setDouble(1, location.getLatitude());
            statement.setDouble(2, location.getLongitude());

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected != 1) {
                throw new SQLException("Location was not inserted.");
            }

            try (ResultSet resultSet = statement.getGeneratedKeys()) {

                if (resultSet.next()) {
                    return resultSet.getLong(1);
                }

                throw new SQLException("Generated location ID was not returned.");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save location.", e);
        }
    }

    public Optional<Location> findById(long id) {

        String sql = """
            SELECT id, latitude, longitude
            FROM locations
            WHERE id = ?
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {

                    double latitude = resultSet.getDouble("latitude");
                    double longitude = resultSet.getDouble("longitude");

                    Location location = new Location(latitude, longitude);

                    return Optional.of(location);
                }

                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    }
