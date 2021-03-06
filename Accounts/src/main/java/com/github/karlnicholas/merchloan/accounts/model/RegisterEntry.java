package com.github.karlnicholas.merchloan.accounts.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegisterEntry {
    private UUID id;
    private UUID loanId;
    private LocalDate date;
    private String description;
    private BigDecimal debit;
    private BigDecimal credit;
    private LocalDateTime timeStamp;
}
