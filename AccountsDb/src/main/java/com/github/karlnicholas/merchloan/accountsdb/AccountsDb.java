package com.github.karlnicholas.merchloan.accountsdb;

import org.h2.tools.Server;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class AccountsDb {
    public static final int SERVER_PORT = 9101;
    private AccountsDb() {
        throw new IllegalStateException("Should not construct utility class");
    }
    public static void startServer() throws SQLException, IOException {
        try ( Connection con = DriverManager.getConnection("jdbc:h2:mem:accounts;DB_CLOSE_DELAY=-1") ) {
            SqlInitialization.initialize(con, AccountsDb.class.getResourceAsStream("/sql/schema.sql"));
        }
        Server.createTcpServer("-tcp", "-tcpPort", Integer.toString(SERVER_PORT), "-tcpAllowOthers").start();
    }
}
