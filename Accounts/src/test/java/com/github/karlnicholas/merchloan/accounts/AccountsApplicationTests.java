package com.github.karlnicholas.merchloan.accounts;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ComponentScan("com.github.karlnicholas.merchloan")
class AccountsApplicationTests {

    @Test
    void contextLoads() {
        assertTrue(true);
    }

}
