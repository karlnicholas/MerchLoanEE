package com.github.karlnicholas.merchloan.sqlutil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SqlInitialization {
    public static void initialize(Connection con, InputStream inputStream) throws SQLException, IOException {
        try (PreparedStatement ps = con.prepareStatement(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8))) {
            ps.executeUpdate();
        }
    }
}
