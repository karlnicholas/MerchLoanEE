package com.github.karlnicholas.merchloan.accounts;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.sql.DataSource;

@SpringBootApplication(scanBasePackages = {"com.github.karlnicholas.merchloan"})
@Slf4j
public class AccountsApplication {
    public static void main(String[] args) {
        SpringApplication.run(AccountsApplication.class, args);
    }

    @Autowired
    private DataSource dataSource;

//    @EventListener(ApplicationReadyEvent.class)
//    public void startupEvent() throws SQLException, IOException {
//        try(Connection con = dataSource.getConnection()) {
//            log.info("SqlInitialization {}", SqlInitialization.initialize(con, AccountsApplication.class.getResourceAsStream("/sql/schema.sql")));
//        }
//    }
}
