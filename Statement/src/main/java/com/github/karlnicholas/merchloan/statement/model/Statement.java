package com.github.karlnicholas.merchloan.statement.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
//@Table(indexes = {@Index(unique = true, columnList = "loanId, statementDate")})
public class Statement {
    private UUID id;
    private UUID loanId;
    private LocalDate statementDate;
    private BigDecimal startingBalance;
    private BigDecimal endingBalance;
    private String statementDoc;
}
