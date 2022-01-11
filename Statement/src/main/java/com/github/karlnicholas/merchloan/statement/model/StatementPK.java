package com.github.karlnicholas.merchloan.statement.model;

import lombok.*;

import javax.persistence.Embeddable;
import javax.persistence.Id;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatementPK implements Serializable {
    private UUID loanId;
    private LocalDate statementDate;
}
