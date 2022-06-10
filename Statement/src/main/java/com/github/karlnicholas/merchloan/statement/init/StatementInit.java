package com.github.karlnicholas.merchloan.statement.init;

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
public class StatementInit {
    @Resource(lookup = "java:jboss/datasources/StatementDS")
    private DataSource dataSource;

    @PostConstruct
    public void init() {
        try {
            SqlInitialization.initialize(dataSource.getConnection(), StatementInit.class.getResourceAsStream("/sql/schema.sql"));
            log.info("Database init");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
