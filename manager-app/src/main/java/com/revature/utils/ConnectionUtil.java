package com.revature.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.nio.file.Paths;

public class ConnectionUtil {

    // Lets tests point at a disposable database instead of the real one,
    // without touching any DAO code - falls back to the real db when unset.
    private static final String DB_PATH_PROPERTY = "expense.db.path";

    public static Connection getConnection() throws SQLException {

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("problem occurred locating driver");
        }

        String dbPath = System.getProperty(DB_PATH_PROPERTY);
        if (dbPath == null) {
            dbPath = Paths.get("").toAbsolutePath()
                    .resolve("../database/expense_manager.db")
                    .normalize()
                    .toString();
        }

        String url = "jdbc:sqlite:" + dbPath;

        return DriverManager.getConnection(url);
    }
}