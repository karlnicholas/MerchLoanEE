package com.github.karlnicholas.merchloan.jmsmessage;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
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
