package com.github.karlnicholas.merchloan.jmsmessage;

import com.github.karlnicholas.merchloan.dto.LoanDto;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class CloseLoan implements Serializable {
    private UUID id;
    private UUID loanId;
    private UUID interestChargeId;
    private UUID paymentId;
    private BigDecimal amount;
    private LocalDate lastStatementDate;
    private LocalDate date;
    private String description;
    private int retryCount;
    private LoanDto loanDto;
}
