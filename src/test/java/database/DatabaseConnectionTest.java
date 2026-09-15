package database;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DatabaseConnectionTest {

    @Test
    void shouldConnectToDatabase() throws SQLException {

        Connection connection = DatabaseConnection.getConnection();

        assertNotNull(connection);
        assertFalse(connection.isClosed());

        connection.close();
    }
}