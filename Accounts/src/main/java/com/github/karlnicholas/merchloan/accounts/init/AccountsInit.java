package com.github.karlnicholas.merchloan.accounts.init;

import com.github.karlnicholas.merchloan.sqlutil.SqlInitialization;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

@Startup
@Singleton
@Slf4j
public class AccountsInit {
    @Resource(lookup = "java:jboss/datasources/ServiceRequestDS")
    private DataSource dataSource;

    @PostConstruct
    public void init() {
        try {
            log.info("Database init: {}", SqlInitialization.initialize(dataSource.getConnection(), AccountsInit.class.getResourceAsStream("/sql/schema.sql")));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
