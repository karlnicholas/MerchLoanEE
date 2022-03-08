package com.github.karlnicholas.merchloan.client.process;

import com.github.karlnicholas.merchloan.dto.LoanDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoanData {
    public static final String FUNDING_DESCRIPTION = "FUNDING";
    public static final String PAYMENT_DESCRIPTION= "PAYMENT";
    public static final String CLOSE_DESCRIPTION = "CLOSE";
    private String customer;
    private BigDecimal fundingAmount;
    private LoanDto loanState;
    private UUID loanId;
    private LocalDate lastStatementDate;
}
