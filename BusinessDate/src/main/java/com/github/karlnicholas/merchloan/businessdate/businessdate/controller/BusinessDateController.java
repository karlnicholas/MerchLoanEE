package com.github.karlnicholas.merchloan.businessdate.businessdate.controller;

import com.github.karlnicholas.merchloan.businessdate.businessdate.model.BusinessDate;
import com.github.karlnicholas.merchloan.businessdate.businessdate.service.BusinessDateService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api")
public class BusinessDateController {
    private final BusinessDateService businessDateService;

    public BusinessDateController(BusinessDateService businessDateService) {
        this.businessDateService = businessDateService;
    }

    @PostMapping(value = "businessdate")
    public void postBusinessDate(@RequestBody LocalDate businessDate) {
        BusinessDate priorBusinessDate = businessDateService.updateBusinessDate(businessDate);
        businessDateService.startBillingCycle(priorBusinessDate);
    }
}
