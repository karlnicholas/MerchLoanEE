package com.github.karlnicholas.merchloan.statement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.sql.DataSource;

@SpringBootApplication(scanBasePackages = {"com.github.karlnicholas.merchloan"})
public class StatementApplication {

    public static void main(String[] args) {
        SpringApplication.run(StatementApplication.class, args);
    }

    @Autowired
    private DataSource dataSource;

}
