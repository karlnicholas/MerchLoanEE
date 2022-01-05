package com.github.karlnicholas.merchloan.statement.model;

import javax.persistence.Embeddable;
import javax.persistence.Id;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Embeddable
public class StatementPK implements Serializable {
    private UUID loanId;
    private LocalDate statementDate;
}
