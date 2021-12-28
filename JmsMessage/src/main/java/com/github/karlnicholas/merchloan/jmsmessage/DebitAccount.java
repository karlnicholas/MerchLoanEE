package com.github.karlnicholas.merchloan.jmsmessage;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class DebitAccount implements Serializable {
    private UUID id;
    private UUID loanId;
    private BigDecimal amount;
    private LocalDate date;
}
