package com.github.karlnicholas.merchloan.client.model;

import com.github.karlnicholas.merchloan.dto.LoanDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountLoan {
    private UUID accountId;
    private UUID loanId;
    private LoanDto loanStatus;
}
