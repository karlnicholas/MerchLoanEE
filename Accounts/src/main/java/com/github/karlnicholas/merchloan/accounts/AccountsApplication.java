package com.github.karlnicholas.merchloan.accounts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;

import javax.sql.DataSource;

@SpringBootApplication(scanBasePackages = {"com.github.karlnicholas.merchloan"})
public class AccountsApplication {
    public static void main(String[] args) {
        SpringApplication.run(AccountsApplication.class, args);
    }
    @Autowired
    private DataSource dataSource;
    @EventListener(ApplicationReadyEvent.class)
    public void startupEvent() {

    }
}
