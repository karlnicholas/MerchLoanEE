package com.github.karlnicholas.merchloan.accounts.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoanState {
//    @Id
//    @Column(columnDefinition = "BINARY(16)")
    private UUID loanId;
    private LocalDate startDate;
    private Integer currentRowNum;
    private BigDecimal balance;
}
