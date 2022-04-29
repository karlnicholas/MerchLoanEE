package com.github.karlnicholas.merchloan.statementdb;

import org.h2.tools.Server;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class StatementDb {
    public static final int serverPort = 9100;
    public static void startServer() throws Exception {

        //        Driver driver = DriverManager.getDriver("jdbc:h2:mem:statement;DB_CLOSE_DELAY=-1");
//        Properties p = new Properties();
//        p.setProperty("username", "sa");
//        p.setProperty("password", "");
        try ( Connection con = DriverManager.getConnection("jdbc:h2:mem:statement;DB_CLOSE_DELAY=-1") ) {
            SqlInitialization.initialize(con, StatementDb.class.getResourceAsStream("/sql/schema.sql"));
        }
        Server.createTcpServer("-tcp", "-tcpPort", Integer.toString(serverPort), "-tcpAllowOthers").start();
    }
}
