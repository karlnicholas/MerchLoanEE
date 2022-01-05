package com.github.karlnicholas.merchloan.statement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatementRegisterEntry {
    private Integer rowNum;
    private LocalDate date;
    private String description;
    private BigDecimal debit;
    private BigDecimal credit;
    private BigDecimal balance;
}
