package com.github.karlnicholas.merchloan.sqlutil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SqlInitialization {
    private SqlInitialization() {
        throw new IllegalStateException("Do not construct utility class");
    }

    public static void initialize(Connection con, InputStream inputStream) {
        new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines()
                .forEach(s -> {
                    try (PreparedStatement ps = con.prepareStatement(s)) {
                        ps.executeUpdate();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}