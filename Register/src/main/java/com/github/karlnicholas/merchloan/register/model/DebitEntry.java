package com.github.karlnicholas.merchloan.register.model;

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
public class DebitEntry extends RegisterEntry {
    private BigDecimal debit;
}
