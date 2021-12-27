package com.github.karlnicholas.merchloan.ledger.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.time.LocalDate;
import java.util.UUID;

@MappedSuperclass
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract class LedgerEntry {
    @Id
    private UUID id;
    private UUID loanId;
    private LocalDate date;
}
