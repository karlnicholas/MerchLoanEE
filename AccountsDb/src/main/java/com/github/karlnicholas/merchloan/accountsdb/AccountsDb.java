package com.github.karlnicholas.merchloan.accountsdb;

import org.h2.tools.Server;

import java.sql.Connection;
import java.sql.DriverManager;

public class AccountsDb {
    public static final int serverPort = 9101;
    public static void startServer() throws Exception {
        try ( Connection con = DriverManager.getConnection("jdbc:h2:mem:accounts;DB_CLOSE_DELAY=-1") ) {
            com.github.karlnicholas.merchloan.statementdb.SqlInitialization.initialize(con, AccountsDb.class.getResourceAsStream("/sql/schema.sql"));
        }
        Server.createTcpServer("-tcp", "-tcpPort", Integer.toString(serverPort), "-tcpAllowOthers").start();
    }
}
