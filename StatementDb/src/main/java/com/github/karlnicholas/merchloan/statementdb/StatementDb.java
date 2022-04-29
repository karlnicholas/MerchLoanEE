package com.github.karlnicholas.merchloan.statementdb;

import org.h2.tools.Server;

import java.sql.Connection;
import java.sql.DriverManager;

public class StatementDb {
    public static final int serverPort = 9100;
    public static void startServer() throws Exception {
        try ( Connection con = DriverManager.getConnection("jdbc:h2:mem:statement;DB_CLOSE_DELAY=-1") ) {
            SqlInitialization.initialize(con, StatementDb.class.getResourceAsStream("/sql/schema.sql"));
        }
        Server.createTcpServer("-tcp", "-tcpPort", Integer.toString(serverPort), "-tcpAllowOthers").start();
    }
}
