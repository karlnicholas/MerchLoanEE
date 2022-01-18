package com.github.karlnicholas.merchloan.businessdate.businessdate;

import com.github.karlnicholas.merchloan.businessdate.businessdate.service.BusinessDateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication(scanBasePackages = "com.github.karlnicholas.merchloan")
public class BusinessDateApplication {

    public static void main(String[] args) {
        SpringApplication.run(BusinessDateApplication.class, args);
    }

    @Autowired
    private BusinessDateService businessDateService;

    @EventListener(ContextRefreshedEvent.class)
    public void initialize() {
        businessDateService.initializeBusinessDate();
    }

}
