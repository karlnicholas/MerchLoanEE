package com.github.karlnicholas.merchloan.statementdb;

import org.h2.tools.Server;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class StatementDb {
    public static final int SERVER_PORT = 9100;

    private StatementDb() {throw new IllegalStateException("Do not construct utility class");}

    public static void startServer() throws SQLException, IOException {
        try ( Connection con = DriverManager.getConnection("jdbc:h2:mem:statement;DB_CLOSE_DELAY=-1") ) {
            SqlInitialization.initialize(con, StatementDb.class.getResourceAsStream("/sql/schema.sql"));
        }
        Server.createTcpServer("-tcp", "-tcpPort", Integer.toString(SERVER_PORT), "-tcpAllowOthers").start();
    }
}
