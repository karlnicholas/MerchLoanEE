package com.github.karlnicholas.merchloan.jmsmessage;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class BillingCycleCharge implements Serializable {
    private UUID id;
    private UUID loanId;
    private BigDecimal debit;
    private BigDecimal credit;
    private LocalDate date;
    private String description;
    private Integer rowNum;
    private Boolean retry;
}
