package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String DB_URL = EnvironmentConfig.get(
            "RESQGRID_DB_URL",
            "jdbc:postgresql://localhost:5432/resqgrid"
    );

    private static final String DB_USERNAME =
            EnvironmentConfig.get("RESQGRID_DB_USER", "postgres");

    private static final String DB_PASSWORD =
            EnvironmentConfig.get("RESQGRID_DB_PASSWORD", "");

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                DB_URL,
                DB_USERNAME,
                DB_PASSWORD
        );
    }
}
