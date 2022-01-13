package com.github.karlnicholas.merchloan.statement.model;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(indexes = {@Index(unique = true, columnList = "loanId, statementDate")})
public class Statement {
    @Id
    private UUID id;
    private UUID loanId;
    private LocalDate statementDate;
    private BigDecimal startingBalance;
    private BigDecimal endingBalance;
    @Lob
    private String statement;
}
