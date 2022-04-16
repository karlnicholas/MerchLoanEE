package com.github.karlnicholas.merchloan.statement;

import com.github.karlnicholas.merchloan.sqlutil.SqlInitialization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@SpringBootApplication(scanBasePackages = {"com.github.karlnicholas.merchloan"})
public class StatementApplication {

    public static void main(String[] args) {
        SpringApplication.run(StatementApplication.class, args);
    }

    @Autowired
    private DataSource dataSource;

    @EventListener(ApplicationReadyEvent.class)
    public void initialize() throws SQLException, IOException {
        try(Connection con = dataSource.getConnection()) {
            SqlInitialization.initialize(con, StatementApplication.class.getResourceAsStream("/sql/schema.sql"));
        }
    }

}
