package com.github.karlnicholas.merchloan.ledger.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.persistence.Entity;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class DebitEntry extends LedgerEntry {
    private BigDecimal amount;
}
