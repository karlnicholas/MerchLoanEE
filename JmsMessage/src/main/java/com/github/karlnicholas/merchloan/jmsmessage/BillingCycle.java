package com.github.karlnicholas.merchloan.jmsmessage;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class BillingCycle implements Serializable {
    private UUID id;
    private UUID accountId;
    private UUID loanId;
    private LocalDate statementDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private int retryCount;
}
