package com.github.karlnicholas.merchloan.apimessage.message;

import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class StatementRequest implements ServiceRequestMessage {
    private UUID loanId;
    private LocalDate statementDate;
    private LocalDate startDate;
    private LocalDate endDate;
}
