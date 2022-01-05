package com.github.karlnicholas.merchloan.statement.model;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDate;
import java.util.UUID;

@Entity
public class Statement {
    @EmbeddedId
    private StatementPK id;
    private UUID accountId;
    private String statement;
}
