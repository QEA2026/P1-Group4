package com.revature.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionUtil {

    /*
     * Database configuration.
     * Environment variables override defaults.
     * Defaults support local development.
     */
    private static final String HOST =
            System.getenv().getOrDefault("DB_HOST", "localhost");

    private static final String PORT =
            System.getenv().getOrDefault("DB_PORT", "5432");

    private static final String DATABASE =
            System.getenv().getOrDefault("DB_NAME", "expense_manager");

    private static final String USERNAME =
            System.getenv().getOrDefault("DB_USER", "postgres");

    private static final String PASSWORD =
            System.getenv().getOrDefault("DB_PASSWORD", "newPostgresqlUser26");


    public static Connection getConnection() throws SQLException {

        /*
         * Allows integration tests to use SQLite
         * without changing DAO code.
         */
        String sqlitePath = System.getProperty("expense.db.path");

        if (sqlitePath != null) {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                throw new SQLException(e);
            }

            return DriverManager.getConnection("jdbc:sqlite:" + sqlitePath);
        }


        /*
         * PostgreSQL connection.
         * Uses environment variables when provided,
         * otherwise falls back to local defaults.
         */
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException(e);
        }


        String url = String.format(
                "jdbc:postgresql://%s:%s/%s",
                HOST,
                PORT,
                DATABASE
        );


        return DriverManager.getConnection(
                url,
                USERNAME,
                PASSWORD
        );
    }
}