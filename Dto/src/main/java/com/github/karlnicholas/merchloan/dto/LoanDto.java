package com.github.karlnicholas.merchloan.dto;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoanDto implements Serializable {
    private UUID loanId;
    private UUID accountId;
    private String customer;
    private LocalDate startDate;
    private BigDecimal funding;
    private Integer months;
    private BigDecimal interestRate;
    private BigDecimal monthlyPayments;
    private String loanState;
    private LocalDate lastStatementDate;
    private BigDecimal lastStatementBalance;
    private BigDecimal currentBalance;
    private BigDecimal currentPayment;
    private BigDecimal currentInterest;
    private BigDecimal payoffAmount;
}
