package com.resqgrid.config;

public final class DatabaseConfig {

    private DatabaseConfig() {
    }

    public static final String URL =
            "jdbc:postgresql://localhost:5432/resqgrid";

    public static final String USER =
            "postgres";

    public static final String PASSWORD =
            System.getenv("RESQGRID_DB_PASSWORD");
}