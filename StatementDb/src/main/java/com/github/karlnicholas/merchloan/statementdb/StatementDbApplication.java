package com.github.karlnicholas.merchloan.statementdb;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class StatementDbApplication implements ApplicationRunner {
    public static void main(String[] args) {
        SpringApplication.run(StatementDbApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        StatementDb.startServer();
    }

}
