package com.github.karlnicholas.merchloan.businessdate.businessdate.init;

import com.github.karlnicholas.merchloan.businessdate.businessdate.BusinessDateApplication;
import com.github.karlnicholas.merchloan.businessdate.businessdate.service.BusinessDateService;
import com.github.karlnicholas.merchloan.sqlutil.SqlInitialization;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@Singleton
@Startup
public class BusinessDateInit {
    @Inject
    private BusinessDateService businessDateService;
    @Resource(lookup = "java:jboss/datasources/BusinessDateDS")
    private DataSource dataSource;

    @PostConstruct
    public void initialize() {
        try(Connection con = dataSource.getConnection()) {
            SqlInitialization.initialize(con, BusinessDateApplication.class.getResourceAsStream("/sql/schema.sql"));
            businessDateService.initializeBusinessDate();
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
