package com.github.karlnicholas.merchloan.statement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.github.karlnicholas.merchloan"})
public class StatementApplication {

    public static void main(String[] args) {
        SpringApplication.run(StatementApplication.class, args);
    }

}
