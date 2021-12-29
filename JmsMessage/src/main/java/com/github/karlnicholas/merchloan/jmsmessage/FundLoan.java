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
public class FundLoan implements Serializable {
    private UUID id;
    private UUID accountId;
    private BigDecimal amount;
    private LocalDate startDate;
    private String description;
    private int retryCount;
}
