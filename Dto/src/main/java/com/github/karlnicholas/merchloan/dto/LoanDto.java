package com.github.karlnicholas.merchloan.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDate lastStatementDate;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal lastStatementBalance;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal currentBalance;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal currentPayment;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal currentInterest;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal payoffAmount;
}
