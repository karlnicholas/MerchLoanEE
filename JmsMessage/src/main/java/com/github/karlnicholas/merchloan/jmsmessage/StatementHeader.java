package com.github.karlnicholas.merchloan.jmsmessage;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class StatementHeader implements Serializable {
    private UUID id;
    private UUID accountId;
    private UUID loanId;
    private UUID interestChargeId;
    private UUID feeChargeId;
    private LocalDate statementDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private String customer;
    private List<RegisterEntry> registerEntries;
    private Boolean retry;
}
