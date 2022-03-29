package com.github.karlnicholas.merchloan.jmsmessage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.UUID;


@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class StatementCompleteResponse extends ServiceRequestResponse {
    private UUID loanId;
    private LocalDate statementDate;
}
